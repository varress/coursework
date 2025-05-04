package fi.secureprogramming.gateway.filters;

import fi.secureprogramming.gateway.services.DeviceVerificationService;
import fi.secureprogramming.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Filter to verify mobile client requests.
 * This filter checks for the presence of specific headers
 * and verifies the device using the DeviceVerificationService.
 * If the verification fails, it returns a 401 Unauthorized response.
 */
@Component
public class MobileClientVerificationFilter implements GatewayFilter {

    private static final Logger logger = LoggerFactory.getLogger(MobileClientVerificationFilter.class);

    private final DeviceVerificationService deviceVerificationService;

    public MobileClientVerificationFilter(DeviceVerificationService deviceVerificationService) {
        this.deviceVerificationService = deviceVerificationService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();

        String uuid = headers.getFirst("X-Device-UUID");
        String signature = headers.getFirst("X-Signature");
        String timestamp = headers.getFirst("X-Timestamp");

        if (uuid == null || signature == null || timestamp == null) {
            return unauthorized(exchange, "Missing required headers: X-Device-UUID, X-Signature, X-Timestamp");
        }

        try {
            Device device = deviceVerificationService.verifyDevice(uuid, signature, timestamp);
            if (device == null) {
                return unauthorized(exchange, "Device not verified");
            }
        } catch (Exception e) {
            return unauthorized(exchange, e.getMessage());
        }

        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
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
}
