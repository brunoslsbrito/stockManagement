package br.com.brittosw.stockmanagement.domain.customer.service;

import br.com.brittosw.stockmanagement.domain.customer.dto.AddressRequest;
import br.com.brittosw.stockmanagement.domain.customer.dto.CustomerRequest;
import br.com.brittosw.stockmanagement.domain.customer.model.Customer;
import br.com.brittosw.stockmanagement.domain.customer.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
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

        Optional.ofNullable(request.getAddress())
                .map(AddressRequest::toAddress)
                .ifPresent(customer::addAddress);

        Optional.ofNullable(request.getPhone())
                .ifPresent(customer::addPhone);


        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateCustomer(UUID customerId, CustomerRequest request) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        validateExistingCustomer(request, customer);

        customer.update(request.getName(), request.getEmail(), request.getStatus());

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
    public Page<Customer> findActiveCustomers(Pageable pageable) {
        return customerRepository.findActiveCustomers(pageable);
    }

    @Cacheable(value = "customers", key = "#customerId")
    @Transactional(readOnly = true)
    public Customer findById(UUID customerId) {
        log.debug("Fetching customer {}", customerId);
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
    }

    private void validateNewCustomer(CustomerRequest request) {
        if (customerRepository.existsByDocument(request.getDocument())) {
            throw new IllegalArgumentException("Documento já cadastrado");
        }

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
    }

    private void validateExistingCustomer(CustomerRequest request, Customer customer) {
        if (!request.getEmail().equals(customer.getEmail()) &&
                customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
    }
}
