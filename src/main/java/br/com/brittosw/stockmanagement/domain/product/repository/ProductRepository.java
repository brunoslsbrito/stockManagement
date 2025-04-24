package br.com.brittosw.stockmanagement.domain.product.repository;

import br.com.brittosw.stockmanagement.domain.product.model.Product;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minimumStock")
    Page<Product> findProductsNeedingRestock(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> searchProducts(String searchTerm, Pageable pageable);

    boolean existsBySku(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(UUID id);

    @Query("SELECT p FROM Product p WHERE p.restockDate <= :date AND " +
            "(p.lastNotificationSent IS NULL OR p.lastNotificationSent < :date)")
    List<Product> findByRestockDateLessThanEqualAndLastNotificationSentNot(
            LocalDate date, LocalDate notificationDate);

    @Query("SELECT p FROM Product p WHERE p.restockDate < :currentDate")
    Page<Product> findProductsWithOverdueRestockDate(
            LocalDate currentDate,
            Pageable pageable);
}
