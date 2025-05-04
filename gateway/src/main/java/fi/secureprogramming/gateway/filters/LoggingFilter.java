package fi.secureprogramming.gateway.filters;

import fi.secureprogramming.gateway.services.IPAddressResolver;
import fi.secureprogramming.gateway.services.MobileClientResolver;
import fi.secureprogramming.model.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@Component
@Order(1)
public class LoggingFilter implements GlobalFilter {

    @Autowired
    IPAddressResolver ipAddressResolver;

    @Autowired
    MobileClientResolver mobileClientResolver;

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class.getName());

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String ipAddress = ipAddressResolver.resolve(exchange).blockOptional().orElse("unknown");
        String uuid = mobileClientResolver.resolve(exchange).blockOptional().orElse("unknown");

        String method = request.getMethod().name();
        String uri = request.getURI().toString();
        String userAgent = request.getHeaders().getFirst("User-Agent");
        String timestamp = Instant.now().toString();

        logger.info(String.format("[REQUEST] %s %s | IP: %s | UUID: %s | UA: %s | Time: %s",
                method, uri, ipAddress, uuid, userAgent, timestamp));

        return chain.filter(exchange).doOnSuccess(avoid -> {
            int status = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value()
                    : 0;
            logger.info(String.format("[RESPONSE] %s %s => %d", method, uri, status));
        });
    }
}
