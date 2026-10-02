package com.eve.healthcare.service;

import com.eve.healthcare.dto.BookingDtos;
import com.eve.healthcare.entity.*;
import com.eve.healthcare.exception.BadRequestException;
import com.eve.healthcare.exception.ForbiddenException;
import com.eve.healthcare.exception.NotFoundException;
import com.eve.healthcare.repository.BookingRepository;
import com.eve.healthcare.repository.DiagnosticCentreRepository;
import com.eve.healthcare.repository.DiagnosticTestRepository;
import com.eve.healthcare.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookings;
    private final UserRepository users;
    private final DiagnosticTestRepository tests;
    private final DiagnosticCentreRepository centres;

    public BookingService(BookingRepository bookings, UserRepository users,
                          DiagnosticTestRepository tests, DiagnosticCentreRepository centres) {
        this.bookings = bookings;
        this.users = users;
        this.tests = tests;
        this.centres = centres;
    }

    @Transactional
    public BookingDtos.BookingResponse create(String email, BookingDtos.CreateBookingRequest request) {
        User user = currentUser(email);
        DiagnosticCentre centre = centres.findById(request.centreId())
                .orElseThrow(() -> new NotFoundException("Diagnostic centre not found: " + request.centreId()));
        DiagnosticTest test = tests.findById(request.testId())
                .orElseThrow(() -> new NotFoundException("Diagnostic test not found: " + request.testId()));
        if (!test.getCentre().getId().equals(centre.getId())) {
            throw new BadRequestException("The selected test is not offered by this centre");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCentre(centre);
        booking.setTest(test);
        booking.setAppointmentAt(request.appointmentAt());
        booking.setAmount(test.getPrice());
        booking.setStatus(BookingStatus.PENDING);
        return toResponse(bookings.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingDtos.BookingResponse> list(String email) {
        User user = currentUser(email);
        return bookings.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookingDtos.BookingResponse get(String email, Long id) {
        User user = currentUser(email);
        return toResponse(bookings.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Booking not found: " + id)));
    }

    @Transactional
    public Booking cancel(String email, Long id) {
        User user = currentUser(email);
        Booking booking = bookings.findById(id).orElseThrow(() -> new NotFoundException("Booking not found: " + id));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You cannot modify this booking");
        }
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new BadRequestException("A confirmed booking cannot be cancelled here");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return booking;
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return bookings.save(booking);
    }

    public User currentUser(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User account not found"));
    }

    private BookingDtos.BookingResponse toResponse(Booking booking) {
        return new BookingDtos.BookingResponse(
                booking.getId(), booking.getUser().getId(), booking.getTest().getId(), booking.getTest().getName(),
                booking.getCentre().getId(), booking.getCentre().getName(), booking.getAppointmentAt(),
                booking.getAmount(), booking.getStatus());
    }
}
