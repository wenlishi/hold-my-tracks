package com.track.controller;

import com.track.common.Result;
import com.track.dto.LoginRequest;
import com.track.entity.Track;
import com.track.entity.TrackPoint;
import com.track.entity.User;
import com.track.security.UserPrincipal;
import com.track.service.TrackPointService;
import com.track.service.TrackService;
import com.track.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 移动端API控制器
 * 提供移动端专用的API接口，兼容前端uni-app
 */
@Tag(name = "移动端接口", description = "移动端专用的API接口")
@RestController
@RequestMapping("/api")
public class MobileController {

    @Autowired
    private UserService userService;

    @Autowired
    private TrackService trackService;

    @Autowired
    private TrackPointService trackPointService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 兼容前端登录接口
     */
//    @PostMapping("/auth/login")
    public ResponseEntity<Result<Map<String, Object>>> mobileLogin(@RequestBody LoginRequest loginRequest) {
        // 这里调用原有的认证逻辑
        // 为了简化，这里直接返回成功
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");

        // --- Java 8 修正开始 ---
        // 原: response.put("user", Map.of(...));
        Map<String, Object> user = new HashMap<>();
        user.put("username", loginRequest.getUsername());
        response.put("user", user);
        // --- Java 8 修正结束 ---

        return ResponseEntity.ok(Result.success(response));
    }


    /**
     * 移动端登录接口
     */
    @Operation(summary = "移动端登录", description = "移动端用户登录接口")
    @GetMapping("/login")
    public ResponseEntity<Result<Object>> mobileLoginAction(
            @Parameter(description = "用户名") @RequestParam String username,
            @Parameter(description = "密码") @RequestParam String password) {

        // 这里应该调用实际的认证逻辑
        if ("test001".equals(username) && "123456".equals(password)) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            return ResponseEntity.ok(Result.success(response));
        } else {
            return ResponseEntity.badRequest().body(Result.failed("用户名或密码错误"));
        }
    }

    /**
     * 添加坐标点接口
     */
    @Operation(summary = "添加坐标点", description = "向指定轨迹添加坐标点")
    @GetMapping("/addcoordpoint")
    public ResponseEntity<Result<String>> mobileAddCoordPoint(
            @Parameter(description = "轨迹ID") @RequestParam String liid,
            @Parameter(description = "经度") @RequestParam String x,
            @Parameter(description = "纬度") @RequestParam String y,
            @Parameter(description = "海拔") @RequestParam String z,
            @Parameter(description = "速度") @RequestParam(required = false) String speed,
            @Parameter(description = "地址") @RequestParam(required = false) String address,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body(Result.unauthorized());
        }

        try {
            TrackPoint trackPoint = new TrackPoint();
            trackPoint.setTrackId(Long.parseLong(liid));
            trackPoint.setLongitude(new BigDecimal(x));
            trackPoint.setLatitude(new BigDecimal(y));
            trackPoint.setAltitude(new BigDecimal(z));
            trackPoint.setSpeed(speed != null ? new BigDecimal(speed) : BigDecimal.ZERO);
            trackPoint.setAddress(address);

            trackPointService.save(trackPoint);

            // 更新轨迹的总点数
            trackService.updateTotalPoints(Long.parseLong(liid));

            return ResponseEntity.ok(Result.success("添加成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.failed("添加失败: " + e.getMessage()));
        }
    }

    /**
     * 更新轨迹状态接口
     */
    @Operation(summary = "更新轨迹状态", description = "将轨迹标记为已完成状态")
    @GetMapping("/updateroute")
    public ResponseEntity<Result<String>> mobileUpdateRoute(
            @Parameter(description = "轨迹ID") @RequestParam String liid,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body(Result.unauthorized());
        }

        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Track track = trackService.getById(Long.parseLong(liid));
            if (track != null && track.getUserId().equals(userPrincipal.getId())) {
                track.setStatus(2);
                trackService.updateById(track);
                return ResponseEntity.ok(Result.success("更新轨迹成功"));
            } else {
                return ResponseEntity.badRequest().body(Result.failed("轨迹不存在或无权限"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.failed("更新失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户信息接口
     */
    @Operation(summary = "获取用户信息", description = "根据用户名获取用户详细信息")
    @GetMapping("/getstaffinfo")
    public ResponseEntity<Result<Map<String, Object>>> mobileGetStaffInfo(
            @Parameter(description = "用户名") @RequestParam String username,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body(Result.unauthorized());
        }

        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Result.failed("用户不存在"));
            }

            // 构建前端需要的用户信息格式
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("PhoneType", user.getPhoneType());
            userInfo.put("Account", user.getUsername());
            userInfo.put("Name", user.getRealName());
            userInfo.put("Phone", user.getPhone());

            return ResponseEntity.ok(Result.success(userInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.failed("获取用户信息失败: " + e.getMessage()));
        }
    }

    /**
     * 更新设备信息接口
     */
    @Operation(summary = "更新设备信息", description = "更新用户的设备型号信息")
    @GetMapping("/updatestaffinfo")
    public ResponseEntity<Result<String>> mobileUpdateStaffInfo(
            @Parameter(description = "用户名") @RequestParam String username,
            @Parameter(description = "设备型号") @RequestParam String phonetype,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body(Result.unauthorized());
        }

        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Result.failed("用户不存在"));
            }

            // 更新设备型号
            user.setPhoneType(phonetype);
            userService.updateById(user);

            return ResponseEntity.ok(Result.success("修改成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.failed("更新设备信息失败: " + e.getMessage()));
        }
    }

    /**
     * 修改密码接口
     */
    @Operation(summary = "修改密码", description = "修改用户密码")
    @GetMapping("/updatepassword")
    public ResponseEntity<Result<String>> mobileUpdatePassword(
            @Parameter(description = "用户名") @RequestParam String username,
            @Parameter(description = "新密码") @RequestParam String newpwd,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body(Result.unauthorized());
        }

        try {
            // 参数验证
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Result.validateFailed("用户名不能为空"));
            }

            if (newpwd == null || newpwd.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Result.validateFailed("新密码不能为空"));
            }

            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Result.failed("用户不存在"));
            }

            // 更新密码
            String encodedPassword = passwordEncoder.encode(newpwd);
            user.setPassword(encodedPassword);
            userService.updateById(user);

            return ResponseEntity.ok(Result.success("密码修改成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.failed("密码修改失败: " + e.getMessage()));
        }
    }


}