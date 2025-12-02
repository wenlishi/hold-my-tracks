package com.track.util;

import com.track.entity.TrackPoint;
import com.track.starter.model.Point;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 轨迹点转换器
 * 用于在TrackPoint（数据库实体）和Point（轨迹处理模型）之间进行转换
 */
public class PointConverter {

    private PointConverter() {
        // 工具类，私有构造函数
    }

    /**
     * 将TrackPoint转换为Point
     *
     * @param trackPoint 数据库轨迹点实体
     * @return 轨迹处理模型Point
     */
    public static Point toPoint(TrackPoint trackPoint) {
        if (trackPoint == null) {
            return null;
        }

        Point point = new Point();

        // 坐标转换：BigDecimal -> double
        if (trackPoint.getLatitude() != null) {
            point.setLat(trackPoint.getLatitude().doubleValue());
        }
        if (trackPoint.getLongitude() != null) {
            point.setLng(trackPoint.getLongitude().doubleValue());
        }

        // 时间转换：LocalDateTime -> long timestamp (毫秒)
        if (trackPoint.getCreateTime() != null) {
            point.setTimestamp(toTimestamp(trackPoint.getCreateTime()));
        }

        // 海拔转换：BigDecimal -> Double
        if (trackPoint.getAltitude() != null) {
            point.setAltitude(trackPoint.getAltitude().doubleValue());
        }

        // 速度转换：BigDecimal -> Double (注意单位：m/s -> km/h)
        if (trackPoint.getSpeed() != null) {
            // 后端存储的是m/s，starter期望的是km/h
            point.setSpeed(trackPoint.getSpeed().doubleValue() * 3.6);
        }

        // 精度转换：BigDecimal -> Double
        if (trackPoint.getAccuracy() != null) {
            point.setAccuracy(trackPoint.getAccuracy().doubleValue());
        }

        // 方向角：TrackPoint中没有bearing字段，留空

        return point;
    }

    /**
     * 将Point转换为TrackPoint
     *
     * @param point 轨迹处理模型Point
     * @param trackId 轨迹ID
     * @return 数据库轨迹点实体
     */
    public static TrackPoint toTrackPoint(Point point, Long trackId) {
        if (point == null) {
            return null;
        }

        TrackPoint trackPoint = new TrackPoint();
        trackPoint.setTrackId(trackId);

        // 坐标转换：double -> BigDecimal
        trackPoint.setLatitude(BigDecimal.valueOf(point.getLat()));
        trackPoint.setLongitude(BigDecimal.valueOf(point.getLng()));

        // 时间转换：long timestamp -> LocalDateTime
        trackPoint.setCreateTime(toLocalDateTime(point.getTimestamp()));

        // 海拔转换：Double -> BigDecimal
        if (point.getAltitude() != null) {
            trackPoint.setAltitude(BigDecimal.valueOf(point.getAltitude()));
        }

        // 速度转换：Double -> BigDecimal (注意单位：km/h -> m/s)
        if (point.getSpeed() != null) {
            trackPoint.setSpeed(BigDecimal.valueOf(point.getSpeed() / 3.6));
        }

        // 精度转换：Double -> BigDecimal
        if (point.getAccuracy() != null) {
            trackPoint.setAccuracy(BigDecimal.valueOf(point.getAccuracy()));
        }

        return trackPoint;
    }

    /**
     * 批量转换：List<TrackPoint> -> List<Point>
     */
    public static List<Point> toPoints(List<TrackPoint> trackPoints) {
        if (trackPoints == null || trackPoints.isEmpty()) {
            return Collections.emptyList();
        }
        return trackPoints.stream()
                .map(PointConverter::toPoint)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换：List<Point> -> List<TrackPoint>
     */
    public static List<TrackPoint> toTrackPoints(List<Point> points, Long trackId) {
        if (points == null || points.isEmpty()) {
            return Collections.emptyList();
        }
        return points.stream()
                .map(point -> toTrackPoint(point, trackId))
                .collect(Collectors.toList());
    }

    /**
     * LocalDateTime 转 时间戳（毫秒）
     */
    private static long toTimestamp(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 时间戳（毫秒）转 LocalDateTime
     */
    private static LocalDateTime toLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
}