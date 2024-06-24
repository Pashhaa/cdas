package com.example.cdas.services;

import com.example.cdas.models.*;
import com.example.cdas.repositories.ReportRepository;
import com.example.cdas.repositories.UserHospitalRepository;
import com.example.cdas.repositories.UserReportRepository;
import com.example.cdas.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ReportService {
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserReportRepository userReportRepository;
    @Autowired
    private UserReportService userReportService;


    public void saveReport(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            throw new IllegalArgumentException("Невірний формат файлу");
        }
        byte[] fileContent = file.getBytes();

        Report report = readReportFromFile(fileContent);
        report.setReportName(filename);
        report.setReportFile(fileContent);
        report = reportRepository.save(report);

        CDASUser currentUser = getCurrentUser();
        UserReport userReport = new UserReport();
        userReport.setUser(currentUser);
        userReport.setReport(report);
        userReport.setUploadDate(new Date());
        userReportRepository.save(userReport);
    }


    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                switch (cell.getCachedFormulaResultType()) {
                    case STRING:
                        return cell.getRichStringCellValue().getString();
                    case NUMERIC:
                        return String.valueOf(cell.getNumericCellValue());
                    case BOOLEAN:
                        return String.valueOf(cell.getBooleanCellValue());
                    default:
                        return "";
                }
            default:
                return "";
        }
    }


    private Report readReportFromFile(byte[] fileContent) throws IOException {
        Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(fileContent));
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(3); // строка с данными, замените номер строки при необходимости

        Report report = new Report();
        report.setReportMonth(row.getCell(0).getStringCellValue());
        report.setRegion(getCellValueAsString(row.getCell(1)));
        report.setEstablishmentCode(getCellValueAsString(row.getCell(2)));
        report.setRestOfEstablishFunds(row.getCell(3).getNumericCellValue());
        report.setSalaryExpenses(row.getCell(4).getNumericCellValue());
        report.setHeadSalary(row.getCell(5).getNumericCellValue());
        report.setSubDivisionHeadSalary(row.getCell(6).getNumericCellValue());
        report.setDoctorSalary(row.getCell(7).getNumericCellValue());
        report.setMiddleMedicalStaffSalary(row.getCell(8).getNumericCellValue());
        report.setJuniorMedicalStaffSalary(row.getCell(9).getNumericCellValue());
        report.setOtherStaffSalary(row.getCell(10).getNumericCellValue());
        report.setSalaryExpensesForReportMonth(row.getCell(11).getNumericCellValue());
        report.setActualEmployeeNumber(row.getCell(12).getNumericCellValue());
        report.setActualHeadsNumber(row.getCell(13).getNumericCellValue());
        report.setActualSubdivisionsHeadsNumber(row.getCell(14).getNumericCellValue());
        report.setDoctorsNumber(row.getCell(15).getNumericCellValue());
        report.setMiddleMedicalStaffNumber(row.getCell(16).getNumericCellValue());
        report.setJuniorMedicalStaffNumber(row.getCell(17).getNumericCellValue());
        report.setOtherStaffNumber(row.getCell(18).getNumericCellValue());
        report.setSalaryCalculationStatus(row.getCell(19).getNumericCellValue());
        report.setApproximateTermOfPayment(getCellValueAsString(row.getCell(20)));
        report.setDateOfReportCreation(row.getCell(21).getDateCellValue());

        workbook.close();
        return report;
    }

    public ResponseEntity<byte[]> downloadReport(Long reportId) {
        Optional<Report> report = reportRepository.findById(reportId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.get().getReportName() + "\"")
                .body(report.get().getReportFile());
    }

    public ResponseEntity<byte[]> downloadReports(String code, String region, String startPeriod, String endPeriod) throws IOException {
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
    public ResponseEntity<byte[]> downloadConsolidatedReport(String code,
                                                             String region,
                                                             String startPeriod,
                                                             String endPeriod,
                                                             Double salaryMin,
                                                            Double salaryMax) throws IOException {
        List<UserReport> reports = userReportService.findAllReports(code, region, startPeriod, endPeriod);

        if(reports.isEmpty()){
            throw new IOException();
        }

        if (salaryMin != null || salaryMax != null) {
            reports = reports.stream().filter(report -> {
                double averageSalary = report.getReport().getSalaryExpenses() / report.getReport().getActualEmployeeNumber();
                return (salaryMin == null || averageSalary >= salaryMin) && (salaryMax == null || averageSalary <= salaryMax);
            }).collect(Collectors.toList());
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Консолідоваий звіт");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Період");
        headerRow.createCell(1).setCellValue("Область");
        headerRow.createCell(2).setCellValue("ЄДРПОУ");
        headerRow.createCell(3).setCellValue("Залишок коштів");
        headerRow.createCell(4).setCellValue("Витрати на оплату праці");
        headerRow.createCell(5).setCellValue("Керівники");
        headerRow.createCell(6).setCellValue("Ккерівники підрозділів");
        headerRow.createCell(7).setCellValue("Лікарі");
        headerRow.createCell(8).setCellValue("Серед мед персонал");
        headerRow.createCell(9).setCellValue("Молодш мед персонал");
        headerRow.createCell(10).setCellValue("Інші працівники");
        headerRow.createCell(11).setCellValue("Оплата праці пмг");
        headerRow.createCell(12).setCellValue("Кількість працівників");
        headerRow.createCell(13).setCellValue("Керівники");
        headerRow.createCell(14).setCellValue("Керівники підрозділів");
        headerRow.createCell(15).setCellValue("Лікарі");
        headerRow.createCell(16).setCellValue("Серед мед персонал");
        headerRow.createCell(17).setCellValue("Молодш мед персонал");
        headerRow.createCell(18).setCellValue("Інші працівники");
        headerRow.createCell(19).setCellValue("Стан виплаи зп");

        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(startPeriod != null && !startPeriod.isEmpty() ? startPeriod +" - "+ endPeriod : "-");
        dataRow.createCell(1).setCellValue(region != null && !region.isEmpty() ? region : "-");
        dataRow.createCell(2).setCellValue(code != null && !code.isEmpty() ? code : "-");

        double[] totals = new double[17];
        int reportCount = reports.size();

        for (UserReport report : reports) {
            Report r = report.getReport();
            totals[0] += r.getRestOfEstablishFunds();
            totals[1] += r.getSalaryExpenses();
            totals[2] += r.getHeadSalary();
            totals[3] += r.getSubDivisionHeadSalary();
            totals[4] += r.getDoctorSalary();
            totals[5] += r.getMiddleMedicalStaffSalary();
            totals[6] += r.getJuniorMedicalStaffSalary();
            totals[7] += r.getOtherStaffSalary();
            totals[8] += r.getSalaryExpensesForReportMonth();
            totals[9] += r.getActualEmployeeNumber();
            totals[10] += r.getActualHeadsNumber();
            totals[11] += r.getActualSubdivisionsHeadsNumber();
            totals[12] += r.getDoctorsNumber();
            totals[13] += r.getMiddleMedicalStaffNumber();
            totals[14] += r.getJuniorMedicalStaffNumber();
            totals[15] += r.getOtherStaffNumber();
            totals[16] += r.getSalaryCalculationStatus();
        }

        for (int i = 0; i < totals.length - 1; i++) {
            dataRow.createCell(i + 3).setCellValue(totals[i]);
        }

        if (reportCount > 0) {
            dataRow.createCell(19).setCellValue(totals[16] / reportCount);
        } else {
            dataRow.createCell(19).setCellValue(0);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        byte[] bytes = outputStream.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"consolidated_report.xlsx\"")
                .body(bytes);
    }
    public CDASUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findUserByEmail(username);
        }
        return null;
    }

    public Report getReportById(Long reportId) {
        return reportRepository.findById(reportId).orElseThrow(() -> new IllegalArgumentException("Отчет не найден"));
    }

    @Transactional
    public void deleteReport(Long reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new IllegalArgumentException("Отчет не найден"));
        userReportRepository.deleteByReportId(reportId);
        reportRepository.deleteById(report.getId());
    }

}