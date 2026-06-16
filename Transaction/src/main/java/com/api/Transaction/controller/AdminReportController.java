package com.api.Transaction.controller;

import com.api.Transaction.dto.AdminPlanStatsDTO;
import com.api.Transaction.dto.MonthlyRevenueDTO;
import com.api.Transaction.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping("/plans")
    public ResponseEntity<List<AdminPlanStatsDTO>> getPlanStats() {
        return ResponseEntity.ok(adminReportService.getPlanStats());
    }

    @GetMapping("/revenue/monthly")
    public ResponseEntity<List<MonthlyRevenueDTO>> getMonthlyRevenue(
            @RequestParam(defaultValue = "6") int months) {
        if (months < 1) months = 1;
        if (months > 24) months = 24;
        return ResponseEntity.ok(adminReportService.getMonthlyRevenue(months));
    }

    @GetMapping("/sales/export")
    public ResponseEntity<byte[]> exportCsv() {
        String csv = adminReportService.buildCsv();
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        String filename = "ventas_" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(bytes.length)
                .body(bytes);
    }
}
