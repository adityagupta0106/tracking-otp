package com.serviceplus.metadata.feignClient;

import com.serviceplus.metadata.aadhaarConfiguration.dto.AadhaarConfiguration;
import com.serviceplus.metadata.aadhaarConfiguration.dto.AadhaarConfigurationResponse;
import com.serviceplus.metadata.aadhaarConfiguration.dto.AadhaarDefinerConfigurationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.serviceplus.metadata.dto.ConfigurationDTO;


@FeignClient(name = "InstanceConfigRegistry", path = "/configuration", url = "${instanceconfigregistry.service.url}")
public interface TenantConfigurationFeignClient {
    final public static String HOST_HEADER = "SP-Client-Domain";

    @PostMapping("/a/find")
    ResponseEntity<ConfigurationDTO> getConfig(@RequestParam(required = false) String tenantId, @RequestHeader(value = HOST_HEADER, required = false) String host);

    @GetMapping("/a/aua/get")
    ResponseEntity<AadhaarConfiguration> getAadhaarConfiguration(@RequestParam String configurationId, @RequestHeader(value = "USER-DETAILS", required = false) String userSession);

    @GetMapping("/a/aua/definer-configuration")
    ResponseEntity<AadhaarDefinerConfigurationResponse> getAadhaarDefinerConfiguration(
            @RequestParam String configurationId,
            @RequestHeader(value = "USER-DETAILS", required = false) String userSession
    );

    @PostMapping("/b/central-copy-repo/push")
    ResponseEntity<java.util.Map<String, Object>> pushToCentralRepo(
            @org.springframework.web.bind.annotation.RequestBody Object request,
            @RequestHeader(value = "USER-DETAILS", required = false) String userSession
    );

    @GetMapping("/a/central-copy-repo/check-exists")
    ResponseEntity<java.util.Map<String, Object>> checkExistsInCentralRepo(
            @RequestParam("serviceId") Integer serviceId
    );

    @GetMapping("/a/central-copy-repo/catalogue")
    ResponseEntity<java.util.List<java.util.Map<String, Object>>> getCentralCatalogue(
            @RequestParam(value = "search", required = false) String search
    );

    @GetMapping("/a/central-copy-repo/details")
    ResponseEntity<java.util.Map<String, Object>> getCentralRepoDetails(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "serviceId", required = false) Integer serviceId
    );

    @GetMapping("/a/central-copy-repo/available-to-copy")
    ResponseEntity<Object> getAvailableServicesToCopy(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    @GetMapping("/b/central-copy-repo/is-approved")
    ResponseEntity<java.util.Map<String, Object>> isServiceApproved(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "serviceId", required = false) Integer serviceId
    );
}
