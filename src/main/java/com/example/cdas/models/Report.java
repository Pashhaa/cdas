package com.example.cdas.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



import java.time.LocalDate;
import java.util.Date;


@Data
@Entity
@Table(name = "reports")
@AllArgsConstructor
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true,nullable = false)
    private Long id;

    @Column(name = "report_name",unique = true, nullable = false, length = 50)
    private String reportName;

    @Column(name = "report_month", nullable = false, length = 20)
    private String reportMonth;

    @Column(name = "region", nullable = false, length = 30)
    private String region;

    @Column(name = "establishment_code", nullable = false, length = 10)
    private String establishmentCode;

    @Column(name = "rest_of_establishment_funds", nullable = false)
    private double restOfEstablishFunds;

    @Column(name = "salary_expenses", nullable = false)
    private double salaryExpenses;

    @Column(name = "head_salary", nullable = false)
    private double headSalary;

    @Column(name = "subdivision_head_salary", nullable = false)
    private double subDivisionHeadSalary;

    @Column(name = "doctor_salary", nullable = false)
    private double doctorSalary;

    @Column(name = "middle_medical_staff_salary", nullable = false)
    private double middleMedicalStaffSalary;

    @Column(name = "junior_medical_staff_salary", nullable = false)
    private double juniorMedicalStaffSalary;

    @Column(name = "other_staff_salary", nullable = false)
    private double otherStaffSalary;

    @Column(name = "salary_expenses_for_report_month", nullable = false)
    private double salaryExpensesForReportMonth;

    @Column(name = "actual_employee_number", nullable = false)
    private double actualEmployeeNumber;

    @Column(name = "actual_heads_number", nullable = false)
    private double actualHeadsNumber;

    @Column(name = "actual_subdivision_heads_number", nullable = false)
    private double actualSubdivisionsHeadsNumber;

    @Column(name = "doctor_number", nullable = false)
    private double doctorsNumber;

    @Column(name = "middle_medical_staff_number", nullable = false)
    private double middleMedicalStaffNumber;

    @Column(name = "junior_medical_staff_number", nullable = false)
    private double juniorMedicalStaffNumber;

    @Column(name = "other_staff_number", nullable = false)
    private double otherStaffNumber;

    @Column(name = "salary_calculation_status", nullable = false)
    private double salaryCalculationStatus;

    @Column(name = "approximate_term_of_payment", nullable = false)
    private String approximateTermOfPayment;

    @Column(name = "date_of_report_creatation", nullable = false)
    private Date dateOfReportCreation;
    @Lob
    @Column(name = "report_file", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] reportFile;
}
