package com.example.cdas;

import com.example.cdas.models.CDASUser;
import com.example.cdas.models.UserHospital;
import com.example.cdas.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class CdasApplication implements CommandLineRunner {

    public static void main ( String[] args ) {SpringApplication.run ( CdasApplication.class , args );}

    @Override
    public void run(String... args) throws Exception{
    }
}
