package br.com.brittosw.stockmanagement.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequest {
    private UUID productId;
    private Integer quantity;
}
