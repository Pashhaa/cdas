package com.example.cdas.models;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "users_hospitals")
@Data
public class UserHospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "hospital_id", nullable = false)
    private Long hospitalId;
    public UserHospital() {}

    public UserHospital(Long userId, Long hospitalId) {
        this.userId = userId;
        this.hospitalId = hospitalId;
    }
}
