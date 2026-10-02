package com.eve.healthcare.service;

import com.eve.healthcare.dto.PaymentDtos;
import com.eve.healthcare.entity.*;
import com.eve.healthcare.exception.BadRequestException;
import com.eve.healthcare.exception.NotFoundException;
import com.eve.healthcare.repository.BookingRepository;
import com.eve.healthcare.repository.PaymentRepository;
import com.eve.healthcare.repository.UserRepository;
import com.eve.healthcare.exception.ForbiddenException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService {
    private final BookingRepository bookings;
    private final PaymentRepository payments;
    private final UserRepository users;

    public PaymentService(BookingRepository bookings, PaymentRepository payments, UserRepository users) {
        this.bookings = bookings;
        this.payments = payments;
        this.users = users;
    }

    @Transactional
    public PaymentDtos.PaymentResponse simulate(String email, Long bookingId) {
        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User account not found"));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You cannot pay for this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cannot pay for a cancelled booking");
        }
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new BadRequestException("Booking is already confirmed");
        }
        if (payments.existsByBookingIdAndStatus(bookingId, PaymentStatus.SUCCESS)) {
            throw new BadRequestException("A successful payment already exists for this booking");
        }

        PaymentStatus status = ThreadLocalRandom.current().nextBoolean()
                ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentId("pay_" + UUID.randomUUID());
        payment.setEventId("sim_" + UUID.randomUUID());
        payment.setAmount(booking.getAmount());
        payment.setStatus(status);
        payments.save(payment);

        booking.setStatus(status == PaymentStatus.SUCCESS ? BookingStatus.CONFIRMED : BookingStatus.FAILED);
        return new PaymentDtos.PaymentResponse(payment.getPaymentId(), bookingId, status);
    }

    @Transactional
    public PaymentDtos.PaymentResponse handleWebhook(PaymentDtos.WebhookRequest request) {
        var existingEvent = payments.findByEventId(request.eventId());
        if (existingEvent.isPresent()) {
            Payment payment = existingEvent.get();
            return new PaymentDtos.PaymentResponse(payment.getPaymentId(), payment.getBooking().getId(), payment.getStatus());
        }

        Booking booking = bookings.findById(request.bookingId())
                .orElseThrow(() -> new NotFoundException("Booking not found: " + request.bookingId()));

        if (payments.findByPaymentId(request.paymentId()).isPresent()) {
            throw new BadRequestException("Payment ID has already been processed");
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentId(request.paymentId());
        payment.setEventId(request.eventId());
        payment.setAmount(booking.getAmount());
        payment.setStatus(request.status());
        try {
            payments.saveAndFlush(payment);
        } catch (DataIntegrityViolationException ex) {
            Payment sameEvent = payments.findByEventId(request.eventId())
                    .orElseThrow(() -> ex);
            return new PaymentDtos.PaymentResponse(sameEvent.getPaymentId(), sameEvent.getBooking().getId(), sameEvent.getStatus());
        }

        if (request.status() == PaymentStatus.SUCCESS) {
            booking.setStatus(BookingStatus.CONFIRMED);
        } else if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.FAILED);
        }
        return new PaymentDtos.PaymentResponse(payment.getPaymentId(), booking.getId(), payment.getStatus());
    }
}
