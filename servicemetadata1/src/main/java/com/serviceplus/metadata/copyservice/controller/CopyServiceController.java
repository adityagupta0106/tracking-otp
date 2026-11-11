package com.serviceplus.metadata.copyservice.controller;

import com.serviceplus.metadata.dto.ServerSidePaginationRecord;
import com.serviceplus.metadata.copyservice.dto.*;
import com.serviceplus.metadata.copyservice.service.CopyServiceService;
import com.serviceplus.metadata.dto.UserSessionDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.serviceplus.metadata.utility.CommonUtil.getUserSessionDetails;

@RestController
@RequestMapping("/a")
public class CopyServiceController {

    private final CopyServiceService copyServiceService;

    public CopyServiceController(CopyServiceService copyServiceService) {
        this.copyServiceService = copyServiceService;
    }

    /**
     * Option 1: Departmental Services for Service Definer.
     * Displays services available for copying under the selected department within the tenant.
     * Supports search by complete or partial name.
     */
    @GetMapping("/copy-service/departmental-services")
    public ResponseEntity<List<DepartmentalServiceItemDTO>> getDepartmentalServices(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String search,
            HttpServletRequest request) {
        UserSessionDTO user = getUserSessionDetails(request);
        List<DepartmentalServiceItemDTO> result = copyServiceService.getDepartmentalServices(departmentId, search, user);
        return ResponseEntity.ok(result);
    }

    /**
     * Option 2: Global Services for Service Definer.
     * Displays services published in Central Copy Service Catalogue.
     * Supports search by complete or partial name.
     */
    @GetMapping("/copy-service/global-services")
    public ResponseEntity<List<GlobalServiceItemDTO>> getGlobalServices(
            @RequestParam(required = false) String search,
            HttpServletRequest request) {
        UserSessionDTO user = getUserSessionDetails(request);
        List<GlobalServiceItemDTO> result = copyServiceService.getGlobalServices(search, user);
        return ResponseEntity.ok(result);
    }

    /**
     * List services available to Department Admin for copying from Central Repository.
     * Categorized into:
     * 1. SAME_DEPARTMENT (Departmental services within the same department from Central Repository)
     * 2. GLOBAL_SERVICES (Global services across multiple tenants from Central Catalogue)
     * Supports server-side pagination with ServerSidePaginationRecord.
     */
    @GetMapping("/copy-service/available-to-copy")
    public ResponseEntity<ServerSidePaginationRecord<AvailableToCopyItemDTO>> getAvailableServicesToCopy(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        UserSessionDTO user = getUserSessionDetails(request);
        String safeSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        ServerSidePaginationRecord<AvailableToCopyItemDTO> result =
                copyServiceService.getServicesAvailableToCopy(category, departmentId, safeSearch, page, size, user);
        return ResponseEntity.ok(result);
    }

    /**
     * Department Admin pushes an eligible LAUNCHED service (with at least 2 submitted applications)
     * from their tenant into the Central Copy Service Repository.
     * Same service cannot be pushed twice.
     */
    @PostMapping("/copy-service/push-to-central")
    public ResponseEntity<Map<String, Object>> pushToCentral(
            @RequestBody PushToCentralRequest req,
            HttpServletRequest request) {
        UserSessionDTO user = getUserSessionDetails(request);
        Map<String, Object> response = copyServiceService.pushServiceToCentral(req.getServiceId(), user);
        return ResponseEntity.ok(response);
    }

    /**
     * List services available to Department Admin to push to Central Repository.
     * Returns launched services belonging to the user's tenant/department.
     * Evaluates submitted application count and central repo presence to determine eligibility.
     */
    @GetMapping("/copy-service/available-to-push")
    public ResponseEntity<ServerSidePaginationRecord<AvailableServicePushDTO>> getAvailableServicesToPush(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        int safePage = Math.max(0, Math.min(page, 10000));
        int safeSize = (size <= 0) ? 10 : Math.min(size, 100);
        UserSessionDTO user = getUserSessionDetails(request);
        ServerSidePaginationRecord<AvailableServicePushDTO> result =
                copyServiceService.getAvailableServicesToPush(search, safePage, safeSize, user);
        return ResponseEntity.ok(result);
    }

    /**
     * List services available to be pulled by Central Instance Admin.
     * Search by service name (case-insensitive ILIKE) with server-side pagination.
     */
    @GetMapping("/copy-service/available-to-pull")
    public ResponseEntity<ServerSidePaginationRecord<AvailableServicePullDTO>> getAvailableServicesToPull(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "2") int minApplications) {
        int safePage = Math.max(0, Math.min(page, 10000));
        int safeSize = (size <= 0) ? 10 : Math.min(size, 100);
        ServerSidePaginationRecord<AvailableServicePullDTO> result =
                copyServiceService.getAvailableServicesToPull(search, safePage, safeSize, minApplications);
        return ResponseEntity.ok(result);
    }

    /**
     * Complete service info for Central Instance Admin direct pull by serviceId.
     */
    @GetMapping("/copy-service/pull-info")
    public ResponseEntity<ServicePullInfoDTO> getServicePullInfo(
            @RequestParam Integer serviceId) {
        ServicePullInfoDTO result = copyServiceService.getServicePullInfo(serviceId);
        return ResponseEntity.ok(result);
    }

    /**
     * Copy Service execution (handles Departmental same-dept, Departmental cross-dept, and Global copy).
     * Created in DEFINED status.
     * Cross-department copies populate placeholders (*****) and require reconfiguration before freezing.
     */
    @PostMapping("/copy-service/copy")
    public ResponseEntity<CopyServiceResponseDTO> copyService(
            @RequestBody CopyServiceRequestDTO requestDto,
            HttpServletRequest request) {
        UserSessionDTO user = getUserSessionDetails(request);
        CopyServiceResponseDTO response = copyServiceService.copyService(requestDto, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Export complete service blueprint.
     */
    @GetMapping("/copy-service/export-blueprint")
    public ResponseEntity<ServiceBlueprintDTO> exportBlueprint(
            @RequestParam Integer serviceId,
            HttpServletRequest request) {
        UserSessionDTO user = getUserSessionDetails(request);
        ServiceBlueprintDTO blueprint = copyServiceService.exportServiceBlueprint(serviceId, user);
        return ResponseEntity.ok(blueprint);
    }

    /**
     * Pre-freeze validation check: Verifies that configurations requiring completion
     * (External System, Notifications, Filters, SMS, Mapped Webservices, eSign, Aadhaar, Payment)
     * do not contain placeholder values (*****).
     */
    @GetMapping("/copy-service/validate-freeze")
    public ResponseEntity<Map<String, Object>> validateFreeze(
            @RequestParam Integer serviceId,
            HttpServletRequest request) {
        UserSessionDTO user = getUserSessionDetails(request);
        copyServiceService.validateServiceForFreeze(serviceId, user);
        return ResponseEntity.ok(Map.of("valid", true, "message", "All required configurations are complete and verified"));
    }
}
