package com.serviceplus.metadata.copyservice.dto;

import com.serviceplus.metadata.aadhaarConfiguration.entity.AuaApiFieldMapping;
import com.serviceplus.metadata.correlationConfig.entiry.CorrelationConfiguration;
import com.serviceplus.metadata.entity.*;
import com.serviceplus.metadata.inboxSentboxFilterConfig.entity.InboxSentBoxFilter;
import com.serviceplus.metadata.mvel.entity.MvelEventMapping;
import com.serviceplus.metadata.mvel.entity.MvelFunction;
import com.serviceplus.metadata.notification.entity.NotificationConfiguration;

import java.io.Serializable;
import java.util.List;

public class ServiceBlueprintDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private ServiceLog serviceLog;
    private List<ServiceDefinition> serviceDefinitions;
    private ServiceDeliveryUnitDefinition deliveryUnitDefinition;
    private List<ServiceProcessFlow> processFlows;
    private List<ServiceOutputFormat> outputFormats;
    private List<MvelFunction> mvelFunctions;
    private List<NotificationConfiguration> notifications;
    private List<InboxSentBoxFilter> filters;
    private List<AuaApiFieldMapping> auaMappings;
    private List<ServiceChargeDetail> serviceCharges;
    private List<ServiceDeliveryUnits> deliveryUnits;
    private List<ServicePaymentGatewayConfig> paymentGatewayConfigs;
    private List<ServiceTemplate> templates;
    private List<MvelEventMapping> mvelEventMappings;
    private List<CorrelationConfiguration> correlationConfigs;

    public ServiceBlueprintDTO() {
    }

    public ServiceLog getServiceLog() {
        return serviceLog;
    }

    public void setServiceLog(ServiceLog serviceLog) {
        this.serviceLog = serviceLog;
    }

    public List<ServiceDefinition> getServiceDefinitions() {
        return serviceDefinitions;
    }

    public void setServiceDefinitions(List<ServiceDefinition> serviceDefinitions) {
        this.serviceDefinitions = serviceDefinitions;
    }

    public ServiceDeliveryUnitDefinition getDeliveryUnitDefinition() {
        return deliveryUnitDefinition;
    }

    public void setDeliveryUnitDefinition(ServiceDeliveryUnitDefinition deliveryUnitDefinition) {
        this.deliveryUnitDefinition = deliveryUnitDefinition;
    }

    public List<ServiceProcessFlow> getProcessFlows() {
        return processFlows;
    }

    public void setProcessFlows(List<ServiceProcessFlow> processFlows) {
        this.processFlows = processFlows;
    }

    public List<ServiceOutputFormat> getOutputFormats() {
        return outputFormats;
    }

    public void setOutputFormats(List<ServiceOutputFormat> outputFormats) {
        this.outputFormats = outputFormats;
    }

    public List<MvelFunction> getMvelFunctions() {
        return mvelFunctions;
    }

    public void setMvelFunctions(List<MvelFunction> mvelFunctions) {
        this.mvelFunctions = mvelFunctions;
    }

    public List<NotificationConfiguration> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationConfiguration> notifications) {
        this.notifications = notifications;
    }

    public List<InboxSentBoxFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<InboxSentBoxFilter> filters) {
        this.filters = filters;
    }

    public List<AuaApiFieldMapping> getAuaMappings() {
        return auaMappings;
    }

    public void setAuaMappings(List<AuaApiFieldMapping> auaMappings) {
        this.auaMappings = auaMappings;
    }

    public List<ServiceChargeDetail> getServiceCharges() {
        return serviceCharges;
    }

    public void setServiceCharges(List<ServiceChargeDetail> serviceCharges) {
        this.serviceCharges = serviceCharges;
    }

    public List<ServiceDeliveryUnits> getDeliveryUnits() {
        return deliveryUnits;
    }

    public void setDeliveryUnits(List<ServiceDeliveryUnits> deliveryUnits) {
        this.deliveryUnits = deliveryUnits;
    }

    public List<ServicePaymentGatewayConfig> getPaymentGatewayConfigs() {
        return paymentGatewayConfigs;
    }

    public void setPaymentGatewayConfigs(List<ServicePaymentGatewayConfig> paymentGatewayConfigs) {
        this.paymentGatewayConfigs = paymentGatewayConfigs;
    }

    public List<ServiceTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<ServiceTemplate> templates) {
        this.templates = templates;
    }

    public List<MvelEventMapping> getMvelEventMappings() {
        return mvelEventMappings;
    }

    public void setMvelEventMappings(List<MvelEventMapping> mvelEventMappings) {
        this.mvelEventMappings = mvelEventMappings;
    }

    public List<CorrelationConfiguration> getCorrelationConfigs() {
        return correlationConfigs;
    }

    public void setCorrelationConfigs(List<CorrelationConfiguration> correlationConfigs) {
        this.correlationConfigs = correlationConfigs;
    }
}
