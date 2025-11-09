package com.bionicpro.reports.service;

import com.bionicpro.reports.model.CustomerReport;
import com.bionicpro.reports.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Optional<CustomerReport> getCustomerReport(String username) {
        return reportRepository.findLatestByUsername(username);
    }

    public List<CustomerReport> getCustomerReportHistory(String username, String startDate, String endDate) {
        return reportRepository.findByUsernameAndDateRange(username, startDate, endDate);
    }
}