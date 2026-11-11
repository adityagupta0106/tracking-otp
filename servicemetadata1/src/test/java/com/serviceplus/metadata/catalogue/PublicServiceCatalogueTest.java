package com.serviceplus.metadata.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviceplus.metadata.catalogue.controller.PublicServiceCatalogueController;
import com.serviceplus.metadata.catalogue.controller.ServiceCatalogueController;
import com.serviceplus.metadata.catalogue.repository.PublicServiceProjection;
import com.serviceplus.metadata.catalogue.repository.PublicServiceSummaryProjection;
import com.serviceplus.metadata.catalogue.service.ServiceCatalogueService;
import com.serviceplus.metadata.entity.ServiceDefinition;
import com.serviceplus.metadata.enums.ServiceStatus;
import com.serviceplus.metadata.exception.GlobalExceptionHandler;
import com.serviceplus.metadata.exception.SPRuntimeError;
import com.serviceplus.metadata.feignClient.LgdFeignClient;
import com.serviceplus.metadata.feignClient.ProcessingFeignClient;
import com.serviceplus.metadata.repository.IServiceDefinitionRepository;
import com.serviceplus.metadata.repository.IServiceLogRepository;

class PublicServiceCatalogueTest {
    private IServiceDefinitionRepository repository;
    private IServiceLogRepository logs;
    private ProcessingFeignClient processing;
    private LgdFeignClient lgd;
    private ServiceCatalogueService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        repository = mock(IServiceDefinitionRepository.class);
        logs = mock(IServiceLogRepository.class);
        processing = mock(ProcessingFeignClient.class);
        lgd = mock(LgdFeignClient.class);
        service = new ServiceCatalogueService(null, null, repository, logs, null, new ObjectMapper(),
                null, null, null, null, null, processing, lgd);
        mvc = MockMvcBuilders.standaloneSetup(new PublicServiceCatalogueController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void summaryReturnsReconciledGrandTotalsAndNumericGroupsWithoutNamesOrExternalCalls() throws Exception {
        when(repository.findPublicServiceSummary()).thenReturn(List.of(summary(32, 2, 1, 1), summary(33, 1, 0, 1)));
        mvc.perform(get("/catalogue/public/service-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].stateId").value(32))
                .andExpect(jsonPath("$.data[0].stateName").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.grandTotal").value(3))
                .andExpect(jsonPath("$.grandTotals.g2c").value(1))
                .andExpect(jsonPath("$.grandTotals.others").value(2));
        verify(repository).findPublicServiceSummary();
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(logs, processing, lgd);
    }

    @Test
    void listUsesDefaultsRetainsNullsAndReturnsOnlyRequiredFields() throws Exception {
        when(repository.findPublicServices(32, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(item(410001, null)), PageRequest.of(0, 10), 1));
        mvc.perform(get("/catalogue/public/services").param("stateId", "32"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].length()").value(5))
                .andExpect(jsonPath("$.data[0].serviceId").value(410001))
                .andExpect(jsonPath("$.data[0].stateId").value(32))
                .andExpect(jsonPath("$.data[0].stateName").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data[0].launchedDate").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data[0].serviceName").value("Example"))
                .andExpect(jsonPath("$.data[0].serviceKey").doesNotExist())
                .andExpect(jsonPath("$.data[0].applyUrl").doesNotExist())
                .andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalRecords").value(1)).andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
        verify(repository).findPublicServices(32, PageRequest.of(0, 10));
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(logs, processing, lgd);
    }

    @ParameterizedTest
    @CsvSource({"-1,10", "0,0", "0,-1", "0,101"})
    void invalidPaginationIs400BeforeQuery(int page, int size) throws Exception {
        mvc.perform(get("/catalogue/public/services").param("stateId", "32")
                .param("page", String.valueOf(page)).param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository, logs, processing, lgd);
    }

    @Test
    void missingAndNonnumericParametersAre400WithRealExceptionAdvice() throws Exception {
        mvc.perform(get("/catalogue/public/services")).andExpect(status().isBadRequest());
        for (String parameter : List.of("stateId", "page", "size")) {
            var request = get("/catalogue/public/services");
            if (!parameter.equals("stateId")) request.param("stateId", "32");
            mvc.perform(request.param(parameter, "invalid")).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(repository);
        assertThatThrownBy(() -> service.getPublicServices(null, 0, 10))
                .isInstanceOfSatisfying(SPRuntimeError.class, ex -> assertThat(ex.getErrorCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void emptyResultsAndOutOfRangePageAreValid() {
        when(repository.findPublicServiceSummary()).thenReturn(List.of());
        assertThat(service.getPublicServiceSummary().grandTotal()).isZero();
        assertThat(service.getPublicServiceSummary().grandTotals().others()).isZero();
        when(repository.findPublicServices(999, PageRequest.of(5, 100)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(5, 100), 0));
        var response = service.getPublicServices(999, 5, 100);
        assertThat(response.data()).isEmpty();
        assertThat(response.totalRecords()).isZero();
        assertThat(response.page()).isEqualTo(5);
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    void paginationMetadataIsNotDerivedFromCurrentPageLength() {
        when(repository.findPublicServices(32, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(List.of(item(410002, Instant.parse("2026-10-13T10:48:00Z"))), PageRequest.of(1, 1), 3));
        var response = service.getPublicServices(32, 1, 1);
        assertThat(response.totalRecords()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.data().getFirst().launchedDate()).isEqualTo(Instant.parse("2026-10-13T10:48:00Z"));
    }

    @Test
    void authenticatedCatalogueRetainsStatusesOrderingFieldsAndOptionalProcessingKey() {
        ServiceDefinition definition = new ServiceDefinition();
        definition.setServiceId(410001);
        definition.setServiceName("Existing");
        when(repository.findCataloguePage(eq(32), eq(null), any(), any()))
                .thenReturn(new PageImpl<>(List.of(definition), PageRequest.of(0, 20), 1));
        when(processing.getServiceList("existing-session")).thenReturn(List.of(java.util.Map.of("serviceId", 410001, "serviceKey", "key+value")));
        var response = service.getServiceCatalogue(32, null, 0, 20, "existing-session");
        assertThat(response.get("size")).isEqualTo(20);
        var row = (java.util.Map<?, ?>) ((List<?>) response.get("data")).getFirst();
        assertThat(row.get("serviceKey")).isEqualTo("key%2Bvalue");
        assertThat(row.get("service")).isEqualTo("Existing");
        assertThat(row.keySet().stream().map(String::valueOf).toList())
                .contains("departmentId", "department", "description", "sla", "fee", "serviceId");
        verify(repository).findCataloguePage(32, null,
                List.of(ServiceStatus.FROZEN.getCode(), ServiceStatus.LAUNCHED.getCode(), ServiceStatus.ACTIVATED.getCode()),
                PageRequest.of(0, 20, org.springframework.data.domain.Sort.by("serviceId").ascending()));
        verify(processing).getServiceList("existing-session");
    }

    @Test
    void existingAuthenticatedControllerMappingStillUsesExistingServiceMethod() throws Exception {
        var existing = mock(ServiceCatalogueService.class);
        when(existing.getServiceCatalogue(null, null, 0, 20, null)).thenReturn(java.util.Map.of("data", List.of()));
        var existingMvc = MockMvcBuilders.standaloneSetup(new ServiceCatalogueController(existing, null)).build();
        existingMvc.perform(get("/catalogue/service-catalogue")).andExpect(status().isOk());
        verify(existing).getServiceCatalogue(null, null, 0, 20, null);
        verifyNoMoreInteractions(existing);
    }

    private PublicServiceProjection item(int id, Instant launchedDate) {
        return new PublicServiceProjection() {
            public Integer getServiceId() { return id; }
            public Integer getStateId() { return 32; }
            public String getServiceName() { return "Example"; }
            public Instant getLaunchedDate() { return launchedDate; }
        };
    }

    private PublicServiceSummaryProjection summary(int state, long total, long g2c, long others) {
        return new PublicServiceSummaryProjection() {
            public Integer getStateId() { return state; }
            public long getTotalServices() { return total; }
            public long getG2b() { return 0; }
            public long getG2c() { return g2c; }
            public long getG2g() { return 0; }
            public long getG2e() { return 0; }
            public long getOthers() { return others; }
        };
    }
}
