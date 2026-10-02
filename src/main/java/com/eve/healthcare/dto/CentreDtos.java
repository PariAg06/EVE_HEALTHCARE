package com.eve.healthcare.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public final class CentreDtos {
    private CentreDtos() {}

    public record TestRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 500) String description,
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal price) {}

    public record CentreRequest(
            @NotBlank @Size(max = 150) String name,
            @NotBlank @Size(max = 250) String location,
            @Valid List<TestRequest> tests) {}

    public record TestResponse(Long id, String name, String description, BigDecimal price) {}

    public record CentreResponse(Long id, String name, String location, List<TestResponse> tests) {}
}
