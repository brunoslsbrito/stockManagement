package br.com.brittosw.stockmanagement.domain.product.service;

import br.com.brittosw.stockmanagement.domain.events.StockUpdatedEvent;
import br.com.brittosw.stockmanagement.domain.product.dto.ProductRequest;
import br.com.brittosw.stockmanagement.domain.product.dto.StockMovementRequest;
import br.com.brittosw.stockmanagement.domain.product.model.Product;
import br.com.brittosw.stockmanagement.domain.product.repository.ProductRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

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
    public Product decreaseStock(UUID productId, int quantity) {
        log.info("Decreasing stock for product {}: {} units", productId, quantity);

        var product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> {
                    log.error("Product {} not found", productId);
                    return new EntityNotFoundException("Produto não encontrado");
                });

        product.updateStock(-Math.abs(quantity));

        eventPublisher.publishEvent(new StockUpdatedEvent(this, productId, -Math.abs(quantity)));
        meterRegistry.counter("product.stock.decreased").increment();

        return productRepository.save(product);
    }

}
