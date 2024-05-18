package com.example.cdas.services;

import com.example.cdas.config.JWTCore;
import com.example.cdas.dto.JSONRequestResponse;
import com.example.cdas.models.CDASUser;
import com.example.cdas.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTCore jwtCore;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;

    public JSONRequestResponse register (JSONRequestResponse registerReq) {
        JSONRequestResponse registerResponse = new JSONRequestResponse();
        try{
            CDASUser user = new CDASUser();
            user.setEmail(registerReq.getEmail());
            user.setSurname(registerReq.getSurname());
            user.setName(registerReq.getName());
            user.setPatronymic(registerReq.getPatronymic());
            user.setPassword(passwordEncoder.encode(registerReq.getPassword()));
            user.setRoles(registerReq.getRoles());
            CDASUser newUser = userRepository.save(user);
            if(newUser != null && newUser.getId() > 0){
                registerResponse.setMessage("Registration successful");
                registerResponse.setUser(newUser);
                registerResponse.setStatusCode(200);
            }
        } catch (Exception e){
            registerResponse.setError("Registration failed: " + e.getMessage());
            registerResponse.setStatusCode(400);
        }
        return registerResponse;
    }

    public JSONRequestResponse login (JSONRequestResponse loginReq) {
        JSONRequestResponse loginResponse = new JSONRequestResponse();
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginReq.getEmail(), loginReq.getPassword()));
            CDASUser user = userRepository.findUserByEmail(loginReq.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            String token = jwtCore.generateToken(user);
            String refreshToken = jwtCore.generateRefreshToken(new HashMap<>(), user);
            loginResponse.setStatusCode(200);
            loginResponse.setMessage("Login successful");
            loginResponse.setToken(token);
            loginResponse.setRefreshToken(refreshToken);
            loginResponse.setExpirationTime("24h");
        } catch (AuthenticationException e){
            loginResponse.setError("Authentication failed: " + e.getMessage());
            loginResponse.setStatusCode(401);
        } catch (Exception e){
            loginResponse.setError("Login failed: " + e.getMessage());
            loginResponse.setStatusCode(400);
        }
        return loginResponse;
    }

    public JSONRequestResponse refreshToken(JSONRequestResponse refreshTokenReq){
        JSONRequestResponse response = new JSONRequestResponse();
        try {
            String email = jwtCore.extractUsername(refreshTokenReq.getToken());
            CDASUser user = userRepository.findUserByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if (jwtCore.isTokenValid(refreshTokenReq.getToken(), user)) {
                var jwt = jwtCore.generateToken(user);
                response.setStatusCode(200);
                response.setToken(jwt);
                response.setRefreshToken(refreshTokenReq.getToken());
                response.setExpirationTime("24Hr");
                response.setMessage("Successfully refreshed token");
            }
        } catch (UsernameNotFoundException e){
            response.setError("Refresh token failed: " + e.getMessage());
            response.setStatusCode(404);
        } catch (Exception e){
            response.setError("Refresh token failed: " + e.getMessage());
            response.setStatusCode(500);
        }
        return response;
    }
}
