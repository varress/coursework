package fi.secureprogramming.gateway.config;

import fi.secureprogramming.gateway.dto.ProductDTO;
import fi.secureprogramming.gateway.services.IPAddressResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class GatewayConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    private final static int REQUESTS_PER_SECOND = 1;
    private final static int REQUESTS_BURST = 1;
    private final static int REQUESTS_CAPACITY = 1;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setPassword(redisPassword);
        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("create-product", r -> r.path("/products")
                        .and().method("POST")
                        .and().readBody(ProductDTO.class, productDTO -> true)
                        .filters(f -> f.requestRateLimiter(r2 -> r2.setRateLimiter(redisRateLimiter())
                                .setKeyResolver(new IPAddressResolver())))
                        .uri("http://app:8080"))
                .route("get-products", r -> r.path("/products")
                        .and().method("GET")
                        .uri("http://app:8080"))
                .build();
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(REQUESTS_PER_SECOND, REQUESTS_BURST, REQUESTS_CAPACITY);
    }
}
