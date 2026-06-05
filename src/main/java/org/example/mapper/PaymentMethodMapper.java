package org.example.mapper;

import org.example.domain.PaymentMethod;
import org.example.dto.response.PaymentMethodResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentMethodMapper {

    public PaymentMethodResponse toResponse(PaymentMethod paymentMethod) {
        return new PaymentMethodResponse(
                paymentMethod.getId(),
                paymentMethod.getCurrency(),
                paymentMethod.getNetwork(),
                paymentMethod.getChainId(),
                paymentMethod.getTokenContract(),
                paymentMethod.getEscrowContract(),
                paymentMethod.getDecimals(),
                paymentMethod.getMinAmount(),
                paymentMethod.getEnabled()
        );
    }

    public List<PaymentMethodResponse> toResponseList(List<PaymentMethod> paymentMethods) {
        return paymentMethods.stream()
                .map(this::toResponse)
                .toList();
    }
}