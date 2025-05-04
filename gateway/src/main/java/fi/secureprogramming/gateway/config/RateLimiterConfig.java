package fi.secureprogramming.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * This class configures the rate limiter for the Spring Cloud Gateway.
 * It uses Redis as the backend for storing rate limit information.
 */
@Configuration
public class RateLimiterConfig {

    private final static int REQUESTS_PER_SECOND = 1;
    private final static int REQUESTS_BURST = 1;
    private final static int REQUESTS_CAPACITY = 1;

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(REQUESTS_PER_SECOND, REQUESTS_BURST, REQUESTS_CAPACITY);
    }
}
