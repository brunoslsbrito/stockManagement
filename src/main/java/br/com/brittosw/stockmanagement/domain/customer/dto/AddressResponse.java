package br.com.brittosw.stockmanagement.domain.customer.dto;

import br.com.brittosw.stockmanagement.domain.shared.model.Address;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

// src/main/java/br/com/brittosw/stockmanagement/domain/customer/dto/AddressResponse.java
@Data
@EqualsAndHashCode(callSuper = true)
public class AddressResponse extends RepresentationModel<AddressResponse> {
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;
    private boolean main;

    public static AddressResponse fromAddress(Address address) {
        AddressResponse response = new AddressResponse();
        response.setStreet(address.getStreet());
        response.setNumber(address.getNumber());
        response.setComplement(address.getComplement());
        response.setNeighborhood(address.getNeighborhood());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setZipCode(address.getZipCode());
        response.setMain(address.isMain());
        return response;
    }
}
