package org.example.mapper;

import org.example.domain.AppOrder;
import org.example.dto.response.OrderResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(AppOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getBuyer().getId(),
                order.getBuyer().getUsername(),
                order.getSeller().getId(),
                order.getSeller().getUsername(),
                order.getListing().getId(),
                order.getListing().getTitle(),
                order.getTotalPrice(),
                order.getCurrency(),
                order.getNetwork(),
                order.getStatus(),
                order.getEscrowContract(),
                order.getEscrowDealId(),
                order.getPaymentTxHash(),
                order.getReleaseTxHash(),
                order.getCreatedAt(),
                order.getPaidAt(),
                order.getSellerMarkedDoneAt(),
                order.getCompletedAt()
        );
    }

    public List<OrderResponse> toResponseList(List<AppOrder> orders) {
        return orders.stream()
                .map(this::toResponse)
                .toList();
    }
}