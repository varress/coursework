package fi.secureprogramming.gateway.filters;

import fi.secureprogramming.gateway.services.DeviceVerificationService;
import fi.secureprogramming.model.Device;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.security.sasl.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MobileClientVerificationFilterTest {

    @Mock
    private DeviceVerificationService deviceVerificationService;

    @InjectMocks
    private MobileClientVerificationFilter mobileClientVerificationFilter;

    private DataBufferFactory dataBufferFactory;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        dataBufferFactory = new DefaultDataBufferFactory();
    }

    @Test
    public void testMissingUUIDHeader() {
        ServerWebExchange exchange = createMockExchange(null, "signature", "timestamp");

        Mono<Void> result = mobileClientVerificationFilter.filter(exchange, mockFilterChain());

        assertUnauthorizedResponse(exchange, result, "Missing required headers: X-Device-UUID, X-Signature, X-Timestamp");
    }

    @Test
    public void testMissingSignatureHeader() {
        ServerWebExchange exchange = createMockExchange("uuid", null, "timestamp");

        Mono<Void> result = mobileClientVerificationFilter.filter(exchange, mockFilterChain());

        assertUnauthorizedResponse(exchange, result, "Missing required headers: X-Device-UUID, X-Signature, X-Timestamp");
    }

    @Test
    public void testMissingTimestampHeader() {
        ServerWebExchange exchange = createMockExchange("uuid", "signature", null);

        Mono<Void> result = mobileClientVerificationFilter.filter(exchange, mockFilterChain());

        assertUnauthorizedResponse(exchange, result, "Missing required headers: X-Device-UUID, X-Signature, X-Timestamp");
    }

    @Test
    public void testInactiveDevice() throws Exception {
        ServerWebExchange exchange = createMockExchange("uuid", "signature", "timestamp");
        when(deviceVerificationService.verifyDevice("uuid", "signature", "timestamp"))
                .thenThrow(new AuthenticationException("Device is not active"));

        Mono<Void> result = mobileClientVerificationFilter.filter(exchange, mockFilterChain());

        assertUnauthorizedResponse(exchange, result, "Device is not active");
    }

    @Test
    public void testInvalidSignature() throws Exception {
        ServerWebExchange exchange = createMockExchange("uuid", "invalid-signature", "timestamp");
        when(deviceVerificationService.verifyDevice("uuid", "invalid-signature", "timestamp"))
                .thenThrow(new AuthenticationException("Invalid signature"));

        Mono<Void> result = mobileClientVerificationFilter.filter(exchange, mockFilterChain());

        assertUnauthorizedResponse(exchange, result, "Invalid signature");
    }

    @Test
    public void testValidHeaders() throws Exception {
        ServerWebExchange exchange = createMockExchange("valid-uuid", "valid-signature", "timestamp");
        when(deviceVerificationService.verifyDevice("valid-uuid", "valid-signature", "timestamp"))
                .thenReturn(new Device("valid-uuid", "valid-secret", true));

        Mono<Void> result = mobileClientVerificationFilter.filter(exchange, mockFilterChain());

        verify(exchange.getResponse(), never()).setStatusCode(HttpStatus.UNAUTHORIZED);
        assertEquals(Mono.empty(), result);
    }

    @Test
    public void testEmptyHeaders() {
        ServerWebExchange exchange = createMockExchange("", "", "");

        Mono<Void> result = mobileClientVerificationFilter.filter(exchange, mockFilterChain());

        assertUnauthorizedResponse(exchange, result, "Missing required headers: X-Device-UUID, X-Signature, X-Timestamp");
    }

    @Test
    public void testNullHeaders() {
        ServerWebExchange exchange = createMockExchange(null, null, null);

        Mono<Void> result = mobileClientVerificationFilter.filter(exchange, mockFilterChain());

        assertUnauthorizedResponse(exchange, result, "Missing required headers: X-Device-UUID, X-Signature, X-Timestamp");
    }

    @Test
    public void testInvalidTimestampFormat() throws Exception {
        ServerWebExchange exchange = createMockExchange("valid-uuid", "valid-signature", "invalid-timestamp");
        when(deviceVerificationService.verifyDevice("valid-uuid", "valid-signature", "invalid-timestamp"))
                .thenThrow(new AuthenticationException("Invalid timestamp format"));

        Mono<Void> result = mobileClientVerificationFilter.filter(exchange, mockFilterChain());

        assertUnauthorizedResponse(exchange, result, "Invalid timestamp format");
    }

    @Test
    public void testExceptionDuringVerification() throws Exception {
        ServerWebExchange exchange = createMockExchange("valid-uuid", "valid-signature", "1744224120");
        when(deviceVerificationService.verifyDevice("valid-uuid", "valid-signature", "1744224120"))
                .thenThrow(new RuntimeException("Unexpected error"));

        Mono<Void> result = mobileClientVerificationFilter.filter(exchange, mockFilterChain());

        assertUnauthorizedResponse(exchange, result, "Unexpected error");
    }

    private ServerWebExchange createMockExchange(String uuid, String signature, String timestamp) {
        HttpHeaders headers = mock(HttpHeaders.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        when(headers.getFirst("X-Device-UUID")).thenReturn(uuid);
        when(headers.getFirst("X-Signature")).thenReturn(signature);
        when(headers.getFirst("X-Timestamp")).thenReturn(timestamp);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getMethod()).thenReturn(mock(HttpMethod.class));
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getURI()).thenReturn(null);
        when(response.bufferFactory()).thenReturn(dataBufferFactory);
        when(response.writeWith(any())).thenReturn(Mono.empty());

        return exchange;
    }

    private GatewayFilterChain mockFilterChain() {
        GatewayFilterChain filterChain = mock(GatewayFilterChain.class);
        when(filterChain.filter(any())).thenReturn(Mono.empty());
        return filterChain;
    }

    private void assertUnauthorizedResponse(ServerWebExchange exchange, Mono<Void> result, String expectedMessage) {
        verify(exchange.getResponse()).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(exchange.getResponse()).writeWith(any());
        assertEquals(Mono.empty(), result);
    }
}