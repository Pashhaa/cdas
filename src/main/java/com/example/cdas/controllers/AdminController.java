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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    CDASUserDetailsService userDetailsService;
    @Autowired
    CDASUserService userService;
    @Autowired
    UserReportService userReportService;
    @Autowired
    ReportService reportService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Principal principal) {
        CDASUserDetails userDetails = (CDASUserDetails) userDetailsService.loadUserByUsername(principal.getName());
        model.addAttribute("user", userDetails);
        return "admin_dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model,
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
        return "admin_users";
    }


    @PostMapping("/users/toggleStatus/{id}")
    public String toggleUserStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/add")
    public String addUserForm(Model model) {
        model.addAttribute("code", "");
        return "add_user";
    }

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute("code") String code) {
        userService.addUser(code);
        return "redirect:/admin/users";
    }
    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        CDASUser user = userService.getUserById(id);
        Hospital hospital = userService.getHospitalByEmail(user.getEmail());
        model.addAttribute("user", user);
        model.addAttribute("hospital", hospital);
        return "edit_user";
    }

    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id,
                           @RequestParam("hospitalName") String hospitalName,
                           @RequestParam("email") String email,
                           @RequestParam("phone") String phone,
                           @RequestParam("code") String code) {
        CDASUser user = userService.getUserById(id);
        userService.updateUser(user, hospitalName, email, phone, code);
        return "redirect:/admin/users";
    }

    @GetMapping("/viewHospitalReports")
    public String viewHospitalReports(@RequestParam("userId") Long userId, Model model) {
        CDASUser user = userService.getUserById(userId);
        List<UserReport> userReports = userReportService.findReportsByUser(user);
        model.addAttribute("userReports", userReports);
        return "hospital_reports";
    }

    @GetMapping("/reports")
    public String viewAllReports(@RequestParam(value = "code", required = false) String code,
                                 @RequestParam(value = "region", required = false) String region,
                                 @RequestParam(value = "startPeriod", required = false) String startPeriod,
                                 @RequestParam(value = "endPeriod", required = false) String endPeriod,
                                 Model model) {
        List<UserReport> reports = userReportService.findAllReports(code, region, startPeriod, endPeriod);
        model.addAttribute("reports", reports);
        return "admin_reports";
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
        return reportService.downloadReports(code,region,startPeriod,endPeriod);
    }


    @PostMapping("/deleteReport")
    public String deleteReport(@RequestParam("reportId") Long reportId) {
        reportService.deleteReport(reportId);
        return "redirect:/admin/reports";
    }
}
