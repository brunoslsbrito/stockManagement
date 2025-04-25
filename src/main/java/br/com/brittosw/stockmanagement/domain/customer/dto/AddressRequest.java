package br.com.brittosw.stockmanagement.domain.customer.dto;

import br.com.brittosw.stockmanagement.domain.shared.model.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class AddressRequest {
    @NotBlank(message = "Rua é obrigatória")
    private String street;
    
    @NotBlank(message = "Número é obrigatório")
    private String number;
    
    private String complement;
    
    @NotBlank(message = "Bairro é obrigatório")
    private String neighborhood;
    
    @NotBlank(message = "Cidade é obrigatória")
    private String city;
    
    @NotBlank(message = "Estado é obrigatório")
    private String state;
    
    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP inválido")
    private String zipCode;

    @NotNull(message = "Principal é obrigatório")
    private boolean main;

    public Address toAddress() {
        return Address.builder()
                .street(this.street)
                .number(this.number)
                .complement(this.complement)
                .neighborhood(this.neighborhood)
                .city(this.city)
                .state(this.state)
                .zipCode(this.zipCode)
                .main(this.main)
                .build();
    }

}
