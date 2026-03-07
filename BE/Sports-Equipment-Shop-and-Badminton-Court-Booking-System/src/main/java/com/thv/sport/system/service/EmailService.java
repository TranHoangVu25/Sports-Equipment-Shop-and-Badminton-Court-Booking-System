package com.thv.sport.system.service;

public interface EmailService {
    void sendMail(String to, String subject, String body);
}
