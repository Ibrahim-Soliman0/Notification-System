package org.registrationservice.model;

public record UserRegistrationRequest(
        String name,
        String email,
        String phone,
        Integer age
) {

}