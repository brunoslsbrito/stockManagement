package br.com.brittosw.stockmanagement.domain.customer.repository;

import br.com.brittosw.stockmanagement.domain.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    
    Optional<Customer> findByEmail(String email);
    
    Optional<Customer> findByDocument(String document);
    
    @Query("SELECT c FROM Customer c WHERE c.status = 'ACTIVE'")
    List<Customer> findActiveCustomers();
    
    boolean existsByEmail(String email);
    
    boolean existsByDocument(String document);
}
