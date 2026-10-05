package com.onewordmax.splitsmart.gateway.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Locale;

@Configuration
public class InternationalizationConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/translations/messages"); // base name without locale
        messageSource.setDefaultEncoding("UTF-8");       // important for non-ASCII chars
        messageSource.setUseCodeAsDefaultMessage(true);  // optional: fallback
        messageSource.setDefaultLocale(Locale.ENGLISH);
        return messageSource;
    }



//    @Bean
//    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
//        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
//        bean.setValidationMessageSource(messageSource);
//        return bean;
//    }
}
