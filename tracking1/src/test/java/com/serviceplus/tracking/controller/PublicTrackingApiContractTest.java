package com.serviceplus.tracking.controller;

import com.serviceplus.tracking.ExceptionHandler.GlobalExceptionHandler;
import com.serviceplus.tracking.ExceptionHandler.SPRuntimeError;
import com.serviceplus.tracking.dto.ApplicationTrackingResponse;
import com.serviceplus.tracking.service.ApplicationTrackingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PublicTrackingApiContractTest {
    private final ApplicationTrackingService service = mock(ApplicationTrackingService.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new PublicApiController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void bothConfigurationUrlsAndServicesKeepTheirContracts() throws Exception {
        when(service.getConfiguredServices(any())).thenReturn(List.of(Map.of("serviceId", 7)));
        when(service.getPublicTrackingConfig(eq(7L), any())).thenReturn(Map.of("serviceId", 7));
        mvc.perform(get("/o/appl/track/services")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serviceId").value(7));
        mvc.perform(get("/o/appl/track/services/config").param("serviceId", "7"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.serviceId").value(7));
        mvc.perform(get("/o/appl/track/services/7/config"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.serviceId").value(7));
        verify(service, times(2)).getPublicTrackingConfig(eq(7L), any());
    }

    @Test
    void otpAndAttributeEndpointsUseVerificationService() throws Exception {
        when(service.sendServiceTrackingOtp(anyMap(), any())).thenReturn(Map.of("requestId", "request"));
        when(service.verifyServiceTrackingOtp(anyMap(), any())).thenReturn(Map.of("applications", List.of()));
        when(service.verifyServiceTrackingAttributes(anyMap(), any()))
                .thenReturn(Map.of("additionalVerificationRequired", true));
        mvc.perform(post("/o/appl/track/otp/send").contentType(MediaType.APPLICATION_JSON)
                .content("{\"serviceId\":7,\"method\":\"EMAIL_OTP\",\"contact\":\"a@example.test\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.requestId").value("request"));
        mvc.perform(post("/o/appl/track/otp/verify").contentType(MediaType.APPLICATION_JSON)
                .content("{\"serviceId\":7,\"requestId\":\"request\",\"otp\":\"123456\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.applications").isArray());
        mvc.perform(post("/o/appl/track/attributes/verify").contentType(MediaType.APPLICATION_JSON)
                .content("{\"serviceId\":7,\"values\":{\"a\":\"x\",\"b\":\"y\",\"c\":\"z\"}}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.additionalVerificationRequired").value(true));
        verify(service).sendServiceTrackingOtp(eq(Map.of("serviceId", 7, "method", "EMAIL_OTP", "contact", "a@example.test")), any());
    }

    @Test
    void publicTrackingBindsScopedProofAndPropagatesDenial() throws Exception {
        ApplicationTrackingResponse result = new ApplicationTrackingResponse(); result.setApplId("app");
        when(service.trackPublicApplication(any(), any())).thenReturn(result);
        mvc.perform(post("/o/appl/track/public").contentType(MediaType.APPLICATION_JSON)
                .content("{\"serviceId\":7,\"applId\":\"app\",\"verificationProof\":\"proof\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.applId").value("app"));
        verify(service).trackPublicApplication(argThat(body -> body.getServiceId() == 7
                && "app".equals(body.getApplId()) && "proof".equals(body.getVerificationProof())), any());
        when(service.trackPublicApplication(any(), any()))
                .thenThrow(new SPRuntimeError("Verification required or expired", HttpStatus.FORBIDDEN));
        mvc.perform(post("/o/appl/track/public").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }
}
