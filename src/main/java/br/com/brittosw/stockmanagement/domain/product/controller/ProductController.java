package br.com.brittosw.stockmanagement.domain.product.controller;

import br.com.brittosw.stockmanagement.domain.product.dto.ProductRequest;
import br.com.brittosw.stockmanagement.domain.product.dto.ProductResponse;
import br.com.brittosw.stockmanagement.domain.product.dto.StockMovementRequest;
import br.com.brittosw.stockmanagement.domain.product.service.ProductService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final PagedResourcesAssembler<ProductResponse> pagedResourcesAssembler;

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "product.list")
    public ResponseEntity<PagedModel<EntityModel<ProductResponse>>> listProducts(
            Pageable pageable) {
        Page<ProductResponse> products = productService.findAll(pageable)
                .map(product -> {
                    ProductResponse response = ProductResponse.fromProduct(product);
                    return addLinks(response);
                });

        return ResponseEntity.ok(pagedResourcesAssembler.toModel(products));
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "product.get")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(
                addLinks(ProductResponse.fromProduct(productService.findById(id)))
        );
    }

    @PostMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "product.create")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = ProductResponse.fromProduct(
                productService.createProduct(request));

        return ResponseEntity
                .created(linkTo(methodOn(ProductController.class)
                        .getProduct(response.getId())).toUri())
                .body(addLinks(response));
    }

    @PutMapping(value = "/{id}/stock", produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "product.stock.update")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(
                addLinks(ProductResponse.fromProduct(
                        productService.updateStock(id, request)))
        );
    }

    @GetMapping(value = "/search", produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "product.search")
    public ResponseEntity<PagedModel<EntityModel<ProductResponse>>> searchProducts(
            @RequestParam String query,
            Pageable pageable) {
        Page<ProductResponse> products = productService.searchProducts(query, pageable)
                .map(product -> {
                    ProductResponse response = ProductResponse.fromProduct(product);
                    return addLinks(response);
                });

        return ResponseEntity.ok(pagedResourcesAssembler.toModel(products));
    }

    private ProductResponse addLinks(ProductResponse product) {
        product.add(linkTo(methodOn(ProductController.class)
                .getProduct(product.getId())).withSelfRel());

        product.add(linkTo(methodOn(ProductController.class)
                .updateStock(product.getId(), null))
                .withRel("update-stock"));

        product.add(linkTo(methodOn(ProductController.class)
                .listProducts(Pageable.unpaged()))
                .withRel(IanaLinkRelations.COLLECTION));

        if (product.getStockQuantity() <= product.getMinimumStock()) {
            product.add(linkTo(methodOn(ProductController.class)
                    .updateStock(product.getId(), null))
                    .withRel("restock-needed"));
        }

        return product;
    }
}
