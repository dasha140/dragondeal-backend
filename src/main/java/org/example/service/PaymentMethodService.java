package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.PaymentMethod;
import org.example.repository.PaymentMethodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.exception.ResourceNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    @Transactional(readOnly = true)
    public List<PaymentMethod> findEnabledPaymentMethods() {
        return paymentMethodRepository.findByEnabledTrueOrderBySortOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<PaymentMethod> findAllPaymentMethods() {
        return paymentMethodRepository.findAll();
    }

    @Transactional
    public PaymentMethod updateContracts(
            Long paymentMethodId,
            String tokenContract,
            String escrowContract,
            Boolean enabled
    ) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found: " + paymentMethodId));

        paymentMethod.setTokenContract(tokenContract);
        paymentMethod.setEscrowContract(escrowContract);

        if (enabled != null) {
            paymentMethod.setEnabled(enabled);
        }

        return paymentMethodRepository.save(paymentMethod);
    }
}