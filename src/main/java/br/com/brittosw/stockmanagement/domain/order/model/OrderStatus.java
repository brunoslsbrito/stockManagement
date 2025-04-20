package br.com.brittosw.stockmanagement.domain.order.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("Pendente"),
    CONFIRMED("Confirmado"),
    IN_PROGRESS("Em Processamento"),
    SHIPPED("Enviado"),
    DELIVERED("Entregue"),
    CANCELLED("Cancelado");

    private final String description;
}
