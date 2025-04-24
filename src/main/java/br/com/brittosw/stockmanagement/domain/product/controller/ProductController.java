package br.com.brittosw.stockmanagement.domain.product.controller;

import br.com.brittosw.stockmanagement.domain.product.dto.ProductRequest;
import br.com.brittosw.stockmanagement.domain.product.dto.ProductResponse;
import br.com.brittosw.stockmanagement.domain.product.dto.StockMovementRequest;
import br.com.brittosw.stockmanagement.domain.product.model.Product;
import br.com.brittosw.stockmanagement.domain.product.service.ProductService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Produtos", description = "API para gerenciamento de produtos e estoque")
public class ProductController {

    private final ProductService productService;
    private final PagedResourcesAssembler<ProductResponse> pagedResourcesAssembler;

    @Operation(summary = "Listar todos os produtos",
            description = "Retorna uma lista paginada de todos os produtos")

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
    @Operation(summary = "Buscar produto por ID",
            description = "Retorna um produto específico baseado no ID fornecido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "product.get")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(
                addLinks(ProductResponse.fromProduct(productService.findById(id)))
        );
    }

    @Operation(summary = "Criar novo produto",
            description = "Cria um novo produto no sistema com as informações fornecidas")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou SKU já existente"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })

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

    @Operation(summary = "Atualizar estoque",
            description = "Atualiza a quantidade em estoque de um produto específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estoque atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida")
    })

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

    @Operation(summary = "Pesquisar produtos",
            description = "Pesquisa produtos com base em um termo de busca")
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
