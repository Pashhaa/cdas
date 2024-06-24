package com.example.cdas.controllers;

import com.example.cdas.config.CDASUserDetails;
import com.example.cdas.models.CDASUser;
import com.example.cdas.models.Hospital;
import com.example.cdas.models.Report;
import com.example.cdas.models.UserReport;
import com.example.cdas.services.CDASUserDetailsService;
import com.example.cdas.services.CDASUserService;
import com.example.cdas.services.ReportService;
import com.example.cdas.services.UserReportService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/financier")
public class FinancierController {
    @Autowired
    private CDASUserService userService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserReportService userReportService;
    @Autowired
    CDASUserDetailsService userDetailsService;

    @GetMapping("/dashboard")
    public String financierDashboard(Model model, Principal principal) {
        CDASUserDetails userDetails = (CDASUserDetails) userDetailsService.loadUserByUsername(principal.getName());
        model.addAttribute("user", userDetails);
        return "financier_dashboard";
    }

    @GetMapping("/hospitals")
    public String viewHospitals(Model model,
                                @RequestParam(required = false) String region,
                                @RequestParam(required = false) String code,
                                @RequestParam(required = false) String status) {
        List<CDASUser> users = userService.filterHospitalUsers(region, code, status);
        List<Hospital> hospitals = userService.getAllHospitals();

        Map<String, Hospital> hospitalMap = new HashMap<>();
        for (Hospital hospital : hospitals) {
            hospitalMap.put(hospital.getEmail(), hospital);
        }

        model.addAttribute("users", users);
        model.addAttribute("hospitalMap", hospitalMap);
        model.addAttribute("regions", userService.getAllRegions());

        return "financier_hospitals";
    }


    @GetMapping("/viewHospitalReports")
    public String viewHospitalReports(@RequestParam("userId") Long userId, Model model) {
        CDASUser user = userService.getUserById(userId);
        List<UserReport> userReports = userReportService.findReportsByUser(user);
        model.addAttribute("userReports", userReports);
        return "financier_hospital_reports";
    }

    @GetMapping("/reports")
    public String viewAllReports(@RequestParam(value = "code", required = false) String code,
                                 @RequestParam(value = "region", required = false) String region,
                                 @RequestParam(value = "startPeriod", required = false) String startPeriod,
                                 @RequestParam(value = "endPeriod", required = false) String endPeriod,
                                 Model model) {
        List<UserReport> reports = userReportService.findAllReports(code, region, startPeriod, endPeriod);
        model.addAttribute("reports", reports);
        return "financier_reports";
    }

    @GetMapping("/downloadReport")
    public ResponseEntity<byte[]> downloadReport(@RequestParam("reportId") Long reportId) {
        return reportService.downloadReport(reportId);
    }
    @GetMapping("/downloadReports")
    public ResponseEntity<byte[]> downloadReports(@RequestParam(value = "code", required = false) String code,
                                                  @RequestParam(value = "region", required = false) String region,
                                                  @RequestParam(value = "startPeriod", required = false) String startPeriod,
                                                  @RequestParam(value = "endPeriod", required = false) String endPeriod) throws IOException {
        List<UserReport> reports = userReportService.findAllReports(code, region, startPeriod, endPeriod);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        for (UserReport report : reports) {
            ZipEntry zipEntry = new ZipEntry(report.getReport().getReportName());
            zipEntry.setSize(report.getReport().getReportFile().length);
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(report.getReport().getReportFile());
            zipOutputStream.closeEntry();
        }

        zipOutputStream.finish();
        zipOutputStream.close();

        byte[] zipBytes = byteArrayOutputStream.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reports.zip\"")
                .body(zipBytes);
    }


    @GetMapping("/consolidateReports")
    public String consolidateReportsForm() {
        return "consolidate_reports";
    }



    @GetMapping("/downloadConsolidatedReport")
    public ResponseEntity<byte[]> downloadConsolidatedReport(@RequestParam(value = "code", required = false) String code,
                                                             @RequestParam(value = "region", required = false) String region,
                                                             @RequestParam(value = "startPeriod", required = false) String startPeriod,
                                                             @RequestParam(value = "endPeriod", required = false) String endPeriod,
                                                             @RequestParam(value = "salaryMin", required = false) Double salaryMin,
                                                             @RequestParam(value = "salaryMax", required = false) Double salaryMax) throws IOException {
        return reportService.downloadConsolidatedReport(code,region,startPeriod,endPeriod,salaryMin,salaryMax);
    }

    @ExceptionHandler(IOException.class)
    public String handleNoRecordsFoundException(Model model) {
        model.addAttribute("message", "За вашими параметрами не знайдено жодного звіту");
        return "consolidate_reports";
    }
}
