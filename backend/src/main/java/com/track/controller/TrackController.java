package com.track.controller;

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
    public ResponseEntity<Track> getTrack(@PathVariable Long id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Track track = trackService.findByIdAndUserId(id, userPrincipal.getId());
        if (track == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(track);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Track> updateTrack(@PathVariable Long id, @RequestBody Track track, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Track existingTrack = trackService.findByIdAndUserId(id, userPrincipal.getId());
        if (existingTrack == null) {
            return ResponseEntity.notFound().build();
        }

        track.setId(id);
        track.setUserId(userPrincipal.getId());
        trackService.updateById(track);
        return ResponseEntity.ok(track);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrack(@PathVariable Long id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Track track = trackService.findByIdAndUserId(id, userPrincipal.getId());
        if (track == null) {
            return ResponseEntity.notFound().build();
        }

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
    public ResponseEntity<?> getTrackDetail(@PathVariable Long id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        TrackDetail trackDetail = trackService.getTrackDetail(id, userPrincipal.getId());

        if (trackDetail == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(trackDetail);
    }

    /**
     * 导出轨迹
     */
    @GetMapping("/{id}/export/{format}")
    public ResponseEntity<byte[]> exportTrack(
            @PathVariable Long id,
            @PathVariable String format,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        TrackDetail trackDetail = trackService.getTrackDetail(id, userPrincipal.getId());

        if (trackDetail == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] fileContent;
            String fileName;
            String contentType;

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
                // 使用RFC 5987编码处理中文文件名
                encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                        .replaceAll("\\+", "%20");
                // 设置Content-Disposition头，使用filename*参数支持UTF-8编码
                String contentDisposition = String.format(
                        "attachment; filename=\"%s\"; filename*=UTF-8''%s",
                        fileName.replaceAll("\"", "\\\""), // 转义双引号
                        encodedFileName
                );
                headers.set("Content-Disposition", contentDisposition);
            } catch (Exception e) {
                // 如果编码失败，使用原始文件名
                headers.setContentDispositionFormData("attachment", fileName);
            }

            headers.setContentLength(fileContent.length);

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}