package com.mjy.exchange.service;

import com.mjy.exchange.dto.EmailRequest;

public interface MailService {
    String sendEmail(EmailRequest emailRequest, String title, String content);
}
