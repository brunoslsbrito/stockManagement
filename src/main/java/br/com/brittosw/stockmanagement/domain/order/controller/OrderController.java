package br.com.brittosw.stockmanagement.domain.order.controller;

import br.com.brittosw.stockmanagement.domain.order.dto.OrderItemRequest;
import br.com.brittosw.stockmanagement.domain.order.dto.OrderRequest;
import br.com.brittosw.stockmanagement.domain.order.dto.OrderResponse;
import br.com.brittosw.stockmanagement.domain.order.model.OrderStatus;
import br.com.brittosw.stockmanagement.domain.order.service.OrderService;
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


import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import java.util.UUID;
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PagedResourcesAssembler<OrderResponse> pagedResourcesAssembler;

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "order.list")
    public ResponseEntity<PagedModel<EntityModel<OrderResponse>>> listOrders(
            Pageable pageable) {
        Page<OrderResponse> orders = orderService.findAll(pageable)
                .map(order -> {
                    OrderResponse response = OrderResponse.fromOrder(order);
                    return addLinks(response);
                });

        return ResponseEntity.ok(pagedResourcesAssembler.toModel(orders));
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "order.get")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(
                addLinks(OrderResponse.fromOrder(orderService.findById(id)))
        );
    }

    @PostMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "order.create")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request) {
        OrderResponse response = OrderResponse.fromOrder(
                orderService.createOrder(request));

        return ResponseEntity
                .created(linkTo(methodOn(OrderController.class)
                        .getOrder(response.getId())).toUri())
                .body(addLinks(response));
    }

    @PostMapping(value = "/{id}/items", produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "order.item.add")
    public ResponseEntity<OrderResponse> addItem(
            @PathVariable UUID id,
            @Valid @RequestBody OrderItemRequest request) {
        return ResponseEntity.ok(
                addLinks(OrderResponse.fromOrder(
                        orderService.addItem(id,request)))
        );
    }

    @DeleteMapping(value = "/{orderId}/items/{itemId}/quantity/{quantity}")
    @Timed(value = "order.item.remove")
    public ResponseEntity<Void> removeItem(
            @PathVariable UUID orderId,
            @PathVariable UUID itemId,
            @PathVariable int quantity) {
        orderService.removeItem(orderId, itemId,quantity);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/confirm", produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "order.confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(
                addLinks(OrderResponse.fromOrder(orderService.confirmOrder(id)))
        );
    }

    @GetMapping(value = "/customer/{customerId}", produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "order.customer.list")
    public ResponseEntity<PagedModel<EntityModel<OrderResponse>>> listCustomerOrders(
            @PathVariable UUID customerId,
            Pageable pageable) {
        Page<OrderResponse> orders = orderService.findByCustomer(customerId, pageable)
                .map(order -> {
                    OrderResponse response = OrderResponse.fromOrder(order);
                    return addLinks(response);
                });

        return ResponseEntity.ok(pagedResourcesAssembler.toModel(orders));
    }

    private OrderResponse addLinks(OrderResponse order) {
        order.add(linkTo(methodOn(OrderController.class)
                .getOrder(order.getId())).withSelfRel());

        order.add(linkTo(methodOn(OrderController.class)
                .addItem(order.getId(), null))
                .withRel("add-item"));

        order.add(linkTo(methodOn(OrderController.class)
                .listOrders(Pageable.unpaged()))
                .withRel(IanaLinkRelations.COLLECTION));

        if (order.getStatus() == OrderStatus.PENDING) {
            order.add(linkTo(methodOn(OrderController.class)
                    .confirmOrder(order.getId()))
                    .withRel("confirm"));
        }

        return order;
    }
}
