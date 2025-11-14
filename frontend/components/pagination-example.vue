<template>
  <view class="pagination-example">
    <!-- 搜索区域 -->
    <view class="search-section">
      <view class="search-box">
        <uni-icons type="search" size="20" color="#999"></uni-icons>
        <input
          class="search-input"
          placeholder="搜索..."
          v-model="searchKeyword"
          @input="handleSearch"
        />
      </view>
    </view>

    <!-- 数据列表 -->
    <view class="data-list">
      <!-- 空状态 -->
      <view v-if="filteredData.length === 0 && !loading" class="empty-state">
        <text class="empty-text">{{ searchKeyword ? '未找到相关数据' : '暂无数据' }}</text>
      </view>

      <!-- 数据项 -->
      <view
        v-for="(item, index) in filteredData"
        :key="item.id || index"
        class="data-item"
      >
        <text class="item-name">{{ item.name }}</text>
        <text class="item-description">{{ item.description }}</text>
      </view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-state">
      <uni-load-more status="loading" content="正在加载..."></uni-load-more>
    </view>

    <!-- 没有更多数据 -->
    <view v-if="!hasMore && filteredData.length > 0" class="no-more">
      <text>没有更多数据了</text>
    </view>
  </view>
</template>

<script>
import { createPaginationManager, createSearchManager } from '@/utils/pagination.js'

export default {
  name: 'PaginationExample',
  props: {
    // 数据API函数
    apiCall: {
      type: Function,
      required: true
    },
    // 搜索字段
    searchFields: {
      type: Array,
      default: () => ['name', 'description']
    },
    // 每页大小
    pageSize: {
      type: Number,
      default: 10
    }
  },
  data() {
    return {
      searchKeyword: '',
      loading: false,
      hasMore: true,
      total: 0,
      data: [],
      filteredData: [],
      paginationManager: null,
      searchManager: null
    }
  },

  created() {
    this.initManagers()
    this.loadData()
  },

  methods: {
    // 初始化管理器
    initManagers() {
      // 分页管理器
      this.paginationManager = createPaginationManager({
        page: 1,
        pageSize: this.pageSize
      })

      // 设置分页状态变化回调
      this.paginationManager.setStateChangeCallback((state) => {
        this.loading = state.loading
        this.hasMore = state.hasMore
        this.total = state.total
        this.data = state.data

        // 更新搜索管理器的数据
        if (this.searchManager) {
          this.searchManager.setAllData(state.data)
        }
      })

      // 搜索管理器
      this.searchManager = createSearchManager()

      // 设置搜索结果回调
      this.searchManager.setSearchCallback((filteredData) => {
        this.filteredData = filteredData
      })
    },

    // 加载数据
    async loadData() {
      try {
        await this.paginationManager.loadFirstPage(this.apiCall)
      } catch (error) {
        console.error('加载数据失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    },

    // 加载更多数据
    async loadMore() {
      try {
        await this.paginationManager.loadMore(this.apiCall)
      } catch (error) {
        console.error('加载更多数据失败:', error)
      }
    },

    // 搜索
    handleSearch() {
      this.searchManager.search(this.searchKeyword, this.searchFields)
    },

    // 刷新数据
    async refresh() {
      this.searchKeyword = ''
      this.searchManager.clear()
      await this.loadData()
      uni.showToast({
        title: '刷新成功',
        icon: 'success'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.pagination-example {
  padding: 20rpx;
}

.search-section {
  margin-bottom: 30rpx;
}

.search-box {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 50rpx;
  padding: 20rpx 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.search-input {
  flex: 1;
  margin-left: 20rpx;
  font-size: 28rpx;
  color: #333;
}

.data-list {
  min-height: 200rpx;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 100rpx 40rpx;
  color: #666;
  background: #fff;
  border-radius: 20rpx;
}

.empty-text {
  font-size: 28rpx;
}

.data-item {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.item-name {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 10rpx;
}

.item-description {
  font-size: 26rpx;
  color: #666;
}

.loading-state,
.no-more {
  text-align: center;
  padding: 40rpx 0;
  color: #666;
}
</style>