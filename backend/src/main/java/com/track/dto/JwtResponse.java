package com.track.dto;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String realName;

    public JwtResponse(String token, Long id, String username, String email, String realName) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.realName = realName;
    }
}