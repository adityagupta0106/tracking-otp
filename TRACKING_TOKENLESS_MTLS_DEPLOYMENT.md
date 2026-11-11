# Tokenless tracking APIs — mTLS deployment contract

This branch removes the custom `TRACKING_INTERNAL_TOKEN` setting and `X-Tracking-Internal-Token` request header from the ServicePlus public-tracking workflow.

## Security boundary

Internal endpoints continue to be private. Their controllers now authorize the **servlet container's validated TLS client certificate**, rather than a shared tracking token. The `jakarta.servlet.request.X509Certificate` request attribute is connector-managed. An arbitrary HTTP header never grants access.

By default, the peer allowlist is empty and **every internal tracking request is rejected with HTTP 403**. This is intentional.

Deploy these endpoints behind private network access controls. Enable HTTPS on the *receiving* microservices, configure server-side trust for the client-certificate issuing CA, and set `server.ssl.client-auth=want` (or `need` for a dedicated internal listener). With `want`, public HTTPS requests without a client certificate may still connect, but these internal endpoints reject them.

Example **deployment-supplied** Spring properties (substitute your actual secret mount paths; DO NOT commit credentials or PKCS12 files):

```properties
server.ssl.enabled=true
server.ssl.key-store=file:/run/secrets/service-server.p12
server.ssl.key-store-type=PKCS12
server.ssl.key-store-password=${SERVER_TLS_KEYSTORE_PASSWORD}
server.ssl.trust-store=file:/run/secrets/trusted-clients.p12
server.ssl.trust-store-type=PKCS12
server.ssl.trust-store-password=${CLIENT_CA_TRUSTSTORE_PASSWORD}
server.ssl.client-auth=want
```

The trust store must trust **only your approved internal client CA**. Each calling service must have its **own** client certificate issued by that CA, including the expected exact DNS Subject Alternative Name (SAN), and must present it over TLS. The servlet container or a trusted TLS-pass-through connector must validate the certificate chain before exposing the certificate request attribute. **Do not forward client certificates through `X-Forwarded-Client-Cert` or another request header** to bypass this check.

Recommended deployment properties for receiving services (replace these example internal DNS names with the DNS SANs on your actual service certificates):

```properties
# ServiceMetadata receives calls from TrackingService and FormDesigner
tracking.internal.mtls.allowed-peer-dns=trackingservice.internal,formdesigner.internal

# FormDesigner receives calls from TrackingService
tracking.internal.mtls.allowed-peer-dns=trackingservice.internal

# UserManagement receives calls from TrackingService
tracking.internal.mtls.allowed-peer-dns=trackingservice.internal
```

Set those properties **independently for each service**; the three examples are not one combined property value.

## Caller configuration

TrackingService must connect to mTLS-enabled HTTPS targets. Change the target URLs in its deployment configuration, **not** to the legacy `http://` defaults in the repository snapshot:

```properties
servicemetadata.service.url=https://<metadata-internal-host>:<tls-port>
formdesigner.service.url=https://<formdesigner-internal-host>:<tls-port>
usermanagement.service.url=https://<usermanagement-internal-host>:<tls-port>
```

FormDesigner must also use an HTTPS ServiceMetadata target. Its `ServiceMataDataFeignClient` now supports a `servicemetadata.service.url` override; set it to the internal HTTPS metadata endpoint, or configure service discovery to advertise HTTPS. Ensure the actual Feign HTTP client can present the calling service's client certificate and validate the server certificate. Configure its keystore/truststore via your chosen Feign client SSL context; JVM default TLS system properties work only if that client honors them. Verify this in the full deployed project.

For each caller, client-certificate material must be managed as secrets. Example JVM system properties if supported by its actual Feign transport:

```text
-Djavax.net.ssl.keyStore=/run/secrets/tracking-client.p12
-Djavax.net.ssl.keyStoreType=PKCS12
-Djavax.net.ssl.keyStorePassword=<injected secret>
-Djavax.net.ssl.trustStore=/run/secrets/server-ca.p12
-Djavax.net.ssl.trustStoreType=PKCS12
-Djavax.net.ssl.trustStorePassword=<injected secret>
```

## Public endpoint contract (unchanged)

- `GET /tracking/o/appl/track/services`
- `GET /tracking/o/appl/track/services/config?serviceId=830001`
- `POST /tracking/o/appl/track/otp/send`
- `POST /tracking/o/appl/track/otp/verify`
- `POST /tracking/o/appl/track/attributes/verify`

Their existing tenant scoping, rate limiting, strong-attribute policy and OTP Redis/HMAC secret are preserved. **Do not remove `tracking.otp.hmac-secret`**, which is unrelated to the removed internal shared token.

## Verification checklist before deployment

1. Compile all **four complete parent projects** and execute their unit/integration tests. This GitHub snapshot contains `src` folders, not the original Maven/Gradle build files; compilation has not been executed here.
2. Assert direct HTTP / unauthenticated HTTPS / spoofed client-cert-header requests to every `/internal/tracking/**` endpoint return 403.
3. Assert requests with an expired, untrusted or incorrect SAN certificate are denied. Assert correct trusted SAN certificates reach the controller, and unrelated public endpoints still work.
4. Verify TrackingService → ServiceMetadata, TrackingService → FormDesigner, TrackingService → UserManagement, and FormDesigner → ServiceMetadata HTTPS/mTLS handshakes.
5. Repeat public `/services`, `/services/config`, Mobile OTP, Email OTP, OTP verify and Form Attributes tests. These depend on tenant configuration, Mongo/Postgres data consistency, Redis and ESB.
6. Resolve the **separate existing FormDesigner submission/reference mismatch (503)** before expecting the entire OTP-send flow to work.

## Scope and deployment gate

The source removes only the custom shared tracking token and introduces a certificate-based authorization boundary. It does **not** create PKI, certificates, TLS listeners or service-mesh policies in your infrastructure. **Do not deploy without mTLS and tests.** An HTTP-only office environment will correctly return 403 on internal tracking calls until TLS peer identity is configured.
