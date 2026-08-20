package com.bfsi.risk.web;

import com.bfsi.risk.dto.CategoryCount;
import com.bfsi.risk.dto.IncidentFlag;
import com.bfsi.risk.rules.IncidentAnalysisService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin(origins = "http://localhost:4200")
public class IncidentController {

    private final IncidentAnalysisService incidentAnalysisService;

    public IncidentController(IncidentAnalysisService incidentAnalysisService) {
        this.incidentAnalysisService = incidentAnalysisService;
    }

    @GetMapping("/sla-breaches")
    public List<IncidentFlag> slaBreaches() {
        return incidentAnalysisService.slaBreaches();
    }

    @GetMapping("/status-date-conflicts")
    public List<IncidentFlag> statusDateConflicts() {
        return incidentAnalysisService.statusDateConflicts();
    }

    @GetMapping("/module-hotspots")
    public List<CategoryCount> moduleHotspots(@RequestParam(defaultValue = "10") int limit) {
        return incidentAnalysisService.moduleHotspots(limit).stream()
                .map(e -> new CategoryCount(e.getKey(), e.getValue()))
                .toList();
    }

    @GetMapping("/top-root-causes")
    public List<CategoryCount> topRootCauses(@RequestParam(defaultValue = "10") int limit) {
        return incidentAnalysisService.topRootCauses(limit).stream()
                .map(e -> new CategoryCount(e.getKey(), e.getValue()))
                .toList();
    }
}
