package dev.francode.ordersystem.entity;

import dev.francode.ordersystem.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Pedidos_Productos")
public class OrderProduct extends BaseEntity<Long> {

    @ManyToOne
    @JoinColumn(name = "id_pedido", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Product product;

    @Column(name = "nombre_producto", nullable = false)
    private String productName;

    @Column(name = "cantidad", nullable = false)
    private Integer quantity;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
}