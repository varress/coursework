package fi.secureprogramming.gateway.config;

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
                .route("create-product", createProductRoute -> createProductRoute
                        .path("/products")
                        .and().method("POST")
                        .filters(createProductFilters -> createProductFilters
                                .requestRateLimiter(rateLimiterConfig -> rateLimiterConfig
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(ipAddressResolver)))
                        .uri("http://app:8080"))
                .route("get-products", getProductsRoute -> getProductsRoute
                        .path("/products")
                        .and().method("GET")
                        .filters(getProductsFilters -> getProductsFilters
                                .requestRateLimiter(rateLimiterConfig -> rateLimiterConfig
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(mobileClientResolver)))
                        .uri("http://app:8080"))
                .route("register", registerDeviceRoute -> registerDeviceRoute
                        .path("/device/register")
                        .and().method("POST")
                        .filters(getProductsFilters -> getProductsFilters
                                .requestRateLimiter(rateLimiterConfig -> rateLimiterConfig
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(ipAddressResolver)))
                        .uri("http://app:8080"))
                .route("inactivate", inactivateDeviceRoute -> inactivateDeviceRoute
                        .path("/device/inactivate")
                        .and().method("POST")
                        .filters(inactivateDeviceFilters -> inactivateDeviceFilters
                                .requestRateLimiter(rateLimiterConfig -> rateLimiterConfig
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(ipAddressResolver)))
                        .uri("http://app:8080"))
                .route("activate", activateDeviceRoute -> activateDeviceRoute
                        .path("/device/activate")
                        .and().method("POST")
                        .filters(activateDeviceFilters -> activateDeviceFilters
                                .requestRateLimiter(rateLimiterConfig -> rateLimiterConfig
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(ipAddressResolver)))
                        .uri("http://app:8080"))
                .route("get-devices", getDevicesRoute -> getDevicesRoute
                        .path("/device")
                        .and().method("GET")
                        .filters(getDevicesFilters -> getDevicesFilters
                                .requestRateLimiter(rateLimiterConfig -> rateLimiterConfig
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(ipAddressResolver)))
                        .uri("http://app:8080"))
                .build();
    }
}
