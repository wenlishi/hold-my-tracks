package com.track.controller;

import com.track.annotation.LogOperation;
import com.track.annotation.RequirePermission;
import com.track.common.Result;
import com.track.dto.PageResponse;
import com.track.dto.TrackDetail;
import com.track.entity.Track;
import com.track.security.UserPrincipal;
import com.track.service.TrackExportService;
import com.track.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "轨迹管理")
@RestController
@RequestMapping("/api/tracks")
public class TrackController {

    @Autowired
    private TrackService trackService;

    @Autowired
    private TrackExportService trackExportService;

    @PostMapping
    @LogOperation(operation = "创建轨迹", logParams = true)
    public ResponseEntity<Result<Track>> createTrack(@RequestBody Track track, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        // 检查用户是否有进行中的轨迹
        if (trackService.hasActiveTrack(userId)) {
            throw new IllegalArgumentException("用户已有进行中的轨迹任务，请先完成或结束当前轨迹后再创建新轨迹");
        }

        track.setUserId(userId);
        track.setStatus(1); // 进行中
        trackService.save(track);
        return ResponseEntity.ok(Result.success(track));
    }

    @GetMapping
    @LogOperation(operation = "查询用户轨迹列表")
    public ResponseEntity<Result<Object>> getUserTracks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 如果page和pageSize都是默认值，返回所有数据（兼容旧版本）
        if (page == 1 && pageSize == 10) {
            List<Track> tracks = trackService.findByUserId(userPrincipal.getId());
            return ResponseEntity.ok(Result.success(tracks));
        }

