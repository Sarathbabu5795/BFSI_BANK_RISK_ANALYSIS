package com.bfsi.risk.rules;

import com.bfsi.risk.dto.IncidentFlag;
import com.bfsi.risk.loader.CsvDataStore;
import com.bfsi.risk.model.Incident;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BR-007 (SLA breach), BR-008 (status/date lifecycle conflict), and hotspot
 * analysis (repeated root causes / modules) from
 * docs/01_Data_Dictionary_and_Ground_Truth.xlsx.
 */
@Service
public class IncidentAnalysisService {

    private static final Set<String> RESOLVED_STATES = Set.of("RESOLVED", "CLOSED");

    private final CsvDataStore store;

    public IncidentAnalysisService(CsvDataStore store) {
        this.store = store;
    }

    /** BR-007: incidents whose SLA has been breached (per the dataset's sla_breached flag). */
    public List<IncidentFlag> slaBreaches() {
        List<IncidentFlag> flags = new ArrayList<>();
        for (Incident i : store.incidents()) {
            if ("Y".equals(i.slaBreached())) {
                flags.add(new IncidentFlag("BR-007", "SLA breached", i,
                        i.severity() + " incident (SLA " + i.slaHours() + "h) breached its resolution SLA"));
            }
        }
        return flags;
    }

    /** BR-008: RESOLVED/CLOSED incidents missing a resolved_datetime, or vice-versa. */
    public List<IncidentFlag> statusDateConflicts() {
        List<IncidentFlag> flags = new ArrayList<>();
        for (Incident i : store.incidents()) {
            boolean hasResolvedDate = i.resolvedDatetime() != null;
            boolean isResolvedStatus = RESOLVED_STATES.contains(i.incidentStatus());
            if (isResolvedStatus && !hasResolvedDate) {
                flags.add(new IncidentFlag("BR-008", "Status/date conflict", i,
                        "Incident status is " + i.incidentStatus() + " but resolved_datetime is missing"));
            } else if (!isResolvedStatus && hasResolvedDate) {
                flags.add(new IncidentFlag("BR-008", "Status/date conflict", i,
                        "resolved_datetime is set but incident status is still " + i.incidentStatus()));
            }
        }
        return flags;
    }

    /** Operational hotspots: application modules with the most incidents, ranked. */
    public List<Map.Entry<String, Long>> moduleHotspots(int limit) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Incident i : store.incidents()) {
            counts.merge(i.applicationModule(), 1L, Long::sum);
        }
        return counts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed())
                .limit(limit)
                .toList();
    }

    /** Root causes ranked by frequency, restricted to resolved incidents that recorded one. */
    public List<Map.Entry<String, Long>> topRootCauses(int limit) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Incident i : store.incidents()) {
            if (!i.rootCause().isBlank()) {
                counts.merge(i.rootCause(), 1L, Long::sum);
            }
        }
        return counts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed())
                .limit(limit)
                .toList();
    }
}
