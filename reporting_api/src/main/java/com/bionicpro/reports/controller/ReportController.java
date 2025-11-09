package com.bionicpro.reports.controller;

import com.bionicpro.reports.model.ApiResponse;
import com.bionicpro.reports.model.CustomerReport;
import com.bionicpro.reports.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports API", description = "API для получения отчетов по пользователям BionicPRO")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/customer/{username}")
    @Operation(summary = "Получить последний отчет по пользователю")
    public ResponseEntity<ApiResponse<CustomerReport>> getCustomerReport(
            @Parameter(description = "ID пользователя")
            @PathVariable String username) {

        Optional<CustomerReport> report = reportService.getCustomerReport(username);

        return report.map(customerReport -> ResponseEntity.ok(ApiResponse.success(customerReport))).orElseGet(()
                -> ResponseEntity.ok(ApiResponse.error("Отчет для пользователя " + username + " не найден")));
    }

    @GetMapping("/customer/{username}/history")
    @Operation(summary = "Получить историю отчетов по пользователю за период")
    public ResponseEntity<ApiResponse<List<CustomerReport>>> getCustomerReportHistory(
            @Parameter(description = "ID пользователя")
            @PathVariable String username,
            @Parameter(description = "Начальная дата (формат: YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Конечная дата (формат: YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

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
    @Operation(summary = "Проверка здоровья сервиса")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Service is healthy"));
    }
}