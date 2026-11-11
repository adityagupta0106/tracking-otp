package com.serviceplus.metadata.prod.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.serviceplus.metadata.dto.ConfigurationDTO;
import com.serviceplus.metadata.exception.SPRuntimeError;
import com.serviceplus.metadata.feignClient.TenantConfigurationFeignClient;

public abstract class AbstractProdPushService<T> implements ProductionClient<T> {

	protected final Logger logger = LogManager.getLogger(getClass());

	private final TenantConfigurationFeignClient tenantConfigurationFeignClient;
	private final RestClient restClient;
	private final Set<String> allowedHosts;
	private final boolean enabled;

	protected AbstractProdPushService(TenantConfigurationFeignClient tenantConfigurationFeignClient,
			RestClient restClient, Set<String> allowedHosts, boolean enabled) {
		this.tenantConfigurationFeignClient = tenantConfigurationFeignClient;
		this.restClient = restClient;
		this.allowedHosts = allowedHosts.stream().map(h -> h.trim().toLowerCase(Locale.ROOT))
				.collect(Collectors.toUnmodifiableSet());
		this.enabled = enabled;
	}

	protected abstract String endpoint();

	protected Map<String, String> post(String tenantId, String prodToken, List<T> payload) {
		if (!enabled) {
			throw new SPRuntimeError("Prod operations are disabled in this environment", HttpStatus.FORBIDDEN);
		}
		if (prodToken == null || prodToken.isBlank()) {
			throw new SPRuntimeError("Prod token is required", HttpStatus.BAD_REQUEST);
		}

		String prodUrl = resolveProdUrl(tenantId);
		logger.info("PROD call: POST {} tenant={}", endpoint(), tenantId);

		try {
			return restClient.post().uri("https://"+prodUrl + endpoint()).header("prod-token", prodToken)
					.contentType(MediaType.APPLICATION_JSON).body(payload).retrieve()
					.body(new ParameterizedTypeReference<Map<String, String>>() {
					});
		} catch (RestClientResponseException e) {
			logger.error("Prod push failed. endpoint={}, status={}, body={}", endpoint(), e.getStatusCode(),
					e.getResponseBodyAsString());
			throw new SPRuntimeError("Prod rejected the request: " + e.getStatusCode(), HttpStatus.BAD_GATEWAY);
		} catch (ResourceAccessException e) {
			logger.error("Prod unreachable. endpoint={}", endpoint(), e);
			throw new SPRuntimeError("Production not reachable", HttpStatus.GATEWAY_TIMEOUT);
		}
	}

	private String resolveProdUrl(String tenantId) {
		ResponseEntity<ConfigurationDTO> response = tenantConfigurationFeignClient.getConfig(tenantId, null);

		Map<String, Object> data = Optional.ofNullable(response.getBody()).map(ConfigurationDTO::getData)
				.orElseThrow(() -> new SPRuntimeError("Production URL Not Found", HttpStatus.BAD_REQUEST));

		if (!(data.get("BASIC") instanceof Map<?, ?> basic) || basic.get("PROD_URL") == null) {
			throw new SPRuntimeError("Production URL Not Found", HttpStatus.BAD_REQUEST);
		}

		String prodUrl = basic.get("PROD_URL").toString().trim();
//		validate(prodUrl);
		return prodUrl.endsWith("/") ? prodUrl.substring(0, prodUrl.length() - 1) : prodUrl;
	}

//	private void validate(String url) {
//		URI uri;
//		try {
//			uri = URI.create(url);
//		} catch (IllegalArgumentException e) {
//			throw new SPRuntimeError("Invalid Production URL", HttpStatus.BAD_REQUEST);
//		}
//
//		String host = uri.getHost();
//		boolean valid = "https".equalsIgnoreCase(uri.getScheme()) && host != null
//				&& allowedHosts.contains(host.toLowerCase(Locale.ROOT));
//
//		if (!valid) {
//			logger.warn("Rejected production URL host={}", host);
//			throw new SPRuntimeError("Invalid Production URL", HttpStatus.BAD_REQUEST);
//		}
//	}
}