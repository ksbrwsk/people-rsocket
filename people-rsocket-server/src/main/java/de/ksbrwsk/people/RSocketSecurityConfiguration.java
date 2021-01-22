package de.ksbrwsk.people;

import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;

@Configuration
@EnableRSocketSecurity
@EnableReactiveMethodSecurity
public class RSocketSecurityConfiguration {

    @Bean
    RSocketStrategiesCustomizer rSocketStrategiesCustomizer() {
        return strategies -> strategies
                .encoder(new SimpleAuthenticationEncoder(), new Jackson2JsonEncoder())
                .decoder(new Jackson2JsonDecoder());
    }

    @Bean
    RSocketMessageHandler rSocketMessageHandler(RSocketStrategies strategies) {
        var messageHandler = new RSocketMessageHandler();
        messageHandler
                .getArgumentResolverConfigurer()
                .addCustomResolver(new org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver());
        messageHandler.setRSocketStrategies(strategies);
        return messageHandler;
    }

    @Bean
    PayloadSocketAcceptorInterceptor authorization(RSocketSecurity rSocketSecurity) {
        return rSocketSecurity
                .authorizePayload(authorize ->
                        authorize
                                .route("people-*").authenticated()
                                .anyExchange().permitAll()
                )
                .simpleAuthentication(Customizer.withDefaults())
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    MapReactiveUserDetailsService userDetailsService() {
        var user = User
                .withUsername("user")
                .password(passwordEncoder().encode("{noop}pwd"))
                .roles("USER", "ADMIN")
                .build();
        var test = User
                .withUsername("test")
                .password(passwordEncoder().encode("{noop}pwd"))
                .roles("USER")
                .build();
        return new MapReactiveUserDetailsService(user, test);
    }
}
