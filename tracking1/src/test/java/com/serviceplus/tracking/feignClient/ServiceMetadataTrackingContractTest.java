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
    void realFeignTemplatesCarryTokenTenantAndCorrectMetadataPaths() {
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

        client.getConfigureServices("test-shared-token", "tenant-a");
        client.getPublicTrackingConfiguration("test-shared-token", 7L, "tenant-a");
        assertEquals("https://metadata.invalid/services/internal/tracking/configured-services?tenantId=tenant-a", requests.get(0).url());
        assertEquals("https://metadata.invalid/services/internal/tracking/configuration?serviceId=7&tenantId=tenant-a", requests.get(1).url());
        for (Request request : requests) {
            assertEquals(Request.HttpMethod.GET, request.httpMethod());
            assertEquals(List.of("test-shared-token"), new ArrayList<>(request.headers().get("X-Tracking-Internal-Token")));
            assertFalse(request.url().contains("test-shared-token"));
        }
    }
}
