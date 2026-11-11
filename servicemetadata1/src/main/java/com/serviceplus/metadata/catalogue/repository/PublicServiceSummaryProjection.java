package com.serviceplus.metadata.catalogue.repository;

public interface PublicServiceSummaryProjection {
    Integer getStateId();
    long getTotalServices();
    long getG2b();
    long getG2c();
    long getG2g();
    long getG2e();
    long getOthers();
}