        // 使用分页查询
        PageResponse<Track> pageResponse = trackService.findByUserIdWithPagination(
            userPrincipal.getId(), page, pageSize);
        return ResponseEntity.ok(Result.success(pageResponse));
    }

    @GetMapping("/{id}")
    @RequirePermission(resourceType = "track", resourceIdParam = "id")
    @LogOperation(operation = "查询轨迹详情", resourceId = "#id")
    public ResponseEntity<Result<Track>> getTrack(@PathVariable Long id, Authentication authentication) {
        // 权限验证已通过AOP处理，直接查询数据
        Track track = trackService.getById(id);
        return ResponseEntity.ok(Result.success(track));
    }

    @PutMapping("/{id}")
    @RequirePermission(resourceType = "track", resourceIdParam = "id", operation = "write")
    @LogOperation(operation = "更新轨迹", resourceId = "#id", logParams = true)
    public ResponseEntity<Result<Track>> updateTrack(@PathVariable Long id, @RequestBody Track track, Authentication authentication) {
        // 权限验证已通过AOP处理
        track.setId(id);
        trackService.updateById(track);
        return ResponseEntity.ok(Result.success(track));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(resourceType = "track", resourceIdParam = "id", operation = "delete")
    @LogOperation(operation = "删除轨迹", resourceId = "#id")
    public ResponseEntity<Result<String>> deleteTrack(@PathVariable Long id, Authentication authentication) {
        // 权限验证已通过AOP处理
        try {
            trackService.removeTrackWithPoints(id);
            return ResponseEntity.ok(Result.success("轨迹删除成功"));
        } catch (Exception e) {
            throw new IllegalArgumentException("删除轨迹失败: " + e.getMessage());
        }
    }

    /**
     * 获取轨迹详情，包含轨迹点和统计信息
     */
    @GetMapping("/{id}/detail")
    @RequirePermission(resourceType = "track", resourceIdParam = "id")
    @LogOperation(operation = "查询轨迹详情", resourceId = "#id")
    public ResponseEntity<Result<TrackDetail>> getTrackDetail(@PathVariable Long id, Authentication authentication) {
        // 1. 获取当前用户 ID
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        // 2. 调用 Service (传入 trackId 和 userId)
        // Service 内部会执行: SELECT ... FROM tracks WHERE id=? AND user_id=?
        // 如果查不到或无权访问，Service 会直接抛出异常
        TrackDetail trackDetail = trackService.getTrackDetail(id, userId);

        return ResponseEntity.ok(Result.success(trackDetail));
    }

    /**
     * 搜索轨迹（支持关键字和日期范围查询）
     */
    @GetMapping("/search")
    @LogOperation(operation = "搜索轨迹", logParams = true)
    public ResponseEntity<Result<PageResponse<Track>>> searchTracks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 执行搜索查询
        PageResponse<Track> pageResponse = trackService.searchTracks(
            userPrincipal.getId(), keyword, startDate, endDate, page, pageSize);

        return ResponseEntity.ok(Result.success(pageResponse));
    }

    /**
     * 导出轨迹
    //  */
    // @GetMapping("/{id}/export/{format}")
    // @RequirePermission(resourceType = "track", resourceIdParam = "id")
    // @LogOperation(operation = "导出轨迹", resourceId = "#id")
    // public ResponseEntity<byte[]> exportTrack(
    //         @PathVariable Long id,
    //         @PathVariable String format,
    //         Authentication authentication) {

    //     // 1. 获取当前用户 ID
    //     UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    //     Long userId = userPrincipal.getId();

    //     // 2. 获取数据 (Service 内部已做权限检查)
    //     TrackDetail trackDetail = trackService.getTrackDetail(id, userId);

    //     try {
    //         // 3. 生成文件名 (使用 Service 封装的逻辑，统一管理后缀)
    //         // 例如：fileName = "夜跑轨迹.gpx"
    //         String fileName = trackExportService.generateFileName(trackDetail.getTrack(), format);

    //         byte[] fileContent;
    //         String contentType;

    //         // 4. 生成文件内容 & 确定 Content-Type
    //         switch (format.toLowerCase()) {
    //             case "gpx":
    //                 fileContent = trackExportService.exportToGpx(trackDetail);
    //                 contentType = "application/gpx+xml";
    //                 break;
    //             case "kml":
    //                 fileContent = trackExportService.exportToKml(trackDetail);
    //                 contentType = "application/vnd.google-earth.kml+xml";
    //                 break;
    //             case "csv":
    //                 fileContent = trackExportService.exportToCsv(trackDetail);
    //                 contentType = "text/csv"; // 标准 CSV 类型
    //                 break;
    //             case "geojson":
    //                 fileContent = trackExportService.exportToGeoJson(trackDetail);
    //                 contentType = "application/geo+json";
    //                 break;
    //             default:
    //                 return ResponseEntity.badRequest().build();
    //         }

    //         // 5. 设置响应头
    //         HttpHeaders headers = new HttpHeaders();
    //         headers.setContentType(MediaType.parseMediaType(contentType));

    //         // 【关键修复】处理文件名编码
    //         // URLEncoder.encode 会把 "夜跑" 变成 "%E5%A4%9C%E8%B7%91" (纯 ASCII 字符)
    //         String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
    //                 .replaceAll("\\+", "%20"); // 处理空格变加号的问题

    //         // 【核心修改】
    //         // filename="...": 这里的 ... 必须是 ASCII。为了不报错，我们这里也放编码后的名字
    //         // filename*=...: 现代浏览器（Chrome/Edge/Firefox）会优先读取这个，并自动解码出中文
    //         String contentDisposition = "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName;
            
    //         headers.set("Content-Disposition", contentDisposition);
    //         headers.setContentLength(fileContent.length);

    //         return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);

    //     } catch (IOException e) {
    //         // 建议打印日志
    //         e.printStackTrace();
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    //     }
    // }
        // Controller 代码
    @GetMapping("/{id}/export/{format}")
    @RequirePermission(resourceType = "track", resourceIdParam = "id")
    @LogOperation(operation = "导出轨迹", resourceId = "#id")
    public ResponseEntity<StreamingResponseBody> exportTrack(
            @PathVariable Long id,
            @PathVariable String format,
            Authentication authentication) {

        // 1. 获取数据
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        TrackDetail trackDetail = trackService.getTrackDetail(id, userPrincipal.getId());

        // 2. 准备文件名 (例如: "周末夜跑.gpx")
        String fileName = trackExportService.generateFileName(trackDetail.getTrack(), format);

        // 3. 确定 Content-Type (根据不同格式设置不同的响应类型)
        String mediaTypeStr;
        switch (format.toLowerCase()) {
            case "gpx": mediaTypeStr = "application/gpx+xml"; break;
            case "kml": mediaTypeStr = "application/vnd.google-earth.kml+xml"; break;
            case "csv": mediaTypeStr = "text/csv"; break;
            case "geojson": mediaTypeStr = "application/geo+json"; break;
            default: return ResponseEntity.badRequest().build();
        }

        // 4. 设置响应头 (关键：处理中文文件名乱码)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mediaTypeStr));

        try {
            // URL编码处理：解决中文乱码，同时把空格(+)替换为%20
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            // 双重设置：filename="..." 兼容旧浏览器，filename*=UTF-8''... 适配新浏览器
            String contentDisposition = "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName;
            headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
            
        } catch (UnsupportedEncodingException e) {
            // 理论上 UTF-8 不会报错，但为了严谨捕获一下
            e.printStackTrace();
        }

        // 5. 定义流式输出 (根据 format 调用不同的 Service 方法)
        StreamingResponseBody stream = outputStream -> {
            // 这里根据 format 动态分发
            switch (format.toLowerCase()) {
                case "gpx":
                    trackExportService.exportToGpxStream(trackDetail, outputStream);
                    break;
                case "kml":
                    trackExportService.exportToKmlStream(trackDetail, outputStream);
                    break;
                case "csv":
                    trackExportService.exportToCsvStream(trackDetail, outputStream);
                    break;
                case "geojson":
                    trackExportService.exportToGeoJsonStream(trackDetail, outputStream);
                    break;
            }
        };

        return new ResponseEntity<>(stream, headers, HttpStatus.OK);
    }

}