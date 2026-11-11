package com.serviceplus.metadata.prod.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.serviceplus.metadata.dto.NotificationConfigurationKafkaDTO;
import com.serviceplus.metadata.exception.SPRuntimeError;
import com.serviceplus.metadata.feignClient.TenantConfigurationFeignClient;

@Service
public class NotificationConfigProdPushService extends AbstractProdPushService<NotificationConfigurationKafkaDTO>
		implements NotificationProdClient {

	private static final String ENDPOINT = "/spgateway/notification/o/move-to-prod";

	public NotificationConfigProdPushService(TenantConfigurationFeignClient tenantConfigurationFeignClient,
			@Qualifier("prodSyncRestClient") RestClient restClient,
			@Value("${sync.prod.allowed-hosts}") Set<String> allowedHosts,
			@Value("${sync.prod.enabled:false}") boolean enabled) {
		super(tenantConfigurationFeignClient, restClient, allowedHosts, enabled);
	}

	@Override
	protected String endpoint() {
		return ENDPOINT;
	}

	@Override
	public Map<String, String> pushToProd(String tenantId, String prodToken,
			List<NotificationConfigurationKafkaDTO> payload) {
		if (payload == null || payload.isEmpty()) {
			throw new SPRuntimeError("Nothing to push", HttpStatus.BAD_REQUEST);
		}
		logger.info("Pushing {} notification configuration(s) to prod, tenant={}", payload.size(), tenantId);
		return post(tenantId, prodToken, payload);
	}
}