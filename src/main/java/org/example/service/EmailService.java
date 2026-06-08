package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.EmailDeliveryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class EmailService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final RestTemplate restTemplate;

    @Value("${app.mail.dev-mode:false}")
    private boolean devMode;

    @Value("${app.mail.from:#{null}}")
    private String fromAddress;

    @Value("${app.mail.resend-api-key:#{null}}")
    private String resendApiKey;

    @PostConstruct
    public void init() {
        log.info("=== EmailService: mail dev mode enabled = {}, resend configured = {} ===",
                devMode, resendApiKey != null && !resendApiKey.isBlank());
    }

    public EmailService() {
        this.restTemplate = new RestTemplate();
    }

    public Optional<String> sendVerificationCode(String email, String code) {
        if (devMode) {
            log.info("DEV EMAIL CODE for {}: {}", email, code);
            return Optional.of(code);
        }

        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.error("Resend API key is not configured (RESEND_API_KEY env var)");
            throw new EmailDeliveryException("Email service is not configured", null);
        }

        String from = (fromAddress != null && !fromAddress.isBlank())
                ? fromAddress
                : "onboarding@resend.dev";

        Map<String, Object> requestBody = Map.of(
                "from", from,
                "to", email,
                "subject", "DragonDeal — код подтверждения",
                "text", "Ваш код подтверждения DragonDeal: " + code + "\n\n"
                        + "Код действует 10 минут.\n"
                        + "Если вы не запрашивали этот код, просто проигнорируйте письмо."
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    RESEND_API_URL,
                    HttpMethod.POST,
                    request,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Resend API returned HTTP {} for email {}", response.getStatusCodeValue(), email);
                throw new EmailDeliveryException("Не удалось отправить код подтверждения", null);
            }

            log.info("Verification code sent to {} via Resend (id={})",
                    email, response.getBody() != null ? response.getBody().get("id") : "unknown");
            return Optional.empty();

        } catch (RestClientException e) {
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            log.error("Failed to send verification code to {} via Resend — [{}] {}",
                    email, root.getClass().getSimpleName(), root.getMessage());
            throw new EmailDeliveryException("Не удалось отправить код подтверждения", e);
        }
    }
}
