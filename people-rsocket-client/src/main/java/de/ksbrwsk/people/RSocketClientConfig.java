package de.ksbrwsk.people;

import io.rsocket.metadata.WellKnownMimeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
public class RSocketClientConfig {

    private static final MimeType MIME_TYPE =
            MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());

    @Bean
    public RSocketRequester rSocketRequester(RSocketRequester.Builder builder,
                                             @Value("${rsocket.server.host}") String host,
                                             @Value("${rsocket.server.port}") Integer port) {
        UsernamePasswordMetadata creds = new UsernamePasswordMetadata("test", "{noop}pwd");
        return builder
                .rsocketConnector(connector -> connector.reconnect(Retry.backoff(10, Duration.ofSeconds(1))))
                .dataMimeType(MediaType.APPLICATION_JSON)
                .setupMetadata(creds, MIME_TYPE)
                .connectTcp(host, port)
                .block();
    }

    @Bean
    RSocketStrategiesCustomizer rSocketStrategiesCustomizer() {
        return strategies -> strategies
                .encoder(new Jackson2JsonEncoder(), new SimpleAuthenticationEncoder())
                .decoder(new Jackson2JsonDecoder());
    }
}
