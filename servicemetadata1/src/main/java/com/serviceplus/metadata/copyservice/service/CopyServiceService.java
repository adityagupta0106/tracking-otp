package com.serviceplus.metadata.copyservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.serviceplus.metadata.aadhaarConfiguration.entity.AuaApiFieldMapping;
import com.serviceplus.metadata.aadhaarConfiguration.repository.AuaApiFieldMappingRepository;
import com.serviceplus.metadata.dto.ServerSidePaginationRecord;
import com.serviceplus.metadata.copyservice.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.serviceplus.metadata.correlationConfig.entiry.CorrelationConfiguration;
import com.serviceplus.metadata.correlationConfig.repository.ICorrelationConfigurationRepository;
import com.serviceplus.metadata.dto.ProcessFlowDTO;
import com.serviceplus.metadata.dto.ServiceDefinitionDTO;
import com.serviceplus.metadata.dto.UserSessionDTO;
import com.serviceplus.metadata.entity.*;
import com.serviceplus.metadata.enums.ServiceStatus;
import com.serviceplus.metadata.esign.entity.ServiceEsignConfiguration;
import com.serviceplus.metadata.esign.repository.ServiceEsignConfigurationRepository;
import com.serviceplus.metadata.exception.SPRuntimeError;
import com.serviceplus.metadata.feignClient.ProcessingFeignClient;
import com.serviceplus.metadata.feignClient.TenantConfigurationFeignClient;
import com.serviceplus.metadata.feignClient.LgdFeignClient;
import com.serviceplus.metadata.dto.EntityDetailsDTO;
import com.serviceplus.metadata.dto.EntityRequestDTO;
import com.serviceplus.metadata.dto.ConfigurationDTO;
import com.serviceplus.metadata.inboxSentboxFilterConfig.entity.InboxSentBoxFilter;
import com.serviceplus.metadata.inboxSentboxFilterConfig.repository.InboxSentBoxFilterRepository;
import com.serviceplus.metadata.mvel.entity.MvelEventMapping;
import com.serviceplus.metadata.mvel.entity.MvelFunction;
import com.serviceplus.metadata.mvel.repository.IMvelEventMappingRepository;
import com.serviceplus.metadata.mvel.repository.IMvelFunctionRepository;
import com.serviceplus.metadata.notification.dto.NotificationDTO;
import com.serviceplus.metadata.notification.entity.NotificationConfiguration;
import com.serviceplus.metadata.notification.repository.INotificationRepository;
import com.serviceplus.metadata.repository.*;
import com.serviceplus.metadata.utility.CommonUtil;
import com.serviceplus.metadata.utility.EntityCopyUtil;
import com.serviceplus.metadata.versioning.service.ServiceVersionHistoryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CopyServiceService {

    private static final Logger log = LogManager.getLogger(CopyServiceService.class);

    private static final String PLACEHOLDER = "*******";
    private static final String LEGACY_PLACEHOLDER = "xxxxxxx";

    private final IServiceLogRepository serviceLogRepository;
    private final IServiceDefinitionRepository serviceDefinitionRepository;
    private final ProcessingFeignClient processingFeignClient;
    private final ICopiedServiceTrackerRepository copiedServiceTrackerRepository;
    private final ISpeSequenceLastNoRepository speSequenceLastNoRepo;
    private final ServiceVersionHistoryService historyService;
    private final TenantConfigurationFeignClient tenantConfigurationFeignClient;

    private final IServiceDeliveryUnitDefinitionRepository serviceDeliveryUnitDefinitionRepository;
    private final IProcessFlowRepository processFlowRepository;
    private final IServiceOutputFormatRepository serviceOutputFormatRepository;
    private final IMvelFunctionRepository mvelFunctionRepository;
    private final INotificationRepository notificationRepository;
    private final InboxSentBoxFilterRepository inboxSentBoxFilterRepository;
    private final AuaApiFieldMappingRepository auaApiFieldMappingRepository;
    private final IServiceChargeRepository serviceChargeRepository;
    private final ServiceDeliveryUnitsRepository serviceDeliveryUnitsRepository;
    private final ServicePaymentGatewayConfigRepository servicePaymentGatewayConfigRepository;
    private final IServiceTemplateRepository serviceTemplateRepository;
    private final IMvelEventMappingRepository mvelEventMappingRepository;
    private final ICorrelationConfigurationRepository correlationConfigurationRepository;
    private final ServiceEsignConfigurationRepository serviceEsignRepository;
    private final LgdFeignClient lgdFeignClient;
    private final ObjectMapper objectMapper;

    public CopyServiceService(
            IServiceLogRepository serviceLogRepository,
            IServiceDefinitionRepository serviceDefinitionRepository,
            ProcessingFeignClient processingFeignClient,
            ICopiedServiceTrackerRepository copiedServiceTrackerRepository,
            ISpeSequenceLastNoRepository speSequenceLastNoRepo,
            ServiceVersionHistoryService historyService,
            TenantConfigurationFeignClient tenantConfigurationFeignClient,
            IServiceDeliveryUnitDefinitionRepository serviceDeliveryUnitDefinitionRepository,
            IProcessFlowRepository processFlowRepository,
            IServiceOutputFormatRepository serviceOutputFormatRepository,
            IMvelFunctionRepository mvelFunctionRepository,
            INotificationRepository notificationRepository,
            InboxSentBoxFilterRepository inboxSentBoxFilterRepository,
            AuaApiFieldMappingRepository auaApiFieldMappingRepository,
            IServiceChargeRepository serviceChargeRepository,
            ServiceDeliveryUnitsRepository serviceDeliveryUnitsRepository,
            ServicePaymentGatewayConfigRepository servicePaymentGatewayConfigRepository,
            IServiceTemplateRepository serviceTemplateRepository,
            IMvelEventMappingRepository mvelEventMappingRepository,
            ICorrelationConfigurationRepository correlationConfigurationRepository,
            ServiceEsignConfigurationRepository serviceEsignRepository,
            LgdFeignClient lgdFeignClient,
            ObjectMapper objectMapper) {
        this.serviceLogRepository = serviceLogRepository;
        this.serviceDefinitionRepository = serviceDefinitionRepository;
        this.processingFeignClient = processingFeignClient;
        this.copiedServiceTrackerRepository = copiedServiceTrackerRepository;
        this.speSequenceLastNoRepo = speSequenceLastNoRepo;
        this.historyService = historyService;
        this.tenantConfigurationFeignClient = tenantConfigurationFeignClient;
        this.serviceDeliveryUnitDefinitionRepository = serviceDeliveryUnitDefinitionRepository;
        this.processFlowRepository = processFlowRepository;
        this.serviceOutputFormatRepository = serviceOutputFormatRepository;
        this.mvelFunctionRepository = mvelFunctionRepository;
        this.notificationRepository = notificationRepository;
        this.inboxSentBoxFilterRepository = inboxSentBoxFilterRepository;
        this.auaApiFieldMappingRepository = auaApiFieldMappingRepository;
        this.serviceChargeRepository = serviceChargeRepository;
        this.serviceDeliveryUnitsRepository = serviceDeliveryUnitsRepository;
        this.servicePaymentGatewayConfigRepository = servicePaymentGatewayConfigRepository;
        this.serviceTemplateRepository = serviceTemplateRepository;
        this.mvelEventMappingRepository = mvelEventMappingRepository;
        this.correlationConfigurationRepository = correlationConfigurationRepository;
        this.serviceEsignRepository = serviceEsignRepository;
        this.lgdFeignClient = lgdFeignClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Option 1: Departmental Services for Service Definer.
     * Enforces tenant isolation. Lists LAUNCHED services available in the selected department.
     */
    public List<DepartmentalServiceItemDTO> getDepartmentalServices(Long departmentId, String search, UserSessionDTO userSession) {
        validateUserSession(userSession);
        String tenantId = userSession.getTenantId();

        Long targetDeptId = (departmentId != null) ? departmentId : (userSession.getEntityId() != null ? userSession.getEntityId().longValue() : null);
        if (targetDeptId == null) {
            throw new SPRuntimeError("Department ID must be specified", HttpStatus.BAD_REQUEST);
        }

        List<ServiceLog> logs = serviceLogRepository.findByServiceStatusInAndTenantId(
                List.of(ServiceStatus.LAUNCHED.getCode()), tenantId);
        if (logs == null || logs.isEmpty()) {
            return Collections.emptyList();
        }

        List<DepartmentalServiceItemDTO> result = new ArrayList<>();

        for (ServiceLog logEntry : logs) {
            // Only LAUNCHED services are eligible to be copied
            if (!Objects.equals(logEntry.getServiceStatus(), ServiceStatus.LAUNCHED.getCode())) {
                continue;
            }

            List<ServiceDefinition> definitions = serviceDefinitionRepository.findByServiceId(logEntry.getServiceId());
            if (definitions == null || definitions.isEmpty()) {
                continue;
            }
            ServiceDefinition def = definitions.get(0);

            // Filter by department
            if (!Objects.equals(def.getDepartmentId(), targetDeptId)) {
                continue;
            }

            // Filter by search string if present
            if (search != null && !search.isBlank()) {
                String term = search.trim().toLowerCase();
                boolean matchesName = def.getServiceName() != null && def.getServiceName().toLowerCase().contains(term);
                boolean matchesAbbr = def.getServiceAbbreviation() != null && def.getServiceAbbreviation().toLowerCase().contains(term);
                if (!matchesName && !matchesAbbr) {
                    continue;
                }
            }

            long submittedCount = getSubmittedApplicationCount(logEntry.getServiceId(), tenantId);
            boolean alreadyInCentral = checkAlreadyInCentralRepo(logEntry.getServiceId());

            DepartmentalServiceItemDTO dto = new DepartmentalServiceItemDTO();
            dto.setServiceId(logEntry.getServiceId());
            dto.setBaseServiceId(logEntry.getBaseServiceId());
            dto.setServiceName(def.getServiceName());
            dto.setServiceAbbreviation(def.getServiceAbbreviation());
            dto.setVersionNo(logEntry.getVersionNo());
            dto.setMinorVersionNo(logEntry.getMinorVersionNo());
            dto.setServiceStatus(logEntry.getServiceStatus());
            dto.setStatusLabel(ServiceStatus.fromCode(logEntry.getServiceStatus()).name());
            dto.setDepartmentName(userSession.getClcName() != null ? userSession.getClcName() : "Department " + def.getDepartmentId());
            dto.setSubmittedApplicationsCount(submittedCount);
            dto.setEligibleForCopy(true);
            dto.setEligibleForPush(submittedCount >= 2 && !alreadyInCentral);
            dto.setAlreadyInCentralRepo(alreadyInCentral);
            dto.setLaunchedDate(logEntry.getLaunchedDate());

            result.add(dto);
        }

        return result;
    }

    /**
     * Option 2: Global Services from Central Copy Service Catalogue.
     * Enforces user session and queries approved services published in Central Repository.
     */
    public List<GlobalServiceItemDTO> getGlobalServices(String search, UserSessionDTO userSession) {
        validateUserSession(userSession);

        try {
            ResponseEntity<List<Map<String, Object>>> response = tenantConfigurationFeignClient.getCentralCatalogue(search);
            if (response != null && response.getBody() != null) {
                List<GlobalServiceItemDTO> result = new ArrayList<>();
                for (Map<String, Object> map : response.getBody()) {
                    GlobalServiceItemDTO dto = new GlobalServiceItemDTO();
                    dto.setId((String) map.get("id"));
                    dto.setServiceId(map.get("serviceId") != null ? ((Number) map.get("serviceId")).intValue() : null);
                    dto.setServiceName((String) map.get("serviceName"));
                    dto.setServiceAbbreviation((String) map.get("serviceAbbreviation"));
                    dto.setSourceTenantName((String) map.get("sourceTenantName"));
                    dto.setSourceDepartmentName((String) map.get("sourceDepartmentName"));
                    dto.setVersionNo(map.get("versionNo") != null ? ((Number) map.get("versionNo")).intValue() : 1);
                    dto.setMinorVersionNo(map.get("minorVersionNo") != null ? ((Number) map.get("minorVersionNo")).intValue() : 0);
                    dto.setSubmittedApplicationsCount(map.get("submittedApplicationsCount") != null ? ((Number) map.get("submittedApplicationsCount")).longValue() : 0L);
                    dto.setStatus((String) map.get("status"));
                    dto.setSlaDays(map.get("slaDays") != null ? ((Number) map.get("slaDays")).intValue() : null);
                    result.add(dto);
                }
                return result;
            }
        } catch (Exception e) {
            log.error("Failed to fetch Central Copy Service Catalogue from InstanceConfigRegistry: ", e);
        }
        return Collections.emptyList();
    }

    /**
     * List services available to Department Admin for copying from Central Repository.
     * Categorized into:
     * 1. SAME_DEPARTMENT (Departmental services within the same department from Central Repository)
     * 2. GLOBAL_SERVICES (Global services across multiple tenants from Central Catalogue)
     * Supports server-side pagination with ServerSidePaginationRecord.
     */
    public ServerSidePaginationRecord<AvailableToCopyItemDTO> getServicesAvailableToCopy(
            String category, Long departmentId, String search, int page, int size, UserSessionDTO userSession) {
        validateUserSession(userSession);

        int safePage = Math.max(0, Math.min(page, 10000));
        int safeSize = (size <= 0) ? 10 : Math.min(size, 100);

        Long targetDeptId = departmentId;
        if (targetDeptId == null && ("SAME_DEPARTMENT".equalsIgnoreCase(category) || "DEPARTMENTAL".equalsIgnoreCase(category)) && userSession.getEntityId() != null) {
            targetDeptId = userSession.getEntityId().longValue();
        }

        try {
            ResponseEntity<Object> response = tenantConfigurationFeignClient.getAvailableServicesToCopy(
                    category, targetDeptId, search, safePage, safeSize);

            if (response != null && response.getBody() != null) {
                Object body = response.getBody();
                ServerSidePaginationRecord<?> rawRecord;
                if (body instanceof ServerSidePaginationRecord) {
                    rawRecord = (ServerSidePaginationRecord<?>) body;
                } else {
                    rawRecord = objectMapper.convertValue(body, ServerSidePaginationRecord.class);
                }

                List<AvailableToCopyItemDTO> items = new ArrayList<>();
                if (rawRecord != null && rawRecord.getData() != null) {
                    for (Object obj : rawRecord.getData()) {
                        AvailableToCopyItemDTO item = (obj instanceof AvailableToCopyItemDTO)
                                ? (AvailableToCopyItemDTO) obj
                                : objectMapper.convertValue(obj, AvailableToCopyItemDTO.class);

                        if (item.getCategory() == null) {
                            item.setCategory(category != null ? category : "GLOBAL_SERVICES");
                        }
                        if (item.getBaseServiceId() == null && item.getServiceId() != null) {
                            item.setBaseServiceId(item.getServiceId() / 10000);
                        }
                        if (item.getDepartmentName() == null) {
                            item.setDepartmentName(item.getSourceDepartmentName());
                        }
                        item.setEligibleForCopy(true);
                        item.setServiceStatus(ServiceStatus.LAUNCHED.getCode());
                        items.add(item);
                    }
                }

                return new ServerSidePaginationRecord<>(
                        items,
                        rawRecord != null ? rawRecord.getTotalRecords() : 0,
                        rawRecord != null ? rawRecord.getCurrentPage() : safePage,
                        rawRecord != null ? rawRecord.getPageSize() : safeSize,
                        rawRecord != null ? rawRecord.getTotalPages() : 0
                );
            }
        } catch (Exception e) {
            log.error("Failed to fetch available to copy services from Central Repository: ", e);
            throw new SPRuntimeError("Failed to fetch available services from Central Repository", HttpStatus.BAD_GATEWAY);
        }

        return new ServerSidePaginationRecord<>(Collections.emptyList(), 0, safePage, safeSize, 0);
    }

    /**
     * Push Service to Central Repository by Department Admin.
     * Enforces:
     * 1. Tenant ownership
     * 2. LAUNCHED status
     * 3. At least 2 submitted applications
     * 4. Same service cannot be pushed twice
     */
    @Transactional(readOnly = true)
    public Map<String, Object> pushServiceToCentral(Integer serviceId, UserSessionDTO userSession) {
        validateUserSession(userSession);
        String tenantId = userSession.getTenantId();

        ServiceLog serviceLog = serviceLogRepository.findByServiceId(serviceId);
        if (serviceLog == null) {
            throw new SPRuntimeError("Service not found: " + serviceId, HttpStatus.NOT_FOUND);
        }
        if (!tenantId.equals(serviceLog.getTenantId())) {
            throw new SPRuntimeError("Access denied: Tenant mismatch", HttpStatus.UNAUTHORIZED);
        }

        // Eligibility 1: Must be in Launched status
        if (!Objects.equals(serviceLog.getServiceStatus(), ServiceStatus.LAUNCHED.getCode())) {
            throw new SPRuntimeError("Eligibility check failed: Service must be in LAUNCHED status to be pushed to Central Repository", HttpStatus.BAD_REQUEST);
        }

        // Eligibility 2: At least two applications must have been submitted
        long applicationCount = getSubmittedApplicationCount(serviceId, tenantId);
        if (applicationCount < 2) {
            throw new SPRuntimeError("Eligibility check failed: At least two applications must have been submitted against the service. Found: " + applicationCount, HttpStatus.BAD_REQUEST);
        }

        // Rule: Same service cannot be pushed twice
        if (checkAlreadyInCentralRepo(serviceId)) {
            throw new SPRuntimeError("Same service cannot be pushed/pulled twice. Service already exists in Central Repository.", HttpStatus.BAD_REQUEST);
        }

        ServiceDefinition def = serviceDefinitionRepository.findByServiceId(serviceId).stream().findFirst().orElse(null);
        if (def == null) {
            throw new SPRuntimeError("Service Definition not found for serviceId: " + serviceId, HttpStatus.NOT_FOUND);
        }

        Long entityId = (userSession.getEntityId() != null) ? userSession.getEntityId().longValue() : def.getDepartmentId();
        String entityName = fetchEntityName(entityId);
        if (entityName == null || entityName.isBlank()) {
            entityName = (userSession.getLocationName() != null && !userSession.getLocationName().isBlank())
                    ? userSession.getLocationName()
                    : "Department " + def.getDepartmentId();
        }

        String tenantName = fetchTenantName(tenantId);

        Map<String, Object> pushRequest = new HashMap<>();
        pushRequest.put("serviceId", serviceId);
        pushRequest.put("serviceName", def.getServiceName());
        pushRequest.put("serviceAbbreviation", def.getServiceAbbreviation());
        pushRequest.put("sourceTenantId", tenantId);
        pushRequest.put("tenantName", tenantName);
        pushRequest.put("entityId", entityId);
        pushRequest.put("entityName", entityName);
        pushRequest.put("versionNo", serviceLog.getVersionNo());
        pushRequest.put("minorVersionNo", serviceLog.getMinorVersionNo());
        pushRequest.put("submittedApplicationsCount", applicationCount);
        pushRequest.put("serviceStatus", serviceLog.getServiceStatus());

        try {
            tenantConfigurationFeignClient.pushToCentralRepo(pushRequest, CommonUtil.entityToString(userSession));
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("serviceId", serviceId);
            result.put("serviceName", def.getServiceName());
            result.put("status", "PENDING_REVIEW");
            result.put("message", "Service pushed successfully to Central Repository for Instance Admin review");
            return result;
        } catch (Exception e) {
            log.error("Error pushing service to central repository via Feign client: ", e);
            throw new SPRuntimeError("Failed to push service to Central Repository", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * List services available to Department Admin / User to push to Central Repository.
     * Only LAUNCHED services (status = 4) in the user's tenant (and department if specified) are eligible.
     * Enforces:
     * - Minimum 2 submitted applications
     * - Must not already be published/pushed in Central Repository
     * Supports search (ILIKE, optional) and server-side pagination (with max caps).
     */
    @Transactional(readOnly = true)
    public ServerSidePaginationRecord<AvailableServicePushDTO> getAvailableServicesToPush(
            String search, int page, int size, UserSessionDTO userSession) {
        validateUserSession(userSession);
        String tenantId = userSession.getTenantId();

        int safePage = Math.max(0, Math.min(page, 10000));
        int safeSize = (size <= 0) ? 10 : Math.min(size, 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        String safeSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Integer targetDeptId = userSession.getEntityId();

        Page<ServiceDefinition> defPage = serviceDefinitionRepository.findLaunchedServicesByTenant(
                tenantId,
                targetDeptId,
                safeSearch,
                pageable
        );

        List<AvailableServicePushDTO> dtoList = new ArrayList<>();
        for (ServiceDefinition def : defPage.getContent()) {
            ServiceLog logEntry = serviceLogRepository.findByServiceId(def.getServiceId());
            if (logEntry == null) {
                continue;
            }
            long appCount = getSubmittedApplicationCount(def.getServiceId(), tenantId);
            boolean alreadyInCentral = checkAlreadyInCentralRepo(def.getServiceId());
            boolean eligible = (appCount >= 2 && !alreadyInCentral);

            AvailableServicePushDTO dto = new AvailableServicePushDTO();
            dto.setServiceId(def.getServiceId());
            dto.setBaseServiceId(logEntry.getBaseServiceId());
            dto.setServiceName(def.getServiceName());
            dto.setServiceAbbreviation(def.getServiceAbbreviation());
            dto.setDepartmentId(def.getDepartmentId());
            dto.setVersionNo(logEntry.getVersionNo());
            dto.setMinorVersionNo(logEntry.getMinorVersionNo());
            dto.setServiceStatus(logEntry.getServiceStatus());
            dto.setStatusLabel(ServiceStatus.fromCode(logEntry.getServiceStatus()).name());
            dto.setSubmittedApplicationsCount(appCount);
            dto.setLaunchedDate(logEntry.getLaunchedDate());
            dto.setAlreadyInCentralRepo(alreadyInCentral);
            dto.setEligible(eligible);
            List<String> additionalConfigs = getAdditionalConfigurationsForService(def.getServiceId(), tenantId, def);
            dto.setAdditionalConfigurations(additionalConfigs);

            if (alreadyInCentral) {
                dto.setMessage("Already published/pushed to Central Repository");
            } else if (appCount < 2) {
                dto.setMessage("Requires at least 2 submitted applications (currently: " + appCount + ")");
            } else {
                dto.setMessage("Eligible to push to Central Repository");
            }

            dtoList.add(dto);
        }

        return new ServerSidePaginationRecord<>(
                dtoList,
                defPage.getTotalElements(),
                defPage.getNumber(),
                defPage.getSize(),
                defPage.getTotalPages()
        );
    }

    /**
     * Search and list services available to be pulled by Central Instance Admin.
     * Searches launched services by case-insensitive name (ILIKE) with server-side pagination.
     */
    @Transactional(readOnly = true)
    public ServerSidePaginationRecord<AvailableServicePullDTO> getAvailableServicesToPull(
            String search, int page, int size, int minApplications) {
        int safePage = Math.max(0, Math.min(page, 10000));
        int safeSize = (size <= 0) ? 10 : Math.min(size, 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        String safeSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<ServiceDefinition> defPage = serviceDefinitionRepository.findLaunchedServices(
                safeSearch,
                pageable
        );

        List<AvailableServicePullDTO> dtoList = new ArrayList<>();
        for (ServiceDefinition def : defPage.getContent()) {
            ServiceLog logEntry = serviceLogRepository.findByServiceId(def.getServiceId());
            if (logEntry == null) {
                continue;
            }
            long appCount = getSubmittedApplicationCount(def.getServiceId(), logEntry.getTenantId());
            boolean eligible = (appCount >= minApplications);
            boolean alreadyInCentral = checkAlreadyInCentralRepo(def.getServiceId());

            AvailableServicePullDTO dto = new AvailableServicePullDTO();
            dto.setServiceId(def.getServiceId());
            dto.setServiceName(def.getServiceName());
            dto.setServiceAbbreviation(def.getServiceAbbreviation());
            dto.setVersionNo(logEntry.getVersionNo());
            dto.setMinorVersionNo(logEntry.getMinorVersionNo());
            dto.setSubmittedApplicationsCount(appCount);
            dto.setLaunchedDate(logEntry.getLaunchedDate());
            dto.setEligible(eligible);
            dto.setAlreadyInCentralRepo(alreadyInCentral);

            dtoList.add(dto);
        }

        return new ServerSidePaginationRecord<>(
                dtoList,
                defPage.getTotalElements(),
                defPage.getNumber(),
                defPage.getSize(),
                defPage.getTotalPages()
        );
    }

    /**
     * Provide complete service pull info & blueprint to Central Instance Admin for Direct Pull.
     */
    @Transactional(readOnly = true)
    public ServicePullInfoDTO getServicePullInfo(Integer serviceId) {
        if (serviceId == null) {
            throw new SPRuntimeError("Service ID must be specified", HttpStatus.BAD_REQUEST);
        }

        ServiceLog serviceLog = serviceLogRepository.findByServiceId(serviceId);
        if (serviceLog == null) {
            throw new SPRuntimeError("Service not found with ID: " + serviceId, HttpStatus.NOT_FOUND);
        }

        List<ServiceDefinition> definitions = serviceDefinitionRepository.findByServiceId(serviceId);
        if (definitions == null || definitions.isEmpty()) {
            throw new SPRuntimeError("Service Definition not found for serviceId: " + serviceId, HttpStatus.NOT_FOUND);
        }
        ServiceDefinition def = definitions.getFirst();

        String tenantId = serviceLog.getTenantId();
        long applicationCount = getSubmittedApplicationCount(serviceId, tenantId);

        ServicePullInfoDTO dto = new ServicePullInfoDTO();
        dto.setServiceId(serviceId);
        dto.setServiceName(def.getServiceName());
        dto.setServiceAbbreviation(def.getServiceAbbreviation());
        dto.setSourceTenantId(tenantId);
        dto.setSourceTenantName(tenantId);
        dto.setSourceDepartmentId(def.getDepartmentId());
        dto.setSourceDepartmentName("Department " + def.getDepartmentId());
        dto.setVersionNo(serviceLog.getVersionNo());
        dto.setMinorVersionNo(serviceLog.getMinorVersionNo());
        dto.setServiceStatus(serviceLog.getServiceStatus());
        dto.setSubmittedApplicationsCount(applicationCount);

        return dto;
    }

    /**
     * Copy Service execution (handles both Same Department and Cross Department / Global).
     */
    @Transactional
    public CopyServiceResponseDTO copyService(CopyServiceRequestDTO request, UserSessionDTO userSession) {
        validateUserSession(userSession);
        String targetTenantId = userSession.getTenantId();

        Long targetDeptId = userSession.getEntityId().longValue();

        Integer sourceServiceId = request.getSourceServiceId();
        String centralRepoId = request.getCentralRepoId();
        ServiceLog sourceLog = (sourceServiceId != null) ? serviceLogRepository.findByServiceId(sourceServiceId) : null;

        // Cross-tenant service copying requires Central Repo Global Copy approval
        boolean isCrossTenant = (sourceLog != null && !targetTenantId.equals(sourceLog.getTenantId()));
        boolean isGlobal = Boolean.TRUE.equals(request.getGlobal())
                || (centralRepoId != null && !centralRepoId.isBlank())
                || isCrossTenant;

        validateInstanceAdminApproval(centralRepoId, sourceServiceId);

        // Generate new serviceId via sequence
        SpeSequenceLastNo speSequenceLastNo = speSequenceLastNoRepo.getSequenceLastNo(1);
        if (speSequenceLastNo == null || speSequenceLastNo.getFnc_generate_seq() == null) {
            throw new SPRuntimeError("Failed to generate new service sequence", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Integer newServiceId = Integer.valueOf(String.valueOf(speSequenceLastNo.getFnc_generate_seq()).concat("0001"));
        Integer newBaseServiceId = newServiceId / 10000;

        if (isGlobal) {
            // Global copy from Central Repo
            return executeGlobalCopy(request, newServiceId, newBaseServiceId, targetTenantId, targetDeptId, userSession);
        } else {
            // Departmental copy within tenant
            return executeDepartmentalCopy(request, newServiceId, newBaseServiceId, targetTenantId, targetDeptId, userSession);
        }
    }

    /**
     * Calls internal /b/central-copy-repo/is-approved endpoint in InstanceConfigRegistry
     * to verify if the Instance Admin has reviewed and APPROVED this service for copying.
     */
    private void validateInstanceAdminApproval(String centralRepoId, Integer serviceId) {
        if ((centralRepoId == null || centralRepoId.isBlank()) && serviceId == null) {
            throw new SPRuntimeError("Either centralRepoId or sourceServiceId must be specified for global copy", HttpStatus.BAD_REQUEST);
        }
        try {
            ResponseEntity<Map<String, Object>> resp = tenantConfigurationFeignClient.isServiceApproved(centralRepoId, serviceId);
            if (resp == null || resp.getBody() == null || !Boolean.TRUE.equals(resp.getBody().get("approved"))) {
                throw new SPRuntimeError(
                        "Cannot copy service: Service has not been approved by the Instance Admin. Only APPROVED services in the Central Repository can be copied.",
                        HttpStatus.BAD_REQUEST
                );
            }
        } catch (SPRuntimeError e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to verify Instance Admin approval with Central Repository: {}", e.getMessage());
            throw new SPRuntimeError("Cannot copy service: Failed to verify Instance Admin approval from Central Repository (" + e.getMessage() + ")", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private CopyServiceResponseDTO executeDepartmentalCopy(
            CopyServiceRequestDTO request,
            Integer newServiceId,
            Integer newBaseServiceId,
            String targetTenantId,
            Long targetDeptId,
            UserSessionDTO userSession) {

        Integer sourceServiceId = request.getSourceServiceId();
        if (sourceServiceId == null) {
            throw new SPRuntimeError("Source service ID is required for departmental copy", HttpStatus.BAD_REQUEST);
        }

        ServiceLog sourceLog = serviceLogRepository.findByServiceId(sourceServiceId);
        if (sourceLog == null) {
            throw new SPRuntimeError("Source service not found: " + sourceServiceId, HttpStatus.NOT_FOUND);
        }

        // Validate tenant isolation
        if (!targetTenantId.equals(sourceLog.getTenantId())) {
            throw new SPRuntimeError("Access denied: Source service does not belong to your tenant", HttpStatus.UNAUTHORIZED);
        }

        List<ServiceDefinition> sourceDefs = serviceDefinitionRepository.findByServiceId(sourceServiceId);
        if (sourceDefs == null || sourceDefs.isEmpty()) {
            throw new SPRuntimeError("Service Definition not found for source service: " + sourceServiceId, HttpStatus.NOT_FOUND);
        }
        ServiceDefinition sourceDef = sourceDefs.get(0);
        Long sourceDeptId = sourceDef.getDepartmentId();

        boolean isSameDepartment = Objects.equals(sourceDeptId, targetDeptId);

        String effectiveName = request.getNewServiceName();
        if (effectiveName == null || effectiveName.isBlank()) {
            effectiveName = sourceDef.getServiceName() + (isSameDepartment ? " (Copy)" : "");
        }

        String effectiveAbbr = request.getNewServiceAbbreviation();
        if (effectiveAbbr == null || effectiveAbbr.isBlank()) {
            effectiveAbbr = sourceDef.getServiceAbbreviation() + "C";
            if (effectiveAbbr.length() > 10) {
                effectiveAbbr = effectiveAbbr.substring(0, 10);
            }
        }

        if (isSameDepartment) {
            // 2.A: Copying Within the Same Department
            // Complete service configuration copied as is, in DEFINED status.
            // Retains External System, SMS, Aadhaar, eSign, Payment Gateway, SPGD as-is.
            log.info("Executing Same-Department copy from {} to {}", sourceServiceId, newServiceId);

            copyAllComponentsSameDepartment(
                    sourceServiceId,
                    newServiceId,
                    newBaseServiceId,
                    effectiveName,
                    effectiveAbbr,
                    userSession.getUserID(),
                    targetTenantId,
                    targetDeptId
            );

            recordTracker(newServiceId, newBaseServiceId, sourceServiceId, targetTenantId, sourceDeptId, targetTenantId, targetDeptId, false, false, null, "VERIFIED", userSession.getUserID());

            CopyServiceResponseDTO response = new CopyServiceResponseDTO();
            response.setSuccess(true);
            response.setNewServiceId(newServiceId);
            response.setNewBaseServiceId(newBaseServiceId);
            response.setNewServiceName(effectiveName);
            response.setNewServiceAbbreviation(effectiveAbbr);
            response.setServiceStatus(ServiceStatus.DEFINED.getCode());
            response.setCrossDepartment(false);
            response.setMessage("Service copied successfully within the same department. All existing configurations and references have been retained in DEFINED status.");
            return response;

        } else {
            // 2.B: Copying Across Different Departments
            // Complete service definition copied in DEFINED status.
            // Populates placeholder values (*****) for eSign, Aadhaar, Payment, SMS, External System, Mapped Webservice.
            // SPGD copied as is.
            log.info("Executing Cross-Department copy from {} to {} (dept {} -> {})", sourceServiceId, newServiceId, sourceDeptId, targetDeptId);

            copyAllComponentsCrossDepartment(
                    sourceServiceId,
                    newServiceId,
                    newBaseServiceId,
                    effectiveName,
                    effectiveAbbr,
                    userSession.getUserID(),
                    targetTenantId,
                    targetDeptId
            );

            recordTracker(newServiceId, newBaseServiceId, sourceServiceId, targetTenantId, sourceDeptId, targetTenantId, targetDeptId, true, false, null, "PENDING_VERIFICATION", userSession.getUserID());

            CopyServiceResponseDTO response = new CopyServiceResponseDTO();
            response.setSuccess(true);
            response.setNewServiceId(newServiceId);
            response.setNewBaseServiceId(newBaseServiceId);
            response.setNewServiceName(effectiveName);
            response.setNewServiceAbbreviation(effectiveAbbr);
            response.setServiceStatus(ServiceStatus.DEFINED.getCode());
            response.setCrossDepartment(true);
            response.setPendingValidationModules(List.of(
                    "Payment Gateway and Payment Webservice Configuration",
                    "SMS and Notification Configuration",
                    "Aadhaar Configuration",
                    "eSign Configuration",
                    "External System & Mapped Webservice Configuration"
            ));
            response.setMessage("Service copied successfully across departments in DEFINED status. Credentials and department-specific settings have been sanitized with placeholders (*****). All 8 modules must be reconfigured and verified before freezing.");
            return response;
        }
    }

    private CopyServiceResponseDTO executeGlobalCopy(
            CopyServiceRequestDTO request,
            Integer newServiceId,
            Integer newBaseServiceId,
            String targetTenantId,
            Long targetDeptId,
            UserSessionDTO userSession) {

        String centralRepoId = request.getCentralRepoId();
        Integer reqSourceServiceId = request.getSourceServiceId();

        if ((centralRepoId == null || centralRepoId.isBlank()) && reqSourceServiceId == null) {
            throw new SPRuntimeError("Either Central Repository ID or Source Service ID must be specified for global copy", HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<Map<String, Object>> centralResp;
        try {
            centralResp = tenantConfigurationFeignClient.getCentralRepoDetails(centralRepoId, reqSourceServiceId);
        } catch (Exception e) {
            throw new SPRuntimeError("Failed to fetch service details from Central Repository", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (centralResp == null || centralResp.getBody() == null) {
            throw new SPRuntimeError("Service not found in Central Repository. Service must be pushed/pulled and approved by Instance Admin before copying.", HttpStatus.NOT_FOUND);
        }

        Map<String, Object> centralItem = centralResp.getBody();

        // VALIDATION: Ensure Instance Admin has reviewed and APPROVED this service for copying
        Object statusObj = centralItem.get("status");
        String status = (statusObj != null) ? statusObj.toString() : "";
        if (!"APPROVED".equalsIgnoreCase(status)) {
            throw new SPRuntimeError(
                    "Cannot copy service: Service has not been approved by the Instance Admin. Current status in Central Repository: "
                            + (status.isBlank() ? "NOT_APPROVED" : status)
                            + ". Only APPROVED services in the Central Repository can be copied.",
                    HttpStatus.BAD_REQUEST
            );
        }

        Integer sourceServiceId = centralItem.get("serviceId") != null ? ((Number) centralItem.get("serviceId")).intValue() : reqSourceServiceId;
        if (sourceServiceId == null) {
            throw new SPRuntimeError("Central Repository service record has no serviceId", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String sourceTenantId = (String) centralItem.get("sourceTenantId");
        if (sourceTenantId == null || sourceTenantId.isBlank()) {
            throw new SPRuntimeError("Central Repository service record has no sourceTenantId", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ServiceBlueprintDTO blueprint = exportServiceBlueprintInternal(sourceServiceId, sourceTenantId);

        Long sourceDeptId = centralItem.get("sourceDepartmentId") != null ? ((Number) centralItem.get("sourceDepartmentId")).longValue() : null;

        String effectiveName = request.getNewServiceName();
        if (effectiveName == null || effectiveName.isBlank()) {
            effectiveName = (String) centralItem.get("serviceName");
        }

        String effectiveAbbr = request.getNewServiceAbbreviation();
        if (effectiveAbbr == null || effectiveAbbr.isBlank()) {
            effectiveAbbr = (String) centralItem.get("serviceAbbreviation");
            if (effectiveAbbr != null && effectiveAbbr.length() > 8) {
                effectiveAbbr = effectiveAbbr.substring(0, 8) + "CP";
            }
        }

        // Global copy is always cross-department / cross-tenant with placeholder sanitization
        importBlueprintCrossDepartment(
                blueprint,
                newServiceId,
                newBaseServiceId,
                effectiveName,
                effectiveAbbr,
                userSession.getUserID(),
                targetTenantId,
                targetDeptId
        );


        String effectiveCentralRepoId = (centralRepoId != null && !centralRepoId.isBlank()) ? centralRepoId : (String) centralItem.get("id");
        recordTracker(newServiceId, newBaseServiceId, sourceServiceId, sourceTenantId, sourceDeptId, targetTenantId, targetDeptId, true, true, effectiveCentralRepoId, "PENDING_VERIFICATION", userSession.getUserID());

        CopyServiceResponseDTO response = new CopyServiceResponseDTO();
        response.setSuccess(true);
        response.setNewServiceId(newServiceId);
        response.setNewBaseServiceId(newBaseServiceId);
        response.setNewServiceName(effectiveName);
        response.setNewServiceAbbreviation(effectiveAbbr);
        response.setServiceStatus(ServiceStatus.DEFINED.getCode());
        response.setCrossDepartment(true);
        response.setPendingValidationModules(List.of(
                "Payment Gateway and Payment Webservice Configuration",
                "SMS and Notification Configuration",
                "Aadhaar Configuration",
                "eSign Configuration",
                "External System & Mapped Webservice Configuration"
        ));
        response.setMessage("Global service copied successfully to your department in DEFINED status. Environment-specific credentials and configurations have been populated with placeholders (*****). Please review and complete validations before freezing.");
        return response;
    }

    /*
     * ============================================================
     * COPY ENGINES: SAME DEPARTMENT VS CROSS DEPARTMENT
     * ============================================================
     */

    private void copyAllComponentsSameDepartment(
            Integer sourceServiceId,
            Integer targetServiceId,
            Integer targetBaseServiceId,
            String newServiceName,
            String newServiceAbbreviation,
            Long userId,
            String tenantId,
            Long departmentId
    ) {
        // Copy service log in DEFINED status (0)
        copyServiceLog(sourceServiceId, targetServiceId, targetBaseServiceId, tenantId);

        // Copy service definition
        copyServiceDefinition(sourceServiceId, targetServiceId, targetBaseServiceId, newServiceName, newServiceAbbreviation, userId, departmentId, false);

        // Copy SDU (SPGD tab 1) as-is
        copyDeliveryUnit(sourceServiceId, targetServiceId, userId);
        copyDeliveryUnits(sourceServiceId, targetServiceId, targetBaseServiceId, tenantId, userId);

        // Copy Process Flow (SPGD tab 2 entity) as-is
        copyProcessFlow(sourceServiceId, targetServiceId, targetBaseServiceId, userId, departmentId, false);

        // Copy Output Format, MVEL, Charges, Templates, Correlation as-is
        copyOutputFormat(sourceServiceId, targetServiceId, targetBaseServiceId, userId);
        copyMvelFunctions(sourceServiceId, targetServiceId, userId);
        copyServiceCharges(sourceServiceId, targetServiceId, targetBaseServiceId, userId);
        copyTemplates(sourceServiceId, targetServiceId, targetBaseServiceId, userId);
        copyMvelEventMappings(sourceServiceId, targetServiceId, userId);
        copyCorrelationConfigs(sourceServiceId, targetServiceId, targetBaseServiceId, newServiceName, tenantId, userId);

        // Copy configurations preserving references: External, SMS, Aadhaar, eSign, Payment Gateway
        copyNotifications(sourceServiceId, targetServiceId, targetBaseServiceId, tenantId, userId, false);
        copyFilters(sourceServiceId, targetServiceId, targetBaseServiceId);
        copyAuaMappings(sourceServiceId, targetServiceId, tenantId, userId, false);
        copyPaymentGatewayConfigs(sourceServiceId, targetServiceId, tenantId, userId, false);
    }

    private void copyAllComponentsCrossDepartment(
            Integer sourceServiceId,
            Integer targetServiceId,
            Integer targetBaseServiceId,
            String newServiceName,
            String newServiceAbbreviation,
            Long userId,
            String tenantId,
            Long departmentId
    ) {
        // Copy service log in DEFINED status (0)
        copyServiceLog(sourceServiceId, targetServiceId, targetBaseServiceId, tenantId);

        // Copy service definition with target department and sanitized credentials
        copyServiceDefinition(sourceServiceId, targetServiceId, targetBaseServiceId, newServiceName, newServiceAbbreviation, userId, departmentId, true);

        // Copy SDU (SPGD tab 1) as-is
        copyDeliveryUnit(sourceServiceId, targetServiceId, userId);
        copyDeliveryUnits(sourceServiceId, targetServiceId, targetBaseServiceId, tenantId, userId);

        // Copy Process Flow (SPGD tab 2 entity) with web service URL/auth placeholder sanitization
        copyProcessFlow(sourceServiceId, targetServiceId, targetBaseServiceId, userId, departmentId, true);

        // Copy Output Format, MVEL, Charges, Templates, Correlation
        copyOutputFormat(sourceServiceId, targetServiceId, targetBaseServiceId, userId);
        copyMvelFunctions(sourceServiceId, targetServiceId, userId);
        copyServiceCharges(sourceServiceId, targetServiceId, targetBaseServiceId, userId);
        copyTemplates(sourceServiceId, targetServiceId, targetBaseServiceId, userId);
        copyMvelEventMappings(sourceServiceId, targetServiceId, userId);
        copyCorrelationConfigs(sourceServiceId, targetServiceId, targetBaseServiceId, newServiceName, tenantId, userId);

        // Copy with placeholder values (*****): External, SMS, Aadhaar, eSign, Payment Gateway
        copyNotifications(sourceServiceId, targetServiceId, targetBaseServiceId, tenantId, userId, true);
        copyFilters(sourceServiceId, targetServiceId, targetBaseServiceId);
        copyAuaMappings(sourceServiceId, targetServiceId, tenantId, userId, true);
        copyPaymentGatewayConfigs(sourceServiceId, targetServiceId, tenantId, userId, true);
    }

    /*
     * ============================================================
     * BLUEPRINT IMPORT FOR GLOBAL COPY
     * ============================================================
     */

    private void importBlueprintCrossDepartment(
            ServiceBlueprintDTO blueprint,
            Integer targetServiceId,
            Integer targetBaseServiceId,
            String newServiceName,
            String newServiceAbbreviation,
            Long userId,
            String tenantId,
            Long departmentId
    ) {
        // 1. ServiceLog
        ServiceLog log = new ServiceLog();
        log.setServiceId(targetServiceId);
        log.setBaseServiceId(targetBaseServiceId);
        log.setServiceStatus(ServiceStatus.DEFINED.getCode());
        log.setVersionNo(1);
        log.setMinorVersionNo(0);
        log.setTenantId(tenantId);
        log.setLaunchFlag(blueprint.getServiceLog() != null ? blueprint.getServiceLog().getLaunchFlag() : 'N');
        log.setTablist(blueprint.getServiceLog() != null ? blueprint.getServiceLog().getTablist() : "tab1,tab2");
        log.setLaunchDocFileId("N/A");
        log.setcDate(new Date());
        log.setuDate(new Date());
        serviceLogRepository.save(log);

        // 2. ServiceDefinition
        if (blueprint.getServiceDefinitions() != null) {
            for (ServiceDefinition srcDef : blueprint.getServiceDefinitions()) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("baseServiceId", targetBaseServiceId);
                overrides.put("versionNo", 1);
                overrides.put("minorVersionNo", 0);
                overrides.put("departmentId", departmentId);
                overrides.put("userId", userId);
                overrides.put("serviceName", newServiceName);
                overrides.put("serviceAbbreviation", newServiceAbbreviation);
                overrides.put("cDate", new Date());
                overrides.put("uDate", new Date());

                ServiceDefinition targetDef = EntityCopyUtil.copy(srcDef, ServiceDefinition.class, overrides, "serviceId");
                if (targetDef.getDefinitionJson() != null) {
                    ServiceDefinitionDTO defJson = objectMapper.convertValue(targetDef.getDefinitionJson(), ServiceDefinitionDTO.class);
                    if (newServiceName != null && !newServiceName.isBlank()) {
                        defJson.setName(newServiceName);
                    }
                    if (newServiceAbbreviation != null && !newServiceAbbreviation.isBlank()) {
                        defJson.setAbbreviation(newServiceAbbreviation);
                    }
                    defJson.setServiceId(targetServiceId);
                    targetDef.setDefinitionJson(defJson);
                }
                serviceDefinitionRepository.save(targetDef);
            }
        }

        // 3. SDU Definition (SPGD Tab 1) - as-is
        if (blueprint.getDeliveryUnitDefinition() != null) {
            Map<String, Object> overrides = new HashMap<>();
            overrides.put("serviceId", targetServiceId);
            overrides.put("minorVersionNo", 0);
            overrides.put("userId", userId);
            overrides.put("creationDate", new Date());
            overrides.put("updateDate", new Date());
            ServiceDeliveryUnitDefinition target = EntityCopyUtil.copy(blueprint.getDeliveryUnitDefinition(), ServiceDeliveryUnitDefinition.class, overrides, "serviceId");
            serviceDeliveryUnitDefinitionRepository.save(target);
        }

        // 4. Delivery Units - as-is
        if (blueprint.getDeliveryUnits() != null) {
            for (ServiceDeliveryUnits unit : blueprint.getDeliveryUnits()) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("baseServiceId", targetBaseServiceId);
                overrides.put("tenantId", tenantId);
                overrides.put("createdBy", userId);
                overrides.put("modifiedBy", userId);
                overrides.put("createdOn", new Date());
                overrides.put("modifiedOn", new Date());
                ServiceDeliveryUnits target = EntityCopyUtil.copy(unit, ServiceDeliveryUnits.class, overrides, "id", "serviceId");
                serviceDeliveryUnitsRepository.save(target);
            }
        }

        // 5. Process Flow (SPGD Tab 2 entity) - sanitize web services
        if (blueprint.getProcessFlows() != null) {
            for (ServiceProcessFlow flow : blueprint.getProcessFlows()) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("baseServiceId", targetBaseServiceId);
                overrides.put("versionNo", 1);
                overrides.put("minorVersionNo", 0);
                overrides.put("departmentId", departmentId.intValue());
                overrides.put("userId", userId);
                overrides.put("cDate", new Date());
                overrides.put("uDate", new Date());

                ServiceProcessFlow target = EntityCopyUtil.copy(flow, ServiceProcessFlow.class, overrides, "id", "serviceId");
                sanitizeProcessFlowJson(target);
                processFlowRepository.save(target);
            }
        }

        // 6. Output Format
        if (blueprint.getOutputFormats() != null) {
            for (ServiceOutputFormat format : blueprint.getOutputFormats()) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("baseServiceId", targetBaseServiceId);
                overrides.put("minorVersion", 0);
                overrides.put("userId", userId);
                overrides.put("cDate", new Date());
                overrides.put("uDate", new Date());
                ServiceOutputFormat target = EntityCopyUtil.copy(format, ServiceOutputFormat.class, overrides, "id", "serviceId");
                serviceOutputFormatRepository.save(target);
            }
        }

        // 7. MVEL Functions
        if (blueprint.getMvelFunctions() != null) {
            for (MvelFunction fn : blueprint.getMvelFunctions()) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("minorVersionNo", 0);
                overrides.put("createdBy", userId);
                overrides.put("modifiedBy", userId);
                overrides.put("createdOn", new Date());
                overrides.put("modifiedOn", new Date());
                MvelFunction target = EntityCopyUtil.copy(fn, MvelFunction.class, overrides, "id", "serviceId");
                mvelFunctionRepository.save(target);
            }
        }

        // 8. Notifications - sanitized
        if (blueprint.getNotifications() != null) {
            for (NotificationConfiguration notif : blueprint.getNotifications()) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("baseServiceId", targetBaseServiceId);
                overrides.put("tenantId", tenantId);
                overrides.put("createdBy", userId);
                overrides.put("modifiedBy", userId);
                overrides.put("createdOn", new Date());
                overrides.put("modifiedOn", new Date());
                NotificationConfiguration target = EntityCopyUtil.copy(notif, NotificationConfiguration.class, overrides, "notificationId", "serviceId");
                sanitizeNotification(target);
                notificationRepository.save(target);
            }
        }

        // 9. Payment Gateway Config - sanitized
        if (blueprint.getPaymentGatewayConfigs() != null) {
            for (ServicePaymentGatewayConfig pgc : blueprint.getPaymentGatewayConfigs()) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("tenantId", tenantId);
                overrides.put("createdBy", userId);
                overrides.put("createdAt", LocalDateTime.now());
                overrides.put("updatedAt", LocalDateTime.now());
                overrides.put("mappingJson", sanitizeJsonCredentials(pgc.getMappingJson()));
                ServicePaymentGatewayConfig target = EntityCopyUtil.copy(pgc, ServicePaymentGatewayConfig.class, overrides, "id", "serviceId");
                servicePaymentGatewayConfigRepository.save(target);
            }
        }

        // 10. AUA Mappings - sanitized
        if (blueprint.getAuaMappings() != null) {
            for (AuaApiFieldMapping aua : blueprint.getAuaMappings()) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("tenantId", tenantId);
                overrides.put("createdBy", userId);
                overrides.put("updatedBy", userId);
                overrides.put("defaultValue", PLACEHOLDER);
                overrides.put("createdAt", java.time.OffsetDateTime.now());
                overrides.put("updatedAt", java.time.OffsetDateTime.now());
                AuaApiFieldMapping target = EntityCopyUtil.copy(aua, AuaApiFieldMapping.class, overrides, "id", "serviceId");
                auaApiFieldMappingRepository.save(target);
            }
        }

        // 11. Templates, Charges, Correlation
        if (blueprint.getTemplates() != null) {
            for (ServiceTemplate tmpl : blueprint.getTemplates()) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("baseServiceId", targetBaseServiceId);
                overrides.put("versionNo", 1);
                overrides.put("minorVersion", 0);
                overrides.put("userId", userId);
                overrides.put("cDate", new Date());
                overrides.put("uDate", new Date());
                ServiceTemplate target = EntityCopyUtil.copy(tmpl, ServiceTemplate.class, overrides, "id", "serviceId");
                serviceTemplateRepository.save(target);
            }
        }

        if (blueprint.getServiceCharges() != null) {
            for (ServiceChargeDetail charge : blueprint.getServiceCharges()) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("baseServiceId", targetBaseServiceId);
                overrides.put("minorVersion", 0);
                overrides.put("userId", userId);
                overrides.put("cDate", new Date());
                overrides.put("uDate", new Date());
                ServiceChargeDetail target = EntityCopyUtil.copy(charge, ServiceChargeDetail.class, overrides, "id", "serviceId");
                serviceChargeRepository.save(target);
            }
        }
    }

    /*
     * ============================================================
     * VALIDATION BEFORE FREEZE (MANDATORY REQUIREMENT)
     * ============================================================
     */

    /**
     * During service modification and before the service is frozen,
     * validates configurations that require completion or verification:
     * - External System Configuration
     * - Notification Module Configuration
     * - Filter Configuration
     * - SMS Configuration
     * - Mapped Webservice Configuration
     * - eSign Configuration
     * - Aadhaar Configuration
     * - Payment-related configurations
     */
    public void validateServiceForFreeze(Integer serviceId, UserSessionDTO userSession) {
        validateUserSession(userSession);
        ServiceLog serviceLog = serviceLogRepository.findByServiceId(serviceId);
        if (serviceLog == null) {
            throw new SPRuntimeError("Service not found: " + serviceId, HttpStatus.NOT_FOUND);
        }

        // Rule: TenantId check everywhere in ServiceMetadata
        if (!userSession.getTenantId().equals(serviceLog.getTenantId())) {
            throw new SPRuntimeError("Access denied: Tenant mismatch", HttpStatus.UNAUTHORIZED);
        }

        List<String> unverifiedModules = new ArrayList<>();

        // 1. Payment Gateway Configuration validation
        List<ServicePaymentGatewayConfig> paymentConfigs = servicePaymentGatewayConfigRepository.findByServiceId(serviceId);
        if (paymentConfigs != null) {
            for (ServicePaymentGatewayConfig config : paymentConfigs) {
                if (Boolean.TRUE.equals(config.getEnabled()) && containsPlaceholder(config.getMappingJson())) {
                    unverifiedModules.add("Payment Gateway and Payment Webservice Configuration");
                    break;
                }
            }
        }

        // 2. SMS and Notification Module Configuration validation
        List<NotificationConfiguration> notifications = notificationRepository.findByTenantIdAndServiceId(userSession.getTenantId(), serviceId);
        if (notifications != null) {
            for (NotificationConfiguration notif : notifications) {
                if (notif.getConfigJson() != null) {
                    try {
                        String json = objectMapper.writeValueAsString(notif.getConfigJson());
                        if (containsPlaceholder(json)) {
                            unverifiedModules.add("SMS and Notification Module Configuration");
                            break;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // 3. Aadhaar / AUA Configuration validation
        List<AuaApiFieldMapping> auaMappings = auaApiFieldMappingRepository.findByServiceId(serviceId);
        if (auaMappings != null) {
            for (AuaApiFieldMapping mapping : auaMappings) {
                if (containsPlaceholder(mapping.getConfigurationId()) || containsPlaceholder(mapping.getDefaultValue())) {
                    unverifiedModules.add("Aadhaar Configuration");
                    break;
                }
            }
        }

        // 4. External System & Mapped Webservice Configuration in Process Flow
        List<ServiceProcessFlow> processFlows = processFlowRepository.findAllByServiceId(serviceId);
        if (processFlows != null) {
            for (ServiceProcessFlow flow : processFlows) {
                if (flow.getProcessFlowJson() != null) {
                    try {
                        String flowJson = objectMapper.writeValueAsString(flow.getProcessFlowJson());
                        if (containsPlaceholder(flowJson)) {
                            unverifiedModules.add("External System & Mapped Webservice Configuration");
                            break;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // 5. Filter Configuration validation
        List<InboxSentBoxFilter> filters = inboxSentBoxFilterRepository.findByServiceId(serviceId.longValue());
        if (filters != null) {
            for (InboxSentBoxFilter filter : filters) {
                if (containsPlaceholder(filter.getCondition()) || containsPlaceholder(filter.getFilterName())) {
                    unverifiedModules.add("Filter Configuration");
                    break;
                }
            }
        }

        if (!unverifiedModules.isEmpty()) {
            String modulesStr = String.join(", ", unverifiedModules);
            log.warn("Freeze blocked for serviceId={}. Unverified configurations with placeholders: {}", serviceId, modulesStr);
            throw new SPRuntimeError(
                    "Cannot freeze service: The following configurations require completion or verification before freezing: ["
                            + modulesStr + "]. Placeholder values (" + PLACEHOLDER + ") must be replaced with valid department credentials.",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Mark tracker as verified if present
        Optional<CopiedServiceTracker> trackerOpt = copiedServiceTrackerRepository.findByServiceIdAndTargetTenantId(serviceId, userSession.getTenantId());
        if (trackerOpt.isPresent()) {
            CopiedServiceTracker tracker = trackerOpt.get();
            tracker.setVerificationStatus("VERIFIED");
            copiedServiceTrackerRepository.save(tracker);
        }
    }

    /*
     * ============================================================
     * BLUEPRINT EXPORT FOR DIRECT PULL / PUSH
     * ============================================================
     */

    public ServiceBlueprintDTO exportServiceBlueprint(Integer serviceId, UserSessionDTO userSession) {
        validateUserSession(userSession);
        ServiceLog serviceLog = serviceLogRepository.findByServiceId(serviceId);
        if (serviceLog == null) {
            throw new SPRuntimeError("Service not found: " + serviceId, HttpStatus.NOT_FOUND);
        }
        if (!userSession.getTenantId().equals(serviceLog.getTenantId())) {
            throw new SPRuntimeError("Access denied: Tenant mismatch", HttpStatus.UNAUTHORIZED);
        }
        return exportServiceBlueprintInternal(serviceId, userSession.getTenantId());
    }

    private ServiceBlueprintDTO exportServiceBlueprintInternal(Integer serviceId, String tenantId) {
        ServiceBlueprintDTO dto = new ServiceBlueprintDTO();
        dto.setServiceLog(serviceLogRepository.findByServiceId(serviceId));
        dto.setServiceDefinitions(serviceDefinitionRepository.findByServiceId(serviceId));
        dto.setDeliveryUnitDefinition(serviceDeliveryUnitDefinitionRepository.findByServiceId(serviceId));
        dto.setProcessFlows(processFlowRepository.findAllByServiceId(serviceId));
        dto.setOutputFormats(serviceOutputFormatRepository.findByServiceId(serviceId));
        dto.setMvelFunctions(mvelFunctionRepository.findByServiceId(serviceId));
        dto.setNotifications(notificationRepository.findByTenantIdAndServiceId(tenantId, serviceId));
        dto.setFilters(inboxSentBoxFilterRepository.findByServiceId(serviceId.longValue()));
        dto.setAuaMappings(auaApiFieldMappingRepository.findByServiceId(serviceId));
        dto.setServiceCharges(serviceChargeRepository.findAllByServiceId(serviceId));
        dto.setDeliveryUnits(serviceDeliveryUnitsRepository.findByServiceId(serviceId));
        dto.setPaymentGatewayConfigs(servicePaymentGatewayConfigRepository.findByServiceId(serviceId));
        dto.setTemplates(serviceTemplateRepository.findByServiceId(serviceId));
        dto.setMvelEventMappings(mvelEventMappingRepository.findByServiceId(serviceId));
        dto.setCorrelationConfigs(correlationConfigurationRepository.findByServiceId(serviceId).orElse(Collections.emptyList()));
        return dto;
    }

    /*
     * ============================================================
     * COMPONENT COPY HELPERS
     * ============================================================
     */

    private void copyServiceLog(Integer sourceServiceId, Integer targetServiceId, Integer targetBaseServiceId, String tenantId) {
        ServiceLog source = serviceLogRepository.findByServiceId(sourceServiceId);
        if (source == null) {
            throw new SPRuntimeError("Service log not found for serviceId: " + sourceServiceId, HttpStatus.NOT_FOUND);
        }

        Map<String, Object> overrides = new HashMap<>();
        overrides.put("serviceId", targetServiceId);
        overrides.put("baseServiceId", targetBaseServiceId);
        overrides.put("minorVersionNo", 0);
        overrides.put("versionNo", 1);
        overrides.put("serviceStatus", ServiceStatus.DEFINED.getCode()); // Created in DEFINED status
        overrides.put("launchedBy", null);
        overrides.put("launchApprovedBy", null);
        overrides.put("launchedDate", null);
        overrides.put("cDate", new Date());
        overrides.put("uDate", new Date());
        if (tenantId != null) {
            overrides.put("tenantId", tenantId);
        }

        ServiceLog target = EntityCopyUtil.copy(source, ServiceLog.class, overrides, "serviceId");
        serviceLogRepository.save(target);
    }

    private void copyServiceDefinition(
            Integer sourceServiceId,
            Integer targetServiceId,
            Integer targetBaseServiceId,
            String newServiceName,
            String newServiceAbbreviation,
            Long userId,
            Long departmentId,
            boolean sanitizePlaceholders
    ) {
        List<ServiceDefinition> sources = serviceDefinitionRepository.findByServiceId(sourceServiceId);
        if (sources != null) {
            for (ServiceDefinition srcDef : sources) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("baseServiceId", targetBaseServiceId);
                overrides.put("versionNo", 1);
                overrides.put("minorVersionNo", 0);
                overrides.put("cDate", new Date());
                overrides.put("uDate", new Date());
                if (newServiceName != null && !newServiceName.isBlank()) {
                    overrides.put("serviceName", newServiceName);
                }
                if (newServiceAbbreviation != null && !newServiceAbbreviation.isBlank()) {
                    overrides.put("serviceAbbreviation", newServiceAbbreviation);
                }
                if (userId != null) {
                    overrides.put("userId", userId);
                }
                if (departmentId != null) {
                    overrides.put("departmentId", departmentId);
                }

                ServiceDefinition targetDef = EntityCopyUtil.copy(srcDef, ServiceDefinition.class, overrides, "serviceId");
                if (targetDef.getDefinitionJson() != null) {
                    ServiceDefinitionDTO defJson = objectMapper.convertValue(targetDef.getDefinitionJson(), ServiceDefinitionDTO.class);
                    if (newServiceName != null && !newServiceName.isBlank()) {
                        defJson.setName(newServiceName);
                    }
                    if (newServiceAbbreviation != null && !newServiceAbbreviation.isBlank()) {
                        defJson.setAbbreviation(newServiceAbbreviation);
                    }
                    defJson.setServiceId(targetServiceId);
                    targetDef.setDefinitionJson(defJson);
                }
                if (sanitizePlaceholders && targetDef.getDefinitionJson() != null) {
                    ServiceDefinitionDTO defJson = targetDef.getDefinitionJson();
                    if (defJson.getAuaConfig() != null) {
                        defJson.setAuaConfig(List.of(PLACEHOLDER));
                    }
                }
                serviceDefinitionRepository.save(targetDef);
            }
        }
    }

    private void copyDeliveryUnit(Integer sourceServiceId, Integer targetServiceId, Long userId) {
        ServiceDeliveryUnitDefinition source = serviceDeliveryUnitDefinitionRepository.findByServiceId(sourceServiceId);
        if (source != null) {
            Map<String, Object> overrides = new HashMap<>();
            overrides.put("serviceId", targetServiceId);
            overrides.put("minorVersionNo", 0);
            overrides.put("creationDate", new Date());
            overrides.put("updateDate", new Date());
            if (userId != null) {
                overrides.put("userId", userId);
            }
            ServiceDeliveryUnitDefinition target = EntityCopyUtil.copy(source, ServiceDeliveryUnitDefinition.class, overrides, "serviceId");
            serviceDeliveryUnitDefinitionRepository.save(target);
        }
    }

    private void copyDeliveryUnits(Integer sourceServiceId, Integer targetServiceId, Integer targetBaseServiceId, String tenantId, Long userId) {
        List<ServiceDeliveryUnits> sources = serviceDeliveryUnitsRepository.findByServiceId(sourceServiceId);
        if (sources != null) {
            for (ServiceDeliveryUnits source : sources) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("baseServiceId", targetBaseServiceId);
                overrides.put("tenantId", tenantId);
                overrides.put("createdOn", new Date());
                overrides.put("modifiedOn", new Date());
                if (userId != null) {
                    overrides.put("createdBy", userId);
                    overrides.put("modifiedBy", userId);
                }
                ServiceDeliveryUnits target = EntityCopyUtil.copy(source, ServiceDeliveryUnits.class, overrides, "id", "serviceId");
                serviceDeliveryUnitsRepository.save(target);
            }
        }
    }

    private void copyProcessFlow(Integer sourceServiceId, Integer targetServiceId, Integer targetBaseServiceId, Long userId, Long departmentId, boolean sanitizePlaceholders) {
        List<ServiceProcessFlow> sources = processFlowRepository.findAllByServiceId(sourceServiceId);
        if (sources != null) {
            for (ServiceProcessFlow source : sources) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("baseServiceId", targetBaseServiceId);
                overrides.put("versionNo", 1);
                overrides.put("minorVersionNo", 0);
                overrides.put("cDate", new Date());
                overrides.put("uDate", new Date());
                if (departmentId != null) {
                    overrides.put("departmentId", departmentId.intValue());
                }
                if (userId != null) {
                    overrides.put("userId", userId);
                }
                ServiceProcessFlow target = EntityCopyUtil.copy(source, ServiceProcessFlow.class, overrides, "id", "serviceId");
                if (sanitizePlaceholders) {
                    sanitizeProcessFlowJson(target);
                }
                processFlowRepository.save(target);
            }
        }
    }

    private void copyOutputFormat(Integer sourceServiceId, Integer targetServiceId, Integer targetBaseServiceId, Long userId) {
        List<ServiceOutputFormat> sources = serviceOutputFormatRepository.findByServiceId(sourceServiceId);
        if (sources != null) {
            for (ServiceOutputFormat source : sources) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("baseServiceId", targetBaseServiceId);
                overrides.put("minorVersion", 0);
                overrides.put("cDate", new Date());
                overrides.put("uDate", new Date());
                if (userId != null) {
                    overrides.put("userId", userId);
                }
                ServiceOutputFormat target = EntityCopyUtil.copy(source, ServiceOutputFormat.class, overrides, "id", "serviceId");
                serviceOutputFormatRepository.save(target);
            }
        }
    }

    private void copyMvelFunctions(Integer sourceServiceId, Integer targetServiceId, Long userId) {
        List<MvelFunction> sources = mvelFunctionRepository.findByServiceId(sourceServiceId);
        if (sources != null) {
            for (MvelFunction source : sources) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("minorVersionNo", 0);
                overrides.put("createdOn", new Date());
                overrides.put("modifiedOn", new Date());
                if (userId != null) {
                    overrides.put("createdBy", userId);
                    overrides.put("modifiedBy", userId);
                }
                MvelFunction target = EntityCopyUtil.copy(source, MvelFunction.class, overrides, "id", "serviceId");
                mvelFunctionRepository.save(target);
            }
        }
    }

    private void copyNotifications(Integer sourceServiceId, Integer targetServiceId, Integer targetBaseServiceId, String tenantId, Long userId, boolean sanitizePlaceholders) {
        List<NotificationConfiguration> sources = notificationRepository.findByServiceId(sourceServiceId);
        if (sources != null) {
            for (NotificationConfiguration source : sources) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("baseServiceId", targetBaseServiceId);
                overrides.put("tenantId", tenantId);
                overrides.put("createdOn", new Date());
                overrides.put("modifiedOn", new Date());
                if (userId != null) {
                    overrides.put("createdBy", userId);
                    overrides.put("modifiedBy", userId);
                }
                NotificationConfiguration target = EntityCopyUtil.copy(source, NotificationConfiguration.class, overrides, "notificationId", "serviceId");
                if (sanitizePlaceholders) {
                    sanitizeNotification(target);
                }
                notificationRepository.save(target);
            }
        }
    }

    private void copyFilters(Integer sourceServiceId, Integer targetServiceId, Integer targetBaseServiceId) {
        List<InboxSentBoxFilter> sources = inboxSentBoxFilterRepository.findByServiceId(sourceServiceId.longValue());
        if (sources != null) {
            for (InboxSentBoxFilter source : sources) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId.longValue());
                overrides.put("baseServiceId", targetBaseServiceId != null ? targetBaseServiceId.longValue() : null);
                overrides.put("crDate", new Date());
                overrides.put("upDate", new Date());
                InboxSentBoxFilter target = EntityCopyUtil.copy(source, InboxSentBoxFilter.class, overrides, "id", "serviceId");
                inboxSentBoxFilterRepository.save(target);
            }
        }
    }

    private void copyAuaMappings(Integer sourceServiceId, Integer targetServiceId, String tenantId, Long userId, boolean sanitizePlaceholders) {
        List<AuaApiFieldMapping> sources = auaApiFieldMappingRepository.findByServiceId(sourceServiceId);
        if (sources != null) {
            for (AuaApiFieldMapping source : sources) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("tenantId", tenantId);
                overrides.put("createdAt", java.time.OffsetDateTime.now());
                overrides.put("updatedAt", java.time.OffsetDateTime.now());
                if (userId != null) {
                    overrides.put("createdBy", userId);
                    overrides.put("updatedBy", userId);
                }
                if (sanitizePlaceholders) {
                    overrides.put("defaultValue", PLACEHOLDER);
                }
                AuaApiFieldMapping target = EntityCopyUtil.copy(source, AuaApiFieldMapping.class, overrides, "id", "serviceId");
                auaApiFieldMappingRepository.save(target);
            }
        }
    }

    private void copyPaymentGatewayConfigs(Integer sourceServiceId, Integer targetServiceId, String tenantId, Long userId, boolean sanitizePlaceholders) {
        List<ServicePaymentGatewayConfig> sources = servicePaymentGatewayConfigRepository.findByServiceId(sourceServiceId);
        if (sources != null) {
            for (ServicePaymentGatewayConfig source : sources) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("tenantId", tenantId);
                overrides.put("createdAt", LocalDateTime.now());
                overrides.put("updatedAt", LocalDateTime.now());
                if (userId != null) {
                    overrides.put("createdBy", userId);
                }
                if (sanitizePlaceholders) {
                    overrides.put("mappingJson", sanitizeJsonCredentials(source.getMappingJson()));
                }
                ServicePaymentGatewayConfig target = EntityCopyUtil.copy(source, ServicePaymentGatewayConfig.class, overrides, "id", "serviceId");
                servicePaymentGatewayConfigRepository.save(target);
            }
        }
    }

    private void copyServiceCharges(Integer sourceServiceId, Integer targetServiceId, Integer targetBaseServiceId, Long userId) {
        List<ServiceChargeDetail> sources = serviceChargeRepository.findAllByServiceId(sourceServiceId);
        if (sources != null) {
            for (ServiceChargeDetail source : sources) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("baseServiceId", targetBaseServiceId);
                overrides.put("minorVersion", 0);
                overrides.put("cDate", new Date());
                overrides.put("uDate", new Date());
                if (userId != null) {
                    overrides.put("userId", userId);
                }
                ServiceChargeDetail target = EntityCopyUtil.copy(source, ServiceChargeDetail.class, overrides, "id", "serviceId");
                serviceChargeRepository.save(target);
            }
        }
    }

    private void copyTemplates(Integer sourceServiceId, Integer targetServiceId, Integer targetBaseServiceId, Long userId) {
        List<ServiceTemplate> sources = serviceTemplateRepository.findByServiceId(sourceServiceId);
        if (sources != null) {
            for (ServiceTemplate source : sources) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("baseServiceId", targetBaseServiceId);
                overrides.put("versionNo", 1);
                overrides.put("minorVersion", 0);
                overrides.put("cDate", new Date());
                overrides.put("uDate", new Date());
                if (userId != null) {
                    overrides.put("userId", userId);
                }
                ServiceTemplate target = EntityCopyUtil.copy(source, ServiceTemplate.class, overrides, "id", "serviceId");
                serviceTemplateRepository.save(target);
            }
        }
    }

    private void copyMvelEventMappings(Integer sourceServiceId, Integer targetServiceId, Long userId) {
        List<MvelEventMapping> sources = mvelEventMappingRepository.findByServiceId(sourceServiceId);
        if (sources != null) {
            for (MvelEventMapping source : sources) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("createdOn", new Date());
                if (userId != null) {
                    overrides.put("createdBy", userId);
                }
                MvelEventMapping target = EntityCopyUtil.copy(source, MvelEventMapping.class, overrides, "eventId", "serviceId");
                mvelEventMappingRepository.save(target);
            }
        }
    }

    private void copyCorrelationConfigs(Integer sourceServiceId, Integer targetServiceId, Integer targetBaseServiceId, String serviceName, String tenantId, Long userId) {
        List<CorrelationConfiguration> sources = correlationConfigurationRepository.findByServiceId(sourceServiceId).orElse(Collections.emptyList());
        if (!sources.isEmpty()) {
            for (CorrelationConfiguration source : sources) {
                Map<String, Object> overrides = new HashMap<>();
                overrides.put("serviceId", targetServiceId);
                overrides.put("baseServiceId", targetBaseServiceId);
                overrides.put("crDate", new Date());
                overrides.put("upDate", new Date());
                if (serviceName != null && !serviceName.isBlank()) {
                    overrides.put("serviceName", serviceName);
                }
                if (tenantId != null) {
                    overrides.put("tenantId", tenantId);
                }
                if (userId != null) {
                    overrides.put("crBy", userId);
                    overrides.put("upBy", userId);
                }
                CorrelationConfiguration target = EntityCopyUtil.copy(source, CorrelationConfiguration.class, overrides, "id", "serviceId");
                correlationConfigurationRepository.save(target);
            }
        }
    }

    /*
     * ============================================================
     * SANITIZATION & PLACEHOLDER HELPERS
     * ============================================================
     */

    private void sanitizeProcessFlowJson(ServiceProcessFlow flow) {
        if (flow == null) {
            return;
        }
        if (flow.getProcessFlowJson() != null) {
            try {
                String jsonStr = objectMapper.writeValueAsString(flow.getProcessFlowJson());
                String sanitized = sanitizeJsonCredentials(jsonStr);
                flow.setProcessFlowJson(objectMapper.readValue(sanitized, ProcessFlowDTO.class));
            } catch (Exception e) {
                log.warn("Error sanitizing processFlowJson: {}", e.getMessage());
            }
        }
        if (flow.getProcessFlowObject() != null) {
            try {
                String sanitized = sanitizeJsonCredentials(flow.getProcessFlowObject().toString());
                flow.setProcessFlowObject(objectMapper.readTree(sanitized));
            } catch (Exception e) {
                log.warn("Error sanitizing draft process flow object: {}", e.getMessage());
            }
        }
    }

    private void sanitizeNotification(NotificationConfiguration notif) {
        if (notif == null || notif.getConfigJson() == null) {
            return;
        }
        try {
            String jsonStr = objectMapper.writeValueAsString(notif.getConfigJson());
            String sanitized = sanitizeJsonCredentials(jsonStr);
            notif.setConfigJson(objectMapper.readValue(sanitized, NotificationDTO.class));
        } catch (Exception e) {
            log.warn("Error sanitizing notification configJson: {}", e.getMessage());
        }
    }

    public String sanitizeJsonCredentials(String jsonStr) {
        if (jsonStr == null || jsonStr.isBlank()) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(jsonStr);
            if (root.isObject()) {
                ObjectNode obj = (ObjectNode) root;
                List<String> fieldNames = new ArrayList<>();
                obj.fieldNames().forEachRemaining(fieldNames::add);
                for (String field : fieldNames) {
                    obj.put(field, PLACEHOLDER);
                }
                return objectMapper.writeValueAsString(obj);
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private boolean containsPlaceholder(String val) {
        if (val == null) {
            return false;
        }
        return val.contains(PLACEHOLDER) || val.contains(LEGACY_PLACEHOLDER);
    }

    private long getSubmittedApplicationCount(Integer serviceId, String tenantId) {
        if (serviceId == null) {
            return 0L;
        }
        try {
            ResponseEntity<Map<String, Object>> response = processingFeignClient.getApplicationCount(serviceId, tenantId);
            if (response != null && response.getBody() != null) {
                Object countObj = response.getBody().get("submittedCount");
                if (countObj instanceof Number) {
                    return ((Number) countObj).longValue();
                }
            }
            return 0L;
        } catch (Exception e) {
            log.warn("Could not fetch application count from Processing service for serviceId={}, tenantId={}: {}", serviceId, tenantId, e.getMessage());
            return 0L;
        }
    }

    private boolean checkAlreadyInCentralRepo(Integer serviceId) {
        try {
            ResponseEntity<Map<String, Object>> response = tenantConfigurationFeignClient.checkExistsInCentralRepo(serviceId);
            if (response != null && response.getBody() != null) {
                Object exists = response.getBody().get("exists");
                return Boolean.TRUE.equals(exists);
            }
        } catch (Exception e) {
            log.warn("Failed to check central repo existence for serviceId={}: {}", serviceId, e.getMessage());
        }
        return false;
    }

    private void recordTracker(
            Integer newServiceId,
            Integer newBaseServiceId,
            Integer sourceServiceId,
            String sourceTenantId,
            Long sourceDeptId,
            String targetTenantId,
            Long targetDeptId,
            boolean isCrossDept,
            boolean isGlobal,
            String centralRepoId,
            String verificationStatus,
            Long userId) {
        try {
            CopiedServiceTracker tracker = new CopiedServiceTracker();
            tracker.setServiceId(newServiceId);
            tracker.setBaseServiceId(newBaseServiceId);
            tracker.setSourceServiceId(sourceServiceId);
            tracker.setSourceTenantId(sourceTenantId);
            tracker.setSourceDepartmentId(sourceDeptId);
            tracker.setTargetTenantId(targetTenantId);
            tracker.setTargetDepartmentId(targetDeptId);
            tracker.setCrossDepartment(isCrossDept);
            tracker.setGlobal(isGlobal);
            tracker.setCentralRepoId(centralRepoId);
            tracker.setVerificationStatus(verificationStatus);
            tracker.setCreatedBy(userId);
            tracker.setCreatedAt(new Date());
            copiedServiceTrackerRepository.save(tracker);
        } catch (Exception e) {
            log.warn("Failed to record CopiedServiceTracker for newServiceId={}: {}", newServiceId, e.getMessage());
        }
    }

    private void validateUserSession(UserSessionDTO userSession) {
        if (userSession == null) {
            throw new SPRuntimeError("User session details cannot be null", HttpStatus.UNAUTHORIZED);
        }
        if (userSession.getTenantId() == null || userSession.getTenantId().isBlank()) {
            throw new SPRuntimeError("Tenant ID cannot be null or empty", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Identifies additional configuration modules configured for this service
     * that have masked entries (credentials/settings) when copied across departments or global copy.
     */
    public List<String> getAdditionalConfigurationsForService(Integer serviceId, String tenantId, ServiceDefinition def) {
        List<String> modules = new ArrayList<>();
        if (serviceId == null) {
            return modules;
        }

        // 1. Payment Gateway and Payment Webservice Configuration
        try {
            List<ServicePaymentGatewayConfig> paymentConfigs = servicePaymentGatewayConfigRepository.findByServiceId(serviceId);
            if (paymentConfigs != null && !paymentConfigs.isEmpty()) {
                boolean hasConfig = paymentConfigs.stream().anyMatch(c ->
                        Boolean.TRUE.equals(c.getEnabled()) || (c.getMappingJson() != null && !c.getMappingJson().isEmpty()));
                if (hasConfig) {
                    modules.add("Payment Gateway and Payment Webservice Configuration");
                }
            }
        } catch (Exception e) {
            log.warn("Error checking payment config for serviceId {}: {}", serviceId, e.getMessage());
        }

        // 2. SMS and Notification Configuration
        try {
            List<NotificationConfiguration> notifications = (tenantId != null)
                    ? notificationRepository.findByTenantIdAndServiceId(tenantId, serviceId)
                    : notificationRepository.findByServiceId(serviceId);
            if (notifications != null && !notifications.isEmpty()) {
                boolean hasNotif = notifications.stream().anyMatch(n -> n.getConfigJson() != null);
                if (hasNotif) {
                    modules.add("SMS and Notification Configuration");
                }
            }
        } catch (Exception e) {
            log.warn("Error checking notification config for serviceId {}: {}", serviceId, e.getMessage());
        }

        // 3. Aadhaar Configuration
        try {
            List<AuaApiFieldMapping> auaMappings = auaApiFieldMappingRepository.findByServiceId(serviceId);
            boolean hasAadhaar = (auaMappings != null && !auaMappings.isEmpty());

            if (hasAadhaar) {
                modules.add("Aadhaar Configuration");
            }
        } catch (Exception e) {
            log.warn("Error checking aadhaar config for serviceId {}: {}", serviceId, e.getMessage());
        }

        // 4. eSign Configuration
        try {
            boolean hasEsign = false;
            Optional<ServiceEsignConfiguration> esignOpt = serviceEsignRepository.findByServiceId(serviceId);
            if (esignOpt.isPresent() && (Boolean.TRUE.equals(esignOpt.get().getEsignEnabled()) || esignOpt.get().getEsignTemplateId() != null)) {
                hasEsign = true;
            }

            if (hasEsign) {
                modules.add("eSign Configuration");
            }
        } catch (Exception e) {
            log.warn("Error checking esign config for serviceId {}: {}", serviceId, e.getMessage());
        }

        // 5. Filter Configuration
        try {
            List<InboxSentBoxFilter> filters = inboxSentBoxFilterRepository.findByServiceId(serviceId.longValue());
            if (filters != null && !filters.isEmpty()) {
                modules.add("Filter Configuration");
            }
        } catch (Exception e) {
            log.warn("Error checking filter config for serviceId {}: {}", serviceId, e.getMessage());
        }

        return modules;
    }

    private String fetchEntityName(Long entityId) {

        try {
            EntityRequestDTO req = new EntityRequestDTO(entityId);
            ResponseEntity<EntityDetailsDTO> resp = lgdFeignClient.getEntityDetails(req);

            if (resp != null && resp.getBody() != null && resp.getBody().getEntityName() != null) {
                return resp.getBody().getEntityName();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch entity details from LGD for entityId={}: {}", entityId, e.getMessage());
        }
        return null;
    }

    private String fetchTenantName(String tenantId) {
        if (tenantId == null || tenantId.isBlank() || tenantConfigurationFeignClient == null) {
            return tenantId;
        }
        try {
            ResponseEntity<ConfigurationDTO> configResp = tenantConfigurationFeignClient.getConfig(tenantId, null);
            if (configResp != null && configResp.getBody() != null && configResp.getBody().getData() != null) {
                Object basicObj = configResp.getBody().getData().get("BASIC");
                if (basicObj instanceof Map) {
                    Object tn = ((Map<?, ?>) basicObj).get("tenantName");
                    if (tn != null && !tn.toString().isBlank()) {
                        return tn.toString();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch tenant details for tenantId={}: {}", tenantId, e.getMessage());
        }
        return tenantId;
    }
}
