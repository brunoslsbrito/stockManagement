package br.com.brittosw.stockmanagement.domain.product.service;

import br.com.brittosw.stockmanagement.config.TestEmailConfig;
import br.com.brittosw.stockmanagement.domain.customer.model.Customer;
import br.com.brittosw.stockmanagement.domain.product.model.Product;
import br.com.brittosw.stockmanagement.infraestructure.email.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Import(TestEmailConfig.class)
class ProductServiceTest {
    @Mock
    private EmailService emailService;
    @Mock
    private ProductService productService;

    // ... outros mocks ...

    @Test
    @DisplayName("Deve enviar email quando estoque estiver baixo")
    void shouldSendEmailWhenStockIsLow() {
        Product product = Product.builder()
                .name("Produto Teste")
                .sku("SKU123")
                .stockQuantity(5)
                .minimumStock(10)
                .build();

        productService.hasEnoughStock(product, 1, Customer.builder().email("brsalles87@gmail.com").build());

        verify(emailService).sendEmail(
            eq("gerente@brittosw.com.br"),
            contains("Alerta de Estoque Baixo"),
            contains("Produto com estoque baixo")
        );
    }

    @Test
    @DisplayName("Não deve enviar email quando estoque estiver normal")
    void shouldNotSendEmailWhenStockIsNormal() {
        Product product = Product.builder()
                .name("Produto Teste")
                .sku("SKU123")
                .stockQuantity(15)
                .minimumStock(10)
                .build();

        productService.hasEnoughStock(product, 1, null);

        verify(emailService, never()).sendEmail(any(), any(), any());
    }
}
