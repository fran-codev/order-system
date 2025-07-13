package dev.francode.ordersystem.service.impl;


import dev.francode.ordersystem.dto.order.*;
import dev.francode.ordersystem.entity.Order;
import dev.francode.ordersystem.entity.OrderProduct;
import dev.francode.ordersystem.entity.Product;
import dev.francode.ordersystem.entity.UserApp;
import dev.francode.ordersystem.entity.enums.EStatusOrder;
import dev.francode.ordersystem.exceptions.custom.ValidationException;
import dev.francode.ordersystem.mapper.OrderMapper;
import dev.francode.ordersystem.repository.OrderProductRepository;
import dev.francode.ordersystem.repository.OrderRepository;
import dev.francode.ordersystem.repository.ProductRepository;
import dev.francode.ordersystem.repository.UserRepository;
import dev.francode.ordersystem.service.interfaces.OrderService;
import dev.francode.ordersystem.service.spec.OrderSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.HashSet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest, Long userId) {
        log.info("Iniciando creación de orden para userId: {}", userId);

        if (userId == null) {
            log.error("El userId es nulo");
            throw new ValidationException("Usuario no autenticado");
        }

        UserApp user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Usuario con ID {} no encontrado", userId);
                    return new ValidationException("Usuario no encontrado");
                });

        List<OrderProductRequest> items = orderRequest.getItems();

        if (items == null || items.isEmpty()) {
            log.warn("Intento de crear orden sin productos para el usuario {}", userId);
            throw new ValidationException("Debe agregar al menos un producto al pedido");
        }
        
        // Validar productos duplicados
	validateNoDuplicateProducts(items);


        Order order = new Order();
        order.setUser(user);
        order.setDate(LocalDateTime.now());
        order.setStatus(EStatusOrder.PENDIENTE);

        List<OrderProduct> orderProducts = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderProductRequest prodReq : items) {
            log.info("Procesando producto con ID {} y cantidad {}", prodReq.getProductId(), prodReq.getQuantity());

            if (prodReq.getQuantity() == null || prodReq.getQuantity() <= 0) {
                log.warn("Cantidad inválida para producto ID {}", prodReq.getProductId());
                throw new ValidationException("Cantidad inválida para producto ID " + prodReq.getProductId());
            }

            Product product = productRepository.findById(prodReq.getProductId())
                    .orElseThrow(() -> {
                        log.error("Producto con ID {} no encontrado", prodReq.getProductId());
                        return new ValidationException("Producto con ID " + prodReq.getProductId() + " no encontrado");
                    });

            if (!Boolean.TRUE.equals(product.getStatus())) {
                log.warn("Producto {} no está disponible", product.getName());
                throw new ValidationException("Producto " + product.getName() + " no está disponible");
            }

            if (prodReq.getQuantity() > product.getStock()) {
                log.warn("Stock insuficiente para {}. Stock actual: {}, solicitado: {}",
                          product.getName(), product.getStock(), prodReq.getQuantity());
                throw new ValidationException("Stock insuficiente para " + product.getName());
            }

            product.setStock(product.getStock() - prodReq.getQuantity());
            productRepository.save(product);

            OrderProduct op = new OrderProduct();
            op.setOrder(order);
            op.setProduct(product);
            op.setProductName(product.getName());
            op.setQuantity(prodReq.getQuantity());
            op.setUnitPrice(product.getPrice());
            orderProducts.add(op);

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(prodReq.getQuantity()));
            log.info("Subtotal para {}: {}", product.getName(), subtotal);

            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);
        order.setOrderProducts(orderProducts);

        Order savedOrder = orderRepository.save(order);
        orderProductRepository.saveAll(orderProducts);

        log.info("Orden creada con éxito. ID: {}, Total: {}", savedOrder.getId(), savedOrder.getTotalAmount());

        return orderMapper.toOrderResponse(savedOrder);
    }


    @Override
	@Transactional
	public OrderResponse updateOrder(Long orderId, OrderRequest orderRequest, Long userId) {
	    Order order = orderRepository.findById(orderId)
		    .orElseThrow(() -> new ValidationException("Pedido no encontrado"));

	    if (!order.getUser().getId().equals(userId)) {
		throw new ValidationException("No puede modificar pedidos de otros usuarios");
	    }
	    if (order.getStatus() != EStatusOrder.PENDIENTE) {
		throw new ValidationException("Solo puede editar pedidos en estado PENDIENTE");
	    }

	    List<OrderProductRequest> items = orderRequest.getItems();
	    if (items == null || items.isEmpty()) {
		throw new ValidationException("El pedido debe contener al menos un producto");
	    }

	    // Validar productos duplicados
	    validateNoDuplicateProducts(items);

	    // Validar cantidad > 0 para cada producto
	    for (OrderProductRequest prodReq : items) {
		if (prodReq.getQuantity() == null || prodReq.getQuantity() <= 0) {
		    throw new ValidationException("Cantidad inválida para producto ID " + prodReq.getProductId());
		}
	    }

	    // Devolver stock de productos previos
	    for (OrderProduct op : order.getOrderProducts()) {
		Product p = op.getProduct();
		p.setStock(p.getStock() + op.getQuantity());
		productRepository.save(p);
	    }
	    orderProductRepository.deleteAll(order.getOrderProducts());

	    List<OrderProduct> updatedProducts = new ArrayList<>();
	    BigDecimal totalAmount = BigDecimal.ZERO;

	    for (OrderProductRequest prodReq : items) {
		Product product = productRepository.findById(prodReq.getProductId())
		        .orElseThrow(() -> new ValidationException("Producto con ID " + prodReq.getProductId() + " no encontrado"));
		if (!product.getStatus()) {
		    throw new ValidationException("Producto " + product.getName() + " no está disponible");
		}
		if (prodReq.getQuantity() > product.getStock()) {
		    throw new ValidationException("Stock insuficiente para " + product.getName());
		}

		product.setStock(product.getStock() - prodReq.getQuantity());
		productRepository.save(product);

		OrderProduct op = new OrderProduct();
		op.setOrder(order);
		op.setProduct(product);
		op.setProductName(product.getName());
		op.setQuantity(prodReq.getQuantity());
		op.setUnitPrice(product.getPrice());
		updatedProducts.add(op);

		totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(prodReq.getQuantity())));
	    }

	    order.setOrderProducts(updatedProducts);
	    order.setTotalAmount(totalAmount);

	    Order savedOrder = orderRepository.save(order);
	    orderProductRepository.saveAll(updatedProducts);

	    return orderMapper.toOrderResponse(savedOrder);
	}


    @Override
    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Pedido no encontrado"));

        UserApp user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Usuario no encontrado"));

        // Si no es admin, validar que solo pueda ver su propio pedido
        if (!user.isAdmin() && !order.getUser().getId().equals(userId)) {
            throw new ValidationException("No puede acceder a pedidos de otros usuarios");
        }

        return orderMapper.toOrderResponse(order);
    }

    @Override
    public Page<OrderResponse> getOrdersByUser(Long userId, OrderFilter filter, Pageable pageable) {
        // Validar rango de fechas
        if (!filter.isDateRangeValid()) {
            throw new ValidationException("Rango de fechas inválido");
        }
        Specification<Order> spec = OrderSpecifications.forUserFilter(userId, filter);
        return orderRepository.findAll(spec, pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Override
    public Page<OrderResponse> getOrders(OrderAdminFilter filter, Pageable pageable) {
        if (!filter.isDateRangeValid()) {
            throw new ValidationException("Rango de fechas inválido");
        }
        Specification<Order> spec = OrderSpecifications.forAdminFilter(filter);
        return orderRepository.findAll(spec, pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Pedido no encontrado"));

        UserApp user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Usuario no encontrado"));

        // No puede cancelar pedidos ajenos, salvo que sea admin
        if (!order.getUser().getId().equals(userId) && !user.isAdmin()) {
            throw new ValidationException("No puede cancelar pedidos de otros usuarios");
        }

        // Ya cancelado
        if (order.getStatus() == EStatusOrder.CANCELADO) {
            throw new ValidationException("El pedido ya está cancelado");
        }

        // Si no es admin, solo puede cancelar pedidos PENDIENTES
        if (!user.isAdmin() && order.getStatus() != EStatusOrder.PENDIENTE) {
            throw new ValidationException("Solo puede cancelar pedidos en estado PENDIENTE");
        }

        // Devolver stock (tanto admin como cliente)
        for (OrderProduct op : order.getOrderProducts()) {
            Product p = op.getProduct();
            p.setStock(p.getStock() + op.getQuantity());
            productRepository.save(p);
        }

        order.setStatus(EStatusOrder.CANCELADO);
        orderRepository.save(order);
    }


    @Override
    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Pedido no encontrado"));

        if (order.getStatus() == EStatusOrder.CANCELADO) {
            throw new ValidationException("No se puede confirmar un pedido cancelado");
        }

        if (order.getStatus() == EStatusOrder.CONFIRMADO) {
            throw new ValidationException("El pedido ya está confirmado");
        }

        order.setStatus(EStatusOrder.CONFIRMADO);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void sendOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Pedido no encontrado"));

        if (order.getStatus() == EStatusOrder.CANCELADO) {
            throw new ValidationException("No se puede enviar un pedido cancelado");
        }

        if (order.getStatus() == EStatusOrder.ENVIADO) {
            throw new ValidationException("El pedido ya está enviado");
        }

        order.setStatus(EStatusOrder.ENVIADO);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deliverOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Pedido no encontrado"));

        if (order.getStatus() == EStatusOrder.CANCELADO) {
            throw new ValidationException("No se puede entregar un pedido cancelado");
        }

        if (order.getStatus() == EStatusOrder.ENTREGADO) {
            throw new ValidationException("El pedido ya está entregado");
        }

        order.setStatus(EStatusOrder.ENTREGADO);
        orderRepository.save(order);
    }


        private void validateNoDuplicateProducts(List<OrderProductRequest> items) {
        Set<Long> productIds = new HashSet<>();
        for (OrderProductRequest item : items) {
            if (!productIds.add(item.getProductId())) {
                throw new ValidationException("No se puede repetir el producto con ID " + item.getProductId() + " en el pedido.");
            }
        }
    }


}
