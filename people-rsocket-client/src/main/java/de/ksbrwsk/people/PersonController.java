package de.ksbrwsk.people;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static java.time.Duration.ofSeconds;
import static reactor.util.retry.Retry.backoff;

@RestController
@Log4j2
@RequiredArgsConstructor
public class PersonController {

    private final static String BASE = "/api/people";

    private final RSocketRequester rSocketRequester;

    @GetMapping(BASE)
    Flux<Person> handleFindAll() {
        log.info("handle request GET - {}", BASE);
        return rSocketRequester
                .route("people-read")
                .retrieveFlux(Person.class)
                .retryWhen(backoff(5, ofSeconds(1)).maxBackoff(ofSeconds(30)))
                .doOnError(IOException.class, e -> log.error(e.getMessage()));

    }

    @GetMapping(BASE + "/{id}")
    Mono<Person> handldeFindOneById(@PathVariable("id") Long id) {
        log.info("handle request GET - {}/id", BASE);
        return rSocketRequester
                .route("people-read-{id}", id)
                .retrieveMono(Person.class)
                .doOnError(IOException.class, e -> log.error(e.getMessage()));
    }

    @GetMapping(BASE + "/byName/{name}")
    Mono<Person> handldeFindFirstByName(@PathVariable("name") String name) {
        log.info("handle request GET - {}/byName/name", BASE);
        return rSocketRequester
                .route("people-read-byName-{name}", name)
                .retrieveMono(Person.class)
                .doOnError(IOException.class, e -> log.error(e.getMessage()));
    }

    @DeleteMapping(BASE + "/{id}")
    Mono<String> handleDeleteById(@PathVariable("id") Long id) {
        log.info("handle request DELETE - {}/id", BASE);
        return rSocketRequester
                .route("people-delete-{id}", id)
                .retrieveMono(String.class)
                .doOnError(IOException.class, e -> log.error(e.getMessage()));
    }

    @PostMapping(BASE)
    Mono<Person> handleSave(@RequestBody Person person) {
        log.info("handle request POST - {}", BASE);
        return rSocketRequester
                .route("people-write")
                .data(person)
                .retrieveMono(Person.class)
                .doOnError(IOException.class, e -> log.error(e.getMessage()));
    }
}
