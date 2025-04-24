package br.com.brittosw.stockmanagement.domain.order.service;

import br.com.brittosw.stockmanagement.domain.customer.service.CustomerService;
import br.com.brittosw.stockmanagement.domain.order.dto.OrderItemRequest;
import br.com.brittosw.stockmanagement.domain.order.dto.OrderRequest;
import br.com.brittosw.stockmanagement.domain.order.repository.OrderRepository;
import br.com.brittosw.stockmanagement.domain.product.exception.InsufficientStockException;
import br.com.brittosw.stockmanagement.domain.product.service.ProductService;
import br.com.brittosw.stockmanagement.model.Order;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        var customer = customerService.findById(request.getCustomerId());
        var order = Order.createOrder(customer);

        request.getItems().forEach(item -> {
            var product = productService.findById(item.getProductId());

            if (!productService.hasEnoughStock(product, item.getQuantity(), customer)) {
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
                productService.decreaseStock(item.getProduct().getId(), item.getQuantity(),order.getCustomer()));

        return orderRepository.save(order);
    }

    public Page<Order> findByCustomer(UUID customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable);
    }

    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public Order findById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));
    }

    @Transactional
    public Order addItem(UUID orderId, OrderItemRequest request) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        var product = productService.findById(request.getProductId());

        if (!productService.hasEnoughStock(product, request.getQuantity(), order.getCustomer())) {
            throw new InsufficientStockException("Estoque insuficiente");
        }

        order.addItem(product, request.getQuantity(), product.getPrice());

        return orderRepository.save(order);
    }

    @Transactional
    public void removeItem(UUID orderId, UUID productId, int quantity) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        var orderItem = order.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado no pedido"));

        order.removeItem(orderItem, quantity);

        orderRepository.save(order);
    }

}
