package br.com.brittosw.stockmanagement.domain.order.dto;

import br.com.brittosw.stockmanagement.domain.order.controller.OrderController;
import br.com.brittosw.stockmanagement.domain.order.model.OrderStatus;
import br.com.brittosw.stockmanagement.model.Order;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderSummaryResponse extends RepresentationModel<OrderSummaryResponse> {
    private UUID id;
    private LocalDateTime createdAt;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private int itemCount;

    public static OrderSummaryResponse fromOrder(Order order) {
        OrderSummaryResponse response = new OrderSummaryResponse();
        response.setId(order.getId());
        response.setCreatedAt(order.getCreatedAt());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setItemCount(order.getItems().size());

        response.add(linkTo(methodOn(OrderController.class)
                .getOrder(order.getId())).withSelfRel());

        return response;
    }
}
