package br.com.brittosw.stockmanagement.domain.order.service;

import br.com.brittosw.stockmanagement.domain.customer.model.Customer;
import br.com.brittosw.stockmanagement.domain.customer.service.CustomerService;
import br.com.brittosw.stockmanagement.domain.order.dto.OrderItemRequest;
import br.com.brittosw.stockmanagement.domain.order.dto.OrderRequest;
import br.com.brittosw.stockmanagement.domain.order.model.OrderStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    private static final int DEFAULT_QUANTITY = 2;
    private static final BigDecimal DEFAULT_PRICE = BigDecimal.valueOf(100);

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private OrderService orderService;

    private UUID customerId;
    private UUID productId;
    private UUID orderId;
    private Customer customer;
    private Product product;
    private Order order;
    private OrderRequest orderRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);

        customer = Customer.create("João", "joao@email.com", "123.456.789-00");
        product = Product.builder()
                .id(productId)
                .name("Produto Teste")
                .price(DEFAULT_PRICE)
                .stockQuantity(10)
                .build();

        order = Order.createOrder(customer);
        order.addItem(product, DEFAULT_QUANTITY, product.getPrice());

        orderRequest = new OrderRequest(customerId,
                List.of(new OrderItemRequest(productId, DEFAULT_QUANTITY)));
    }

    @Nested
    @DisplayName("Criar Pedido")
    class CreateOrder {
        @Test
        @DisplayName("Deve criar pedido com sucesso")
        void shouldCreateOrderSuccessfully() {
            when(customerService.findById(customerId)).thenReturn(customer);
            when(productService.findById(productId)).thenReturn(product);
            when(productService.hasEnoughStock(any(), anyInt(),any())).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            Order result = orderService.createOrder(orderRequest);

            assertNotNull(result);
            verify(orderRepository).save(any(Order.class));
            verify(productService).hasEnoughStock(product, DEFAULT_QUANTITY, customer);
        }

        @Test
        @DisplayName("Deve lançar exceção quando estoque insuficiente")
        void shouldThrowExceptionWhenInsufficientStock() {
            when(customerService.findById(customerId)).thenReturn(customer);
            when(productService.findById(productId)).thenReturn(product);
            when(productService.hasEnoughStock(any(), anyInt(), any())).thenReturn(false);

            assertThrows(InsufficientStockException.class,
                    () -> orderService.createOrder(orderRequest));
            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Confirmar Pedido")
    class ConfirmOrder {
        @Test
        @DisplayName("Deve confirmar pedido com sucesso")
        void shouldConfirmOrderSuccessfully() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            Order result = orderService.confirmOrder(orderId);

            assertEquals(OrderStatus.CONFIRMED, result.getStatus());
            verify(productService).decreaseStock(productId, DEFAULT_QUANTITY, result.getCustomer());
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não encontrado")
        void shouldThrowExceptionWhenOrderNotFound() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> orderService.confirmOrder(orderId));
            verify(productService, never()).decreaseStock(any(), anyInt(), any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao confirmar pedido já confirmado")
        void shouldThrowExceptionWhenConfirmingAlreadyConfirmedOrder() {
            order.confirm(); // Confirma o pedido antes do teste
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            assertThrows(IllegalStateException.class,
                    () -> orderService.confirmOrder(orderId));
        }
    }

    @Nested
    @DisplayName("Buscar Pedidos")
    class FindOrders {
        @Test
        @DisplayName("Deve buscar pedidos por cliente")
        void shouldFindOrdersByCustomer() {
            Page<Order> expectedPage = new PageImpl<>(List.of(order));
            when(orderRepository.findByCustomerId(customerId, pageable))
                    .thenReturn(expectedPage);

            Page<Order> result = orderService.findByCustomer(customerId, pageable);

            assertEquals(expectedPage, result);
            verify(orderRepository).findByCustomerId(customerId, pageable);
        }

        @Test
        @DisplayName("Deve buscar todos os pedidos")
        void shouldFindAllOrders() {
            Page<Order> expectedPage = new PageImpl<>(List.of(order));
            when(orderRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Order> result = orderService.findAll(pageable);

            assertEquals(expectedPage, result);
            verify(orderRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Deve buscar pedido por ID")
        void shouldFindOrderById() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            Order result = orderService.findById(orderId);

            assertNotNull(result);
            assertEquals(order, result);
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não encontrado")
        void shouldThrowExceptionWhenOrderNotFoundById() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> orderService.findById(orderId));
        }
    }

    @Nested
    @DisplayName("Gerenciar Itens do Pedido")
    class ManageOrderItems {
        @Test
        @DisplayName("Deve adicionar item ao pedido")
        void shouldAddItemToOrder() {
            OrderItemRequest itemRequest = new OrderItemRequest(productId, DEFAULT_QUANTITY);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(productService.findById(productId)).thenReturn(product);
            when(productService.hasEnoughStock(any(), anyInt(), any())).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            Order result = orderService.addItem(orderId, itemRequest);

            assertNotNull(result);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Deve remover item do pedido")
        void shouldRemoveItemFromOrder() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            orderService.removeItem(orderId, productId, DEFAULT_QUANTITY);

            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao remover item inexistente")
        void shouldThrowExceptionWhenRemovingNonexistentItem() {
            UUID nonexistentProductId = UUID.randomUUID();
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            assertThrows(EntityNotFoundException.class,
                    () -> orderService.removeItem(orderId, nonexistentProductId, DEFAULT_QUANTITY));
        }
    }
}
