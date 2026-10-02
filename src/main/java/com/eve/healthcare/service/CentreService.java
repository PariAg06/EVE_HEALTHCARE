package com.eve.healthcare.service;

import com.eve.healthcare.dto.CentreDtos;
import com.eve.healthcare.entity.DiagnosticCentre;
import com.eve.healthcare.entity.DiagnosticTest;
import com.eve.healthcare.exception.NotFoundException;
import com.eve.healthcare.repository.DiagnosticCentreRepository;
import com.eve.healthcare.repository.DiagnosticTestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CentreService {
    private final DiagnosticCentreRepository centres;
    private final DiagnosticTestRepository tests;

    public CentreService(DiagnosticCentreRepository centres, DiagnosticTestRepository tests) {
        this.centres = centres;
        this.tests = tests;
    }

    @Transactional(readOnly = true)
    public List<CentreDtos.CentreResponse> listCentres() {
        return centres.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CentreDtos.CentreResponse getCentre(Long id) {
        return toResponse(findCentre(id));
    }

    @Transactional
    public CentreDtos.CentreResponse createCentre(CentreDtos.CentreRequest request) {
        DiagnosticCentre centre = new DiagnosticCentre();
        centre.setName(request.name().trim());
        centre.setLocation(request.location().trim());
        if (request.tests() != null) {
            request.tests().forEach(item -> centre.addTest(toEntity(item)));
        }
        return toResponse(centres.save(centre));
    }

    @Transactional(readOnly = true)
    public List<CentreDtos.TestResponse> listTests(Long centreId) {
        findCentre(centreId);
        return tests.findByCentreId(centreId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public CentreDtos.TestResponse addTest(Long centreId, CentreDtos.TestRequest request) {
        DiagnosticCentre centre = findCentre(centreId);
        DiagnosticTest test = toEntity(request);
        test.setCentre(centre);
        return toResponse(tests.save(test));
    }

    private DiagnosticCentre findCentre(Long id) {
        return centres.findById(id).orElseThrow(() -> new NotFoundException("Diagnostic centre not found: " + id));
    }

    private DiagnosticTest toEntity(CentreDtos.TestRequest request) {
        DiagnosticTest test = new DiagnosticTest();
        test.setName(request.name().trim());
        test.setDescription(request.description());
        test.setPrice(request.price());
        return test;
    }

    private CentreDtos.CentreResponse toResponse(DiagnosticCentre centre) {
        return new CentreDtos.CentreResponse(centre.getId(), centre.getName(), centre.getLocation(),
                centre.getTests().stream().map(this::toResponse).toList());
    }

    private CentreDtos.TestResponse toResponse(DiagnosticTest test) {
        return new CentreDtos.TestResponse(test.getId(), test.getName(), test.getDescription(), test.getPrice());
    }
}
