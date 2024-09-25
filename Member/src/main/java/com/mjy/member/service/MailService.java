package com.mjy.member.service;

import com.mjy.member.dto.EmailRequest;

public interface MailService {
    String sendEmail(EmailRequest emailRequest, String title, String content);
}
