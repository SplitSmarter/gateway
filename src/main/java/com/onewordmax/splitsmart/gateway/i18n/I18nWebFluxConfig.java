package com.onewordmax.splitsmart.gateway.i18n;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.accept.RequestedContentTypeResolverBuilder;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityResultHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ReactiveAdapterRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.codec.ServerCodecConfigurer;

import java.util.List;
import java.util.Locale;

@Slf4j
@Configuration
public class I18nWebFluxConfig {

    private final MessageSource messageSource;

    public I18nWebFluxConfig(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Bean
    @Order(-2)
    public ResponseEntityResultHandler i18nResponseEntityResultHandler(
            ServerCodecConfigurer configurer,
            ReactiveAdapterRegistry reactiveAdapterRegistry
    ) {
        List<HttpMessageWriter<?>> writers = configurer.getWriters();
        RequestedContentTypeResolver resolver = new RequestedContentTypeResolverBuilder().build();

        return new ResponseEntityResultHandler(writers, resolver, reactiveAdapterRegistry) {
            @Override
            public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
                Object returnValue = result.getReturnValue();
                if (returnValue == null) {
                    return Mono.empty();
                }

                Locale locale = exchange.getAttributeOrDefault("locale", Locale.ENGLISH);
                Object translated = translateContainer(returnValue, locale);

                HandlerResult translatedResult = new HandlerResult(
                        result.getHandler(),
                        translated,
                        result.getReturnTypeSource(),
                        result.getBindingContext()
                );

                return super.handleResult(exchange, translatedResult);
            }

            private Object translateContainer(Object value, Locale locale) {
                if (value instanceof Mono<?> mono) {
                    return mono.map(v -> translateValue(v, locale));
                }
                if (value instanceof Flux<?> flux) {
                    return flux.map(v -> translateValue(v, locale));
                }
                return translateValue(value, locale);
            }

            private Object translateValue(Object value, Locale locale) {
                switch (value) {
                    case null -> {
                        return null;
                    }
                    case ResponseEntity<?> responseEntity -> {
                        Object body = responseEntity.getBody();
                        if (body != null) {
                            translateValue(body, locale); // Recursive field be careful !!
                        }
                        return responseEntity;
                    }
                    case HasMessage hasMessage -> {
                        String key = hasMessage.getMessage();
                        if (key != null) {
                            hasMessage.setMessage(messageSource.getMessage(key, null, key, locale));
                        }
                    }
                    default -> {
                        log.error("Unexpected object while translating value");
                        return "Unknown";
                    }
                }

                return value;
            }
        };
    }

}
