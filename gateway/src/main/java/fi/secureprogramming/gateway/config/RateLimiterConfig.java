package fi.secureprogramming.gateway.config;

import fi.secureprogramming.gateway.services.DeviceVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

/*
 * This class configures the rate limiter for the Spring Cloud Gateway.
 * It uses Redis as the backend for storing rate limit information.
 */
@Configuration
public class RateLimiterConfig {

    private final DeviceVerificationService deviceService;
    private static final Logger logger = LoggerFactory.getLogger(RateLimiterConfig.class);

    private final static int REQUESTS_PER_SECOND = 1;
    private final static int REQUESTS_BURST = 1;
    private final static int REQUESTS_CAPACITY = 1;

    @Autowired
    public RateLimiterConfig(DeviceVerificationService deviceService) {
        this.deviceService = deviceService;
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(REQUESTS_PER_SECOND, REQUESTS_BURST, REQUESTS_CAPACITY) {
            @Override
            public Mono<Response> isAllowed(String routeId, String id) {
                return super.isAllowed(routeId, id).doOnNext(response -> {
                    if (!response.isAllowed()) {
                        try {
                            deviceService.inactivateDevice(id);
                        } catch (Exception e) {
                            logger.error("Failed to inactivate device: {}", e.getMessage());
                        }
                    }
                });
            }
        };
    }
}
