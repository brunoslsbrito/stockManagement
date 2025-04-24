package br.com.brittosw.stockmanagement.infraestructure.email;

public interface EmailService {
    void sendEmail(String to, String subject, String content);
}
