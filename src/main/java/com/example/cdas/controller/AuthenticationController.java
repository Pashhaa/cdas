package com.example.cdas.controller;

import com.example.cdas.dto.JSONRequestResponse;
import com.example.cdas.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<JSONRequestResponse>register(@RequestBody JSONRequestResponse registerRequest){
        System.out.println(1);
        JSONRequestResponse registerResponse = authenticationService.register(registerRequest);
        if (registerResponse.getStatusCode() == 200) {
            return ResponseEntity.ok(registerResponse);
        } else {
            return ResponseEntity.status(registerResponse.getStatusCode()).body(registerResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<JSONRequestResponse>login(@RequestBody JSONRequestResponse loginRequest){
        JSONRequestResponse loginResponse = authenticationService.login(loginRequest);
        if (loginResponse.getStatusCode() == 200) {
            return ResponseEntity.ok(loginResponse);
        } else {
            return ResponseEntity.status(loginResponse.getStatusCode()).body(loginResponse);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<JSONRequestResponse>refreshToken(@RequestBody JSONRequestResponse refreshRequest){
        JSONRequestResponse refreshTokenResponse = authenticationService.refreshToken(refreshRequest);
        if (refreshTokenResponse.getStatusCode() == 200) {
            return ResponseEntity.ok(refreshTokenResponse);
        } else {
            return ResponseEntity.status(refreshTokenResponse.getStatusCode()).body(refreshTokenResponse);
        }
    }
}
