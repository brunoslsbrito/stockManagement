package br.com.brittosw.stockmanagement.domain.listeners;

import br.com.brittosw.stockmanagement.domain.events.StockUpdatedEvent;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventListener {
    
    private final MeterRegistry meterRegistry;

    @EventListener
    public void handleStockUpdated(StockUpdatedEvent event) {
        log.info("Stock updated for product {}: {} units", 
                event.getProductId(), event.getQuantity());
        
        meterRegistry.counter("stock.updates", 
                "product", event.getProductId().toString())
                .increment();
    }
}
