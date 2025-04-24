package br.com.brittosw.stockmanagement.domain.product.dto;

import br.com.brittosw.stockmanagement.domain.product.controller.ProductController;
import br.com.brittosw.stockmanagement.domain.product.model.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSummaryResponse extends RepresentationModel<ProductSummaryResponse> {
    private UUID id;
    private String name;
    private String sku;
    private BigDecimal price;
    private int stockQuantity;

    public static ProductSummaryResponse fromProduct(Product product) {
        ProductSummaryResponse response = new ProductSummaryResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSku(product.getSku());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());

        response.add(linkTo(methodOn(ProductController.class)
                .getProduct(product.getId())).withSelfRel());

        return response;
    }
}
