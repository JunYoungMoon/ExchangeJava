package com.member.exchange.service;

import com.member.exchange.dto.EmailRequest;

public interface MailService {
    String sendEmail(EmailRequest emailRequest, String title, String content);
}
