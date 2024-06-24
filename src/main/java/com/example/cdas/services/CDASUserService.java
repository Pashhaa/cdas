package com.example.cdas.services;

import com.example.cdas.models.CDASUser;
import com.example.cdas.models.Hospital;
import com.example.cdas.models.UserHospital;
import com.example.cdas.repositories.HospitalRepository;
import com.example.cdas.repositories.UserHospitalRepository;
import com.example.cdas.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CDASUserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private UserHospitalRepository userHospitalRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    public void addUser(String edrpouCode) {
        Optional<Hospital> hospitalOptional = hospitalRepository.findByCode(edrpouCode);
        if (hospitalOptional.isPresent()) {
            Hospital hospital = hospitalOptional.get();
            Optional<CDASUser> existingUser = Optional.ofNullable(userRepository.findUserByEmail(hospital.getEmail()));

            if (existingUser.isPresent()) {
                CDASUser user = existingUser.get();
                if (userHospitalRepository.existsByUserIdAndHospitalId(user.getId(), hospital.getId())) {
                    throw new IllegalArgumentException("Госпиталь уже привязан к этому пользователю");
                }
            } else {
                CDASUser user = new CDASUser();
                user.setName(hospital.getName());
                user.setEmail(hospital.getEmail());
                String generatedPassword = generatePassword();
                user.setPassword(passwordEncoder.encode(generatedPassword));
                user.setRole("HOSPITAL");
                user.setEnabled(true);
                CDASUser savedUser = userRepository.save(user);

                UserHospital userHospital = new UserHospital(savedUser.getId(), hospital.getId());
                userHospitalRepository.save(userHospital);
                String subject = "Система консолідації і аналізу даних";
                String text = "Вітаємо, Ви були зареєстровані в системі консолідації і аналізу даних!\nВаш пароль для входу в систему: "+generatedPassword+"\n\nЗ найкращими побажаннями, команада технічної підтримки.";
                emailService.sendMessage(hospital.getEmail(), subject,text);
            }
        } else {
            throw new IllegalArgumentException("Код ЕДРПОУ не найден в базе данных госпиталей");
        }
    }

    private String generatePassword() {
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }


    public List<CDASUser> getAllHospitalUsers() {
        return userRepository.findByRole("HOSPITAL");
    }

    public void toggleUserStatus(Long id) {
        Optional<CDASUser> userOptional = userRepository.findById(id);
        userOptional.ifPresent(user -> {
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
        });
    }

    public List<CDASUser> filterHospitalUsers(String region, String edrpouCode, String status) {
        List<CDASUser> users = getAllHospitalUsers();
        List<Hospital> hospitals = (List<Hospital>) hospitalRepository.findAll();

        if (region != null && !region.isEmpty()) {
            hospitals = hospitals.stream()
                    .filter(hospital -> region.equals(hospital.getRegion()))
                    .collect(Collectors.toList());
        }

        if (edrpouCode != null && !edrpouCode.isEmpty()) {
            hospitals = hospitals.stream()
                    .filter(hospital -> hospital.getCode().contains(edrpouCode))
                    .collect(Collectors.toList());
        }

        List<String> hospitalEmails = hospitals.stream()
                .map(Hospital::getEmail)
                .collect(Collectors.toList());

        users = users.stream()
                .filter(user -> hospitalEmails.contains(user.getEmail()))
                .collect(Collectors.toList());

        if (status != null && !status.isEmpty()) {
            boolean isActive = "active".equals(status);
            users = users.stream()
                    .filter(user -> user.isEnabled() == isActive)
                    .collect(Collectors.toList());
        }

        return users;
    }
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<Hospital> getAllHospitals() {
        return (List<Hospital>) hospitalRepository.findAll();
    }

    public List<Hospital> getHospitalsByRegion(String region) {
        return hospitalRepository.findByRegion(region);
    }

    public List<Hospital> searchHospitalsByCode(String code) {
        return hospitalRepository.findByCodeContaining(code);
    }

    public CDASUser getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Користувач не знайдений"));
    }
    public void updateUser(CDASUser user, String hospitalName, String email, String phone, String code) {
        Optional<CDASUser> existingUserOptional = userRepository.findById(user.getId());
        if (existingUserOptional.isPresent()) {
            CDASUser existingUser = existingUserOptional.get();
            existingUser.setEmail(email);
            existingUser.setPassword(existingUser.getPassword());
            existingUser.setName(hospitalName);

            Hospital hospital = hospitalRepository.findByCode(code)
                    .orElseThrow(() -> new IllegalArgumentException("Госпіталь не знайдений"));
            hospital.setName(hospitalName);
            hospital.setEmail(email);
            hospital.setPhone(phone);
            hospitalRepository.save(hospital);

            userRepository.save(existingUser);
        } else {
            throw new IllegalArgumentException("Користувач не знайдений");
        }
    }


    public Hospital getHospitalByEmail(String email) {
        return hospitalRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Госпіталь не знайдений"));
    }

    public List<String> getAllRegions() {
        return List.of("АР Крим","Вінницька", "Волинська", "Дніпропетровська", "Донецька", "Житомирська", "Закарпатська", "Запорізька",
                "Івано-Франківська", "Київська", "Кіровоградська", "Луганська", "Львівська", "Миколаївська", "Одеська",
                "Полтавська", "Рівненська", "Сумська", "Тернопільська", "Харківська", "Херсонська", "Хмельницька",
                "Черкаська", "Чернівецька", "Чернігівська", "місто Київ", "місто Севастополь");
    }
}
