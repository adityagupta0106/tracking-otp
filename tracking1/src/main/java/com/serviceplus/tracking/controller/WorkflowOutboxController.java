package com.serviceplus.tracking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serviceplus.tracking.service.WorkflowOutboxService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/a/workflow/outbox")
public class WorkflowOutboxController {

    @Autowired
    private WorkflowOutboxService workflowOutboxService;

    @RequestMapping(value = "/list", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> getOutbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            HttpServletRequest request) {

        return ResponseEntity.ok(
                workflowOutboxService.getOutboxApplications(request, page, size));
    }
}
