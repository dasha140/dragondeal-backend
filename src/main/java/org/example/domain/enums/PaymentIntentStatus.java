package org.example.domain.enums;

public enum PaymentIntentStatus {
    CREATED,
    WAITING_WALLET,
    TX_SUBMITTED,
    PAID,
    EXPIRED,
    CANCELLED,
    FAILED
}