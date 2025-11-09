package com.bionicpro.reports.controller;

import com.bionicpro.reports.model.ApiResponse;
import com.bionicpro.reports.model.CustomerReport;
import com.bionicpro.reports.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/user-report")
    public ResponseEntity<ApiResponse<CustomerReport>> getCustomerReport(JwtAuthenticationToken authentication) {

        Jwt jwt = authentication.getToken();
        String username = jwt.getClaimAsString("preferred_username");

        Optional<CustomerReport> report = reportService.getCustomerReport(username);

        return report.map(customerReport -> ResponseEntity.ok(ApiResponse.success(customerReport))).orElseGet(()
                -> ResponseEntity.ok(ApiResponse.error("Отчет для пользователя " + username + " не найден")));
    }

    @GetMapping("/user-report/history")
    public ResponseEntity<ApiResponse<List<CustomerReport>>> getCustomerReportHistory(
            JwtAuthenticationToken authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Jwt jwt = authentication.getToken();
        String username = jwt.getClaimAsString("preferred_username");

        List<CustomerReport> reports = reportService.getCustomerReportHistory(
                username,
                startDate.toString(),
                endDate.toString()
        );

        if (!reports.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(reports));
        } else {
            return ResponseEntity.ok(ApiResponse.error("Отчеты для пользователя " + username + " за указанный период не найдены"));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Service is healthy"));
    }
}