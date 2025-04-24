package br.com.brittosw.stockmanagement.domain.product.model;

import br.com.brittosw.stockmanagement.domain.product.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private Integer minimumStock;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Column(name = "restock_date")
    private LocalDate restockDate;

    @Column(name = "last_notification_sent")
    private LocalDate lastNotificationSent;


    public static Product create(String name, String description, String sku, 
                               BigDecimal price, Integer stockQuantity, Integer minimumStock, LocalDate restockDate) {
        return Product.builder()
                .name(name)
                .description(description)
                .sku(sku)
                .price(price)
                .stockQuantity(stockQuantity)
                .minimumStock(minimumStock)
                .status(ProductStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .restockDate(restockDate)
                .build();
    }

    public void updateStock(int quantity) {
        if (quantity < 0 && Math.abs(quantity) > this.stockQuantity) {
            throw new InsufficientStockException("Estoque insuficiente");
        }
        this.stockQuantity += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasEnoughStock(int quantity) {
        return this.stockQuantity >= quantity;
    }

    public boolean needsRestock() {
        return this.stockQuantity <= this.minimumStock;
    }

    public void updatePrice(BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que zero");
        }
        this.price = newPrice;
        this.updatedAt = LocalDateTime.now();
    }

    public void setRestockDate(LocalDate restockDate) {
        this.restockDate = restockDate;
        this.updatedAt = LocalDateTime.now();
    }

    public void setLastNotificationSent(LocalDate lastNotificationSent) {
        this.lastNotificationSent = lastNotificationSent;
        this.updatedAt = LocalDateTime.now();
    }


    public void updateRestockDate(LocalDate newRestockDate) {
        if (newRestockDate != null && newRestockDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data de reabastecimento não pode ser no passado");
        }
        setRestockDate(newRestockDate);
        setLastNotificationSent(null);
    }


}
