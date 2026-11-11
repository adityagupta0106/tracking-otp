package com.serviceplus.UserManagement.Kafka;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.serviceplus.UserManagement.Dto.UserCredentialUpdationDto;
import static com.serviceplus.UserManagement.util.Utility.entityToString;

import java.util.Date;

@Service
public class KafkaService {

	@Autowired
	private KafkaProducer kafkaProducer;
	
	@Value("${kafka.update.credential.topic}")
	private String UPDATE_CREDENTIAL_TOPIC;
	
	private static final Logger KafkaLogger = LogManager.getLogger("KafkaLogger");
	
	@Async
	public void updateUserCredential(Integer userId,Integer status,String password,String signNo, String referenceId) {
		
		KafkaLogger.info("Sharing user credential through kafka to authentication for user {} with status {}",signNo,status);
		
		UserCredentialUpdationDto dto = new UserCredentialUpdationDto();
		dto.setPassword(password);
		dto.setSignNo(signNo);
		dto.setStatus(status);
		dto.setUserId(userId.longValue());
		dto.setReferenceId(referenceId);
		String json = entityToString(dto);
		String key = userId + "-" + new Date().getTime();
		
		kafkaProducer.sendMessage(UPDATE_CREDENTIAL_TOPIC, key, json);
	}
}
