package br.com.brittosw.stockmanagement.domain.order.repository;

import br.com.brittosw.stockmanagement.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);


}
