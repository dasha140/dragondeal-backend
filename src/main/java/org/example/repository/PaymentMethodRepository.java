package org.example.repository;

import org.example.domain.PaymentMethod;
import org.example.domain.enums.CryptoCurrency;
import org.example.domain.enums.CryptoNetwork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByEnabledTrueOrderBySortOrderAsc();

    Optional<PaymentMethod> findByCurrencyAndNetwork(
            CryptoCurrency currency,
            CryptoNetwork network
    );
}