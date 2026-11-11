package com.serviceplus.metadata.catalogue.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;

/** stateId is currently a CLC ID, not an LGD state code. */
public final class PublicServiceCatalogueDTO {
    private PublicServiceCatalogueDTO() {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record StateSummary(Integer stateId, String stateName, long totalServices,
            long g2b, long g2c, long g2g, long g2e, long others) {
    }

    public record CategoryTotals(long g2b, long g2c, long g2g, long g2e, long others) {
    }

    public record SummaryResponse(List<StateSummary> data, long grandTotal, CategoryTotals grandTotals) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ServiceItem(Integer serviceId, Integer stateId, String stateName,
            String serviceName, @JsonFormat(shape = JsonFormat.Shape.STRING) Instant launchedDate) {
    }

    public record ServicesResponse(List<ServiceItem> data, int page, int size,
            long totalRecords, int totalPages, boolean hasNext) {
    }
}
