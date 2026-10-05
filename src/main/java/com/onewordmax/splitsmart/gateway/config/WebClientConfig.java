package com.onewordmax.splitsmart.gateway.config;

import com.onewordmax.splitsmart.gateway.constants.SecurityConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Locale;

//    TODO: Pass on authentication headers to microservices
@Configuration
public class WebClientConfig {

    @Value("${USER_SERVICE_HOSTNAME}")
    private String userBackendUrl;

    @Value("${MAIL_SERVICE_HOSTNAME}")
    private String mailBackendUrl;

    @Bean
    public WebClient userServiceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(userBackendUrl)
                .filter(runtimeHeaderFilter())
                .build();
    }

    @Bean
    public WebClient mailServiceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(mailBackendUrl)
                .filter(runtimeHeaderFilter()) // Inject headers automatically
                .build();
    }

    /**
     * Filter that resolves Locale and TraceID from the Reactive Context
     * at runtime and adds them to outgoing headers.
     */
    private ExchangeFilterFunction runtimeHeaderFilter() {
        return (request, next) -> Mono.deferContextual(ctx -> {
            Locale locale = ctx.getOrDefault("locale", Locale.ENGLISH);
            String traceId = ctx.getOrDefault(SecurityConstants.TRACE_ID_CONTEXT_KEY, "unknown");
            ClientRequest filteredRequest = ClientRequest.from(request)
                    .header("Accept-Language", locale.toLanguageTag())
                    .header(SecurityConstants.TRACE_ID_HEADER_NAME, traceId)
                    .build();
            return next.exchange(filteredRequest);
        });
    }
}