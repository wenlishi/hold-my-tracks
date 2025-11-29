package com.track.controller;

import com.track.common.Result;
import com.track.dto.UpdateDeviceInfoRequest;
import com.track.dto.UpdatePasswordRequest;
import com.track.entity.User;
import com.track.security.UserPrincipal;
import com.track.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/profile")
    public ResponseEntity<Result<Map<String, Object>>> getCurrentUserProfile(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body(Result.unauthorized());
        }

        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userService.getById(userPrincipal.getId());

            if (user == null) {
                return ResponseEntity.badRequest().body(Result.failed("用户不存在"));
            }

            // 构建用户信息响应
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("realName", user.getRealName());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("phoneType", user.getPhoneType());
            userInfo.put("createTime", user.getCreateTime());

            return ResponseEntity.ok(Result.success(userInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.failed("获取用户信息失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "更新设备信息", description = "更新当前用户的设备型号信息")
    @PutMapping("/device")
    public ResponseEntity<Result<String>> updateDeviceInfo(
            @Valid @RequestBody UpdateDeviceInfoRequest request,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body(Result.unauthorized());
        }

        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userService.getById(userPrincipal.getId());

            if (user == null) {
                return ResponseEntity.badRequest().body(Result.failed("用户不存在"));
            }

            // 更新设备型号
            user.setPhoneType(request.getDeviceModel());
            userService.updateById(user);

            return ResponseEntity.ok(Result.success("设备信息更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.failed("更新设备信息失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "修改密码", description = "修改当前用户的密码")
    @PutMapping("/password")
    public ResponseEntity<Result<String>> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body(Result.unauthorized());
        }

        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userService.getById(userPrincipal.getId());

            if (user == null) {
                return ResponseEntity.badRequest().body(Result.failed("用户不存在"));
            }

            // 验证新密码
            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Result.validateFailed("新密码不能为空"));
            }

            // 更新密码
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(encodedPassword);
            userService.updateById(user);

            return ResponseEntity.ok(Result.success("密码修改成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.failed("密码修改失败: " + e.getMessage()));
        }
    }
}