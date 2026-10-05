package com.onewordmax.splitsmart.gateway.security;

import com.onewordmax.splitsmart.gateway.i18n.LocaleResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocaleContextWebFilter implements WebFilter {

    private final LocaleResolver localeResolver;
    public static final String CONTEXT_LOCALE_KEY = "locale";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 1. Resolve the locale using the dedicated resolver logic
        Locale locale = localeResolver.resolve(exchange);

        log.trace("Locale resolved: {}", locale.toLanguageTag());

        // 2. Attach to Exchange Attributes
        // This is crucial for your GlobalExceptionHandler and any ResultHandlers
        exchange.getAttributes().put(CONTEXT_LOCALE_KEY, locale);

        // 3. Attach to Reactor Context
        // This allows non-web-aware services to use Mono.deferContextual(ctx -> ...)
        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(CONTEXT_LOCALE_KEY, locale));
    }
}
