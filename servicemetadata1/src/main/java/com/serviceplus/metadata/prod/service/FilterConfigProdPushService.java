package com.serviceplus.metadata.prod.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.serviceplus.metadata.exception.SPRuntimeError;
import com.serviceplus.metadata.feignClient.TenantConfigurationFeignClient;
import com.serviceplus.metadata.inboxSentboxFilterConfig.dto.FilterKafkaMessage;

@Service
public class FilterConfigProdPushService extends AbstractProdPushService<FilterKafkaMessage>
		implements FilterProdClient {

	private static final String ENDPOINT = "/o/move-to-prod";

	public FilterConfigProdPushService(TenantConfigurationFeignClient tenantConfigurationFeignClient,
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
	public Map<String, String> pushToProd(String tenantId, String prodToken, List<FilterKafkaMessage> payload) {

		if (payload == null || payload.isEmpty()) {
			throw new SPRuntimeError("Filter move to prod to push", HttpStatus.BAD_REQUEST);
		}

		logger.info("Pushing {} filter configuration(s) to prod, tenant={}", payload.size(), tenantId);

		return post(tenantId, prodToken, payload);
	}
}