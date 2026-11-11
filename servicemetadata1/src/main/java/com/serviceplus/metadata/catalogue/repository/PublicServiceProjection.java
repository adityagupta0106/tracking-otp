package com.serviceplus.metadata.catalogue.repository;

import java.time.Instant;

public interface PublicServiceProjection {
    Integer getServiceId();
    Integer getStateId();
    String getServiceName();
    Instant getLaunchedDate();
}
