package com.bionicpro.reports.repository;

import com.bionicpro.reports.model.CustomerReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class ReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<CustomerReport> reportRowMapper = (rs, rowNum) -> new CustomerReport(
            rs.getLong("mart_id"),
            rs.getInt("customer_id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("customer_role"),
            rs.getDate("report_date").toLocalDate(),
            rs.getInt("total_usage_minutes"),
            rs.getDouble("avg_daily_usage_minutes"),
            rs.getInt("total_movements"),
            rs.getDouble("avg_success_rate"),
            rs.getDouble("avg_response_time_ms"),
            rs.getInt("total_errors"),
            rs.getDouble("avg_battery_level"),
            rs.getDouble("avg_signal_quality"),
            rs.getDate("registration_date").toLocalDate(),
            rs.getString("product_type"),
            rs.getInt("total_orders"),
            rs.getBigDecimal("total_order_amount"),
            rs.getDouble("usage_efficiency_score"),
            rs.getDouble("movement_accuracy_score"),
            rs.getTimestamp("last_updated").toLocalDateTime()
    );

    public Optional<CustomerReport> findLatestByUsername(String username) {
        String sql = """
            SELECT * FROM customer_telemetry_mart 
            WHERE username = ? 
            ORDER BY report_date DESC, last_updated DESC 
            LIMIT 1
            """;

        try {
            List<CustomerReport> results = jdbcTemplate.query(sql, reportRowMapper, username);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    public List<CustomerReport> findByUsernameAndDateRange(String username, String startDate, String endDate) {
        String sql = """
            SELECT * FROM customer_telemetry_mart 
            WHERE username = ? AND report_date BETWEEN ? AND ?
            ORDER BY report_date DESC
            """;

        try {
            return jdbcTemplate.query(sql, reportRowMapper, username, startDate, endDate);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return List.of();
        }
    }
}