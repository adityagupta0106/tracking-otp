package com.serviceplus.UserManagement.SSO.JanParichay;

import com.google.gson.annotations.SerializedName;

public class JanParichayTenantConfiguration {

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {

        @SerializedName("JANPARICHAY")
        private JanParichayConfig janParichay;

        public JanParichayConfig getJanParichay() {
            return janParichay;
        }

        public void setJanParichay(JanParichayConfig janParichay) {
            this.janParichay = janParichay;
        }
    }

    public static class JanParichayConfig {

        @SerializedName("client_serviceId")
        private String serviceId;

        @SerializedName("client_rest_auth_id")
        private String restAuthId;

        @SerializedName("client_id")
        private String clientId;

        @SerializedName("client_secret")
        private String clientSecret;

        @SerializedName("client_auth_key")
        private String authKey;

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getRestAuthId() {
            return restAuthId;
        }

        public void setRestAuthId(String restAuthId) {
            this.restAuthId = restAuthId;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getAuthKey() {
            return authKey;
        }

        public void setAuthKey(String authKey) {
            this.authKey = authKey;
        }
    }
}