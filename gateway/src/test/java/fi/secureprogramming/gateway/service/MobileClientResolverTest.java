package fi.secureprogramming.gateway.service;

import fi.secureprogramming.gateway.services.DeviceService;
import fi.secureprogramming.gateway.services.MobileClientResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
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
    private DeviceService deviceService;

    @InjectMocks
    private MobileClientResolver mobileClientResolver;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testMissingUUIDHeader() {
        HttpHeaders headers = mock(HttpHeaders.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap("Missing required headers".getBytes());

        ServerWebExchange exchange = mock(ServerWebExchange.class);

        when(headers.getFirst("X-Device-UUID")).thenReturn(null);
        when(headers.getFirst("X-Signature")).thenReturn("something");
        when(headers.getFirst("X-Timestamp")).thenReturn("1744224120");
        when(request.getHeaders()).thenReturn(headers);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(response.bufferFactory()).thenReturn(dataBufferFactory);
        when(response.writeWith(any())).thenReturn(Mono.empty());

        Mono<String> result = mobileClientResolver.resolve(exchange);

        assertTrue(result.blockOptional().isEmpty());
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(response).writeWith(any());
    }

    @Test
    public void testMissingSignatureHeader() {
        HttpHeaders headers = mock(HttpHeaders.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap("Missing required headers".getBytes());

        ServerWebExchange exchange = mock(ServerWebExchange.class);

        when(headers.getFirst("X-Device-UUID")).thenReturn("something");
        when(headers.getFirst("X-Signature")).thenReturn(null);
        when(headers.getFirst("X-Timestamp")).thenReturn("1744224120");
        when(request.getHeaders()).thenReturn(headers);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(response.bufferFactory()).thenReturn(dataBufferFactory);
        when(response.writeWith(any())).thenReturn(Mono.empty());
        when(headers.getFirst("X-Timestamp")).thenReturn("1744224120");
        when(request.getHeaders()).thenReturn(headers);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(response.bufferFactory()).thenReturn(dataBufferFactory);
        when(response.writeWith(any())).thenReturn(Mono.empty());

        Mono<String> result = mobileClientResolver.resolve(exchange);

        assertTrue(result.blockOptional().isEmpty());
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(response).writeWith(any());
    }

    @Test
    public void testMissingTimestampHeader() {
        HttpHeaders headers = mock(HttpHeaders.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap("Missing required headers".getBytes());

        ServerWebExchange exchange = mock(ServerWebExchange.class);

        when(headers.getFirst("X-Device-UUID")).thenReturn("something");
        when(headers.getFirst("X-Signature")).thenReturn("something");
        when(headers.getFirst("X-Timestamp")).thenReturn(null);
        when(request.getHeaders()).thenReturn(headers);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(response.bufferFactory()).thenReturn(dataBufferFactory);
        when(response.writeWith(any())).thenReturn(Mono.empty());

        Mono<String> result = mobileClientResolver.resolve(exchange);

        assertTrue(result.blockOptional().isEmpty());
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(response).writeWith(any());
    }

    @Test
    public void testInactiveDevice() throws AuthenticationException {
        HttpHeaders headers = mock(HttpHeaders.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap("Missing required headers".getBytes());

        ServerWebExchange exchange = mock(ServerWebExchange.class);

        when(headers.getFirst("X-Device-UUID")).thenReturn("something");
        when(headers.getFirst("X-Signature")).thenReturn("something");
        when(headers.getFirst("X-Timestamp")).thenReturn("1744224120");
        when(request.getHeaders()).thenReturn(headers);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(response.bufferFactory()).thenReturn(dataBufferFactory);
        when(response.writeWith(any())).thenReturn(Mono.empty());
        when(deviceService.verifyDevice("something", "something", "1744224120"))
                .thenThrow(new AuthenticationException("Device is not active"));

        Mono<String> result = mobileClientResolver.resolve(exchange);

        assertTrue(result.blockOptional().isEmpty());
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(response).writeWith(any());
    }

    @Test
    public void testInvalidSignature() throws AuthenticationException {
        HttpHeaders headers = mock(HttpHeaders.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap("Missing required headers".getBytes());

        ServerWebExchange exchange = mock(ServerWebExchange.class);

        when(headers.getFirst("X-Device-UUID")).thenReturn("something");
        when(headers.getFirst("X-Signature")).thenReturn("something");
        when(headers.getFirst("X-Timestamp")).thenReturn("1744224120");
        when(request.getHeaders()).thenReturn(headers);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(response.bufferFactory()).thenReturn(dataBufferFactory);
        when(response.writeWith(any())).thenReturn(Mono.empty());
        when(deviceService.verifyDevice("something", "something", "1744224120"))
                .thenThrow(new AuthenticationException("Invalid signature"));

        Mono<String> result = mobileClientResolver.resolve(exchange);

        assertTrue(result.blockOptional().isEmpty());
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(response).writeWith(any());
    }

}