package br.com.brittosw.stockmanagement.domain.customer.dto;

import br.com.brittosw.stockmanagement.domain.customer.model.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class CustomerRequest {
    @NotBlank(message = "Nome é obrigatório")
    private String name;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;
    
    @NotBlank(message = "Documento é obrigatório")
    private String document;

    @Pattern(
            regexp = "^\\(?[1-9]{2}\\)? ?(?:[2-8]|9[1-9])[0-9]{3}\\-?[0-9]{4}$",
            message = "Telefone inválido. Use o formato: (99) 99999-9999 ou 99999999999"
    )
    private String phone;
    
    private AddressRequest address;

    private CustomerStatus status;
}
