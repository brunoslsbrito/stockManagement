package br.com.brittosw.stockmanagement.domain.order.service;

import br.com.brittosw.stockmanagement.domain.customer.service.CustomerService;
import br.com.brittosw.stockmanagement.domain.order.dto.OrderRequest;
import br.com.brittosw.stockmanagement.domain.order.repository.OrderRepository;
import br.com.brittosw.stockmanagement.domain.product.service.ProductService;
import br.com.brittosw.stockmanagement.model.Order;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final CustomerService customerService;

    @Transactional
    public Order createOrder(OrderRequest request) {
        var customer = customerService.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        var order = Order.createOrder(customer);

        request.getItems().forEach(item -> {
            var product = productService.findById(item.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
            
            if (!productService.hasEnoughStock(product, item.getQuantity())) {
                throw new InsufficientStockException("Estoque insuficiente");
            }

            order.addItem(product, item.getQuantity(), product.getPrice());
        });

        return orderRepository.save(order);
    }

    @Transactional
    public Order confirmOrder(UUID orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        order.confirm();
        
        order.getItems().forEach(item -> 
            productService.decreaseStock(item.getProduct().getId(), item.getQuantity()));

        return orderRepository.save(order);
    }
}
