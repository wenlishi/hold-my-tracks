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

    @Operation(summary = "批量添加轨迹点", description = "为指定轨迹批量添加轨迹点，并进行轨迹处理")
    @PostMapping("/batch")
    public ResponseEntity<Result<List<TrackPoint>>> addTrackPointsBatch(
            @Parameter(description = "轨迹ID", required = true) @PathVariable Long trackId,
            @RequestBody List<TrackPoint> trackPoints,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 验证轨迹属于当前用户
        if (!trackService.existsByIdAndUserId(trackId, userPrincipal.getId())) {
            throw new IllegalArgumentException("轨迹不存在或无权限");
        }

        // 批量保存并进行轨迹处理
        List<TrackPoint> savedPoints = trackPointService.saveBatchWithProcessing(trackPoints, trackId);

        // 更新轨迹的总点数
        trackService.updateTotalPoints(trackId);

        return ResponseEntity.ok(Result.success(savedPoints));
    }

    @Operation(summary = "处理轨迹点", description = "对指定轨迹的现有轨迹点进行轨迹处理")
    @PostMapping("/process")
    public ResponseEntity<Result<Integer>> processTrackPoints(
            @Parameter(description = "轨迹ID", required = true) @PathVariable Long trackId,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 验证轨迹属于当前用户
        if (!trackService.existsByIdAndUserId(trackId, userPrincipal.getId())) {
            throw new IllegalArgumentException("轨迹不存在或无权限");
        }

        // 处理轨迹点
        int processedCount = trackPointService.processTrackPoints(trackId);

        // 更新轨迹的总点数
        trackService.updateTotalPoints(trackId);

        return ResponseEntity.ok(Result.success(processedCount));
    }

    @Operation(summary = "获取热力图数据", description = "获取指定轨迹的原始轨迹点数据（用于热力图展示）")
    @GetMapping("/heatmap")
    public ResponseEntity<Result<List<TrackPoint>>> getHeatmapPoints(
            @Parameter(description = "轨迹ID", required = true) @PathVariable Long trackId,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 验证轨迹属于当前用户
        if (!trackService.existsByIdAndUserId(trackId, userPrincipal.getId())) {
            throw new IllegalArgumentException("轨迹不存在或无权限");
        }

        // 获取原始轨迹点（用于热力图）
        List<TrackPoint> points = trackPointService.getRawPointsForHeatmap(trackId);
        return ResponseEntity.ok(Result.success(points));
    }

    @Operation(summary = "获取压缩后的轨迹点", description = "获取指定轨迹的压缩后轨迹点数据（用于轨迹展示）")
    @GetMapping("/compressed")
    public ResponseEntity<Result<List<TrackPoint>>> getCompressedPoints(
            @Parameter(description = "轨迹ID", required = true) @PathVariable Long trackId,
            @Parameter(description = "压缩容差（米）", example = "10.0") @RequestParam(defaultValue = "10.0") double tolerance,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 验证轨迹属于当前用户
        if (!trackService.existsByIdAndUserId(trackId, userPrincipal.getId())) {
            throw new IllegalArgumentException("轨迹不存在或无权限");
        }

        // 获取压缩后的轨迹点
        List<TrackPoint> points = trackPointService.getCompressedPoints(trackId, tolerance);
        return ResponseEntity.ok(Result.success(points));
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