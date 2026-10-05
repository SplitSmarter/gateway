package com.onewordmax.splitsmart.gateway.config;

import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
public class ObservationConfig {

    @PostConstruct
    public void setupHooks() {
        // 1. Register MDC as a ThreadLocal accessor
        ContextRegistry.getInstance().registerThreadLocalAccessor(
                "mdc-context", // Key name
                MDC::getCopyOfContextMap, // How to read from ThreadLocal
                MDC::setContextMap,        // How to write to ThreadLocal
                MDC::clear                 // How to cleanup
        );

        // 2. Enable automatic propagation across Reactor thread swaps
        Hooks.enableAutomaticContextPropagation();
    }
}