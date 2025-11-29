package com.track.controller;

import com.track.common.Result;
import com.track.dto.JwtResponse;
import com.track.dto.LoginRequest;
import com.track.dto.RegisterRequest;
import com.track.entity.User;
import com.track.security.UserPrincipal;
import com.track.service.UserService;
import com.track.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Operation(summary = "用户登录", description = "使用用户名和密码进行登录认证")
    @PostMapping("/login")
    public ResponseEntity<Result<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();

        JwtResponse jwtResponse = new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getRealName());

        return ResponseEntity.ok(Result.success(jwtResponse));
    }

    /**
       58 -       * @RequestBody 负责反序列化（把 JSON 数据，转成 Java 对象）。
       59 -       *
       60 -       * @Valid 校验数据是否合法（规则写在 RegisterRequest 类里面）。
       61 -       * @param registerRequest
       62 -       * @return
       63 -       */
    @Operation(summary = "用户注册", description = "注册新用户账号")
    @PostMapping("/register")
    public ResponseEntity<Result<String>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {

        if (userService.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("用户名已经被使用");
        }

        if (userService.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("邮箱已被使用");
        }

        // Create new user's account
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setRealName(registerRequest.getRealName());

        userService.register(user);

        return ResponseEntity.ok(Result.success("用户注册成功"));
    }
}