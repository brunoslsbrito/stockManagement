package br.com.brittosw.stockmanagement.domain.customer.controller;

import br.com.brittosw.stockmanagement.domain.customer.dto.AddressRequest;
import br.com.brittosw.stockmanagement.domain.customer.dto.CustomerRequest;
import br.com.brittosw.stockmanagement.domain.customer.model.Customer;
import br.com.brittosw.stockmanagement.domain.customer.model.CustomerStatus;
import br.com.brittosw.stockmanagement.domain.customer.repository.CustomerRepository;
import br.com.brittosw.stockmanagement.domain.customer.service.CustomerService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequest customerRequest;
    private Customer customer;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        customerRequest = CustomerRequest.builder()
                .name("João Silva")
                .email("joao@email.com")
                .document("123.456.789-00")
                .phone("81999998888")
                .address(AddressRequest.builder()
                        .street("Rua A")
                        .number("123")
                        .neighborhood("Centro")
                        .city("São Paulo")
                        .state("SP")
                        .zipCode("01234-567")
                        .main(true)
                        .build())
                .build();

        customer = Customer.create(
                customerRequest.getName(),
                customerRequest.getEmail(),
                customerRequest.getDocument()
        );
    }

    @Nested
    @DisplayName("Criar Cliente")
    class CreateCustomer {

        @Test
        @DisplayName("Deve criar cliente com sucesso")
        void shouldCreateCustomerSuccessfully() {
            when(customerRepository.existsByDocument(anyString())).thenReturn(false);
            when(customerRepository.existsByEmail(anyString())).thenReturn(false);
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            Customer created = customerService.createCustomer(customerRequest);

            assertNotNull(created);
            assertEquals(customerRequest.getName(), created.getName());
            assertEquals(customerRequest.getEmail(), created.getEmail());
            assertEquals(customerRequest.getDocument(), created.getDocument());

            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando documento já existe")
        void shouldThrowExceptionWhenDocumentAlreadyExists() {
            when(customerRepository.existsByDocument(anyString())).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () ->
                    customerService.createCustomer(customerRequest));

            verify(customerRepository, never()).save(any(Customer.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando email já existe")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            when(customerRepository.existsByDocument(anyString())).thenReturn(false);
            when(customerRepository.existsByEmail(anyString())).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () ->
                    customerService.createCustomer(customerRequest));

            verify(customerRepository, never()).save(any(Customer.class));
        }
    }

    @Nested
    @DisplayName("Atualizar Cliente")
    class UpdateCustomer {

        @Test
        @DisplayName("Deve atualizar cliente com sucesso")
        void shouldUpdateCustomerSuccessfully() {
            when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(customer));
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            CustomerRequest updateRequest = CustomerRequest.builder()
                    .name("João Silva Atualizado")
                    .email("joao@email.com")
                    .status(CustomerStatus.ACTIVE)
                    .build();

            Customer updated = customerService.updateCustomer(customerId, updateRequest);

            assertNotNull(updated);
            assertEquals(updateRequest.getName(), updated.getName());
            assertEquals(updateRequest.getEmail(), updated.getEmail());

            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado")
        void shouldThrowExceptionWhenCustomerNotFound() {
            when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () ->
                    customerService.updateCustomer(customerId, customerRequest));

            verify(customerRepository, never()).save(any(Customer.class));
        }
    }

    @Nested
    @DisplayName("Adicionar Endereço")
    class AddAddress {

        @Test
        @DisplayName("Deve adicionar endereço com sucesso")
        void shouldAddAddressSuccessfully() {
            when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(customer));
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            AddressRequest addressRequest = AddressRequest.builder()
                    .street("Rua Nova")
                    .number("456")
                    .neighborhood("Jardins")
                    .city("São Paulo")
                    .state("SP")
                    .zipCode("04567-890")
                    .main(true)
                    .build();

            Customer updated = customerService.addAddress(customerId, addressRequest);

            assertNotNull(updated);
            assertFalse(updated.getAddresses().isEmpty());

            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao adicionar endereço em cliente inexistente")
        void shouldThrowExceptionWhenAddingAddressToNonExistentCustomer() {
            when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

            AddressRequest addressRequest = AddressRequest.builder()
                    .street("Rua Nova")
                    .number("456")
                    .build();

            assertThrows(EntityNotFoundException.class, () ->
                    customerService.addAddress(customerId, addressRequest));

            verify(customerRepository, never()).save(any(Customer.class));
        }
    }

    @Nested
    @DisplayName("Buscar Clientes")
    class FindCustomers {

        @Test
        @DisplayName("Deve buscar clientes ativos com paginação")
        void shouldFindActiveCustomersWithPagination() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Customer> customerPage = new PageImpl<>(List.of(customer));

            when(customerRepository.findActiveCustomers(pageable)).thenReturn(customerPage);

            Page<Customer> result = customerService.findActiveCustomers(pageable);

            assertNotNull(result);
            assertFalse(result.getContent().isEmpty());
            assertEquals(1, result.getContent().size());

            verify(customerRepository).findActiveCustomers(pageable);
        }

        @Test
        @DisplayName("Deve encontrar cliente por ID")
        void shouldFindCustomerById() {
            when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(customer));

            Customer found = customerService.findById(customerId);

            assertNotNull(found);
            assertEquals(customer.getName(), found.getName());
            assertEquals(customer.getEmail(), found.getEmail());

            verify(customerRepository).findById(customerId);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado por ID")
        void shouldThrowExceptionWhenCustomerNotFoundById() {
            when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () ->
                    customerService.findById(customerId));

            verify(customerRepository).findById(customerId);
        }
    }
}
