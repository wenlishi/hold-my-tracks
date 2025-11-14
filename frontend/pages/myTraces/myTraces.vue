<template>
  <view class="container">
    <view class="custom-header">
      <text class="header-title">我的轨迹</text>
    </view>

    <!-- 搜索和筛选区域（固定） -->
    <view class="search-fixed-container">
      <search-form
        ref="searchForm"
        :initial-params="searchParams"
        @search="handleAdvancedSearch"
        @reset="resetSearch"
        @keyword-change="handleKeywordChange"
        @start-date-change="handleStartDateChange"
        @end-date-change="handleEndDateChange"
        @expand="handleSearchExpand"
        @collapse="handleSearchCollapse"
      />
    </view>

    <!-- 轨迹列表 -->
    <view class="traces-container" :class="{ 'expanded-margin': searchExpanded }">
      <!-- 空状态 -->
      <view v-if="filteredTraces.length === 0 && !loading" class="empty-state">
        <view class="empty-illustration">
          <uni-icons type="map" size="120" color="#e0e0e0"></uni-icons>
        </view>
        <text class="empty-text">
          {{ paginationManager && paginationManager.hasSearchConditions() ? '未找到符合条件的轨迹' : '暂无轨迹记录' }}
        </text>
        <text class="empty-hint">
          {{ paginationManager && paginationManager.hasSearchConditions() ? '请尝试其他搜索条件' : '开始创建您的第一条轨迹吧' }}
        </text>
        <view v-if="!paginationManager || !paginationManager.hasSearchConditions()" class="empty-action">
          <button class="create-btn" @click="gotoCreateTrack">创建第一条轨迹</button>
        </view>
      </view>

      <!-- 轨迹卡片 -->
      <view class="traces-grid">
        <view
          class="trace-card"
          v-for="(track, index) in filteredTraces"
          :key="track.id || index"
          :class="{ 'expanded': expandedCards[track.id || track.trackMsg] }"
        >
          <!-- 基本信息区域（始终显示） -->
          <view class="card-basic" @click="toggleCard(track)">
            <view class="basic-header">
              <view class="track-info">
                <text class="track-name">{{ track.trackName || track.lineName }}</text>
                <view class="track-status" :class="getStatusClass(track.status)">
                  {{ getStatusText(track.status) }}
                </view>
              </view>
              <view class="header-actions">
                <view class="expand-icon">
                  <uni-icons
                    :type="expandedCards[track.id || track.trackMsg] ? 'arrowup' : 'arrowdown'"
                    size="20"
                    color="#999">
                  </uni-icons>
                </view>
              </view>
            </view>

            <!-- 基本信息统计 -->
            <view class="basic-stats">
              <view class="stat-item">
                <uni-icons type="location-filled" size="16" color="#007AFF"></uni-icons>
                <text class="stat-text">{{ track.totalPoints || 0 }} 个点</text>
              </view>
              <view class="stat-item">
                <uni-icons type="map" size="16" color="#34C759"></uni-icons>
                <text class="stat-text">{{ formatDistance(track.totalDistance) }}</text>
              </view>
              <view class="stat-item">
                <uni-icons type="calendar" size="16" color="#8E8E93"></uni-icons>
                <text class="stat-text">{{ formatCreateTime(track.createTime) }}</text>
              </view>
            </view>
          </view>

          <!-- 详细信息区域（展开时显示） -->
          <view v-if="expandedCards[track.id || track.trackMsg]" class="card-details">
            <!-- 轨迹描述 -->
            <view class="detail-section">
              <text class="detail-label">轨迹描述</text>
              <text class="track-description">
                {{ track.description || '暂无描述' }}
              </text>
            </view>

            <!-- 操作按钮 -->
            <view class="detail-section">
              <view class="card-actions">
                <view
                  class="action-btn blue"
                  @click.stop="gotoVisualization(track.id || track.trackMsg)"
                >
                  <uni-icons type="compose" size="16" color="#666"></uni-icons>
                  <text>可视化</text>
                </view>
                <view
                  class="action-btn delete"
                  @click.stop="showDeleteConfirm(track)"
                >
                  <uni-icons type="trash" size="16" color="#ff4757"></uni-icons>
                  <text>删除</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-state">
      <uni-load-more status="loading" content="正在加载..."></uni-load-more>
    </view>

    <!-- 没有更多数据 -->
    <view v-if="!hasMore && filteredTraces.length > 0" class="no-more">
      <text>没有更多轨迹了</text>
    </view>

    <!-- 刷新按钮 -->
    <view v-if="showRefreshButton" class="refresh-btn" @click="refreshTraces">
      <uni-icons type="refresh" size="20" color="#007AFF"></uni-icons>
      <text>刷新轨迹</text>
    </view>
  </view>
