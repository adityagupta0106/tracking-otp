package com.serviceplus.metadata.copyservice.dto;

import java.io.Serializable;

public class PushToCentralRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer serviceId;

    public PushToCentralRequest() {
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }
}
