package br.com.brittosw.stockmanagement.domain.customer.dto;

import br.com.brittosw.stockmanagement.domain.customer.controller.CustomerController;
import br.com.brittosw.stockmanagement.domain.customer.model.Customer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerSummaryResponse extends RepresentationModel<CustomerSummaryResponse> {
        private UUID id;
        private String name;
        private String email;
        private String document;

        public static CustomerSummaryResponse fromCustomer(Customer customer) {
            CustomerSummaryResponse response = new CustomerSummaryResponse();
            response.setId(customer.getId());
            response.setName(customer.getName());
            response.setEmail(customer.getEmail());
            response.setDocument(customer.getDocument());

            response.add(linkTo(methodOn(CustomerController.class)
                    .getCustomer(customer.getId())).withSelfRel());

            return response;
        }
    }
