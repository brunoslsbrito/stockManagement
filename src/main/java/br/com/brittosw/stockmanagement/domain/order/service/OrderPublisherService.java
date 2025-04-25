package br.com.brittosw.stockmanagement.domain.order.service;

import br.com.brittosw.stockmanagement.config.RabbitMQConfig;
import br.com.brittosw.stockmanagement.domain.order.dto.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderPublisherService {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrder(OrderRequest orderRequest) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.ORDER_EXCHANGE,
            RabbitMQConfig.ORDER_ROUTING_KEY,
            orderRequest
        );
    }
}
