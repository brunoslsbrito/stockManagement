package br.com.brittosw.stockmanagement.domain.product.model;

public enum ProductStatus {
    ACTIVE("Ativo"),
    INACTIVE("Inativo"),
    OUT_OF_STOCK("Sem Estoque");

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
