package com.eve.healthcare.service;

import com.eve.healthcare.dto.BookingDtos;
import com.eve.healthcare.entity.*;
import com.eve.healthcare.exception.BadRequestException;
import com.eve.healthcare.repository.BookingRepository;
import com.eve.healthcare.repository.DiagnosticCentreRepository;
import com.eve.healthcare.repository.DiagnosticTestRepository;
import com.eve.healthcare.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock BookingRepository bookings;
    @Mock UserRepository users;
    @Mock DiagnosticTestRepository tests;
    @Mock DiagnosticCentreRepository centres;
    @InjectMocks BookingService service;

    @Test
    void rejectsTestFromAnotherCentre() {
        User user = new User(); user.setEmail("a@a.com");
        DiagnosticCentre centreA = mock(DiagnosticCentre.class);
        DiagnosticCentre centreB = mock(DiagnosticCentre.class);
        when(centreA.getId()).thenReturn(1L);
        when(centreB.getId()).thenReturn(2L);
        DiagnosticTest test = new DiagnosticTest(); test.setCentre(centreB); test.setPrice(BigDecimal.TEN);

        when(users.findByEmailIgnoreCase("a@a.com")).thenReturn(Optional.of(user));
        when(centres.findById(1L)).thenReturn(Optional.of(centreA));
        when(tests.findById(2L)).thenReturn(Optional.of(test));

        var request = new BookingDtos.CreateBookingRequest(2L, 1L, Instant.now().plusSeconds(3600));
        assertThrows(BadRequestException.class, () -> service.create("a@a.com", request));
        verify(bookings, never()).save(any());
    }

    @Test
    void storesCurrentTestPriceAsBookingAmount() {
        User user = new User(); user.setEmail("a@a.com");
        DiagnosticCentre centre = mock(DiagnosticCentre.class);
        when(centre.getId()).thenReturn(1L);
        DiagnosticTest test = new DiagnosticTest(); test.setCentre(centre); test.setPrice(new BigDecimal("725.00"));

        when(users.findByEmailIgnoreCase("a@a.com")).thenReturn(Optional.of(user));
        when(centres.findById(1L)).thenReturn(Optional.of(centre));
        when(tests.findById(2L)).thenReturn(Optional.of(test));
        when(bookings.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create("a@a.com", new BookingDtos.CreateBookingRequest(2L, 1L, Instant.now().plusSeconds(3600)));
        assertEquals(new BigDecimal("725.00"), result.amount());
        assertEquals(BookingStatus.PENDING, result.status());
    }
}
