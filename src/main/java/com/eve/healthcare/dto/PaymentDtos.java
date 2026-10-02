package com.eve.healthcare.dto;

import com.eve.healthcare.entity.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class PaymentDtos {
    private PaymentDtos() {}

    public record PaymentRequest(@NotNull Long bookingId) {}

    public record PaymentResponse(
            String paymentId,
            Long bookingId,
            PaymentStatus status) {}

    public record WebhookRequest(
            @NotBlank String eventId,
            @NotBlank String paymentId,
            @NotNull Long bookingId,
            @NotNull PaymentStatus status) {}
}
