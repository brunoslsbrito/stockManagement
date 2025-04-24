package br.com.brittosw.stockmanagement.infraestructure.notification;

public interface NotificationService {
    void sendNotification(String to, String subject, String content);
}
