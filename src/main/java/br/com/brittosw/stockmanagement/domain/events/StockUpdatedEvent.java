package br.com.brittosw.stockmanagement.domain.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class StockUpdatedEvent extends ApplicationEvent {
    private final UUID productId;
    private final int quantity;
    private final LocalDateTime timestamp;

    public StockUpdatedEvent(Object source, UUID productId, int quantity) {
        super(source);
        this.productId = productId;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now();
    }
}
