package br.com.brittosw.stockmanagement.model;

import br.com.brittosw.stockmanagement.domain.customer.model.Customer;
import br.com.brittosw.stockmanagement.domain.order.model.OrderStatus;
import br.com.brittosw.stockmanagement.domain.product.model.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> items;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;


    public static Order createOrder(Customer customer) {
        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .items(new HashSet<>())
                .build();
        order.setCustomer(customer);
        return order;
    }


    public void addItem(Product product, int quantity, BigDecimal unitPrice) {
        var orderItem = OrderItem.create(this, product, quantity, unitPrice);
        items.add(orderItem);
        recalculateTotal();
    }

    public void removeItem(OrderItem item, int quantity) {
        items.remove(item);
        recalculateTotal();
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::calculateSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Pedido não pode ser confirmado no status atual");
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        customer.addOrder(this);
    }

}
