package br.com.brittosw.stockmanagement.domain.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Requisição para criação de novo produto")
public class ProductRequest {
    @Schema(description = "Nome do produto", example = "Smartphone XYZ")
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100)
    private String name;

    @Schema(description = "Descrição do produto", example = "Smartphone com 128GB de memória")
    @Size(max = 500)
    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    @Schema(description = "SKU do produto", example = "PHONE-XYZ-128")
    @NotBlank(message = "SKU é obrigatório")
    @Size(min = 3, max = 50)
    private String sku;

    @Schema(description = "Preço do produto", example = "1299.99")
    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    private BigDecimal price;

    @Schema(description = "Quantidade inicial em estoque", example = "100")
    @NotNull(message = "Estoque inicial é obrigatório")
    @Min(value = 0, message = "Estoque inicial não pode ser negativo")
    private Integer initialStock;

    @Schema(description = "Quantidade mínima de estoque", example = "10")
    @NotNull(message = "Estoque mínimo é obrigatório")
    @Min(value = 0, message = "Estoque mínimo não pode ser negativo")
    private Integer minimumStock;

    private LocalDate restockDate;

}
