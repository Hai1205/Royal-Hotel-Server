package com.Server.service.interfac;

import jakarta.mail.MessagingException;

import java.util.Map;

public interface IEmailService {
    void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) throws MessagingException;
}
