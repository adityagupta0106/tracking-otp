package com.serviceplus.metadata.prod.service;

import java.util.List;
import java.util.Map;

public interface ProductionClient<T> {
	Map<String, String> pushToProd(String tenantId, String prodToken, List<T> payload);
}