package br.com.brittosw.stockmanagement.infraestructure.notification;

import br.com.brittosw.stockmanagement.infraestructure.notification.exception.NotificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
@Component
@ConfigurationProperties(prefix = "whatsapp.api")
@RequiredArgsConstructor
public class WhatsAppNotificationService implements NotificationService {
    private String token;
    private String url;
    private final RestTemplate restTemplate;

    @Override
    public void sendNotification(String to, String subject, String content) {
        if (to == null || to.trim().isEmpty()) {
            return;
        }

        try {
            // implementação do envio
            // ... resto do código
        } catch (Exception e) {
            throw new NotificationException("Falha ao enviar WhatsApp", e);
        }
    }

    // Getters e Setters para as propriedades
    public void setToken(String token) {
        this.token = token;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
