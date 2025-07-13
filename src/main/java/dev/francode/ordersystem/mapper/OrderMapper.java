package dev.francode.ordersystem.mapper;

import dev.francode.ordersystem.dto.order.*;
import dev.francode.ordersystem.entity.Order;
import dev.francode.ordersystem.entity.OrderProduct;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    OrderResponse toOrderResponse(Order order);

    @Mapping(source = "product.id", target = "productId")
    OrderProductResponse toOrderProductResponse(OrderProduct orderProduct);

    @Named("statusToString")
    default String statusToString(Enum<?> status) {
        return status == null ? null : status.name();
    }
}