</template>

<script>
import api from '@/utils/api.js'
import { createPaginationManager, createSearchManager } from '@/utils/pagination.js'
import SearchForm from '@/components/search-form.vue'

export default {
  components: {
    SearchForm
  },
  data() {
    return {
      tracks: [],
      filteredTraces: [],
      searchKeyword: '',
      showRefreshButton: false,
      expandedCards: {}, // 记录每个卡片的展开状态
      // 搜索参数
      searchParams: {
        keyword: '',
        startDate: '',
        endDate: ''
      },
      // 分页管理器实例
      paginationManager: null,
      searchManager: null,
      // 搜索组件展开状态
      searchExpanded: false
    }
  },

  onLoad() {
    this.initManagers()
    this.loadTraces()
  },

  onReady() {
    // 确保组件已挂载后再初始化
    this.initManagers()
  },

  onPullDownRefresh() {
    this.refreshTraces()
    uni.stopPullDownRefresh()
  },

  onReachBottom() {
    if (this.hasMore && !this.loading) {
      this.loadMoreTraces()
    }
  },

  methods: {
    // 初始化管理器
    initManagers() {
      if (!this.paginationManager) {
        this.paginationManager = createPaginationManager({
          page: 1,
          pageSize: 10,
          searchParams: this.searchParams
        })

        // 设置分页状态变化回调
        this.paginationManager.setStateChangeCallback((state) => {
          this.loading = state.loading
          this.hasMore = state.hasMore
          this.total = state.total
          this.tracks = state.data

          // 更新搜索管理器的数据
          if (this.searchManager) {
            this.searchManager.setAllData(state.data)
          }
        })

        // 设置搜索参数变化回调
        this.paginationManager.setSearchParamsChangeCallback((params) => {
          this.searchParams = { ...params }
        })
      }

      if (!this.searchManager) {
        this.searchManager = createSearchManager()

        // 设置搜索结果回调
        this.searchManager.setSearchCallback((filteredData) => {
          this.filteredTraces = filteredData
        })
      }
    },

    // 加载轨迹列表
    async loadTraces() {
      try {
        await this.paginationManager.loadFirstPage(async (page, pageSize) => {
          let response

          // 检查是否有搜索条件
          const hasSearchConditions = this.paginationManager.hasSearchConditions()

          if (hasSearchConditions) {
            // 使用搜索API
            try {
              response = await api.trackApi.searchTracks({
                page,
                pageSize,
                keyword: this.searchParams.keyword,
                startDate: this.searchParams.startDate,
                endDate: this.searchParams.endDate
              })
            } catch (error) {
              console.warn('搜索API调用失败，使用普通分页API:', error)
              // 如果搜索API失败，使用普通分页API
              response = await api.trackApi.getTracksWithPagination(page, pageSize)
            }
          } else {
            // 使用普通分页API
            try {
              response = await api.trackApi.getTracksWithPagination(page, pageSize)
            } catch (error) {
              console.warn('分页API调用失败，使用非分页API:', error)
              // 如果分页API失败，使用非分页API
              response = await api.trackApi.getTracks()
            }
          }

          // 为当前页的轨迹获取详细的统计信息，包括距离
          let currentPageTracks = []
          if (response && (response.data !== undefined || response.total !== undefined)) {
            // 后端支持分页，返回PageResponse格式
            const pageData = response || {}
            currentPageTracks = pageData.data || []
          } else {
            // 后端返回的是数组（兼容旧版本），使用前端分页
            const allTracks = response || []
            const startIndex = (page - 1) * pageSize
            const endIndex = startIndex + pageSize
            currentPageTracks = allTracks.slice(startIndex, endIndex)
          }

          const tracksWithStats = await this.getTracksWithStats(currentPageTracks)

          // 返回处理后的数据
          return {
            data: tracksWithStats,
            total: response?.total || response?.length || 0
          }
        })

      } catch (error) {
        console.error('加载轨迹列表失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    },

    // 获取轨迹的统计信息
    async getTracksWithStats(tracks) {
      const tracksWithStats = []

      for (const track of tracks) {
        try {
          // 获取轨迹详情，包含统计信息
          const trackDetail = await api.trackApi.getTrackDetail(track.id || track.trackMsg)

          // 合并轨迹信息和统计信息
          tracksWithStats.push({
            ...track,
            totalDistance: trackDetail.stats?.totalDistance || 0,
            totalPoints: trackDetail.stats?.totalPoints || 0
          })
        } catch (error) {
          console.error(`获取轨迹 ${track.id || track.trackMsg} 详情失败:`, error)
          // 如果获取详情失败，使用原始数据
          tracksWithStats.push({
            ...track,
            totalDistance: track.totalDistance || 0,
            totalPoints: track.totalPoints || 0
          })
        }
      }

      return tracksWithStats
    },


    // 加载更多轨迹
    async loadMoreTraces() {
      try {
        await this.paginationManager.loadMore(async (page, pageSize) => {
          let response

          // 检查是否有搜索条件
          const hasSearchConditions = this.paginationManager.hasSearchConditions()

          if (hasSearchConditions) {
            // 使用搜索API
            try {
              response = await api.trackApi.searchTracks({
                page,
                pageSize,
                keyword: this.searchParams.keyword,
                startDate: this.searchParams.startDate,
                endDate: this.searchParams.endDate
              })
            } catch (error) {
              console.warn('搜索API调用失败，使用普通分页API:', error)
              // 如果搜索API失败，使用普通分页API
              response = await api.trackApi.getTracksWithPagination(page, pageSize)
            }
          } else {
            // 使用普通分页API
            try {
              response = await api.trackApi.getTracksWithPagination(page, pageSize)
            } catch (error) {
              console.warn('分页API调用失败，使用非分页API:', error)
              // 如果分页API失败，使用非分页API
              response = await api.trackApi.getTracks()
            }
          }

          // 为当前页的轨迹获取详细的统计信息，包括距离
          let currentPageTracks = []
          if (response && (response.data !== undefined || response.total !== undefined)) {
            // 后端支持分页，返回PageResponse格式
            const pageData = response || {}
            currentPageTracks = pageData.data || []
          } else {
            // 后端返回的是数组（兼容旧版本），使用前端分页
            const allTracks = response || []
            const startIndex = (page - 1) * pageSize
            const endIndex = startIndex + pageSize
            currentPageTracks = allTracks.slice(startIndex, endIndex)
          }

          const tracksWithStats = await this.getTracksWithStats(currentPageTracks)

          // 返回处理后的数据
          return {
            data: tracksWithStats,
            total: response?.total || response?.length || 0
          }
        })

      } catch (error) {
        console.error('加载更多轨迹失败:', error)
      }
    },

    // 刷新轨迹
    async refreshTraces() {
      this.searchKeyword = ''
      this.searchManager.clear()
      await this.loadTraces()
      uni.showToast({
        title: '刷新成功',
        icon: 'success'
      })
    },

    // 高级搜索
    async handleAdvancedSearch(params) {
      // 更新搜索参数
      this.paginationManager.updateSearchParams(params)

      // 重新加载数据
      await this.loadTraces()

      uni.showToast({
        title: '搜索成功',
        icon: 'success'
      })
    },

    // 重置搜索
    async resetSearch() {
      this.paginationManager.resetSearchParams()

      // 重新加载数据
      await this.loadTraces()

      uni.showToast({
        title: '已重置搜索',
        icon: 'success'
      })
    },

    // 关键字变化
    handleKeywordChange(keyword) {
      this.paginationManager.updateSearchParams({ keyword })
    },

    // 开始日期变化
    handleStartDateChange(startDate) {
      this.paginationManager.updateSearchParams({ startDate })
    },

    // 结束日期变化
    handleEndDateChange(endDate) {
      this.paginationManager.updateSearchParams({ endDate })
    },

    // 搜索展开
    handleSearchExpand() {
      this.searchExpanded = true
      console.log('搜索组件展开')
    },

    // 搜索收起
    handleSearchCollapse() {
      this.searchExpanded = false
      console.log('搜索组件收起')
    },

    // 搜索轨迹（兼容旧版本）
    handleSearch() {
      this.searchManager.search(this.searchKeyword, ['trackName', 'lineName', 'description'])
    },

    // 查看轨迹详情
    viewTrackDetail(track) {
      console.log('查看轨迹详情:', track)
      // 可以跳转到轨迹详情页
      // uni.navigateTo({
      //   url: `/pages/trackDetail/trackDetail?id=${track.id}`
      // })
    },

    // 切换卡片展开状态
    toggleCard(track) {
      const trackId = track.id || track.trackMsg
      this.$set(this.expandedCards, trackId, !this.expandedCards[trackId])
    },

    // 跳转到可视化
    gotoVisualization(trackId) {
      console.log('跳转到可视化:', trackId)
      uni.navigateTo({
        url: `/pages/trackDetail/trackDetail?id=${trackId}`
      })
    },

    // 跳转到创建轨迹页面
    gotoCreateTrack() {
      uni.navigateTo({
        url: '/pages/secondaryPage/addNewTrack/addNewTrack'
      })
    },

    // 显示删除确认对话框
    showDeleteConfirm(track) {
      const trackName = track.trackName || track.lineName
      uni.showModal({
        title: '确认删除',
        content: `确定要删除轨迹"${trackName}"吗？此操作不可恢复。`,
        confirmColor: '#ff4757',
        success: (res) => {
          if (res.confirm) {
            this.deleteTrack(track)
          }
        }
      })
    },

    // 删除轨迹
    async deleteTrack(track) {
      try {
        uni.showLoading({
          title: '删除中...',
          mask: true
        })

        const trackId = track.id || track.trackMsg

        // 调用后端API删除轨迹
        await api.trackApi.deleteTrack(trackId)

        uni.hideLoading()
        uni.showToast({
          title: '删除成功',
          icon: 'success',
          duration: 1500
        })

        // 使用分页管理器删除轨迹
        this.paginationManager.removeData(t => (t.id || t.trackMsg) !== trackId)

        // 同时更新本地存储
        const localTracks = uni.getStorageSync('tracks') || []
        const updatedLocalTracks = localTracks.filter(t =>
          (t.id || t.trackMsg) !== trackId
        )
        uni.setStorageSync('tracks', updatedLocalTracks)

      } catch (error) {
        uni.hideLoading()
        console.error('删除轨迹失败:', error)
        uni.showToast({
          title: '删除失败，请重试',
          icon: 'error',
          duration: 2000
        })
      }
    },

    // 获取状态类名
    getStatusClass(status) {
      const statusMap = {
        1: 'status-active',
        2: 'status-completed',
        0: 'status-inactive'
      }
      return statusMap[status] || 'status-inactive'
    },

    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        1: '进行中',
        2: '已完成',
        0: '已结束'
      }
      return statusMap[status] || '未知'
    },

    // 格式化距离
    formatDistance(distance) {
      if (distance === null || distance === undefined) {
        // 如果距离为null或undefined，显示为计算中
        return '计算中'
      }
      const num = parseFloat(distance)
      if (num < 1000) {
        return num.toFixed(0) + ' m'
      } else {
        return (num / 1000).toFixed(2) + ' km'
      }
    },

    // 格式化创建时间
    formatCreateTime(createTime) {
      if (!createTime) return '未知时间'

      try {
        const date = new Date(createTime)
        // 显示具体的日期和时间
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')

        return `${year}-${month}-${day} ${hours}:${minutes}`
      } catch (error) {
        return '未知时间'
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #f8f9fa;
  padding: 0 0 120rpx 0;
  box-sizing: border-box;
  position: relative;
  z-index: 1;
}

/* 自定义header样式 - 无返回按钮 */
.custom-header {
  text-align: center;
  line-height: 200rpx;
  font-size: 34rpx;
  font-weight: 600;
  top: 0;
  color: #ffffff;
  position: fixed;
  z-index: 2;
  height: 150rpx;
  width: 100vw;
  background: #037CD5;
}

.header-title {
  display: block;
}


// 搜索区域
.search-section {
  margin-bottom: 30rpx;
  padding: 0 20rpx;
}

.search-box {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50rpx;
  padding: 20rpx 30rpx;
  backdrop-filter: blur(10px);
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.search-input {
  flex: 1;
  margin-left: 20rpx;
  font-size: 28rpx;
  color: #333;
}

// 固定搜索容器
.search-fixed-container {
  position: fixed;
  top: 160rpx; /* 再往下移动10rpx */
  left: 0;
  right: 0;
  z-index: 100; /* 增加z-index确保在最上层 */
  padding: 0 20rpx;
  background: transparent;
  height: auto;
}

// 轨迹容器
.traces-container {
  min-height: 400rpx;
  padding: 0 20rpx;
  margin-top: 300rpx; /* 为固定搜索区域留出更多空间 */
  transition: margin-top 0.3s ease; /* 添加过渡动画 */
}

// 当搜索组件展开时的轨迹容器样式
.traces-container.expanded-margin {
  margin-top: 600rpx; /* 搜索展开时增加更多间距 */
}

// 搜索表单在固定容器内的样式
.search-fixed-container search-form {
  margin-bottom: 0;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 150rpx 40rpx;
  color: #666;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 20rpx;
  margin: 40rpx 0;
  backdrop-filter: blur(10px);
}

.empty-illustration {
  margin-bottom: 40rpx;
  opacity: 0.6;
}

.empty-text {
  font-size: 32rpx;
  margin-bottom: 16rpx;
  font-weight: 500;
  text-align: center;
}

.empty-hint {
  font-size: 26rpx;
  margin-bottom: 40rpx;
  opacity: 0.7;
  text-align: center;
}

.empty-action {
  margin-top: 20rpx;
}

.create-btn {
  background: #007AFF;
  color: #fff;
  border: none;
  border-radius: 50rpx;
  padding: 20rpx 40rpx;
  font-size: 28rpx;
  font-weight: 500;
  box-shadow: 0 4rpx 20rpx rgba(0, 122, 255, 0.3);
}

.create-btn:active {
  opacity: 0.8;
  transform: scale(0.98);
}

// 轨迹网格
.traces-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 30rpx;
}

// 轨迹卡片
.trace-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20rpx;
  backdrop-filter: blur(10px);
  box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1rpx solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
}

.trace-card.expanded {
  box-shadow: 0 12rpx 50rpx rgba(0, 0, 0, 0.15);
}

// 基本信息区域
.card-basic {
  padding: 30rpx;
  cursor: pointer;
}

.basic-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.expand-icon {
  padding: 8rpx;
  transition: transform 0.3s ease;
}

.trace-card.expanded .expand-icon {
  transform: rotate(180deg);
}

.basic-stats {
  display: flex;
  gap: 30rpx;
  flex-wrap: wrap;
}

.basic-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.basic-stats .stat-text {
  font-size: 24rpx;
  color: #666;
}

// 详细信息区域
.card-details {
  border-top: 1rpx solid #f0f0f0;
  padding: 30rpx;
  background: rgba(248, 249, 250, 0.5);
}

.detail-section {
  margin-bottom: 30rpx;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-label {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 16rpx;
}

.track-description {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  display: block;
}



.track-name {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 10rpx;
}

.track-status {
  display: inline-block;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.status-active {
  background: #e6f7ff;
  color: #1890ff;
}

.status-completed {
  background: #f6ffed;
  color: #52c41a;
}

.status-inactive {
  background: #f5f5f5;
  color: #8c8c8c;
}

// 卡片内容
.card-content {
  margin-bottom: 25rpx;
}

.track-description {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

// 统计信息
.card-stats {
  display: flex;
  gap: 30rpx;
  margin-bottom: 25rpx;
  padding-bottom: 25rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.stat-text {
  font-size: 24rpx;
  color: #666;
}

// 操作按钮
.card-actions {
  display: flex;
  gap: 15rpx;
  margin-bottom: 20rpx;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 16rpx;
  border-radius: 12rpx;
  font-size: 24rpx;
  font-weight: 500;
  transition: all 0.2s ease;
}

.action-btn.blue {
  background: #f5f5f5;
  color: #666;
  border: 1rpx solid #e0e0e0;
}

.action-btn.blue:active {
  background: #e8e8e8;
}

.action-btn.delete {
  background: rgba(255, 71, 87, 0.05);
  color: #ff4757;
  border: 1rpx solid rgba(255, 71, 87, 0.3);
}

.action-btn.delete:active {
  background: rgba(255, 71, 87, 0.1);
}


// 卡片底部
.card-footer {
  text-align: center;
}

.create-time {
  font-size: 22rpx;
  color: #999;
}

// 加载状态
.loading-state,
.no-more {
  text-align: center;
  padding: 40rpx 0;
  color: #666;
}

// 刷新按钮
.refresh-btn {
  position: fixed;
  bottom: 40rpx;
  right: 40rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  background: rgba(255, 255, 255, 0.9);
  padding: 20rpx 30rpx;
  border-radius: 50rpx;
  backdrop-filter: blur(10px);
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.15);
  font-size: 26rpx;
  color: #007AFF;
  font-weight: 500;
}

.refresh-btn:active {
  transform: scale(0.95);
}

// 响应式设计
@media (min-width: 768px) {
  .traces-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .traces-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>