package com.bfsi.risk.web;

import com.bfsi.risk.dto.ApiLogFlag;
import com.bfsi.risk.dto.CategoryCount;
import com.bfsi.risk.rules.ApiLogAnalysisService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/api-logs")
@CrossOrigin(origins = "http://localhost:4200")
public class ApiLogController {

    private final ApiLogAnalysisService apiLogAnalysisService;

    public ApiLogController(ApiLogAnalysisService apiLogAnalysisService) {
        this.apiLogAnalysisService = apiLogAnalysisService;
    }

    @GetMapping("/slow")
    public List<ApiLogFlag> slow() {
        return apiLogAnalysisService.slowApiCalls();
    }

    @GetMapping("/failures")
    public List<ApiLogFlag> failures() {
        return apiLogAnalysisService.serverFailures();
    }

    @GetMapping("/top-failing")
    public List<CategoryCount> topFailing(@RequestParam(defaultValue = "10") int limit) {
        return apiLogAnalysisService.topFailingApis(limit).stream()
                .map(e -> new CategoryCount(e.getKey(), e.getValue()))
                .toList();
    }
}
