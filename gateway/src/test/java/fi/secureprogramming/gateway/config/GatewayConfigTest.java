package fi.secureprogramming.gateway.config;

import fi.secureprogramming.gateway.dto.ProductDTO;
import fi.secureprogramming.gateway.services.IPAddressResolver;
import fi.secureprogramming.gateway.services.MobileClientResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class GatewayConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private RedisRateLimiter redisRateLimiter;

    @MockitoBean
    private IPAddressResolver ipAddressResolver;

    @MockitoBean
    private MobileClientResolver mobileClientResolver;

    @Test
    void testRateLimitingExceededWithIpAddress() {
        RateLimiter.Response response = new RateLimiter.Response(false, getRateLimitResponseHeaders());
        when(redisRateLimiter.isAllowed(any(), any())).thenReturn(Mono.just(response));
        when(ipAddressResolver.resolve(any())).thenReturn(Mono.just("1.2.3.4"));

        webTestClient.post()
                .uri("/products")
                .bodyValue(new ProductDTO())
                .exchange()
                .expectStatus().isEqualTo(429);
    }

    @Test
    void testRateLimitingExceededWithMobileClient() {
        RateLimiter.Response response = new RateLimiter.Response(false, getRateLimitResponseHeaders());
        when(redisRateLimiter.isAllowed(any(), any())).thenReturn(Mono.just(response));
        when(mobileClientResolver.resolve(any())).thenReturn(Mono.just("1132435453"));

        webTestClient.get()
                .uri("/products")
                .exchange()
                .expectStatus().isEqualTo(429);

    }

    private Map<String, String> getRateLimitResponseHeaders() {
        return Map.of(
                "REMAINING_HEADER", "0",
                "REPLENISH_RATE_HEADER", "1",
                "BURST_CAPACITY_HEADER", "1",
                "REQUESTED_TOKENS_HEADER", "1"
        );
    }

}