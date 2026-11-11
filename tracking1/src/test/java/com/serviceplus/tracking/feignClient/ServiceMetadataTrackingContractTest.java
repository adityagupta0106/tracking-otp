package com.serviceplus.tracking.feignClient;

import feign.Feign;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringMvcContract;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ServiceMetadataTrackingContractTest {
    @Test
    void realFeignTemplatesCarryTenantWithoutCustomTrackingHeader() {
        List<Request> requests = new ArrayList<>();
        String prefix = ServiceMetadataFeignClient.class.getAnnotation(FeignClient.class).path();
        ServiceMetadataFeignClient client = Feign.builder().contract(new SpringMvcContract())
                .client((request, options) -> {
                    requests.add(request);
                    return Response.builder().request(request).status(200).reason("OK")
                            .headers(Map.of()).body("{}", java.nio.charset.StandardCharsets.UTF_8).build();
                })
                .decoder((response, type) -> response.request().url().contains("configured-services") ? List.of() : Map.of())
                .target(ServiceMetadataFeignClient.class, "https://metadata.invalid" + prefix);

        client.getConfigureServices("tenant-a");
        client.getPublicTrackingConfiguration(7L, "tenant-a");
        assertEquals("https://metadata.invalid/services/internal/tracking/configured-services?tenantId=tenant-a", requests.get(0).url());
        assertEquals("https://metadata.invalid/services/internal/tracking/configuration?serviceId=7&tenantId=tenant-a", requests.get(1).url());
        for (Request request : requests) {
            assertEquals(Request.HttpMethod.GET, request.httpMethod());
            assertFalse(request.headers().containsKey("X-Tracking-Internal-Token"));
        }
    }
}
