package baji.lab1.controller;

import baji.lab1.service.ExcelReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    @Autowired
    private ExcelReportService excelReportService;

    @GetMapping
    public String reportsPage() {
        return "admin/reports";
    }

    @GetMapping("/export")
    public void exportReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {

        byte[] report = excelReportService.generateReportByPeriod(startDate, endDate);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Отчёт_о_продажах_" + startDate + "_" + endDate + ".xlsx");
        response.getOutputStream().write(report);
    }
}