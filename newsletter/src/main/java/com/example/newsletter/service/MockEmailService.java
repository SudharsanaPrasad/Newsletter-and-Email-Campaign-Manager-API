package com.example.newsletter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// stand-in for a real email provider. the project brief says to simulate
// sending with logs instead of wiring up SMTP, so this just writes a log line.
@Service
@Slf4j
public class MockEmailService {

    public void send(String toEmail, String toName, String subject, String content) {
        log.info("Sending email to {} <{}> | subject: {}", toName, toEmail, subject);
    }
}
