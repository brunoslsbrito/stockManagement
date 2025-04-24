package br.com.brittosw.stockmanagement.infraestructure.notification;

import br.com.brittosw.stockmanagement.infraestructure.notification.exception.NotificationException;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationFacade {
    private final List<NotificationService> notificationServices;
    private final MeterRegistry meterRegistry;

    public void sendNotification(String to, String subject, String content) {
        if (to == null) {
            log.warn("Destino da notificação não informado");
            return;
        }

        List<NotificationService> services = notificationServices != null ?
                notificationServices : Collections.emptyList();

        if (services.isEmpty()) {
            log.warn("Nenhum serviço de notificação disponível");
            return;
        }

        var errors = new ArrayList<Exception>();

        services.forEach(service -> {
            try {
                service.sendNotification(to, subject, content);
                meterRegistry.counter("notification.sent",
                        "type", service.getClass().getSimpleName()).increment();
            } catch (Exception e) {
                log.error("Erro ao enviar notificação: {}", e.getMessage());
                errors.add(e);
                meterRegistry.counter("notification.failed",
                        "type", service.getClass().getSimpleName()).increment();
            }
        });

        if (!errors.isEmpty() && errors.size() == services.size()) {
            throw new NotificationException("Falha ao enviar todas as notificações", errors);
        }
    }
}
