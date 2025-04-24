package br.com.brittosw.stockmanagement.domain.customer.dto;

import br.com.brittosw.stockmanagement.domain.customer.model.Customer;
import br.com.brittosw.stockmanagement.domain.customer.model.CustomerStatus;
import br.com.brittosw.stockmanagement.domain.order.dto.OrderSummaryResponse;
import br.com.brittosw.stockmanagement.model.Order;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerResponse extends RepresentationModel<CustomerResponse> {
    private UUID id;
    private String name;
    private String email;
    private String document;
    private Set<String>  phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CustomerStatus active;
    private Set<AddressResponse> addresses;
    private Set<OrderSummaryResponse> recentOrders;

    public static CustomerResponse fromCustomer(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        response.setDocument(customer.getDocument());
        response.setPhone(customer.getPhones());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        response.setActive(customer.getStatus());

        response.setAddresses(customer.getAddresses().stream()
                .map(AddressResponse::fromAddress)
                .collect(Collectors.toSet()));

        // Opcional: últimos pedidos do cliente (limitado a 5, por exemplo)
        response.setRecentOrders(customer.getOrders().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .limit(5)
                .map(OrderSummaryResponse::fromOrder)
                .collect(Collectors.toSet()));

        return response;
    }
}
