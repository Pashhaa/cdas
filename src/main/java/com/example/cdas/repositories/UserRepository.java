package com.example.cdas.repositories;

import com.example.cdas.models.CDASUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<CDASUser, Long> {
    CDASUser findUserByEmail(String email);
    List<CDASUser> findByRole(String role);

}
