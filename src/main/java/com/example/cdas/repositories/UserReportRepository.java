package com.example.cdas.repositories;

import com.example.cdas.models.CDASUser;
import com.example.cdas.models.Report;
import com.example.cdas.models.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    List<UserReport> findByUser(CDASUser user);
    void deleteByReportId(Long id);
}
