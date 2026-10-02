package com.eve.healthcare.service;

import com.eve.healthcare.dto.PaymentDtos;
import com.eve.healthcare.entity.*;
import com.eve.healthcare.repository.BookingRepository;
import com.eve.healthcare.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock BookingRepository bookings;
    @Mock PaymentRepository payments;
    @InjectMocks PaymentService service;

    @Test
    void duplicateWebhookEventDoesNotCreateAnotherPayment() {
        Booking booking = new Booking();
        Payment existing = new Payment();
        existing.setBooking(booking);
        existing.setPaymentId("pay_1");
        existing.setEventId("evt_1");
        existing.setStatus(PaymentStatus.SUCCESS);

        when(payments.findByEventId("evt_1")).thenReturn(Optional.of(existing));

        PaymentDtos.PaymentResponse response = service.handleWebhook(
                new PaymentDtos.WebhookRequest("evt_1", "pay_1", 99L, PaymentStatus.SUCCESS));

        assertEquals("pay_1", response.paymentId());
        assertEquals(PaymentStatus.SUCCESS, response.status());
        verify(payments, never()).saveAndFlush(any());
        verify(bookings, never()).findById(any());
    }

    @Test
    void successfulWebhookConfirmsBooking() {
        Booking booking = new Booking();
        booking.setAmount(new BigDecimal("500.00"));
        booking.setStatus(BookingStatus.PENDING);
        when(payments.findByEventId("evt_2")).thenReturn(Optional.empty());
        when(payments.findByPaymentId("pay_2")).thenReturn(Optional.empty());
        when(bookings.findById(10L)).thenReturn(Optional.of(booking));
        when(payments.saveAndFlush(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.handleWebhook(new PaymentDtos.WebhookRequest(
                "evt_2", "pay_2", 10L, PaymentStatus.SUCCESS));

        assertEquals(PaymentStatus.SUCCESS, response.status());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(payments).saveAndFlush(any(Payment.class));
    }
}
