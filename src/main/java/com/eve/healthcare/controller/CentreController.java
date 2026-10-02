package com.eve.healthcare.controller;

import com.eve.healthcare.dto.CentreDtos;
import com.eve.healthcare.service.CentreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/centres")
public class CentreController {
    private final CentreService centreService;

    public CentreController(CentreService centreService) { this.centreService = centreService; }

    @GetMapping
    public List<CentreDtos.CentreResponse> list() { return centreService.listCentres(); }

    @GetMapping("/{id}")
    public CentreDtos.CentreResponse get(@PathVariable Long id) { return centreService.getCentre(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CentreDtos.CentreResponse create(@Valid @RequestBody CentreDtos.CentreRequest request) {
        return centreService.createCentre(request);
    }

    @GetMapping("/{centreId}/tests")
    public List<CentreDtos.TestResponse> tests(@PathVariable Long centreId) {
        return centreService.listTests(centreId);
    }

    @PostMapping("/{centreId}/tests")
    @ResponseStatus(HttpStatus.CREATED)
    public CentreDtos.TestResponse addTest(@PathVariable Long centreId,
                                           @Valid @RequestBody CentreDtos.TestRequest request) {
        return centreService.addTest(centreId, request);
    }
}
