package com.serviceplus.UserManagement.Dto;

import jakarta.validation.constraints.NotBlank;

public class DSCRegistrationRequest {

    @NotBlank
    private String certificate;

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }
}
