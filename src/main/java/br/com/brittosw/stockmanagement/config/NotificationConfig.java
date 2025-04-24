package br.com.brittosw.stockmanagement.config;

import br.com.brittosw.stockmanagement.infraestructure.notification.NotificationService;
import br.com.brittosw.stockmanagement.infraestructure.notification.WhatsAppNotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class NotificationConfig {

    @Bean("notificationRestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public WhatsAppNotificationService whatsAppNotificationService(
            @Value("${whatsapp.api.token:default-token}") String apiToken,
            @Value("${whatsapp.api.url:https://api.whatsapp.com/v1}") String apiUrl,
            @Qualifier("notificationRestTemplate") RestTemplate restTemplate) {
        return new WhatsAppNotificationService(restTemplate);
    }

    @Bean
    @Primary
    public List<NotificationService> notificationServices(WhatsAppNotificationService whatsAppService) {
        return List.of(whatsAppService);
    }
}

