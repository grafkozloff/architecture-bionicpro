package com.bionicpro.reports.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerReport {
    private Long martId;
    private Integer customerId;
    private String username;
    private String email;
    private String customerRole;
    private LocalDate reportDate;
    private Integer totalUsageMinutes;
    private Double avgDailyUsageMinutes;
    private Integer totalMovements;
    private Double avgSuccessRate;
    private Double avgResponseTimeMs;
    private Integer totalErrors;
    private Double avgBatteryLevel;
    private Double avgSignalQuality;
    private LocalDate registrationDate;
    private String productType;
    private Integer totalOrders;
    private BigDecimal totalOrderAmount;
    private Double usageEfficiencyScore;
    private Double movementAccuracyScore;
    private LocalDateTime lastUpdated;

    public CustomerReport() {}

    public CustomerReport(Long martId, Integer customerId, String username, String email,
                          String customerRole, LocalDate reportDate, Integer totalUsageMinutes,
                          Double avgDailyUsageMinutes, Integer totalMovements, Double avgSuccessRate,
                          Double avgResponseTimeMs, Integer totalErrors, Double avgBatteryLevel,
                          Double avgSignalQuality, LocalDate registrationDate, String productType,
                          Integer totalOrders, BigDecimal totalOrderAmount, Double usageEfficiencyScore,
                          Double movementAccuracyScore, LocalDateTime lastUpdated) {
        this.martId = martId;
        this.customerId = customerId;
        this.username = username;
        this.email = email;
        this.customerRole = customerRole;
        this.reportDate = reportDate;
        this.totalUsageMinutes = totalUsageMinutes;
        this.avgDailyUsageMinutes = avgDailyUsageMinutes;
        this.totalMovements = totalMovements;
        this.avgSuccessRate = avgSuccessRate;
        this.avgResponseTimeMs = avgResponseTimeMs;
        this.totalErrors = totalErrors;
        this.avgBatteryLevel = avgBatteryLevel;
        this.avgSignalQuality = avgSignalQuality;
        this.registrationDate = registrationDate;
        this.productType = productType;
        this.totalOrders = totalOrders;
        this.totalOrderAmount = totalOrderAmount;
        this.usageEfficiencyScore = usageEfficiencyScore;
        this.movementAccuracyScore = movementAccuracyScore;
        this.lastUpdated = lastUpdated;
    }

    public Long getMartId() { return martId; }
    public void setMartId(Long martId) { this.martId = martId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCustomerRole() { return customerRole; }
    public void setCustomerRole(String customerRole) { this.customerRole = customerRole; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public Integer getTotalUsageMinutes() { return totalUsageMinutes; }
    public void setTotalUsageMinutes(Integer totalUsageMinutes) { this.totalUsageMinutes = totalUsageMinutes; }

    public Double getAvgDailyUsageMinutes() { return avgDailyUsageMinutes; }
    public void setAvgDailyUsageMinutes(Double avgDailyUsageMinutes) { this.avgDailyUsageMinutes = avgDailyUsageMinutes; }

    public Integer getTotalMovements() { return totalMovements; }
    public void setTotalMovements(Integer totalMovements) { this.totalMovements = totalMovements; }

    public Double getAvgSuccessRate() { return avgSuccessRate; }
    public void setAvgSuccessRate(Double avgSuccessRate) { this.avgSuccessRate = avgSuccessRate; }

    public Double getAvgResponseTimeMs() { return avgResponseTimeMs; }
    public void setAvgResponseTimeMs(Double avgResponseTimeMs) { this.avgResponseTimeMs = avgResponseTimeMs; }

    public Integer getTotalErrors() { return totalErrors; }
    public void setTotalErrors(Integer totalErrors) { this.totalErrors = totalErrors; }

    public Double getAvgBatteryLevel() { return avgBatteryLevel; }
    public void setAvgBatteryLevel(Double avgBatteryLevel) { this.avgBatteryLevel = avgBatteryLevel; }

    public Double getAvgSignalQuality() { return avgSignalQuality; }
    public void setAvgSignalQuality(Double avgSignalQuality) { this.avgSignalQuality = avgSignalQuality; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalOrderAmount() { return totalOrderAmount; }
    public void setTotalOrderAmount(BigDecimal totalOrderAmount) { this.totalOrderAmount = totalOrderAmount; }

    public Double getUsageEfficiencyScore() { return usageEfficiencyScore; }
    public void setUsageEfficiencyScore(Double usageEfficiencyScore) { this.usageEfficiencyScore = usageEfficiencyScore; }

    public Double getMovementAccuracyScore() { return movementAccuracyScore; }
    public void setMovementAccuracyScore(Double movementAccuracyScore) { this.movementAccuracyScore = movementAccuracyScore; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}