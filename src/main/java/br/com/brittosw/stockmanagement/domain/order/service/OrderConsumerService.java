package br.com.brittosw.stockmanagement.domain.order.service;

import br.com.brittosw.stockmanagement.config.RabbitMQConfig;
import br.com.brittosw.stockmanagement.domain.customer.service.CustomerService;
import br.com.brittosw.stockmanagement.domain.order.dto.OrderItemRequest;
import br.com.brittosw.stockmanagement.domain.order.dto.OrderRequest;
import br.com.brittosw.stockmanagement.domain.order.model.OrderStatus;
import br.com.brittosw.stockmanagement.domain.order.repository.OrderRepository;
import br.com.brittosw.stockmanagement.domain.product.service.ProductService;
import br.com.brittosw.stockmanagement.model.Order;
import br.com.brittosw.stockmanagement.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderConsumerService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final  ProductService productService;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE, ackMode = "MANUAL")
    public void processOrder(OrderRequest orderRequest) {
        Order order = convertToOrder(orderRequest);
        orderRepository.save(order);
    }

    private Order convertToOrder(OrderRequest orderRequest) {
        if (orderRequest == null) {
            throw new IllegalArgumentException("OrderRequest não pode ser nulo");
        }

        var customer = customerService.findById(orderRequest.getCustomerId());
        var order = Order.createOrder(customer);

        orderRequest.getItems().forEach(item -> {
            var product = productService.findById(item.getProductId());
            order.addItem(product, item.getQuantity(), product.getPrice());
        });

        return order;
    }


}
