package br.com.brittosw.stockmanagement.domain.order.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OrderItemRequest {
    private UUID productId;
    private Integer quantity;
}
