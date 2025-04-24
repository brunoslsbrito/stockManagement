package br.com.brittosw.stockmanagement.domain.order.service;

import br.com.brittosw.stockmanagement.domain.customer.model.Customer;
import br.com.brittosw.stockmanagement.domain.order.dto.OrderItemRequest;
import br.com.brittosw.stockmanagement.domain.order.dto.OrderRequest;
import br.com.brittosw.stockmanagement.domain.order.repository.OrderRepository;
import br.com.brittosw.stockmanagement.domain.product.exception.InsufficientStockException;
import br.com.brittosw.stockmanagement.domain.product.model.Product;
import br.com.brittosw.stockmanagement.domain.product.service.ProductService;
import br.com.brittosw.stockmanagement.model.Order;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@Nested
@DisplayName("Gerenciar Itens do Pedido")
class ManageOrderItems {
    private static final int DEFAULT_QUANTITY = 2;
    private static final BigDecimal DEFAULT_PRICE = BigDecimal.valueOf(100);
    private static final String DEFAULT_PRODUCT_NAME = "Produto Teste";
    private static final String DEFAULT_CUSTOMER_NAME = "João";
    private static final String DEFAULT_CUSTOMER_EMAIL = "joao@email.com";
    private static final String DEFAULT_CUSTOMER_DOCUMENT = "123.456.789-00";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private UUID customerId;
    private UUID productId;
    private UUID orderId;
    private Customer customer;
    private Product product;
    private Order order;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        initializeIds();
        createCustomer();
        createProduct();
        createOrder();
        createOrderRequest();
    }

    private void initializeIds() {
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    private void createCustomer() {
        customer = Customer.create(
                DEFAULT_CUSTOMER_NAME,
                DEFAULT_CUSTOMER_EMAIL,
                DEFAULT_CUSTOMER_DOCUMENT
        );
    }

    private void createProduct() {
        product = Product.builder()
                .id(productId)
                .name(DEFAULT_PRODUCT_NAME)
                .price(DEFAULT_PRICE)
                .stockQuantity(10)
                .build();
    }

    private void createOrder() {
        order = Order.createOrder(customer);
        order.addItem(product, DEFAULT_QUANTITY, product.getPrice());
    }

    private void createOrderRequest() {
        orderRequest = new OrderRequest(
                customerId,
                List.of(new OrderItemRequest(productId, DEFAULT_QUANTITY))
        );
    }

    private void setupSuccessfulOrderMocks() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productService.findById(productId)).thenReturn(product);
        when(productService.hasEnoughStock(any(Product.class), eq(DEFAULT_QUANTITY), any(Customer.class))).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
    }

    @Test
    @DisplayName("Deve adicionar item ao pedido com sucesso")
    void shouldAddItemToOrderSuccessfully() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest(productId, DEFAULT_QUANTITY);
        setupSuccessfulOrderMocks();

        // Act
        Order updated = orderService.addItem(orderId, itemRequest);

        // Assert
        assertNotNull(updated);
        verifyOrderSaveAndProductChecks();
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido não existe")
    void shouldThrowExceptionWhenOrderDoesNotExist() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest(productId, DEFAULT_QUANTITY);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                orderService.addItem(orderId, itemRequest));
        verifyNoProductInteractions();
    }

    @Test
    @DisplayName("Deve lançar exceção quando estoque é insuficiente")
    void shouldThrowExceptionWhenStockIsInsufficient() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest(productId, DEFAULT_QUANTITY);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productService.findById(productId)).thenReturn(product);
        when(productService.hasEnoughStock(any(Product.class), eq(DEFAULT_QUANTITY), any(Customer.class))).thenReturn(false);

        // Act & Assert
        assertThrows(InsufficientStockException.class, () ->
                orderService.addItem(orderId, itemRequest));
        verify(orderRepository, never()).save(any());
    }

    private void verifyOrderSaveAndProductChecks() {
        verify(orderRepository).save(any(Order.class));
        verify(productService).findById(eq(productId));
        verify(productService).hasEnoughStock(any(Product.class), eq(DEFAULT_QUANTITY), any(Customer.class));
    }

    private void verifyNoProductInteractions() {
        verify(productService, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }
}
