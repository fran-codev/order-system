package dev.francode.ordersystem.dto.order;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;

    private Long userId;

    private String userEmail;

    private BigDecimal totalAmount;

    private LocalDateTime date;

    private String status;

    private List<OrderProductResponse> orderProducts;
}
