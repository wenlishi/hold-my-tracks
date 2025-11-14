package com.track.controller;

import com.track.dto.JwtResponse;
import com.track.dto.LoginRequest;
import com.track.dto.RegisterRequest;
import com.track.entity.User;
import com.track.security.UserPrincipal;
import com.track.service.UserService;
import com.track.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getRealName()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {

        if (userService.existsByUsername(registerRequest.getUsername())) {
            // --- JSON 修正 ---
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error: Username is already taken!");
            return ResponseEntity.badRequest().body(response);
            // --- 修正结束 ---
        }

        if (userService.existsByEmail(registerRequest.getEmail())) {
            // --- JSON 修正 ---
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error: Email is already in use!");
            return ResponseEntity.badRequest().body(response);
            // --- 修正结束 ---
        }

        // Create new user's account
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setRealName(registerRequest.getRealName());

        userService.register(user);

        // --- JSON 修正 ---
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully!");
        return ResponseEntity.ok(response);
        // --- 修正结束 ---
    }
}