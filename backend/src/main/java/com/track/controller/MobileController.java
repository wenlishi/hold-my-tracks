package com.track.controller;

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

/**
 * 兼容前端uni-app的API控制器
 * 提供与原有前端API兼容的接口
 */
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
    public ResponseEntity<?> mobileLogin(@RequestBody LoginRequest loginRequest) {
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

        return ResponseEntity.ok(response);
    }

    /**
     * 添加轨迹点 - 兼容前端接口
     */
    @GetMapping("/addcoordpoint")
    public ResponseEntity<?> addCoordPoint(
            @RequestParam String liid,
            @RequestParam String x,
            @RequestParam String y,
            @RequestParam String z,
            @RequestParam(required = false) String azimuth,
            @RequestParam(required = false) String speed,
            @RequestParam(required = false) String satellite,
            @RequestParam(required = false) String other,
            @RequestParam(required = false) String address,
            Authentication authentication) {

        if (authentication == null) {
            // --- Java 8 修正 ---
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "未授权访问");
            return ResponseEntity.status(401).body(errorMsg);
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        try {
            TrackPoint trackPoint = new TrackPoint();
            trackPoint.setTrackId(Long.parseLong(liid));
            trackPoint.setLongitude(new BigDecimal(x));
            trackPoint.setLatitude(new BigDecimal(y));
            trackPoint.setAltitude(new BigDecimal(z));
            trackPoint.setSpeed(speed != null ? new BigDecimal(speed) : BigDecimal.ZERO);
            trackPoint.setAccuracy(BigDecimal.ZERO);
            trackPoint.setSatelliteCount(satellite != null ? Integer.parseInt(satellite) : 0);
            trackPoint.setAddress(address);

            trackPointService.save(trackPoint);

            // 更新轨迹的总点数
            trackService.updateTotalPoints(Long.parseLong(liid));

            // --- Java 8 修正 ---
            Map<String, Object> successMsg = new HashMap<>();
            successMsg.put("msg", "添加成功");
            return ResponseEntity.ok(successMsg);
        } catch (Exception e) {
            // --- Java 8 修正 ---
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "添加失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorMsg);
        }
    }

    /**
     * 更新轨迹状态 - 兼容前端接口
     */
    @GetMapping("/updateroute")
    public ResponseEntity<?> updateRoute(@RequestParam String liid, Authentication authentication) {
        if (authentication == null) {
            // --- Java 8 修正 ---
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "未授权访问");
            return ResponseEntity.status(401).body(errorMsg);
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        try {
            Track track = trackService.getById(Long.parseLong(liid));
            if (track != null && track.getUserId().equals(userPrincipal.getId())) {
                track.setStatus(2); // 标记为已完成
                trackService.updateById(track);
                // --- Java 8 修正 ---
                Map<String, Object> successMsg = new HashMap<>();
                successMsg.put("msg", "更新轨迹成功");
                return ResponseEntity.ok(successMsg);
            } else {
                // --- Java 8 修正 ---
                Map<String, Object> errorMsg = new HashMap<>();
                errorMsg.put("msg", "轨迹不存在或无权限");
                return ResponseEntity.badRequest().body(errorMsg);
            }
        } catch (Exception e) {
            // --- Java 8 修正 ---
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorMsg);
        }
    }

    /**
     * 获取用户信息 - 兼容前端接口
     */
    @GetMapping("/getstaffinfo")
    public ResponseEntity<?> getStaffInfo(
            @RequestParam String username,
            Authentication authentication) {

        if (authentication == null) {
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "未授权访问");
            return ResponseEntity.status(401).body(errorMsg);
        }

        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                Map<String, Object> errorMsg = new HashMap<>();
                errorMsg.put("msg", "用户不存在");
                return ResponseEntity.badRequest().body(errorMsg);
            }

            // 构建前端需要的用户信息格式
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("PhoneType", user.getPhoneType());
            userInfo.put("Account", user.getUsername());
            userInfo.put("Name", user.getRealName());
            userInfo.put("Phone", user.getPhone());

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "获取用户信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorMsg);
        }
    }

    /**
     * 更新设备信息 - 兼容前端接口
     */
    @GetMapping("/updatestaffinfo")
    public ResponseEntity<?> updateStaffInfo(
            @RequestParam String username,
            @RequestParam String phonetype,
            Authentication authentication) {

        if (authentication == null) {
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "未授权访问");
            return ResponseEntity.status(401).body(errorMsg);
        }

        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                Map<String, Object> errorMsg = new HashMap<>();
                errorMsg.put("msg", "用户不存在");
                return ResponseEntity.badRequest().body(errorMsg);
            }

            // 更新设备型号
            user.setPhoneType(phonetype);
            userService.updateById(user);

            Map<String, Object> successMsg = new HashMap<>();
            successMsg.put("msg", "修改成功");
            return ResponseEntity.ok(successMsg);
        } catch (Exception e) {
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "更新设备信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorMsg);
        }
    }

    /**
     * 通用API接口 - 兼容前端各种action
     */
    @GetMapping
    public ResponseEntity<?> handleAction(
            @RequestParam String action,
            HttpServletRequest request,
            Authentication authentication) {

        switch (action) {
            case "login":
                return handleLogin(request);
            case "addcoordpoint":
                return handleAddCoordPoint(request, authentication);
            case "updateroute":
                return handleUpdateRoute(request, authentication);
            case "getstaffinfo":
                return handleGetStaffInfo(request, authentication);
            case "updatestaffinfo":
                return handleUpdateStaffInfo(request, authentication);
            case "updatepassword":
                return handleUpdatePassword(request, authentication);
            default:
                // --- Java 8 修正 ---
                Map<String, Object> errorMsg = new HashMap<>();
                errorMsg.put("msg", "未知的action: " + action);
                return ResponseEntity.badRequest().body(errorMsg);
        }
    }

    private ResponseEntity<?> handleLogin(HttpServletRequest request) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 这里应该调用实际的认证逻辑
        if ("test001".equals(username) && "123456".equals(password)) {
            // --- Java 8 修正 ---
            Map<String, Object> successMsg = new HashMap<>();
            successMsg.put("status", "success");
            return ResponseEntity.ok(successMsg);
        } else {
            // --- Java 8 修正 ---
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("status", "error");
            return ResponseEntity.ok(errorMsg);
        }
    }

    private ResponseEntity<?> handleAddCoordPoint(HttpServletRequest request, Authentication authentication) {
        if (authentication == null) {
            // --- Java 8 修正 ---
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "未授权访问");
            return ResponseEntity.status(401).body(errorMsg);
        }

        try {
            String liid = request.getParameter("liid");
            String x = request.getParameter("x");
            String y = request.getParameter("y");
            String z = request.getParameter("z");
            String speed = request.getParameter("speed");
            String address = request.getParameter("address");

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

            // --- Java 8 修正 ---
            Map<String, Object> successMsg = new HashMap<>();
            successMsg.put("msg", "添加成功");
            return ResponseEntity.ok(successMsg);
        } catch (Exception e) {
            // --- Java 8 修正 ---
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "添加失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorMsg);
        }
    }

    private ResponseEntity<?> handleUpdateRoute(HttpServletRequest request, Authentication authentication) {
        if (authentication == null) {
            // --- Java 8 修正 ---
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "未授权访问");
            return ResponseEntity.status(401).body(errorMsg);
        }

        try {
            String liid = request.getParameter("liid");
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            Track track = trackService.getById(Long.parseLong(liid));
            if (track != null && track.getUserId().equals(userPrincipal.getId())) {
                track.setStatus(2);
                trackService.updateById(track);
                // --- Java 8 修正 ---
                Map<String, Object> successMsg = new HashMap<>();
                successMsg.put("msg", "更新轨迹成功");
                return ResponseEntity.ok(successMsg);
            } else {
                // --- Java 8 修正 ---
                Map<String, Object> errorMsg = new HashMap<>();
                errorMsg.put("msg", "轨迹不存在或无权限");
                return ResponseEntity.badRequest().body(errorMsg);
            }
        } catch (Exception e) {
            // --- Java 8 修正 ---
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorMsg);
        }
    }

    private ResponseEntity<?> handleGetStaffInfo(HttpServletRequest request, Authentication authentication) {
        if (authentication == null) {
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "未授权访问");
            return ResponseEntity.status(401).body(errorMsg);
        }

        try {
            String username = request.getParameter("username");
            User user = userService.findByUsername(username);
            if (user == null) {
                Map<String, Object> errorMsg = new HashMap<>();
                errorMsg.put("msg", "用户不存在");
                return ResponseEntity.badRequest().body(errorMsg);
            }

            // 构建前端需要的用户信息格式
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("PhoneType", user.getPhoneType());
            userInfo.put("Account", user.getUsername());
            userInfo.put("Name", user.getRealName());
            userInfo.put("Phone", user.getPhone());

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "获取用户信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorMsg);
        }
    }

    private ResponseEntity<?> handleUpdateStaffInfo(HttpServletRequest request, Authentication authentication) {
        if (authentication == null) {
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "未授权访问");
            return ResponseEntity.status(401).body(errorMsg);
        }

        try {
            String username = request.getParameter("username");
            String phonetype = request.getParameter("phonetype");

            User user = userService.findByUsername(username);
            if (user == null) {
                Map<String, Object> errorMsg = new HashMap<>();
                errorMsg.put("msg", "用户不存在");
                return ResponseEntity.badRequest().body(errorMsg);
            }

            // 更新设备型号
            user.setPhoneType(phonetype);
            userService.updateById(user);

            Map<String, Object> successMsg = new HashMap<>();
            successMsg.put("msg", "修改成功");
            return ResponseEntity.ok(successMsg);
        } catch (Exception e) {
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "更新设备信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorMsg);
        }
    }

    /**
     * 修改密码接口
     */
    private ResponseEntity<?> handleUpdatePassword(HttpServletRequest request, Authentication authentication) {
        System.out.println("=== 修改密码接口被调用 ===");

        if (authentication == null) {
            System.out.println("未授权访问");
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "未授权访问");
            return ResponseEntity.status(401).body(errorMsg);
        }

        try {
            String username = request.getParameter("username");
            String newpwd = request.getParameter("newpwd");

            System.out.println("接收到的参数 - username: " + username + ", newpwd: " + newpwd);

            // 参数验证
            if (username == null || username.trim().isEmpty()) {
                System.out.println("用户名不能为空");
                Map<String, Object> errorMsg = new HashMap<>();
                errorMsg.put("msg", "用户名不能为空");
                return ResponseEntity.badRequest().body(errorMsg);
            }

            if (newpwd == null || newpwd.trim().isEmpty()) {
                System.out.println("新密码不能为空");
                Map<String, Object> errorMsg = new HashMap<>();
                errorMsg.put("msg", "新密码不能为空");
                return ResponseEntity.badRequest().body(errorMsg);
            }

            User user = userService.findByUsername(username);
            if (user == null) {
                System.out.println("用户不存在: " + username);
                Map<String, Object> errorMsg = new HashMap<>();
                errorMsg.put("msg", "用户不存在");
                return ResponseEntity.badRequest().body(errorMsg);
            }

            System.out.println("找到用户: " + user.getUsername());

            // 更新密码
            String encodedPassword = passwordEncoder.encode(newpwd);
            System.out.println("原密码: " + user.getPassword());
            System.out.println("新密码(加密后): " + encodedPassword);

            user.setPassword(encodedPassword);
            userService.updateById(user);

            System.out.println("密码修改成功");
            Map<String, Object> successMsg = new HashMap<>();
            successMsg.put("msg", "密码修改成功");
            return ResponseEntity.ok(successMsg);
        } catch (Exception e) {
            System.out.println("密码修改失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("msg", "密码修改失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorMsg);
        }
    }
}