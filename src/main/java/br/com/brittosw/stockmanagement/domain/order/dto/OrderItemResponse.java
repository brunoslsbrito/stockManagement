package br.com.brittosw.stockmanagement.domain.order.dto;

import br.com.brittosw.stockmanagement.domain.product.controller.ProductController;
import br.com.brittosw.stockmanagement.domain.product.dto.ProductSummaryResponse;
import br.com.brittosw.stockmanagement.model.OrderItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderItemResponse extends RepresentationModel<OrderItemResponse> {
    private UUID id;
    private ProductSummaryResponse product;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public static OrderItemResponse fromOrderItem(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setProduct(ProductSummaryResponse.fromProduct(orderItem.getProduct()));
        response.setQuantity(orderItem.getQuantity());
        response.setUnitPrice(orderItem.getUnitPrice());
        response.setSubtotal(orderItem.calculateSubtotal());

        response.add(linkTo(methodOn(ProductController.class)
                .getProduct(orderItem.getProduct().getId())).withRel("product"));

        return response;
    }
}
