package com.example.cdas.services;

import com.example.cdas.models.CDASUser;
import com.example.cdas.models.Hospital;
import com.example.cdas.models.UserHospital;
import com.example.cdas.models.UserReport;
import com.example.cdas.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class UserReportService {
    @Autowired
    private UserReportRepository userReportRepository;

    @Autowired
    private UserRepository userRepository;



    public List<UserReport> findReportsByUser(CDASUser user) {
        return userReportRepository.findByUser(user);
    }
    public List<UserReport> findAllReports() {
        return userReportRepository.findAll();

    }
    public List<UserReport> findAllReports(String edrpou, String region, String startPeriod, String endPeriod) {
        List<UserReport> reports = findAllReports();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.yyyy", Locale.getDefault());
        YearMonth startDate = null;
        YearMonth endDate = null;

        try {
            if (startPeriod != null && !startPeriod.isEmpty()) {
                startDate = YearMonth.parse(startPeriod, formatter);
            }
            if (endPeriod != null && !endPeriod.isEmpty()) {
                endDate = YearMonth.parse(endPeriod, formatter);
            }
        } catch (DateTimeParseException e) {
            return List.of();
        }

        final YearMonth finalStartDate = startDate;
        final YearMonth finalEndDate = endDate;

        if (edrpou != null && !edrpou.isEmpty()) {
            reports = reports.stream()
                    .filter(report -> report.getReport().getEstablishmentCode().equals(edrpou))
                    .collect(Collectors.toList());
        }

        if (region != null && !region.isEmpty()) {
            reports = reports.stream()
                    .filter(report -> region.equals(report.getReport().getRegion()))
                    .collect(Collectors.toList());
        }

        if (finalStartDate != null || finalEndDate != null) {
            reports = reports.stream()
                    .filter(report -> {
                        YearMonth reportDate;
                        try {
                            reportDate = YearMonth.parse(report.getReport().getReportMonth(), formatter);
                        } catch (DateTimeParseException e) {
                            return false;
                        }
                        boolean afterStart = finalStartDate == null || reportDate.equals(finalStartDate) || reportDate.isAfter(finalStartDate);
                        boolean beforeEnd = finalEndDate == null || reportDate.equals(finalEndDate) || reportDate.isBefore(finalEndDate);
                        return afterStart && beforeEnd;
                    })
                    .collect(Collectors.toList());
        }

        return reports;
    }


    public CDASUser findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }
}
