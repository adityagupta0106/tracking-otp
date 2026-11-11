
package com.serviceplus.tracking.controller;

import com.serviceplus.tracking.dto.LatestDocumentDownloadResponse;
import com.serviceplus.tracking.service.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import com.serviceplus.tracking.dto.UserSessionDTO;
import com.serviceplus.tracking.service.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.serviceplus.tracking.utility.CommonUtil.getUserSessionDetails;

@RestController
public class ChatbotController {
	private static final Logger log =
            LoggerFactory.getLogger(ChatbotController.class);


	private final ChatbotService chatbotService;

	public ChatbotController(ChatbotService chatbotService) {
	    this.chatbotService = chatbotService;
	}

    // Logged-in user: Download latest permitted document
    @GetMapping("/a/chatbot/document/download")
    public LatestDocumentDownloadResponse downloadLatestDocument(@RequestParam("applRefNo") String applRefNo,HttpServletRequest request)
    {
    	  log.info("Chatbot download API reached");

          UserSessionDTO user = getUserSessionDetails(request);

          log.info(
                  "Chatbot auth check: sessionPresent={}, userIdPresent={}, tenantPresent={}",
                  user != null,
                  user != null && user.getUserID() != null,
                  user != null && user.getTenantId() != null
                          && !user.getTenantId().isBlank()
          );

    	return chatbotService.downloadLatest(applRefNo,getUserSessionDetails(request)
    	);
    }

    // Future chatbot APIs will be added here.
}
