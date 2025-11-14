# 分页系统使用指南

## 概述

本分页系统提供了一套完整的解决方案，支持前端和后端分页，同时集成了搜索功能。系统包含以下核心组件：

- `PaginationManager` - 分页管理器
- `SearchManager` - 搜索管理器
- `SearchForm` - 搜索表单组件

## 快速开始

### 1. 基本分页使用

```javascript
import { createPaginationManager } from '@/utils/pagination.js'

// 创建分页管理器
export default {
  data() {
    return {
      paginationManager: null,
      loading: false,
      hasMore: true,
      total: 0,
      data: []
    }
  },

  created() {
    this.initPagination()
    this.loadData()
  },

  methods: {
    initPagination() {
      this.paginationManager = createPaginationManager({
        page: 1,
        pageSize: 10
      })

      // 设置状态变化回调
      this.paginationManager.setStateChangeCallback((state) => {
        this.loading = state.loading
        this.hasMore = state.hasMore
        this.total = state.total
        this.data = state.data
      })
    },

    async loadData() {
      try {
        await this.paginationManager.loadFirstPage(async (page, pageSize) => {
          const response = await api.getData({ page, pageSize })
          return {
            data: response.data,
            total: response.total
          }
        })
      } catch (error) {
        console.error('加载失败:', error)
      }
    },

    async loadMore() {
      try {
        await this.paginationManager.loadMore(async (page, pageSize) => {
          const response = await api.getData({ page, pageSize })
          return {
            data: response.data,
            total: response.total
          }
        })
      } catch (error) {
        console.error('加载更多失败:', error)
      }
    }
  }
}
```

### 2. 集成搜索功能

```vue
<template>
  <view>
    <!-- 搜索表单 -->
    <search-form
      :initial-params="searchParams"
      @search="handleSearch"
      @reset="resetSearch"
    />

    <!-- 数据列表 -->
    <view v-for="item in data" :key="item.id">
      {{ item.name }}
    </view>

    <!-- 加载状态 -->
    <view v-if="loading">加载中...</view>
    <view v-if="!hasMore && data.length > 0">没有更多数据了</view>
  </view>
</template>

<script>
import { createPaginationManager } from '@/utils/pagination.js'
import SearchForm from '@/components/search-form.vue'

export default {
  components: { SearchForm },
  data() {
    return {
      paginationManager: null,
      searchParams: {
        keyword: '',
        startDate: '',
        endDate: ''
      }
    }
  },

  methods: {
    initPagination() {
      this.paginationManager = createPaginationManager({
        page: 1,
        pageSize: 10,
        searchParams: this.searchParams
      })

      // 设置搜索参数变化回调
      this.paginationManager.setSearchParamsChangeCallback((params) => {
        this.searchParams = { ...params }
      })
    },

    async handleSearch(params) {
      // 更新搜索参数
      this.paginationManager.updateSearchParams(params)

      // 重新加载数据
      await this.loadData()
    },

    async resetSearch() {
      this.paginationManager.resetSearchParams()
      await this.loadData()
    },

    async loadData() {
      try {
        await this.paginationManager.loadFirstPage(async (page, pageSize) => {
          const hasSearch = this.paginationManager.hasSearchConditions()

          if (hasSearch) {
            // 使用搜索API
            const response = await api.searchData({
              page,
              pageSize,
              ...this.searchParams
            })
            return {
              data: response.data,
              total: response.total
            }
          } else {
            // 使用普通分页API
            const response = await api.getData({ page, pageSize })
            return {
              data: response.data,
              total: response.total
            }
          }
        })
      } catch (error) {
        console.error('加载失败:', error)
      }
    }
  }
}
</script>
```

## API 参考

### PaginationManager

#### 构造函数
```javascript
const paginationManager = createPaginationManager(config)
```

配置参数：
- `page` - 当前页码，默认 1
- `pageSize` - 每页大小，默认 10
- `total` - 总数据量，默认 0
- `hasMore` - 是否有更多数据，默认 true
- `loading` - 加载状态，默认 false
- `data` - 数据数组，默认 []
- `searchParams` - 搜索参数

#### 方法

- `loadFirstPage(apiCall)` - 加载第一页数据
- `loadMore(apiCall)` - 加载更多数据
- `updateConfig(newConfig)` - 更新配置
- `updateSearchParams(newParams)` - 更新搜索参数
- `resetSearchParams()` - 重置搜索参数
- `getState()` - 获取当前状态
- `getSearchParams()` - 获取搜索参数
- `hasSearchConditions()` - 检查是否有搜索条件
- `addData(item)` - 添加数据
- `removeData(predicate)` - 删除数据
- `updateData(predicate, updateFn)` - 更新数据

#### 回调函数

- `setStateChangeCallback(callback)` - 状态变化回调
- `setSearchParamsChangeCallback(callback)` - 搜索参数变化回调

### SearchForm 组件

#### Props
- `initialParams` - 初始搜索参数

#### Events
- `search` - 搜索事件，参数为搜索参数
- `reset` - 重置搜索事件
- `keyword-change` - 关键字变化事件
- `start-date-change` - 开始日期变化事件
- `end-date-change` - 结束日期变化事件

## 高级用法

### 1. 自定义搜索字段

```javascript
// 在搜索表单中指定搜索字段
<search-form
  @search="handleSearch"
  @keyword-change="handleKeywordChange"
/>

// 在搜索管理器中设置搜索字段
this.searchManager.search(keyword, ['name', 'description', 'tags'])
```

### 2. 前端搜索（客户端过滤）

```javascript
import { createSearchManager } from '@/utils/pagination.js'

// 创建搜索管理器
this.searchManager = createSearchManager()

// 设置搜索结果回调
this.searchManager.setSearchCallback((filteredData) => {
  this.filteredData = filteredData
})

// 设置所有数据
this.searchManager.setAllData(this.data)

// 执行搜索
this.searchManager.search(keyword, ['name', 'description'])
```

### 3. 错误处理

```javascript
async loadData() {
  try {
    await this.paginationManager.loadFirstPage(async (page, pageSize) => {
      try {
        const response = await api.getData({ page, pageSize })
        return {
          data: response.data,
          total: response.total
        }
      } catch (error) {
        console.warn('API调用失败，使用备用方案:', error)
        // 备用方案
        const allData = await api.getAllData()
        return {
          data: allData.slice((page - 1) * pageSize, page * pageSize),
          total: allData.length
        }
      }
    })
  } catch (error) {
    console.error('加载失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}
```

### 4. 下拉刷新和上拉加载

```javascript
// 下拉刷新
onPullDownRefresh() {
  this.refreshData()
  uni.stopPullDownRefresh()
},

// 上拉加载更多
onReachBottom() {
  if (this.hasMore && !this.loading) {
    this.loadMore()
  }
},

async refreshData() {
  this.paginationManager.resetSearchParams()
  await this.loadData()
}
```

## 最佳实践

1. **统一错误处理** - 在所有API调用中添加错误处理
2. **加载状态管理** - 使用分页管理器的loading状态
3. **搜索条件验证** - 在搜索前验证日期范围等条件
4. **用户体验优化** - 提供空状态、加载状态等反馈
5. **性能优化** - 合理设置pageSize，避免一次性加载过多数据

## 兼容性说明

- 支持后端分页和前端分页
- 支持搜索API和普通分页API的自动切换
- 兼容旧版本API接口
- 支持UniApp框架