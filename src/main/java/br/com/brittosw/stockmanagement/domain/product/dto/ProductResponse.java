package br.com.brittosw.stockmanagement.domain.product.dto;

import br.com.brittosw.stockmanagement.domain.product.model.Product;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "products", itemRelation = "product")
public class ProductResponse extends RepresentationModel<ProductResponse> {
    private UUID id;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer minimumStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
    @Schema(description = "Data prevista para reabastecimento", example = "2024-05-15")
    private LocalDate restockDate;


    public static ProductResponse fromProduct(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .minimumStock(product.getMinimumStock())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .restockDate(product.getRestockDate())
                .status(product.getStatus().toString())
                .build();
    }
}
