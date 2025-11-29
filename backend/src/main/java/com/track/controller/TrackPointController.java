package com.track.controller;

import com.track.common.Result;
import com.track.entity.TrackPoint;
import com.track.security.UserPrincipal;
import com.track.service.TrackPointService;
import com.track.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "轨迹点管理")
@RestController
@RequestMapping("/api/tracks/{trackId}/points")
public class TrackPointController {

    @Autowired
    private TrackPointService trackPointService;

    @Autowired
    private TrackService trackService;

    @Operation(summary = "添加轨迹点", description = "为指定轨迹添加一个新的轨迹点")
    @PostMapping
    public ResponseEntity<Result<TrackPoint>> addTrackPoint(
            @Parameter(description = "轨迹ID", required = true) @PathVariable Long trackId,
            @RequestBody TrackPoint trackPoint,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 验证轨迹属于当前用户
        if (!trackService.existsByIdAndUserId(trackId, userPrincipal.getId())) {
            throw new IllegalArgumentException("轨迹不存在或无权限");
        }

        trackPoint.setTrackId(trackId);
        trackPointService.save(trackPoint);

        // 更新轨迹的总点数
        trackService.updateTotalPoints(trackId);

        return ResponseEntity.ok(Result.success(trackPoint));
    }

    @Operation(summary = "获取轨迹点列表", description = "获取指定轨迹的所有轨迹点列表")
    @GetMapping
    public ResponseEntity<Result<List<TrackPoint>>> getTrackPoints(
            @Parameter(description = "轨迹ID", required = true) @PathVariable Long trackId,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 验证轨迹属于当前用户
        if (!trackService.existsByIdAndUserId(trackId, userPrincipal.getId())) {
            throw new IllegalArgumentException("轨迹不存在或无权限");
        }

        List<TrackPoint> points = trackPointService.findByTrackId(trackId);
        return ResponseEntity.ok(Result.success(points));
    }
}