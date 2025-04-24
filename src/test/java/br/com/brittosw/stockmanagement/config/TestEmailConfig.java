package br.com.brittosw.stockmanagement.config;

import br.com.brittosw.stockmanagement.infraestructure.email.EmailService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@TestConfiguration
public class TestEmailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSenderImpl();
    }

    @Bean
    public EmailService emailService() {
        return Mockito.mock(EmailService.class);
    }

}
