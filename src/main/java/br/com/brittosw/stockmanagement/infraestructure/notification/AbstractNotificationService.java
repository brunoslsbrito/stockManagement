package br.com.brittosw.stockmanagement.infraestructure.notification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractNotificationService implements NotificationService {
    protected void logError(String message, Exception e) {
        log.error(message, e);
    }
}
