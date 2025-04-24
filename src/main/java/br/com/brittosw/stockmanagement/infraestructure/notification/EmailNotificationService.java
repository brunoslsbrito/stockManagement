package br.com.brittosw.stockmanagement.infraestructure.notification;

import br.com.brittosw.stockmanagement.infraestructure.notification.exception.NotificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificationService extends AbstractNotificationService {
    private final JavaMailSender mailSender;

    @Override
    public void sendNotification(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            logError("Erro ao enviar email: " + e.getMessage(), e);
            throw new NotificationException("Falha ao enviar email", e);
        }
    }
}
