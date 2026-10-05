package com.onewordmax.splitsmart.gateway.security;

import com.onewordmax.splitsmart.gateway.constants.SecurityConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // ensure this runs first
public class TraceableRequestLoggingFilter implements WebFilter {

    public static final String TRACE_ID_CONTEXT_KEY = "traceId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();

        String incomingTraceId = request.getHeaders().getFirst(SecurityConstants.TRACE_ID_HEADER_NAME);
        final String traceId = (incomingTraceId == null || incomingTraceId.isBlank())
                ? UUID.randomUUID().toString()
                : incomingTraceId;

        exchange.getResponse().getHeaders().set(SecurityConstants.TRACE_ID_HEADER_NAME, traceId);

        String clientIp = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        String userAgent = request.getHeaders().getFirst("user-agent");

        log.info("INCOMING REQUEST: {} {} query={} client={} user-agent={} traceId={}",
                request.getMethod(),
                request.getURI().getPath(),
                request.getQueryParams().toSingleValueMap(),
                clientIp,
                userAgent,
                traceId
        );

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - startTime;
                    var response = exchange.getResponse();

                    log.info("<<< RESPONSE: {} {} status={} duration={}ms traceId={}",
                            request.getMethod(),
                            request.getURI().getPath(),
                            response.getStatusCode(),
                            duration,
                            traceId
                    );
                })
                // Put the traceId in context for general reactive use
                .contextWrite(ctx -> ctx.put(SecurityConstants.TRACE_ID_CONTEXT_KEY, traceId))
                // Put the entire MDC Map in context so the ContextRegistry can propagate it
                .contextWrite(Context.of("mdc-context", java.util.Map.of(SecurityConstants.TRACE_ID_CONTEXT_KEY, traceId)));
    }

}
