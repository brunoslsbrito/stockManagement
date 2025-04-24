package br.com.brittosw.stockmanagement.domain.customer.controller;


import br.com.brittosw.stockmanagement.domain.customer.dto.AddressRequest;
import br.com.brittosw.stockmanagement.domain.customer.dto.CustomerRequest;
import br.com.brittosw.stockmanagement.domain.customer.dto.CustomerResponse;
import br.com.brittosw.stockmanagement.domain.customer.service.CustomerService;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final PagedResourcesAssembler<CustomerResponse> pagedResourcesAssembler;


    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "customer.list")
    public ResponseEntity<PagedModel<EntityModel<CustomerResponse>>> listCustomers(
            Pageable pageable) {
        Page<CustomerResponse> customers = customerService.findActiveCustomers(pageable)
                .map(customer -> addLinks(CustomerResponse.fromCustomer(customer)));

        return ResponseEntity.ok(pagedResourcesAssembler.toModel(customers));
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "customer.get")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable UUID id) {
        return ResponseEntity.ok(
                addLinks(CustomerResponse.fromCustomer(customerService.findById(id)))
        );
    }

    @PostMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "customer.create")
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = CustomerResponse.fromCustomer(
                customerService.createCustomer(request));

        return ResponseEntity
                .created(linkTo(methodOn(CustomerController.class)
                        .getCustomer(response.getId())).toUri())
                .body(addLinks(response));
    }

    @PutMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "customer.update")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(
                addLinks(CustomerResponse.fromCustomer(
                        customerService.updateCustomer(id, request)))
        );
    }

    @PostMapping(value = "/{id}/addresses", produces = MediaTypes.HAL_JSON_VALUE)
    @Timed(value = "customer.address.add")
    public ResponseEntity<CustomerResponse> addAddress(
            @PathVariable UUID id,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(
                addLinks(CustomerResponse.fromCustomer(
                        customerService.addAddress(id, request)))
        );
    }

    private CustomerResponse addLinks(CustomerResponse customer) {
        customer.add(linkTo(methodOn(CustomerController.class)
                .getCustomer(customer.getId())).withSelfRel());

        customer.add(linkTo(methodOn(CustomerController.class)
                .updateCustomer(customer.getId(), null))
                .withRel("update"));

        customer.add(linkTo(methodOn(CustomerController.class)
                .addAddress(customer.getId(), null))
                .withRel("add-address"));

        customer.add(linkTo(methodOn(CustomerController.class)
                .listCustomers(Pageable.unpaged()))
                .withRel(IanaLinkRelations.COLLECTION));

        return customer;
    }
}
