package com.example.cdas.repositories;

import com.example.cdas.models.Hospital;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRepository extends CrudRepository<Hospital, Long> {
    Optional<Hospital> findByEmail(String email);
    Optional<Hospital> findByCode(String code);
    List<Hospital> findByRegion(String region);
    List<Hospital> findByCodeContaining(String code);

    List<Hospital> findAll();
}
