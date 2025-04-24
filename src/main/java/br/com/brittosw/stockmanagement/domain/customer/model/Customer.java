package br.com.brittosw.stockmanagement.domain.customer.model;

import br.com.brittosw.stockmanagement.domain.shared.model.Address;
import br.com.brittosw.stockmanagement.model.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String document;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status;

    @ElementCollection
    @CollectionTable(
        name = "customer_phones",
        joinColumns = @JoinColumn(name = "customer_id")
    )
    private Set<String> phones = new HashSet<>();

    @ElementCollection
    @CollectionTable(
        name = "customer_addresses",
        joinColumns = @JoinColumn(name = "customer_id")
    )
    private Set<Address> addresses = new HashSet<>();

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Order> orders = new HashSet<>();

    @Version
    private Long version;

    public static Customer create(String name, String email, String document) {
        return Customer.builder()
                .name(name)
                .email(email)
                .document(document)
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .phones(new HashSet<>())
                .addresses(new HashSet<>())
                .build();
    }

    public void addAddress(Address address) {
        this.addresses.add(address);
        this.updatedAt = LocalDateTime.now();
    }

    public void addPhone(String phone) {
        this.phones.add(phone);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(CustomerStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String name, String email, CustomerStatus status) {
        this.name = name;
        this.email = email;
        this.updatedAt = LocalDateTime.now();
        this.status = status;
    }

    public void addOrder(Order order) {
        this.orders.add(order);
        this.updatedAt = LocalDateTime.now();
    }

    public void removeOrder(Order order) {
        this.orders.remove(order);
        this.updatedAt = LocalDateTime.now();
    }

}
