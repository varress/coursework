package fi.secureprogramming.gateway.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class MetricsController {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final MeterRegistry meterRegistry;
    private final Map<String, AtomicLong> gauges = new ConcurrentHashMap<>();

    public MetricsController(ReactiveStringRedisTemplate redisTemplate, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedRate = 60000)
    public void exportUsageMetrics() {
        redisTemplate.keys("usage:hour:*")
                .flatMap(key -> redisTemplate.opsForValue().get(key)
                        .flatMap(countStr -> {
                            try {
                                long count = Long.parseLong(countStr);
                                String[] parts = key.split(":");
                                if (parts.length != 4) return Mono.empty();

                                String uuid = parts[2];
                                String hour = parts[3];
                                String metricKey = uuid + ":" + hour;

                                gauges.computeIfAbsent(metricKey, k -> {
                                    AtomicLong ref = new AtomicLong();
                                    meterRegistry.gauge("device_hourly_usage",
                                            Tags.of("uuid", uuid, "hour", hour),
                                            ref);
                                    return ref;
                                }).set(count); // Dynamically update the value

                            } catch (Exception e) {
                                System.err.println("Gauge parse error for key " + key + ": " + e.getMessage());
                            }
                            return Mono.empty();
                        }))
                .onErrorContinue((e, o) -> System.err.println("Metric export error: " + e.getMessage()))
                .subscribe();
    }
}
