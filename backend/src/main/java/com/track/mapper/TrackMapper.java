package com.track.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.track.entity.Track;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TrackMapper extends BaseMapper<Track> {
}