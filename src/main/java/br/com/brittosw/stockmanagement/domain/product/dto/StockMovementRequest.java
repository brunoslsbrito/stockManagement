package br.com.brittosw.stockmanagement.domain.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Requisição para movimentação de estoque")
public class StockMovementRequest {

    @Schema(description = "Quantidade a ser movimentada (positivo para entrada, negativo para saída)",
            example = "10")
    @NotNull(message = "A quantidade é obrigatória")
    private Integer quantity;
    
    private String observation;
}
