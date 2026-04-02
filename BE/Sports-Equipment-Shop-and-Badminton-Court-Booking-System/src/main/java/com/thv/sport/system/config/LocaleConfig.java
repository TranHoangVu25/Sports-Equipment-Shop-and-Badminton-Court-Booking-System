package com.thv.sport.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Configuration
public class LocaleConfig {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();

        resolver.setDefaultLocale(new Locale("vi"));

        List<Locale> supportedLocales = Arrays.asList(
                new Locale("vi"),
                Locale.ENGLISH
        );
        resolver.setSupportedLocales(supportedLocales);

        return resolver;
    }
}
