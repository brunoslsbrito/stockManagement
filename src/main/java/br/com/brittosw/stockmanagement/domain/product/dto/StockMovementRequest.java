package br.com.brittosw.stockmanagement.domain.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockMovementRequest {
    
    @NotNull(message = "A quantidade é obrigatória")
    private Integer quantity;
    
    private String observation;
}
