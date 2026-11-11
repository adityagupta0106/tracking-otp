package com.serviceplus.metadata.catalogue.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.serviceplus.metadata.catalogue.dto.PublicServiceCatalogueDTO.ServicesResponse;
import com.serviceplus.metadata.catalogue.dto.PublicServiceCatalogueDTO.SummaryResponse;
import com.serviceplus.metadata.catalogue.service.ServiceCatalogueService;

/** Public-facing contract; anonymous gateway exposure requires national scope approval. */
@RestController
@RequestMapping("/o/catalogue/public")
public class PublicServiceCatalogueController {

    private final ServiceCatalogueService serviceCatalogueService;

    public PublicServiceCatalogueController(ServiceCatalogueService serviceCatalogueService) {
        this.serviceCatalogueService = serviceCatalogueService;
    }

    @GetMapping("/service-summary")
    public ResponseEntity<SummaryResponse> getPublicServiceSummary() {
        return ResponseEntity.ok(serviceCatalogueService.getPublicServiceSummary());
    }

    @GetMapping("/services")
    public ResponseEntity<ServicesResponse> getPublicServices(
            @RequestParam(name = "stateId") Integer stateId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(serviceCatalogueService.getPublicServices(stateId, page, size));
    }

    // The application's generic exception advice otherwise turns binding errors into 500s.
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> invalidParameters(Exception exception) {
        return ResponseEntity.badRequest().body(Map.of("message", "stateId, page and size must be valid integers; stateId is required"));
    }
}
