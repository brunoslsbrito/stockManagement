package br.com.brittosw.stockmanagement.domain.customer.controller;

import br.com.brittosw.stockmanagement.domain.customer.dto.AddressRequest;
import br.com.brittosw.stockmanagement.domain.customer.dto.CustomerRequest;
import br.com.brittosw.stockmanagement.domain.customer.model.Customer;
import br.com.brittosw.stockmanagement.domain.customer.model.CustomerStatus;
import br.com.brittosw.stockmanagement.domain.customer.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.Spy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Spy
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
    @DisplayName("GET /api/customers")
    class GetCustomers {

        @Test
        @DisplayName("Deve retornar lista de clientes paginada")
        void shouldReturnPagedCustomerList() throws Exception {
            Page<Customer> customerPage = new PageImpl<>(List.of(customer));
            when(customerService.findActiveCustomers(any(Pageable.class))).thenReturn(customerPage);

            mockMvc.perform(get("/api/customers")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].name").value(customer.getName()))
                    .andExpect(jsonPath("$.content[0].email").value(customer.getEmail()));

            verify(customerService).findActiveCustomers(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/customers/{id}")
    class GetCustomer {

        @Test
        @DisplayName("Deve retornar cliente por ID")
        void shouldReturnCustomerById() throws Exception {
            when(customerService.findById(any(UUID.class))).thenReturn(customer);

            mockMvc.perform(get("/api/customers/{id}", customerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(customer.getName()))
                    .andExpect(jsonPath("$.email").value(customer.getEmail()))
                    .andExpect(jsonPath("$._links").exists());

            verify(customerService).findById(customerId);
        }

        @Test
        @DisplayName("Deve retornar 404 quando cliente não encontrado")
        void shouldReturn404WhenCustomerNotFound() throws Exception {
            when(customerService.findById(any(UUID.class)))
                    .thenThrow(new EntityNotFoundException("Cliente não encontrado"));

            mockMvc.perform(get("/api/customers/{id}", customerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(customerService).findById(customerId);
        }
    }

    @Nested
    @DisplayName("POST /api/customers")
    class CreateCustomer {

        @Test
        @DisplayName("Deve criar cliente com sucesso")
        void shouldCreateCustomerSuccessfully() throws Exception {
            when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(customer);

            mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value(customer.getName()))
                    .andExpect(jsonPath("$.email").value(customer.getEmail()))
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$._links").exists());

            verify(customerService).createCustomer(any(CustomerRequest.class));
        }

        @Test
        @DisplayName("Deve retornar 400 quando request inválido")
        void shouldReturn400WhenInvalidRequest() throws Exception {
            CustomerRequest invalidRequest = CustomerRequest.builder().build(); // Request vazio

            mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(customerService, never()).createCustomer(any(CustomerRequest.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/customers/{id}")
    class UpdateCustomer {

        @Test
        @DisplayName("Deve atualizar cliente com sucesso")
        void shouldUpdateCustomerSuccessfully() throws Exception {
            CustomerRequest updateRequest = CustomerRequest.builder()
                    .name("João Silva Atualizado")
                    .email("joao@email.com")
                    .status(CustomerStatus.ACTIVE)
                    .build();

            when(customerService.updateCustomer(any(UUID.class), any(CustomerRequest.class)))
                    .thenReturn(customer);

            mockMvc.perform(put("/api/customers/{id}", customerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(customer.getName()))
                    .andExpect(jsonPath("$._links").exists());

            verify(customerService).updateCustomer(eq(customerId), any(CustomerRequest.class));
        }

        @Test
        @DisplayName("Deve retornar 404 quando cliente não encontrado na atualização")
        void shouldReturn404WhenCustomerNotFoundOnUpdate() throws Exception {
            when(customerService.updateCustomer(any(UUID.class), any(CustomerRequest.class)))
                    .thenThrow(new EntityNotFoundException("Cliente não encontrado"));

            mockMvc.perform(put("/api/customers/{id}", customerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerRequest)))
                    .andExpect(status().isNotFound());

            verify(customerService).updateCustomer(eq(customerId), any(CustomerRequest.class));
        }
    }

    @Nested
    @DisplayName("POST /api/customers/{id}/addresses")
    class AddAddress {

        @Test
        @DisplayName("Deve adicionar endereço com sucesso")
        void shouldAddAddressSuccessfully() throws Exception {
            when(customerService.addAddress(any(UUID.class), any(AddressRequest.class)))
                    .thenReturn(customer);

            mockMvc.perform(post("/api/customers/{id}/addresses", customerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerRequest.getAddress())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._links").exists());

            verify(customerService).addAddress(eq(customerId), any(AddressRequest.class));
        }

        @Test
        @DisplayName("Deve retornar 404 quando cliente não encontrado ao adicionar endereço")
        void shouldReturn404WhenCustomerNotFoundOnAddAddress() throws Exception {
            when(customerService.addAddress(any(UUID.class), any(AddressRequest.class)))
                    .thenThrow(new EntityNotFoundException("Cliente não encontrado"));

            mockMvc.perform(post("/api/customers/{id}/addresses", customerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerRequest.getAddress())))
                    .andExpect(status().isNotFound());

            verify(customerService).addAddress(eq(customerId), any(AddressRequest.class));
        }
    }
}
