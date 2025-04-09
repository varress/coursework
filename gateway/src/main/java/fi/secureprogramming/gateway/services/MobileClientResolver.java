package fi.secureprogramming.gateway.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Primary
public class MobileClientResolver implements KeyResolver {

    @Autowired
    private DeviceService deviceService;

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String uuid = headers.getFirst("X-Device-UUID");
        String signature = headers.getFirst("X-Signature");
        String timestamp = headers.getFirst("X-Timestamp");

        if (uuid == null || signature == null || timestamp == null) {
            return unauthorized(exchange, "Missing required headers: X-Device-UUID, X-Signature, X-Timestamp")
                    .then(Mono.empty());
        }

        try {
            deviceService.verifyDevice(uuid, signature, timestamp);
            return Mono.just(uuid);
        } catch (Exception e) {
            return unauthorized(exchange, e.getMessage())
                    .then(Mono.empty());
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        String message = reason != null ? reason : "Unauthorized access";
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(message.getBytes())));
    }
}
