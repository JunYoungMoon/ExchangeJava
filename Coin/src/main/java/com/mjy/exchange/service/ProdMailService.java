package com.mjy.exchange.service;

import com.mjy.exchange.dto.EmailRequest;
import com.mjy.exchange.util.CheckSumUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@Profile("prod")  // 운영 환경일 때만 활성화
public class ProdMailService implements MailService {

    private final JavaMailSender emailSender;
    private final MessageSourceAccessor messageSourceAccessor;

    public String sendEmail(EmailRequest emailRequest,
                            String title,
                            String text) {
        SimpleMailMessage emailForm = createEmailForm(emailRequest.getEmail(), title, text);
        try {
            // 클라이언트가 전송한 데이터를 사용하여 체크섬 재생성
            String calculatedCheckSum = CheckSumUtil.generateCheckSum(emailRequest.getEmail());

            // 클라이언트가 전송한 체크섬과 재생성한 체크섬 비교
            if (calculatedCheckSum.equals(emailRequest.getCheckSum())) {
                emailSender.send(emailForm);

                return messageSourceAccessor.getMessage("member.sendMail.success.message");
            } else {
                // 체크섬이 일치하지 않으면 오류 응답 반환
                throw new RuntimeException(messageSourceAccessor.getMessage("email.checkSum.fail.message"));
            }

        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private SimpleMailMessage createEmailForm(String toEmail,
                                              String title,
                                              String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(title);
        message.setText(text);

        return message;
    }
}
