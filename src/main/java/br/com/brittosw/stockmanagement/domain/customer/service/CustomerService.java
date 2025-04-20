package br.com.brittosw.stockmanagement.domain.customer.service;

import br.com.brittosw.stockmanagement.domain.customer.model.Customer;
import br.com.brittosw.stockmanagement.domain.customer.repository.CustomerRepository;
import br.com.brittosw.stockmanagement.domain.customer.dto.CustomerRequest;
import br.com.brittosw.stockmanagement.domain.customer.dto.AddressRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    @Transactional
    public Customer createCustomer(CustomerRequest request) {
        validateNewCustomer(request);
        
        var customer = Customer.create(
            request.getName(),
            request.getEmail(),
            request.getDocument()
        );
        
        if (request.getAddress() != null) {
            customer.addAddress(request.getAddress().toAddress());
        }
        
        if (request.getPhone() != null) {
            customer.addPhone(request.getPhone());
        }
        
        return customerRepository.save(customer);
    }
    
    @Transactional
    public Customer updateCustomer(UUID customerId, CustomerRequest request) {
        var customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
            
        validateExistingCustomer(request, customer);
        
        customer.update(request.getName(), request.getEmail());
        
        return customerRepository.save(customer);
    }
    
    @Transactional
    public Customer addAddress(UUID customerId, AddressRequest request) {
        var customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
            
        customer.addAddress(request.toAddress());
        
        return customerRepository.save(customer);
    }
    
    @Transactional(readOnly = true)
    public List<Customer> findActiveCustomers() {
        return customerRepository.findActiveCustomers();
    }
    
    private void validateNewCustomer(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        
        if (customerRepository.existsByDocument(request.getDocument())) {
            throw new IllegalArgumentException("Documento já cadastrado");
        }
    }
    
    private void validateExistingCustomer(CustomerRequest request, Customer customer) {
        if (!request.getEmail().equals(customer.getEmail()) && 
            customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
    }
}
