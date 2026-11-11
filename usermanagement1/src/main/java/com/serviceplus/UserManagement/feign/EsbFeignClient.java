package com.serviceplus.UserManagement.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.serviceplus.UserManagement.Dto.NotificationRequestDTO;
import com.serviceplus.UserManagement.Dto.NotificationResponseDTO;

@FeignClient(name = "Integration", path = "/esb", url = "${integration.service.url}")
public interface EsbFeignClient {

	@PostMapping("/notification/send")
    NotificationResponseDTO send(@RequestBody NotificationRequestDTO request);
}
