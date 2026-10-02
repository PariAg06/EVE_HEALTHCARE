package com.eve.healthcare.controller;

import com.eve.healthcare.dto.BookingDtos;
import com.eve.healthcare.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) { this.bookingService = bookingService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDtos.BookingResponse create(Authentication authentication,
                                               @Valid @RequestBody BookingDtos.CreateBookingRequest request) {
        return bookingService.create(authentication.getName(), request);
    }

    @GetMapping
    public List<BookingDtos.BookingResponse> list(Authentication authentication) {
        return bookingService.list(authentication.getName());
    }

    @GetMapping("/{id}")
    public BookingDtos.BookingResponse get(Authentication authentication, @PathVariable Long id) {
        return bookingService.get(authentication.getName(), id);
    }

    @PostMapping("/{id}/cancel")
    public BookingDtos.BookingResponse cancel(Authentication authentication, @PathVariable Long id) {
        bookingService.cancel(authentication.getName(), id);
        return bookingService.get(authentication.getName(), id);
    }
}
