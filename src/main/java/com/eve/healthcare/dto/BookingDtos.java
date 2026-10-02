package com.eve.healthcare.dto;

import com.eve.healthcare.entity.BookingStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public final class BookingDtos {
    private BookingDtos() {}

    public record CreateBookingRequest(
            @NotNull Long testId,
            @NotNull Long centreId,
            @NotNull @Future Instant appointmentAt) {}

    public record BookingResponse(
            Long id,
            Long userId,
            Long testId,
            String testName,
            Long centreId,
            String centreName,
            Instant appointmentAt,
            BigDecimal amount,
            BookingStatus status) {}
}
