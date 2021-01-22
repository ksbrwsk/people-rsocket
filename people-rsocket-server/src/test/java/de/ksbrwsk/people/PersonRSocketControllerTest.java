package de.ksbrwsk.people;

import io.rsocket.metadata.WellKnownMimeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PersonRSocketControllerTest {

    private static final MimeType MIME_TYPE = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());
    private final static UsernamePasswordMetadata CREDS = new UsernamePasswordMetadata("test", "{noop}pwd");

    private RSocketRequester rSocketRequester;

    @Autowired
    private RSocketRequester.Builder requester;

    @MockBean
    PersonRepository personRepository;

    @BeforeEach
    public void setUp() {
        rSocketRequester = requester
                .rsocketStrategies((builder) -> builder.encoder(new SimpleAuthenticationEncoder()))
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .tcp("localhost", 8888);
    }

    @Test
    void shouldHandleFindAll() {
        var p1 = new Person(1L, "Name");
        var p2 = new Person(2L, "Sabo");
        Mockito
                .when(this.personRepository.findAll())
                .thenReturn(Flux.just(p1, p2));

        Flux<Person> people = rSocketRequester
                .route("people-read")
                .metadata(CREDS, MIME_TYPE)
                .retrieveFlux(Person.class);

        StepVerifier.create(people)
                .expectNext(p1, p2)
                .verifyComplete();
    }

    @Test
    void shouldHandleFindById() {
        var p1 = new Person(1L, "Name");
        Mockito
                .when(this.personRepository.findById(1L))
                .thenReturn(Mono.just(p1));

        Mono<Person> person = rSocketRequester
                .route("people-read-{id}", 1L)
                .metadata(CREDS, MIME_TYPE)
                .retrieveMono(Person.class);

        StepVerifier.create(person)
                .expectNext(p1)
                .verifyComplete();
    }

    @Test
    void shouldHandleDeleteById() {
        var p1 = new Person(1L, "Name");
        Mockito
                .when(this.personRepository.findById(1L))
                .thenReturn(Mono.just(p1));
        Mockito
                .when(this.personRepository.delete(any(Person.class)))
                .thenReturn(Mono.empty());

        Mono<String> person = rSocketRequester
                .route("people-delete-{id}", 1L)
                .metadata(CREDS, MIME_TYPE)
                .retrieveMono(String.class);

        StepVerifier.create(person)
                .expectNext("Successfully deleted!")
                .verifyComplete();
    }

    @Test
    void shouldHandleFindFirstByName() {
        var p1 = new Person(1L, "Person1");
        Mockito
                .when(this.personRepository.findFirstByName("Person1"))
                .thenReturn(Mono.just(p1));

        Mono<Person> person = rSocketRequester
                .route("people-read-byName-{name}", "Person1")
                .metadata(CREDS, MIME_TYPE)
                .retrieveMono(Person.class);

        StepVerifier.create(person)
                .expectNext(p1)
                .verifyComplete();
    }
}
