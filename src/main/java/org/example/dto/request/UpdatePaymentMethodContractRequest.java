package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdatePaymentMethodContractRequest(
        @NotBlank String tokenContract,
        @NotBlank String escrowContract,
        Boolean enabled
) {
}