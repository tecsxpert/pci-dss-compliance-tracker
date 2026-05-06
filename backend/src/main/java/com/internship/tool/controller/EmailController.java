package com.internship.tool.controller;

import com.internship.tool.service.EmailService;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(
            EmailService emailService
    ) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(
            @RequestParam String to
    ) {

        emailService.sendEmail(
                to,
                "Spring Boot Test Email",
                "Email sent successfully from Spring Boot!"
        );

        return ResponseEntity.ok(
                "Email sent successfully"
        );
    }
}