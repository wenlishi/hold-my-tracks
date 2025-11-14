package com.track.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.track.entity.TrackPoint;

import java.util.List;

public interface TrackPointService extends IService<TrackPoint> {

    List<TrackPoint> findByTrackId(Long trackId);
}