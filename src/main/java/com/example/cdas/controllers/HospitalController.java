package com.example.cdas.controllers;

import com.example.cdas.config.CDASUserDetails;
import com.example.cdas.models.CDASUser;
import com.example.cdas.models.Report;
import com.example.cdas.models.UserReport;
import com.example.cdas.services.CDASUserDetailsService;
import com.example.cdas.services.ReportService;
import com.example.cdas.services.UserReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/hospital")
public class HospitalController {
    @Autowired
    CDASUserDetailsService userDetailsService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private UserReportService userReportService;
    @GetMapping("/dashboard")
    public String hospitalDashboard(Model model, Principal principal) {
        CDASUserDetails userDetails = (CDASUserDetails) userDetailsService.loadUserByUsername(principal.getName());
        model.addAttribute("user", userDetails);
        return "hospital_dashboard";
    }

    @GetMapping("/uploadReport")
    public String showUploadForm() {
        return "upload_report";
    }

    @PostMapping("/uploadReport")
    public String uploadReport(@RequestParam("file") MultipartFile file, Model model) {
        try {
            reportService.saveReport(file);
            model.addAttribute("message", "Отчет успешно загружен.");
        } catch (IOException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/hospital/dashboard";
    }

    @GetMapping("/myReports")
    public String viewMyReports(Model model) {
        CDASUser currentUser = getCurrentUser();
        List<UserReport> userReports = userReportService.findReportsByUser(currentUser);
        model.addAttribute("userReports", userReports);
        return "my_reports";
    }

    @GetMapping("/downloadReport")
    public ResponseEntity<byte[]> downloadReport(@RequestParam("reportId") Long reportId) {
        return reportService.downloadReport(reportId);
    }

    @PostMapping("/deleteReport")
    public String deleteReport(@RequestParam("reportId") Long reportId) {
        reportService.deleteReport(reportId);
        return "redirect:/hospital/myReports";
    }

    private CDASUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userReportService.findUserByEmail(username);
        }
        return null;
    }
}
