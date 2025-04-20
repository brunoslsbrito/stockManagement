package br.com.brittosw.stockmanagement.domain.customer.model;

public enum CustomerStatus {
    ACTIVE("Ativo"),
    INACTIVE("Inativo"),
    BLOCKED("Bloqueado");

    private final String description;

    CustomerStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
