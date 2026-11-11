package com.serviceplus.metadata.feignClient;

import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "PROCESSING", path = "/processing" , url="${processing.service.url}")
public interface ProcessingFeignClient {

    @PostMapping("/a/serviceList")
    List<Map<String, Object>> getServiceList( @RequestHeader("USER-DETAILS") String userDetails
    );

    @GetMapping("/a/application/count")
    ResponseEntity<Map<String, Object>> getApplicationCount(
            @RequestParam("serviceId") Integer serviceId,
            @RequestParam(value = "tenantId", required = false) String tenantId
    );
}