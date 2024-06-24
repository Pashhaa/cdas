package com.example.cdas.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.*;


@Entity
@Table(name="users")
@Data
public class CDASUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true,nullable = false)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String role;
    @Column(nullable = false)
    private boolean enabled;

    public CDASUser() {
        super();
    }

}

