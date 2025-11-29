package com.track.controller;

import com.track.common.Result;
import com.track.entity.TrackPoint;
import com.track.security.UserPrincipal;
import com.track.service.TrackPointService;
import com.track.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracks/{trackId}/points")
public class TrackPointController {

    @Autowired
    private TrackPointService trackPointService;

    @Autowired
    private TrackService trackService;

    @PostMapping
    public ResponseEntity<Result<TrackPoint>> addTrackPoint(@PathVariable Long trackId, @RequestBody TrackPoint trackPoint, Authentication authentication) {
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

    @GetMapping
    public ResponseEntity<Result<List<TrackPoint>>> getTrackPoints(@PathVariable Long trackId, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 验证轨迹属于当前用户
        if (!trackService.existsByIdAndUserId(trackId, userPrincipal.getId())) {
            throw new IllegalArgumentException("轨迹不存在或无权限");
        }

        List<TrackPoint> points = trackPointService.findByTrackId(trackId);
        return ResponseEntity.ok(Result.success(points));
    }
}