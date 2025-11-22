package com.track.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.track.dto.PageResponse;
import com.track.dto.TrackDetail;
import com.track.entity.Track;

import java.time.LocalDate;
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
     * 获取轨迹详情，包含轨迹点列表（权限验证已通过AOP处理）
     * @param trackId 轨迹ID
     * @return 轨迹详情对象
     */
    TrackDetail getTrackDetail(Long trackId, Long userId);

    /**
     * 根据ID获取轨迹（无权限验证，用于注解方式）
     * @param id 轨迹ID
     * @return 轨迹对象
     */
    Track getById(Long id);

    /**
     * 搜索轨迹（支持关键字和日期范围查询）
     * @param userId 用户ID
     * @param keyword 搜索关键字（轨迹名称和描述）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页响应对象
     */
    PageResponse<Track> searchTracks(Long userId, String keyword, LocalDate startDate, LocalDate endDate, int page, int pageSize);
}