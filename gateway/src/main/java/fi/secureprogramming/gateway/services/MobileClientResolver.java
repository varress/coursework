package fi.secureprogramming.gateway.services;

import fi.secureprogramming.gateway.filters.LoggingFilter;
import fi.secureprogramming.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Primary
public class MobileClientResolver implements KeyResolver {

    @Autowired
    private DeviceVerificationService deviceVerificationService;

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class.getName());

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return resolveClient(exchange)
                .map(Device::getUuid)
                .onErrorResume(e -> unauthorized(exchange, e.getMessage()).then(Mono.empty()));
    }

    public Mono<Device> resolveClient(ServerWebExchange exchange) {
        return extractHeaders(exchange)
                .flatMap(headers -> {
                    try {
                        Device device = deviceVerificationService.verifyDevice(
                                headers.uuid(),
                                headers.signature(),
                                headers.timestamp()
                        );
                        return Mono.just(device);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                });
    }

    private Mono<DeviceHeaders> extractHeaders(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();

        String uuid = headers.getFirst("X-Device-UUID");
        String signature = headers.getFirst("X-Signature");
        String timestamp = headers.getFirst("X-Timestamp");

        if (uuid == null || signature == null || timestamp == null) {
            return Mono.error(new IllegalArgumentException("Missing required headers: X-Device-UUID, X-Signature, X-Timestamp"));
        }

        return Mono.just(new DeviceHeaders(uuid, signature, timestamp));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        // Log request too
        logger.info(String.format("[RESPONSE] %s %s => %d",
                exchange.getRequest().getMethod().name(),
                exchange.getRequest().getURI(),
                HttpStatus.UNAUTHORIZED.value()));

        String message = reason != null ? reason : "Unauthorized access";
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(bytes)));
    }

    private record DeviceHeaders(String uuid, String signature, String timestamp) {
    }
}
