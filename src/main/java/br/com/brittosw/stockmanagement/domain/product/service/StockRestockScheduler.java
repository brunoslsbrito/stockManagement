package br.com.brittosw.stockmanagement.domain.product.service;

import br.com.brittosw.stockmanagement.domain.product.model.Product;
import br.com.brittosw.stockmanagement.domain.product.repository.ProductRepository;
import br.com.brittosw.stockmanagement.infraestructure.email.EmailService;
import br.com.brittosw.stockmanagement.infraestructure.notification.NotificationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockRestockScheduler {
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final NotificationFacade notificationFacade;

    @Scheduled(cron = "0 0 8 * * *") // Executa todo dia às 8h
    public void checkRestockNeeded() {
        log.info("Verificando produtos que precisam ser reabastecidos...");
        
        var today = LocalDate.now();
        var productsToRestock = productRepository.findByRestockDateLessThanEqualAndLastNotificationSentNot(today, today);
        
        productsToRestock.forEach(this::sendRestockNotification);
    }

    private void sendRestockNotification(Product product) {
        String subject = "Reabastecimento Necessário - " + product.getName();
        String content = String.format("""
            Produto precisa ser reabastecido:
            
            Nome: %s
            SKU: %s
            Estoque Atual: %d
            Estoque Mínimo: %d
            Data Prevista: %s
            
            Por favor, providencie o reabastecimento do estoque.
            """,
            product.getName(),
            product.getSku(),
            product.getStockQuantity(),
            product.getMinimumStock(),
            product.getRestockDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        try {
            emailService.sendEmail("gerente@empresa.com", subject, content);
            notificationFacade.sendNotification(
                "5511999999999", // Número do responsável pelo estoque
                subject,
                content
            );

            // Atualiza a data da última notificação
            product.setLastNotificationSent(LocalDate.now());
            productRepository.save(product);
            
            log.info("Notificação de reabastecimento enviada para o produto: {}", product.getSku());
        } catch (Exception e) {
            log.error("Erro ao enviar notificação de reabastecimento para o produto {}: {}", 
                product.getSku(), e.getMessage());
        }
    }
}
