package com.track.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.track.dto.PageResponse;
import com.track.dto.TrackDetail;
import com.track.entity.Track;

import java.util.List;

public interface TrackService extends IService<Track> {

    List<Track> findByUserId(Long userId);

    /**
     * 分页查询用户轨迹
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页响应对象
     */
    PageResponse<Track> findByUserIdWithPagination(Long userId, int page, int pageSize);

    Track findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    void updateTotalPoints(Long trackId);

    /**
     * 检查用户是否有进行中的轨迹
     * @param userId 用户ID
     * @return 如果有进行中的轨迹返回true，否则返回false
     */
    boolean hasActiveTrack(Long userId);

    /**
     * 删除轨迹及其关联的轨迹点数据
     * @param trackId 轨迹ID
     * @return 删除成功返回true，失败返回false
     */
    boolean removeTrackWithPoints(Long trackId);

    /**
     * 获取轨迹详情，包含轨迹点列表
     * @param trackId 轨迹ID
     * @param userId 用户ID
     * @return 轨迹详情对象
     */
    TrackDetail getTrackDetail(Long trackId, Long userId);
}