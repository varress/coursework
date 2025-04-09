package fi.secureprogramming.gateway.config;

import fi.secureprogramming.gateway.dto.ProductDTO;
import fi.secureprogramming.gateway.services.IPAddressResolver;
import fi.secureprogramming.gateway.services.MobileClientResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final RedisRateLimiter redisRateLimiter;
    private final IPAddressResolver ipAddressResolver;
    private final MobileClientResolver mobileClientResolver;

    public GatewayConfig(RedisRateLimiter redisRateLimiter, IPAddressResolver ipAddressResolver, MobileClientResolver mobileClientResolver) {
        this.redisRateLimiter = redisRateLimiter;
        this.ipAddressResolver = ipAddressResolver;
        this.mobileClientResolver = mobileClientResolver;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("create-product", r -> r.path("/products")
                        .and().method("POST")
                        .and().readBody(ProductDTO.class, productDTO -> true)
                        .filters(f -> f.requestRateLimiter(r2 -> r2.setRateLimiter(redisRateLimiter)
                                .setKeyResolver(ipAddressResolver)))
                        .uri("http://app:8080"))
                .route("get-products", r -> r.path("/products")
                        .and().method("GET")
                        .filters(f -> f.requestRateLimiter(r2 -> r2.setRateLimiter(redisRateLimiter)
                                .setKeyResolver(mobileClientResolver)))
                        .uri("http://app:8080"))
                .build();
    }
}
