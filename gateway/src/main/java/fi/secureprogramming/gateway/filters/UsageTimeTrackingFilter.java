package fi.secureprogramming.gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Filter to track usage time of devices.
 * This filter increments a Redis key for each device
 * based on the current hour and sets an expiration time of 30 days.
 */
@Component
@Order(2)
public class UsageTimeTrackingFilter implements GlobalFilter {

    private final ReactiveStringRedisTemplate redisTemplate;

    public UsageTimeTrackingFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String uuid = exchange.getRequest().getHeaders().getFirst("X-Device-UUID");
        if (uuid == null) return chain.filter(exchange);

        String hour = String.valueOf(LocalDateTime.now().getHour());
        String key = "usage:hour:" + uuid + ":" + hour;

        return redisTemplate.opsForValue()
                .increment(key)
                .then(redisTemplate.expire(key, Duration.ofDays(30)))
                .then(chain.filter(exchange));
    }
}
