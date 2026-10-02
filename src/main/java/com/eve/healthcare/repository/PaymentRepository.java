package com.eve.healthcare.repository;

import com.eve.healthcare.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByEventId(String eventId);
    Optional<Payment> findByPaymentId(String paymentId);
    boolean existsByBookingIdAndStatus(Long bookingId, com.eve.healthcare.entity.PaymentStatus status);
}
