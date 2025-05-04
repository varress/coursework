package fi.secureprogramming.gateway.config;

import fi.secureprogramming.gateway.filters.MobileClientVerificationFilter;
import fi.secureprogramming.dto.ProductDTO;
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
    private final MobileClientVerificationFilter mobileClientVerificationFilter;

    public GatewayConfig(RedisRateLimiter redisRateLimiter,
                         IPAddressResolver ipAddressResolver,
                         MobileClientResolver mobileClientResolver,
                         MobileClientVerificationFilter mobileClientVerificationFilter) {
        this.redisRateLimiter = redisRateLimiter;
        this.ipAddressResolver = ipAddressResolver;
        this.mobileClientResolver = mobileClientResolver;
        this.mobileClientVerificationFilter = mobileClientVerificationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("create-product", createProductRoute -> createProductRoute
                        .path("/products")
                        .and().method("POST")
                        .and().readBody(ProductDTO.class, productDTO -> true)
                        .filters(createProductFilters -> createProductFilters
                                .requestRateLimiter(rateLimiterConfig -> rateLimiterConfig
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(ipAddressResolver)))
                        .uri("http://app:8080"))
                .route("get-products", getProductsRoute -> getProductsRoute
                        .path("/products")
                        .and().method("GET")
                        .filters(getProductsFilters -> getProductsFilters
                                .filter(mobileClientVerificationFilter)
                                .requestRateLimiter(rateLimiterConfig -> rateLimiterConfig
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(mobileClientResolver)))
                        .uri("http://app:8080"))
                .route("register", registerDeviceRoute -> registerDeviceRoute
                        .path("/device/register")
                        .and().method("POST")
                        .filters(registerFilters -> registerFilters
                                .requestRateLimiter(rateLimiterConfig -> rateLimiterConfig
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(ipAddressResolver)))
                        .uri("http://app:8080"))
                .route("admin-endpoints", adminRoute -> adminRoute
                        .path("/device/admin/**")
                        .filters(adminFilters -> adminFilters
                                .requestRateLimiter(rateLimiterConfig -> rateLimiterConfig
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(ipAddressResolver)))
                        .uri("http://app:8080"))
                .build();
    }
}