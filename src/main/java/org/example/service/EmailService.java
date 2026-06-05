package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.EmailDeliveryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.dev-mode:false}")
    private boolean devMode;

    @Value("${app.mail.from:#{null}}")
    private String fromAddress;

    @PostConstruct
    public void init() {
        log.info("=== EmailService: mail dev mode enabled = {} ===", devMode);
    }

    public void sendVerificationCode(String email, String code) {
        if (devMode) {
            log.info("DEV EMAIL CODE for {}: {}", email, code);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("DragonDeal — код подтверждения");
        message.setText(
                "Ваш код подтверждения DragonDeal: " + code + "\n\n" +
                        "Код действует 10 минут.\n" +
                        "Если вы не запрашивали этот код, просто проигнорируйте письмо."
        );

        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            log.error("Failed to send verification code to {}: {}", email, exception.getMessage());
            throw new EmailDeliveryException("Не удалось отправить код подтверждения", exception);
        }
    }
}
