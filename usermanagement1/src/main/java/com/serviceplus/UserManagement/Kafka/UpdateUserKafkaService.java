package com.serviceplus.UserManagement.Kafka;

import com.serviceplus.UserManagement.Dto.UserCredentialUpdationDto;
import com.serviceplus.UserManagement.Kafka.service.KafkaOutboxService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.serviceplus.UserManagement.util.SnowflakeIdGenerator.createUniqueId;

@Service
public class UpdateUserKafkaService {

    private static final Logger KafkaLogger = LogManager.getLogger("KafkaLogger");

    @Value("${kafka.update.credential.topic}")
    private String UPDATE_CREDENTIAL_TOPIC;

    private final KafkaOutboxService kafkaOutboxService;

    @Autowired
    public UpdateUserKafkaService(KafkaOutboxService kafkaOutboxService) {
        this.kafkaOutboxService = kafkaOutboxService;
    }

    public void updateUserCredential(
            Long userId,
            Integer status,
            String password,
            String signNo, String referenceId) {

        KafkaLogger.info("Saving user credential update event to outbox for user {} with status {}", signNo, status);

        UserCredentialUpdationDto dto = new UserCredentialUpdationDto();

        dto.setPassword(password);
        dto.setSignNo(signNo);
        dto.setStatus(status);
        dto.setUserId(userId);
        dto.setReferenceId(referenceId);

        String key = userId + "-" + createUniqueId();

        kafkaOutboxService.saveEvent(UPDATE_CREDENTIAL_TOPIC, key, dto);

        KafkaLogger.info("User credential update event stored in outbox for user {}", signNo);
    }
}