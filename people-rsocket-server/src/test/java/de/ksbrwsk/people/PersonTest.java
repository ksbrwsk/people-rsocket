package de.ksbrwsk.people;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonTest {

    @Test
    void shouldCreatePerson() {
        Person person = new Person(1L, "Name");
        assertEquals(1L, person.getId());
        assertEquals("Name", person.getName());
    }
}