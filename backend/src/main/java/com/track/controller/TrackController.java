package com.track.controller;

import com.track.annotation.LogOperation;
import com.track.annotation.RequirePermission;
import com.track.dto.PageResponse;
import com.track.dto.TrackDetail;
import com.track.entity.Track;
import com.track.security.UserPrincipal;
import com.track.service.TrackExportService;
import com.track.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracks")
public class TrackController {

    @Autowired
    private TrackService trackService;

    @Autowired
    private TrackExportService trackExportService;

    @PostMapping
    @LogOperation(operation = "创建轨迹", logParams = true)
    public ResponseEntity<?> createTrack(@RequestBody Track track, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        // 检查用户是否有进行中的轨迹
        if (trackService.hasActiveTrack(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "用户已有进行中的轨迹任务，请先完成或结束当前轨迹后再创建新轨迹");
            errorResponse.put("code", "ACTIVE_TRACK_EXISTS");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        track.setUserId(userId);
        track.setStatus(1); // 进行中
        trackService.save(track);
        return ResponseEntity.ok(track);
    }

    @GetMapping
    @LogOperation(operation = "查询用户轨迹列表")
    public ResponseEntity<?> getUserTracks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 如果page和pageSize都是默认值，返回所有数据（兼容旧版本）
        if (page == 1 && pageSize == 10) {
            List<Track> tracks = trackService.findByUserId(userPrincipal.getId());
            return ResponseEntity.ok(tracks);
        }

        // 使用分页查询
        PageResponse<Track> pageResponse = trackService.findByUserIdWithPagination(
            userPrincipal.getId(), page, pageSize);
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/{id}")
    @RequirePermission(resourceType = "track", resourceIdParam = "id")
    @LogOperation(operation = "查询轨迹详情", resourceId = "#id")
    public ResponseEntity<Track> getTrack(@PathVariable Long id, Authentication authentication) {
        // 权限验证已通过AOP处理，直接查询数据
        Track track = trackService.getById(id);
        return ResponseEntity.ok(track);
    }

    @PutMapping("/{id}")
    @RequirePermission(resourceType = "track", resourceIdParam = "id", operation = "write")
    @LogOperation(operation = "更新轨迹", resourceId = "#id", logParams = true)
    public ResponseEntity<Track> updateTrack(@PathVariable Long id, @RequestBody Track track, Authentication authentication) {
        // 权限验证已通过AOP处理
        track.setId(id);
        trackService.updateById(track);
        return ResponseEntity.ok(track);
    }

    @DeleteMapping("/{id}")
    @RequirePermission(resourceType = "track", resourceIdParam = "id", operation = "delete")
    @LogOperation(operation = "删除轨迹", resourceId = "#id")
    public ResponseEntity<?> deleteTrack(@PathVariable Long id, Authentication authentication) {
        // 权限验证已通过AOP处理
        try {
            trackService.removeTrackWithPoints(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "删除轨迹失败: " + e.getMessage());
            errorResponse.put("code", "DELETE_TRACK_FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取轨迹详情，包含轨迹点和统计信息
     */
    @GetMapping("/{id}/detail")
    @RequirePermission(resourceType = "track", resourceIdParam = "id")
    @LogOperation(operation = "查询轨迹详情", resourceId = "#id")
    public ResponseEntity<?> getTrackDetail(@PathVariable Long id, Authentication authentication) {
        // 1. 获取当前用户 ID
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        // 2. 调用 Service (传入 trackId 和 userId)
        // Service 内部会执行: SELECT ... FROM tracks WHERE id=? AND user_id=?
        // 如果查不到或无权访问，Service 会直接抛出异常
        TrackDetail trackDetail = trackService.getTrackDetail(id, userId);

        return ResponseEntity.ok(trackDetail);
    }

    /**
     * 搜索轨迹（支持关键字和日期范围查询）
     */
    @GetMapping("/search")
    @LogOperation(operation = "搜索轨迹", logParams = true)
    public ResponseEntity<?> searchTracks(
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

        return ResponseEntity.ok(pageResponse);
    }

    /**
     * 导出轨迹
     */
    @GetMapping("/{id}/export/{format}")
    @RequirePermission(resourceType = "track", resourceIdParam = "id")
    @LogOperation(operation = "导出轨迹", resourceId = "#id")
    public ResponseEntity<byte[]> exportTrack(
            @PathVariable Long id,
            @PathVariable String format,
            Authentication authentication) {

        // 1. 获取当前用户 ID
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        // 2. 调用 Service (传入 trackId 和 userId)
        // 这一步会执行: SELECT ... FROM tracks WHERE id=? AND user_id=?
        // 如果查不到或无权访问，Service 会直接抛出 RuntimeException，被全局异常处理器捕获
        TrackDetail trackDetail = trackService.getTrackDetail(id, userId);

        try {
            byte[] fileContent;
            String fileName;
            String contentType;

            // 这里的逻辑保持不变，因为 trackDetail 已经安全获取到了
            String trackName = trackDetail.getTrack().getTrackName() != null ?
                    trackDetail.getTrack().getTrackName() : "轨迹";

            switch (format.toLowerCase()) {
                case "gpx":
                    fileContent = trackExportService.exportToGpx(trackDetail);
                    fileName = trackName + ".gpx";
                    contentType = "application/gpx+xml";
                    break;
                case "kml":
                    fileContent = trackExportService.exportToKml(trackDetail);
                    fileName = trackName + ".kml";
                    contentType = "application/vnd.google-earth.kml+xml";
                    break;
                case "csv":
                    fileContent = trackExportService.exportToCsv(trackDetail);
                    fileName = trackName + ".csv";
                    contentType = "text/csv";
                    break;
                case "geojson":
                    fileContent = trackExportService.exportToGeoJson(trackDetail);
                    fileName = trackName + ".geojson";
                    contentType = "application/geo+json";
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));

            // 处理中文文件名编码问题
            String encodedFileName;
            try {
                encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                        .replaceAll("\\+", "%20");
                String contentDisposition = String.format(
                        "attachment; filename=\"%s\"; filename*=UTF-8''%s",
                        fileName.replaceAll("\"", "\\\""),
                        encodedFileName
                );
                headers.set("Content-Disposition", contentDisposition);
            } catch (Exception e) {
                headers.setContentDispositionFormData("attachment", fileName);
            }

            headers.setContentLength(fileContent.length);

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}