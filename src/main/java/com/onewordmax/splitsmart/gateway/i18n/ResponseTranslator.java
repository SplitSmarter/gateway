package com.onewordmax.splitsmart.gateway.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ResponseTranslator {

    private final MessageSource messageSource;

    public <T> Mono<T> translate(Mono<T> responseMono) {
        return responseMono.flatMap(response -> {
            return Mono.deferContextual(ctx -> {
                Locale locale = ctx.getOrDefault("locale", Locale.ENGLISH);

                if (response instanceof HasMessage hasMessage) {
                    String key = hasMessage.getMessage();
                    if (key != null) {
                        String translated = messageSource.getMessage(key, null, key, locale);
                        hasMessage.setMessage(translated);
                    }
                }
                return Mono.just(response);
            });
        });
    }
}
