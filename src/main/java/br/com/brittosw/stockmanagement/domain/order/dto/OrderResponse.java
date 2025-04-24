package br.com.brittosw.stockmanagement.domain.order.dto;

import br.com.brittosw.stockmanagement.domain.customer.dto.CustomerSummaryResponse;
import br.com.brittosw.stockmanagement.domain.order.model.OrderStatus;
import br.com.brittosw.stockmanagement.model.Order;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderResponse extends RepresentationModel<OrderResponse> {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private CustomerSummaryResponse customer;
    private Set<OrderItemResponse> items;

    public static OrderResponse fromOrder(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCustomer(CustomerSummaryResponse.fromCustomer(order.getCustomer()));
        response.setItems(order.getItems().stream()
                .map(OrderItemResponse::fromOrderItem)
                .collect(Collectors.toSet()));
        return response;
    }
}
