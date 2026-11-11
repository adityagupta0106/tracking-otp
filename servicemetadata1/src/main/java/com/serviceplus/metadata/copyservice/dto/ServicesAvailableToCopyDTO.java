package com.serviceplus.metadata.copyservice.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class ServicesAvailableToCopyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String category; // "SAME_DEPARTMENT", "GLOBAL_SERVICES", or "ALL"
    private List<DepartmentalServiceItemDTO> sameDepartmentServices;
    private List<GlobalServiceItemDTO> globalServices;

    public ServicesAvailableToCopyDTO() {
        this.category = "ALL";
        this.sameDepartmentServices = Collections.emptyList();
        this.globalServices = Collections.emptyList();
    }

    public ServicesAvailableToCopyDTO(String category, List<DepartmentalServiceItemDTO> sameDepartmentServices, List<GlobalServiceItemDTO> globalServices) {
        this.category = category != null ? category : "ALL";
        this.sameDepartmentServices = sameDepartmentServices != null ? sameDepartmentServices : Collections.emptyList();
        this.globalServices = globalServices != null ? globalServices : Collections.emptyList();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<DepartmentalServiceItemDTO> getSameDepartmentServices() {
        return sameDepartmentServices;
    }

    public void setSameDepartmentServices(List<DepartmentalServiceItemDTO> sameDepartmentServices) {
        this.sameDepartmentServices = sameDepartmentServices;
    }

    public List<GlobalServiceItemDTO> getGlobalServices() {
        return globalServices;
    }

    public void setGlobalServices(List<GlobalServiceItemDTO> globalServices) {
        this.globalServices = globalServices;
    }
}
