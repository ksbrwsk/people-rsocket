package de.ksbrwsk.people;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
class PersonRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    PersonRepository personRepository;

    @Test
    void shouldFindPersonByName() {
        Flux<Person> personFlux = this.personRepository
                .deleteAll()
                .then(this.personRepository.save(new Person(null, "ByName")))
                .then(this.personRepository.save(new Person(null, "ByName")))
                .thenMany(this.personRepository.findFirstByName("ByName"));
        StepVerifier
                .create(personFlux)
                .expectNextMatches(person -> person.getId() != null &&
                        person.getName().equalsIgnoreCase("byname"))
                .verifyComplete();
    }

    @Test
    void shouldLoadPersonById() {
        Mono<Person> personMono = this.personRepository
                .deleteAll()
                .then(this.personRepository.save(new Person(null, "Name")))
                .flatMap(person -> this.personRepository.findById(person.getId()));
        StepVerifier
                .create(personMono)
                .expectNextMatches(person -> person.getId() != null &&
                        person.getName().equalsIgnoreCase("name"))
                .verifyComplete();
    }

    @Test
    void shouldDeletePersonById() {
        Flux<Person> personFlux = this.personRepository
                .deleteAll()
                .then(this.personRepository.save(new Person(null, "Name")))
                .flatMap(person -> this.personRepository.findById(person.getId()))
                .flatMap(p -> this.personRepository.delete(p))
                .thenMany(this.personRepository.findAll());
        StepVerifier
                .create(personFlux)
                .verifyComplete();
    }

    @Test
    void shouldPersistPeople() {
        Flux<Person> personFlux = this.personRepository
                .deleteAll()
                .then(this.personRepository.save(new Person(null, "Name")))
                .then(this.personRepository.save(new Person(null, "Sabo")))
                .thenMany(this.personRepository.findAll());
        StepVerifier
                .create(personFlux)
                .expectNextMatches(person -> person.getId() != null &&
                        person.getName().equalsIgnoreCase("name"))
                .expectNextMatches(person -> person.getId() != null &&
                        person.getName().equalsIgnoreCase("sabo"))
                .verifyComplete();
    }

}