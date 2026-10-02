package com.eve.healthcare.controller;

import com.eve.healthcare.dto.PaymentDtos;
import com.eve.healthcare.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) { this.paymentService = paymentService; }

    @PostMapping
    public PaymentDtos.PaymentResponse pay(Authentication authentication,
                                           @Valid @RequestBody PaymentDtos.PaymentRequest request) {
        return paymentService.simulate(authentication.getName(), request.bookingId());
    }

    @PostMapping("/webhook")
    public PaymentDtos.PaymentResponse webhook(@Valid @RequestBody PaymentDtos.WebhookRequest request) {
        return paymentService.handleWebhook(request);
    }
}
