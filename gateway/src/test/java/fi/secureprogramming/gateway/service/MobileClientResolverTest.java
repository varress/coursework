package fi.secureprogramming.gateway.service;

import fi.secureprogramming.gateway.services.DeviceVerificationService;
import fi.secureprogramming.gateway.services.MobileClientResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class MobileClientResolverTest {

    @Mock
    private DeviceVerificationService deviceVerificationService;

    @InjectMocks
    private MobileClientResolver mobileClientResolver;

    private DataBufferFactory dataBufferFactory;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        dataBufferFactory = new DefaultDataBufferFactory();
    }

    @Test
    public void testMissingUUIDHeader() {
        ServerWebExchange exchange = createMockExchange(null, "something", "1744224120");

        Mono<String> result = mobileClientResolver.resolve(exchange);

        assertUnauthorizedResponse(exchange, result);
    }

    @Test
    public void testMissingSignatureHeader() {
        ServerWebExchange exchange = createMockExchange("something", null, "1744224120");

        Mono<String> result = mobileClientResolver.resolve(exchange);

        assertUnauthorizedResponse(exchange, result);
    }

    @Test
    public void testMissingTimestampHeader() {
        ServerWebExchange exchange = createMockExchange("something", "something", null);

        Mono<String> result = mobileClientResolver.resolve(exchange);

        assertUnauthorizedResponse(exchange, result);
    }

    @Test
    public void testInactiveDevice() throws Exception {
        ServerWebExchange exchange = createMockExchange("something", "something", "1744224120");
        when(deviceVerificationService.verifyDevice("something", "something", "1744224120"))
                .thenThrow(new AuthenticationException("Device is not active"));

        Mono<String> result = mobileClientResolver.resolve(exchange);

        assertUnauthorizedResponse(exchange, result);
    }

    @Test
    public void testInvalidSignature() throws Exception {
        ServerWebExchange exchange = createMockExchange("something", "something", "1744224120");
        when(deviceVerificationService.verifyDevice("something", "something", "1744224120"))
                .thenThrow(new AuthenticationException("Invalid signature"));

        Mono<String> result = mobileClientResolver.resolve(exchange);

        assertUnauthorizedResponse(exchange, result);
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
        when(request.getURI()).thenReturn(null);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(response.bufferFactory()).thenReturn(dataBufferFactory);
        when(response.writeWith(any())).thenReturn(Mono.empty());

        return exchange;
    }

    private void assertUnauthorizedResponse(ServerWebExchange exchange, Mono<String> result) {
        assertTrue(result.blockOptional().isEmpty());
        verify(exchange.getResponse()).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(exchange.getResponse()).writeWith(any());
    }
}