package de.ksbrwsk.people;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Log4j2
public class PersonRSocketController {

    private final PersonRepository personRepository;

    @PreAuthorize("hasRole('USER')")
    @MessageMapping("people-read")
    Flux<Person> handleFindAll(@AuthenticationPrincipal UserDetails user) {
        log.info("Handle message people-all for user {}", user.getUsername());
        return this.personRepository
                .findAll()
                .doOnError(throwable -> log.error("Error handling message people-all", throwable));
    }

    @PreAuthorize("hasRole('USER')")
    @MessageMapping("people-read-{id}")
    Mono<Person> handleFindOneById(@DestinationVariable("id") Long id, @AuthenticationPrincipal UserDetails user) {
        log.info("Handle message people-read-id -> {} for user {}", id, user.getUsername());
        return this.personRepository
                .findById(id)
                .doOnSuccess(person -> log.info("Person found: {}", person))
                .doOnError(throwable -> log.error("Error handling message people-get-id", throwable));
    }

    @PreAuthorize("hasRole('USER')")
    @MessageMapping("people-read-byName-{name}")
    Mono<Person> handleFindFirstByName(@DestinationVariable("name") String name, @AuthenticationPrincipal UserDetails user) {
        log.info("Handle message people-read-byName -> {} for user {}", name, user.getUsername());
        return this.personRepository
                .findFirstByName(name)
                .doOnSuccess(person -> log.info("Person found: {}", person))
                .doOnError(throwable -> log.error("Error handling message people-get-byName", throwable));
    }

    @PreAuthorize("hasRole('USER')")
    @MessageMapping("people-delete-{id}")
    Mono<String> handleDeleteById(@DestinationVariable Long id, @AuthenticationPrincipal UserDetails user) {
        log.info("Handle message people-delete-id -> {} for user {}", id, user.getUsername());
        return this.personRepository.findById(id)
                .flatMap(this.personRepository::delete)
                .thenReturn("Successfully deleted!")
                .doOnSuccess(person -> log.info("Person deleted: {}", person))
                .doOnError(throwable -> log.error("Error handling message people-delete-id", throwable));
    }

    @PreAuthorize("hasRole('USER')")
    @MessageMapping("people-write")
    Mono<Person> handleSave(@Payload Person person, @AuthenticationPrincipal UserDetails user) {
        log.info("Handle message people-write -> {} for user {}", person, user.getUsername());
        return this.personRepository
                .save(person)
                .doOnSuccess(p -> log.info("Person created: {}", p))
                .doOnError(throwable -> log.error("Error handling message people-write", throwable));
    }
}
