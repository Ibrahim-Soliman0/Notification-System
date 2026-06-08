package org.registrationservice.controller;

import lombok.AllArgsConstructor;
import org.registrationservice.model.UserRegistrationRequest;
import org.registrationservice.service.RegistrationProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class RegistrationFormController {

    public RegistrationProducer registrationProducer;

    @PostMapping("/register")
    public ResponseEntity<String> submitForm(
            @RequestBody UserRegistrationRequest userRegistrationRequest) {

        registrationProducer.publish(userRegistrationRequest);

        return ResponseEntity.ok(
                "User registered successfully"
        );
    }
}
