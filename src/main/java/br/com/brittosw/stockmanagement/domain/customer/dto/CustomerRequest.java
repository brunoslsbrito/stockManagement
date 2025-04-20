package br.com.brittosw.stockmanagement.domain.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerRequest {
    @NotBlank(message = "Nome é obrigatório")
    private String name;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;
    
    @NotBlank(message = "Documento é obrigatório")
    private String document;
    
    @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$", message = "Telefone inválido")
    private String phone;
    
    private AddressRequest address;
}
