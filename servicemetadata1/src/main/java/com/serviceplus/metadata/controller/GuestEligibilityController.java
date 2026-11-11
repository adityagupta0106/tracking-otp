package com.serviceplus.metadata.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serviceplus.metadata.service.GuestEligibilityService;

@RestController
public class GuestEligibilityController {

    private final GuestEligibilityService guestEligibilityService;

    public GuestEligibilityController(GuestEligibilityService guestEligibilityService) {
        this.guestEligibilityService = guestEligibilityService;
    }

    @GetMapping("/internal/guest/eligibility")
    public ResponseEntity<?> getGuestEligibility(  @RequestParam Integer serviceId, @RequestHeader(value = "X-Guest-Internal-Key", required = false) String suppliedSecret) {
    	
        return guestEligibilityService.checkEligibility(serviceId, suppliedSecret);
    }
}
