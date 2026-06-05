package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.AppOrder;
import org.example.domain.AppUser;
import org.example.domain.PaymentIntent;
import org.example.domain.PaymentMethod;
import org.example.domain.enums.OrderStatus;
import org.example.domain.enums.PaymentIntentStatus;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.AppOrderRepository;
import org.example.repository.PaymentIntentRepository;
import org.example.repository.PaymentMethodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.dto.blockchain.EscrowDealBlockchainData;
import java.time.LocalDateTime;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PaymentIntentService {

    private static final Pattern TX_HASH_PATTERN = Pattern.compile("^0x[a-fA-F0-9]{64}$");
    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final AppOrderRepository appOrderRepository;
    private final BlockchainEscrowService blockchainEscrowService;
    private final OrderService orderService;

    @Transactional
    public PaymentIntent createPaymentIntent(
            AppUser currentUser,
            Long orderId,
            Long paymentMethodId
    ) {
        AppOrder order = appOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Only buyer can create payment intent");
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new IllegalArgumentException("Payment intent can be created only for CREATED order");
        }

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found: " + paymentMethodId));

        if (!Boolean.TRUE.equals(paymentMethod.getEnabled())) {
            throw new IllegalArgumentException("Payment method is disabled");
        }

        if ("NOT_DEPLOYED_YET".equals(paymentMethod.getEscrowContract())) {
            throw new IllegalArgumentException("Escrow contract is not deployed yet");
        }

        paymentIntentRepository.findByOrder_Id(orderId)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Payment intent already exists for order: " + orderId);
                });

        BigDecimal amount = order.getTotalPrice();

        if (amount.compareTo(paymentMethod.getMinAmount()) < 0) {
            throw new IllegalArgumentException("Amount is less than min payment amount");
        }

        String amountAtomic = toAtomicAmount(amount, paymentMethod.getDecimals());

        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setOrder(order);
        paymentIntent.setPaymentMethod(paymentMethod);
        paymentIntent.setBuyerId(order.getBuyer().getId());
        paymentIntent.setSellerId(order.getSeller().getId());
        paymentIntent.setAmount(amount);
        paymentIntent.setAmountAtomic(amountAtomic);
        paymentIntent.setChainId(paymentMethod.getChainId());
        paymentIntent.setTokenContract(paymentMethod.getTokenContract());
        paymentIntent.setEscrowContract(paymentMethod.getEscrowContract());
        paymentIntent.setStatus(PaymentIntentStatus.WAITING_WALLET);

        return paymentIntentRepository.save(paymentIntent);
    }

    @Transactional(readOnly = true)
    public PaymentIntent findById(Long paymentIntentId) {
        return paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment intent not found: " + paymentIntentId));
    }

    @Transactional(readOnly = true)
    public PaymentIntent findByOrderId(Long orderId) {
        return paymentIntentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment intent not found for order: " + orderId));
    }

    @Transactional
    public PaymentIntent verifyBlockchainPayment(Long paymentIntentId) {
        PaymentIntent paymentIntent = paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment intent not found: " + paymentIntentId));

        if (paymentIntent.getStatus() != PaymentIntentStatus.TX_SUBMITTED) {
            throw new IllegalArgumentException("Payment intent is not submitted to blockchain");
        }

        if (paymentIntent.getEscrowDealId() == null || paymentIntent.getEscrowDealId().isBlank()) {
            throw new IllegalArgumentException("Escrow deal id is missing");
        }

        if (paymentIntent.getPaymentTxHash() == null || paymentIntent.getPaymentTxHash().isBlank()) {
            throw new IllegalArgumentException("Payment tx hash is missing");
        }

        if (!TX_HASH_PATTERN.matcher(paymentIntent.getPaymentTxHash()).matches()) {
            throw new IllegalArgumentException("Invalid payment tx hash format");
        }

        blockchainEscrowService.verifyPaymentTransactionReceipt(paymentIntent);

        EscrowDealBlockchainData deal = blockchainEscrowService.getDeal(paymentIntent);

        BigInteger paidStatus = BigInteger.valueOf(2);

        if (!deal.status().equals(paidStatus)) {
            throw new IllegalArgumentException("Escrow deal is not PAID on blockchain");
        }

        if (!deal.id().toString().equals(paymentIntent.getEscrowDealId())) {
            throw new IllegalArgumentException("Escrow deal id mismatch");
        }

        if (!normalizeAddress(deal.token()).equals(normalizeAddress(paymentIntent.getTokenContract()))) {
            throw new IllegalArgumentException("Token contract mismatch");
        }

        if (!deal.amount().toString().equals(paymentIntent.getAmountAtomic())) {
            throw new IllegalArgumentException("Amount mismatch");
        }


        paymentIntent.setStatus(PaymentIntentStatus.PAID);
        paymentIntent.setPaidAt(LocalDateTime.now());

        AppOrder order = paymentIntent.getOrder();
        order.setStatus(OrderStatus.PAID);
        order.setEscrowContract(paymentIntent.getEscrowContract());
        order.setEscrowDealId(paymentIntent.getEscrowDealId());
        order.setPaymentTxHash(paymentIntent.getPaymentTxHash());
        order.setPaidAt(paymentIntent.getPaidAt());

        return paymentIntentRepository.save(paymentIntent);
    }

    private String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }

        return address.trim().toLowerCase();
    }

    @Transactional
    public PaymentIntent submitBlockchainPaymentTx(
            AppUser currentUser,
            Long paymentIntentId,
            String escrowDealId,
            String paymentTxHash
    ) {
        PaymentIntent paymentIntent = paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment intent not found: " + paymentIntentId));

        if (!paymentIntent.getBuyerId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Only buyer can submit payment transaction");
        }

        if (paymentIntent.getStatus() != PaymentIntentStatus.WAITING_WALLET) {
            throw new IllegalArgumentException("Payment intent is not waiting for wallet");
        }

        if (escrowDealId == null || escrowDealId.isBlank()) {
            throw new IllegalArgumentException("Escrow deal id is required");
        }

        try {
            new BigInteger(escrowDealId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Escrow deal id must be numeric");
        }

        if (paymentTxHash == null || paymentTxHash.isBlank()) {
            throw new IllegalArgumentException("Payment tx hash is required");
        }

        if (!TX_HASH_PATTERN.matcher(paymentTxHash).matches()) {
            throw new IllegalArgumentException("Invalid payment tx hash format");
        }

        if (paymentIntentRepository.existsByPaymentTxHash(paymentTxHash)) {
            throw new IllegalArgumentException("Payment transaction already used");
        }

        paymentIntent.setEscrowDealId(escrowDealId);
        paymentIntent.setPaymentTxHash(paymentTxHash);
        paymentIntent.setStatus(PaymentIntentStatus.TX_SUBMITTED);

        return paymentIntentRepository.save(paymentIntent);
    }

    private String toAtomicAmount(BigDecimal amount, Integer decimals) {
        BigDecimal multiplier = BigDecimal.TEN.pow(decimals);
        BigDecimal atomicDecimal = amount.multiply(multiplier).stripTrailingZeros();

        try {
            return atomicDecimal.toBigIntegerExact().toString();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Amount has too many decimals for selected token");
        }
    }
}