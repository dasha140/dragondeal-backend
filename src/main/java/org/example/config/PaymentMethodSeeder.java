package org.example.config;

import lombok.RequiredArgsConstructor;
import org.example.domain.PaymentMethod;
import org.example.domain.enums.CryptoCurrency;
import org.example.domain.enums.CryptoNetwork;
import org.example.repository.PaymentMethodRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PaymentMethodSeeder implements CommandLineRunner {

    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    public void run(String... args) {
        seedHardhatUsdc();
        seedHardhatUsdt();
    }

    private void seedHardhatUsdc() {
        PaymentMethod paymentMethod = paymentMethodRepository
                .findByCurrencyAndNetwork(CryptoCurrency.USDC, CryptoNetwork.HARDHAT_LOCAL)
                .orElseGet(PaymentMethod::new);

        paymentMethod.setCurrency(CryptoCurrency.USDC);
        paymentMethod.setNetwork(CryptoNetwork.HARDHAT_LOCAL);
        paymentMethod.setChainId(31337L);
        paymentMethod.setTokenContract("0x5FbDB2315678afecb367f032d93F642f64180aa3");
        paymentMethod.setEscrowContract("0x9fE46736679d2D9a65F0992F2272dE9f3c7fa6e0");
        paymentMethod.setDecimals(6);
        paymentMethod.setMinAmount(new BigDecimal("1.00000000"));
        paymentMethod.setEnabled(true);
        paymentMethod.setSortOrder(1);

        paymentMethodRepository.save(paymentMethod);
    }

    private void seedHardhatUsdt() {
        PaymentMethod paymentMethod = paymentMethodRepository
                .findByCurrencyAndNetwork(CryptoCurrency.USDT, CryptoNetwork.HARDHAT_LOCAL)
                .orElseGet(PaymentMethod::new);

        paymentMethod.setCurrency(CryptoCurrency.USDT);
        paymentMethod.setNetwork(CryptoNetwork.HARDHAT_LOCAL);
        paymentMethod.setChainId(31337L);
        paymentMethod.setTokenContract("0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512");
        paymentMethod.setEscrowContract("0x9fE46736679d2D9a65F0992F2272dE9f3c7fa6e0");
        paymentMethod.setDecimals(6);
        paymentMethod.setMinAmount(new BigDecimal("1.00000000"));
        paymentMethod.setEnabled(true);
        paymentMethod.setSortOrder(2);

        paymentMethodRepository.save(paymentMethod);
    }
}