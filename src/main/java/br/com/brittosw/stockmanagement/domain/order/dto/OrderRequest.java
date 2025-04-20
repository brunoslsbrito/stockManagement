package br.com.brittosw.stockmanagement.domain.order.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class OrderRequest {
    private UUID customerId;
    private List<OrderItemRequest> items;
}


