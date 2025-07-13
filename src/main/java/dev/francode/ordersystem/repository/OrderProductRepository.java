package dev.francode.ordersystem.repository;

import dev.francode.ordersystem.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
}
