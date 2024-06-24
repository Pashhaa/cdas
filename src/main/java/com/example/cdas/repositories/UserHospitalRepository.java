package com.example.cdas.repositories;

import com.example.cdas.models.Hospital;
import com.example.cdas.models.UserHospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserHospitalRepository extends JpaRepository<UserHospital, Long> {
    Optional<UserHospital> findByUserIdAndHospitalId(Long userId, Long hospitalId);
    Hospital findUserHospitalByUserId (Long userId);
    boolean existsByUserIdAndHospitalId(Long userId, Long hospitalId);
}
