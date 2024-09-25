package com.mjy.exchange.service;

import com.mjy.exchange.dto.EmailRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")  // 개발 환경일 때만 활성화
public class DevMailService implements MailService {

    @Override
    public String sendEmail(EmailRequest emailRequest, String title, String content) {
        return "메일 발송 생략 - 개발 환경: 인증 코드 = " + content;
    }
}