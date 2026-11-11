package com.serviceplus.UserManagement.Kafka;

import static com.serviceplus.UserManagement.util.SnowflakeIdGenerator.createUniqueId;
import static com.serviceplus.UserManagement.util.Utility.getNotificationRequestEntity;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.serviceplus.UserManagement.Dto.NotificationRequestDTO;
import com.serviceplus.UserManagement.Kafka.service.KafkaOutboxService;
import com.serviceplus.UserManagement.Service.TenantConfiguration;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class NotificationKafkaService {

    private static final Logger NotificationKafkaLogger = LogManager.getLogger("NotificationKafkaLogger");

    @Value("${kafka.topic.instant-notification}")
    private String INSTANT_NOTIFICATION_TOPIC;

    private final KafkaOutboxService kafkaOutboxService;
    private final TenantConfiguration tenantConfiguration;

    @Autowired
    public NotificationKafkaService(KafkaOutboxService kafkaOutboxService, TenantConfiguration tenantConfiguration) {
        this.kafkaOutboxService = kafkaOutboxService;
		this.tenantConfiguration = tenantConfiguration;
    }

    @SuppressWarnings("unchecked")
	public void sendNotification(
    		HttpServletRequest request, String tenantId,
            String beneficiary,
            String subject,
            String copyTo,
            String filePath,
            String attachmentType,
            String content,
            Integer applId,
            Integer serviceId,
            boolean isSMS,
            boolean isMail,
            Integer toMobileNo,
            Integer countryCode) {
    	Map<String, Object> tenantConfigMap=new HashMap<>();
    	Map<String, Object> configuration=new HashMap<>();
    	
    	String provider="";
    	String keys="MAIL,SMS";
    	
        try {
            NotificationKafkaLogger.info("Calling fetchConfig for keys MAIL,SMS with tenantId {}",tenantId);
        	tenantConfigMap =tenantConfiguration.fetchConfig(request,tenantId, keys);
        	Map<String, Object> data =(Map<String, Object>) tenantConfigMap.get("data");
			Map<String, Object> mailConfiguration =(Map<String, Object>) data.get("MAIL");
			Map<String, Object> smsConfiguration =(Map<String, Object>) data.get("SMS");
        	String channel="";
        	if(isMail) {
        		configuration=mailConfiguration;
        		channel="EMAIL";
        		provider=(String) configuration.getOrDefault("provider","");
        	}
        	if(isSMS) {
        		configuration=smsConfiguration;
        		channel="SMS";
        		provider=(String) configuration.getOrDefault("serviceProvider","");
        	}
        	NotificationRequestDTO notificationDto =getNotificationRequestEntity(tenantId,channel,
    				provider,beneficiary,subject,copyTo,filePath,
    				attachmentType, content, toMobileNo,countryCode,
    				configuration);
            
            kafkaOutboxService.saveEvent(INSTANT_NOTIFICATION_TOPIC, createUniqueId(), notificationDto);

            NotificationKafkaLogger.info("Notification event stored in outbox for beneficiary {}", beneficiary);

        } catch (Exception ex) {

            NotificationKafkaLogger.error("Failed to store notification event in outbox for beneficiary {}", beneficiary, ex);
            throw ex;
        }
    }
}
