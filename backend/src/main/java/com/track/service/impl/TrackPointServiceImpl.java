package com.track.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.track.entity.TrackPoint;
import com.track.mapper.TrackPointMapper;
import com.track.service.TrackPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackPointServiceImpl extends ServiceImpl<TrackPointMapper, TrackPoint> implements TrackPointService {

    @Autowired
    private TrackPointMapper trackPointMapper;

    @Override
    public List<TrackPoint> findByTrackId(Long trackId) {
        QueryWrapper<TrackPoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("track_id", trackId);
        queryWrapper.orderByAsc("create_time");
        return trackPointMapper.selectList(queryWrapper);
    }
}