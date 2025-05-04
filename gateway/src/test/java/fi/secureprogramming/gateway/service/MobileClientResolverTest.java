package fi.secureprogramming.gateway.service;

import fi.secureprogramming.gateway.services.MobileClientResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class MobileClientResolverTest {

    @InjectMocks
    private MobileClientResolver mobileClientResolver;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testMissingUUIDHeader() {
        ServerWebExchange exchange = createMockExchange(null);

        Mono<String> result = mobileClientResolver.resolve(exchange);

        assertTrue(result.blockOptional().isEmpty());
    }

    @Test
    public void testValidUUIDHeader() {
        ServerWebExchange exchange = createMockExchange("valid-uuid");

        Mono<String> result = mobileClientResolver.resolve(exchange);

        assertTrue(result.blockOptional().isPresent());
        assertTrue(result.block().equals("valid-uuid"));
    }

    private ServerWebExchange createMockExchange(String uuid) {
        HttpHeaders headers = mock(HttpHeaders.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        when(headers.getFirst("X-Device-UUID")).thenReturn(uuid);
        when(headers.getFirst("X-Signature")).thenReturn("signature");
        when(headers.getFirst("X-Timestamp")).thenReturn("timestamp");
        when(request.getHeaders()).thenReturn(headers);
        when(request.getMethod()).thenReturn(mock(HttpMethod.class));
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getURI()).thenReturn(null);
        when(response.writeWith(any())).thenReturn(Mono.empty());

        return exchange;
    }
}