package com.track.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.track.dto.PageResponse;
import com.track.dto.TrackDetail;
import com.track.entity.Track;
import com.track.entity.TrackPoint;
import com.track.mapper.TrackMapper;
import com.track.service.TrackPointService;
import com.track.service.TrackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class TrackServiceImpl extends ServiceImpl<TrackMapper, Track> implements TrackService {

    private static final Logger log = LoggerFactory.getLogger(TrackServiceImpl.class);

    // 这里无需注入一个trackMpper，因为继承的ServiceImpl类中已经有注入一个TrackMapper类的baseMapper，可以直接使用
    // @Autowired
    // private TrackMapper trackMapper;

    @Autowired
    private TrackPointService trackPointService;

    @Override
    public List<Track> findByUserId(Long userId) {
        QueryWrapper<Track> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public PageResponse<Track> findByUserIdWithPagination(Long userId, int page, int pageSize) {
        QueryWrapper<Track> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");

        // 创建分页对象
        Page<Track> trackPage = new Page<>(page, pageSize);

        // 执行分页查询
        Page<Track> resultPage = baseMapper.selectPage(trackPage, queryWrapper);

        // 构建分页响应
        return new PageResponse<>(
            resultPage.getRecords(),
            resultPage.getTotal(),
            (int) resultPage.getCurrent(),
            (int) resultPage.getSize()
        );
    }

    /**
     * 根据 轨迹ID 和 用户ID 联合查询数据
     * <p>
     * 核心目的：不仅是查询数据，更是为了进行【数据归属权验证】。
     * 如果通过 ID 查到了数据，但 user_id 对不上，也会返回 null，从而防止水平越权。
     * * @param id 轨迹的主键 ID
     * @param userId 当前登录用户的 ID
     * @return 如果找到且属于该用户，返回 Track 对象；否则返回 null
     */
    @Override
    public Track findByIdAndUserId(Long id, Long userId) {
        // 1.创建MyBatis-Plus的条件构造器

        LambdaQueryWrapper<Track> queryWrapper = new LambdaQueryWrapper<>();
        // 2. 拼接 SQL: WHERE id = ?
        // 精确匹配资源的主键
        queryWrapper.eq(Track::getId, id);
        // 3. 拼接 SQL: AND user_id = ?
        // 【关键一步】强制限制只能查询归属于当前 userId 的数据
        // 如果数据存在但 user_id 不匹配，这里就查不出来
        queryWrapper.eq(Track::getUserId, userId);
        // 4. 执行查询
        // 最终 SQL: SELECT * FROM track WHERE id = ? AND user_id = ? LIMIT 1
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 使用LambdaQueryWrapper的Track::getUserId的写法可以
     * 把 getter 方法的引用传给框架，让框架反推出数据库字段名，从而避免手写字符串导致的拼写错误。
     * 这个双冒号 :: 是 Java 8 引入的一个非常重要的语法糖，它的学名叫做 “方法引用” (Method Reference)。
     * 意思是：不是要现在调用这个方法，而是要把这个方法本身当做一个参数传过去。
     * 是推荐的写法
     */
    @Override
    public boolean existsByIdAndUserId(Long id, Long userId) {
        // 使用 LambdaQueryWrapper 避免硬编码字段名 ("user_id")
        LambdaQueryWrapper<Track> queryWrapper = new LambdaQueryWrapper<>();
        
        // Track::getId -> 自动映射为 SQL 的 id 字段
        queryWrapper.eq(Track::getId, id);
        
        // Track::getUserId -> 自动映射为 SQL 的 user_id 字段
        queryWrapper.eq(Track::getUserId, userId);
        return baseMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public void updateTotalPoints(Long trackId) {
        // 查询轨迹点的总数
        QueryWrapper<TrackPoint> pointQueryWrapper = new QueryWrapper<>();
        pointQueryWrapper.eq("track_id", trackId);
        int totalPoints = Math.toIntExact(trackPointService.count(pointQueryWrapper));

        // 更新轨迹的总点数
        Track track = new Track();
        track.setId(trackId);
        track.setTotalPoints(totalPoints);
        this.updateById(track);
    }

    @Override
    public boolean hasActiveTrack(Long userId) {
        QueryWrapper<Track> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("status", 1); // 状态为1表示进行中
        return baseMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    @Transactional
    public boolean removeTrackWithPoints(Long trackId) {
        try {
            // 先删除关联的轨迹点数据
            QueryWrapper<TrackPoint> pointQueryWrapper = new QueryWrapper<>();
            pointQueryWrapper.eq("track_id", trackId);
            trackPointService.remove(pointQueryWrapper);

            // 再删除轨迹记录
            return this.removeById(trackId);
        } catch (Exception e) {
            throw new RuntimeException("删除轨迹失败: " + e.getMessage(), e);
        }
    }


    /**
     * 计算轨迹统计信息
     */
    private TrackDetail.TrackStats calculateTrackStats(List<TrackPoint> trackPoints) {
        TrackDetail.TrackStats stats = new TrackDetail.TrackStats();

        if (trackPoints == null || trackPoints.isEmpty()) {
            return stats;
        }

        // 计算总点数
        stats.setTotalPoints(trackPoints.size());

        // 计算速度统计
        BigDecimal totalSpeed = BigDecimal.ZERO;
        BigDecimal maxSpeed = BigDecimal.ZERO;
        BigDecimal minAltitude = null;
        BigDecimal maxAltitude = null;

        for (TrackPoint point : trackPoints) {
            // 速度统计
            if (point.getSpeed() != null) {
                totalSpeed = totalSpeed.add(point.getSpeed());
                if (point.getSpeed().compareTo(maxSpeed) > 0) {
                    maxSpeed = point.getSpeed();
                }
            }

            // 海拔统计
            if (point.getAltitude() != null) {
                if (minAltitude == null || point.getAltitude().compareTo(minAltitude) < 0) {
                    minAltitude = point.getAltitude();
                }
                if (maxAltitude == null || point.getAltitude().compareTo(maxAltitude) > 0) {
                    maxAltitude = point.getAltitude();
                }
            }
        }

        // 平均速度
        if (trackPoints.size() > 0) {
            stats.setAverageSpeed(totalSpeed.divide(BigDecimal.valueOf(trackPoints.size()), 2, BigDecimal.ROUND_HALF_UP));
        }

        stats.setMaxSpeed(maxSpeed);

        // 海拔变化
        if (minAltitude != null && maxAltitude != null) {
            stats.setAltitudeChange(maxAltitude.subtract(minAltitude));
        }

        // 计算轨迹长度（简化的直线距离计算）
        BigDecimal totalDistance = BigDecimal.ZERO;
        for (int i = 1; i < trackPoints.size(); i++) {
            TrackPoint prev = trackPoints.get(i - 1);
            TrackPoint curr = trackPoints.get(i);

            if (prev.getLatitude() != null && prev.getLongitude() != null &&
                curr.getLatitude() != null && curr.getLongitude() != null) {

                double distance = calculateDistance(
                    prev.getLatitude().doubleValue(), prev.getLongitude().doubleValue(),
                    curr.getLatitude().doubleValue(), curr.getLongitude().doubleValue()
                );
                totalDistance = totalDistance.add(BigDecimal.valueOf(distance));
            }
        }
        stats.setTotalDistance(totalDistance);

        return stats;
    }

    /**
     * 计算两点之间的距离（米）- 使用Haversine公式
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 地球半径（米）

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    @Override
    public PageResponse<Track> searchTracks(Long userId, String keyword, LocalDate startDate, LocalDate endDate, int page, int pageSize) {
        QueryWrapper<Track> queryWrapper = new QueryWrapper<>();

        // 必须属于当前用户
        queryWrapper.eq("user_id", userId);

        // 关键字搜索（轨迹名称和描述）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("track_name", keyword.trim())
                .or()
                .like("description", keyword.trim())
            );
        }

        // 日期范围查询（基于创建时间）
        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            queryWrapper.ge("create_time", startDateTime);
        }
        if (endDate != null) {
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            queryWrapper.le("create_time", endDateTime);
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc("create_time");

        // 创建分页对象
        Page<Track> trackPage = new Page<>(page, pageSize);

        // 执行分页查询
        Page<Track> resultPage = baseMapper.selectPage(trackPage, queryWrapper);

        // 构建分页响应
        return new PageResponse<>(
            resultPage.getRecords(),
            resultPage.getTotal(),
            (int) resultPage.getCurrent(),
            (int) resultPage.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TrackDetail getTrackDetail(Long trackId, Long userId) {
        // 获取轨迹信息（权限验证已通过AOP处理）
        Track track = this.getById(trackId);
        if (track == null) {
            return null;
        }

        // 获取轨迹点列表
        List<TrackPoint> trackPoints = trackPointService.findByTrackId(trackId);

        // 创建轨迹详情对象
        TrackDetail trackDetail = new TrackDetail();
        trackDetail.setTrack(track);
        trackDetail.setTrackPoints(trackPoints);

        // 计算统计信息
        TrackDetail.TrackStats stats = calculateTrackStats(trackPoints);
        trackDetail.setStats(stats);

        return trackDetail;
    }

    @Override
    @Transactional(readOnly = true)
    public TrackDetail getCompressedTrackDetail(Long trackId, Long userId, double tolerance) {
        // 获取轨迹信息（权限验证已通过AOP处理）
        Track track = this.getById(trackId);
        if (track == null) {
            return null;
        }

        // 获取原始轨迹点列表（用于统计计算）
        List<TrackPoint> rawTrackPoints = trackPointService.findByTrackId(trackId);

        // 获取压缩后的轨迹点列表（用于展示）
        List<TrackPoint> compressedTrackPoints = trackPointService.getCompressedPoints(trackId, tolerance);

        // 创建轨迹详情对象
        TrackDetail trackDetail = new TrackDetail();
        trackDetail.setTrack(track);
        trackDetail.setTrackPoints(compressedTrackPoints); // 使用压缩后的点

        // 计算统计信息（基于原始数据）
        TrackDetail.TrackStats stats = calculateTrackStats(rawTrackPoints);
        trackDetail.setStats(stats);

        log.info("轨迹 {} 压缩详情生成完成，原始点数: {}, 压缩后点数: {}, 压缩率: {:.2f}%",
                trackId, rawTrackPoints.size(), compressedTrackPoints.size(),
                (1.0 - (double) compressedTrackPoints.size() / rawTrackPoints.size()) * 100);

        return trackDetail;
    }

    @Override
    public Track getById(Long id) {
        return baseMapper.selectById(id);
    }
}