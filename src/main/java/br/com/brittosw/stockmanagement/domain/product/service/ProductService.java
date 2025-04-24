package br.com.brittosw.stockmanagement.domain.product.service;

import br.com.brittosw.stockmanagement.domain.customer.model.Customer;
import br.com.brittosw.stockmanagement.domain.events.StockUpdatedEvent;
import br.com.brittosw.stockmanagement.domain.product.dto.ProductRequest;
import br.com.brittosw.stockmanagement.domain.product.dto.StockMovementRequest;
import br.com.brittosw.stockmanagement.domain.product.model.Product;
import br.com.brittosw.stockmanagement.domain.product.repository.ProductRepository;
import br.com.brittosw.stockmanagement.infraestructure.email.EmailSendException;
import br.com.brittosw.stockmanagement.infraestructure.email.EmailService;
import br.com.brittosw.stockmanagement.infraestructure.notification.NotificationFacade;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;
    private final EmailService emailService;
    private final String notificationEmail = "brsalles87@gmail.com";
    private final NotificationFacade notificationFacade;


    @Timed(value = "product.create", description = "Time taken to create a product")
    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public Product createProduct(ProductRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            log.error("Product with SKU {} already exists", request.getSku());
            throw new IllegalArgumentException("Produto com SKU já existente");
        }

        var product = Product.create(
                request.getName(),
                request.getDescription(),
                request.getSku(),
                request.getPrice(),
                request.getInitialStock(),
                request.getMinimumStock()
        );

        meterRegistry.counter("product.created").increment();

        return productRepository.save(product);
    }

    @Timed(value = "product.stock.update")
    @CacheEvict(value = "products", key = "#productId")
    @Transactional
    public Product updateStock(UUID productId, StockMovementRequest request) {
        log.info("Updating stock for product {}: {} units", productId, request.getQuantity());

        var product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> {
                    log.error("Product {} not found", productId);
                    return new EntityNotFoundException("Produto não encontrado");
                });

        product.updateStock(request.getQuantity());

        eventPublisher.publishEvent(new StockUpdatedEvent(this, productId, request.getQuantity()));

        return productRepository.save(product);
    }

    @Cacheable(value = "products", key = "#productId")
    @Transactional(readOnly = true)
    public Product findById(UUID productId) {
        log.debug("Fetching product {}", productId);
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
    }

    @Timed(value = "product.search")
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String searchTerm, Pageable pageable) {
        log.info("Searching products with term: {}", searchTerm);
        return productRepository.searchProducts(searchTerm, pageable);
    }

    @Timed(value = "product.findAll")
    @Transactional(readOnly = true)
    public Page<Product> findAll(Pageable pageable) {
        log.info("Fetching all products with pagination");
        return productRepository.findAll(pageable);
    }


    @Timed(value = "product.stock.decrease")
    @CacheEvict(value = "products", key = "#productId")
    @Transactional
    public void decreaseStock(UUID productId, int quantity, Customer customer) {
        log.info("Decreasing stock for product {}: {} units", productId, quantity);

        var product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> {
                    log.error("Product {} not found", productId);
                    return new EntityNotFoundException("Produto não encontrado");
                });

        product.updateStock(-Math.abs(quantity));

        if (product.getStockQuantity() < LOW_STOCK_THRESHOLD) {
            sendLowStockNotification(product, customer);
        }

        eventPublisher.publishEvent(new StockUpdatedEvent(this, productId, -Math.abs(quantity)));
        meterRegistry.counter("product.stock.decreased").increment();

        productRepository.save(product);
    }


    @Transactional(readOnly = true)
    public boolean hasEnoughStock(Product product, int quantity, Customer customer) {
        log.info("Checking if product {} has enough stock for {} units", product.getId(), product.getStockQuantity());

        if (product.getStockQuantity() < LOW_STOCK_THRESHOLD && customer != null) {
            log.warn("Product {} is running low on stock. Current stock: {}",
                    product.getId(), product.getStockQuantity());
            sendLowStockNotification(product, customer);
        }

        return product.getStockQuantity() >= quantity;
    }

    private void sendLowStockNotification(Product product, Customer customer) {
        if (customer == null || (customer.getEmail() == null && customer.getPhones() == null)) {
            log.warn("Não foi possível enviar notificação: cliente ou contatos não informados");
            return;
        }

        String subject = "Alerta de Estoque Baixo - " + product.getName();
        String content = String.format("""
                    Produto com estoque baixo:
                    
                    Nome: %s
                    SKU: %s
                    Estoque Atual: %d
                    Estoque Mínimo: %d
                    
                    Por favor, providencie a reposição do estoque.
                    """,
                product.getName(),
                product.getSku(),
                product.getStockQuantity(),
                product.getMinimumStock()
        );

        try {
            emailService.sendEmail(customer.getEmail(), subject, content);
            notificationFacade.sendNotification(
                    customer.getPhones().stream().findFirst().orElse(null),
                    subject,
                    content
            );

            meterRegistry.counter("product.stock.notification.sent").increment();
        } catch (EmailSendException e) {
            log.error("Falha ao enviar notificação de estoque baixo para o produto {}: {}",
                    product.getId(), e.getMessage());
            meterRegistry.counter("product.stock.notification.failed").increment();
        }
    }
}

