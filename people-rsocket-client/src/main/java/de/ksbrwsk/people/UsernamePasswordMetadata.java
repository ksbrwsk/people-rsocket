package de.ksbrwsk.people;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class UsernamePasswordMetadata {
    private final String username;
    private final String password;
}
