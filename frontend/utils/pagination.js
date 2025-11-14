/**
 * 分页工具函数
 * 提供通用的分页逻辑，支持前端和后端分页
 */

/**
 * 搜索参数类
 */
export class SearchParams {
  constructor(options = {}) {
    this.keyword = options.keyword || ''
    this.startDate = options.startDate || ''
    this.endDate = options.endDate || ''
    this.fields = options.fields || ['name', 'description']
  }

  /**
   * 检查是否有搜索条件
   */
  hasSearchConditions() {
    return this.keyword || this.startDate || this.endDate
  }

  /**
   * 重置搜索条件
   */
  reset() {
    this.keyword = ''
    this.startDate = ''
    this.endDate = ''
  }
}

/**
 * 分页配置类
 */
export class PaginationConfig {
  constructor(options = {}) {
    this.page = options.page || 1
    this.pageSize = options.pageSize || 10
    this.total = options.total || 0
    this.hasMore = options.hasMore || true
    this.loading = options.loading || false
    this.data = options.data || []
  }
}

/**
 * 分页管理器
 */
export class PaginationManager {
  constructor(config = {}) {
    this.config = new PaginationConfig(config)
    this.searchParams = new SearchParams(config.searchParams)
    this.onDataChange = null // 数据变化回调
    this.onStateChange = null // 状态变化回调
    this.onSearchParamsChange = null // 搜索参数变化回调
  }

  /**
   * 设置数据变化回调
   */
  setDataChangeCallback(callback) {
    this.onDataChange = callback
  }

  /**
   * 设置状态变化回调
   */
  setStateChangeCallback(callback) {
    this.onStateChange = callback
  }

  /**
   * 设置搜索参数变化回调
   */
  setSearchParamsChangeCallback(callback) {
    this.onSearchParamsChange = callback
  }

  /**
   * 更新配置
   */
  updateConfig(newConfig) {
    this.config = { ...this.config, ...newConfig }
    this._notifyStateChange()
  }

  /**
   * 更新搜索参数
   */
  updateSearchParams(newParams) {
    // 更新SearchParams实例的属性，而不是替换整个对象
    Object.assign(this.searchParams, newParams)
    this._notifySearchParamsChange()
  }

  /**
   * 重置搜索参数
   */
  resetSearchParams() {
    this.searchParams.reset()
    this._notifySearchParamsChange()
  }

  /**
   * 获取搜索参数
   */
  getSearchParams() {
    return { ...this.searchParams }
  }

  /**
   * 检查是否有搜索条件
   */
  hasSearchConditions() {
    return this.searchParams.hasSearchConditions()
  }

  /**
   * 重置分页状态
   */
  reset() {
    this.config = new PaginationConfig({
      pageSize: this.config.pageSize
    })
    this._notifyStateChange()
  }

  /**
   * 加载第一页数据
   */
  async loadFirstPage(apiCall, options = {}) {
    if (this.config.loading) return

    this.config.loading = true
    this.config.page = 1
    this.config.hasMore = true
    this._notifyStateChange()

    try {
      const result = await apiCall(this.config.page, this.config.pageSize, options)
      this._handleApiResult(result, true)
      return result
    } catch (error) {
      this.config.loading = false
      this._notifyStateChange()
      throw error
    }
  }

  /**
   * 加载更多数据
   */
  async loadMore(apiCall, options = {}) {
    if (this.config.loading || !this.config.hasMore) return

    this.config.loading = true
    this.config.page++
    this._notifyStateChange()

    try {
      const result = await apiCall(this.config.page, this.config.pageSize, options)
      this._handleApiResult(result, false)
      return result
    } catch (error) {
      this.config.page--
      this.config.loading = false
      this._notifyStateChange()
      throw error
    }
  }

  /**
   * 处理API返回结果
   */
  _handleApiResult(result, isFirstPage) {
    let currentPageData = []
    let totalCount = 0

    // 检查后端是否返回分页结构
    if (result && (result.data !== undefined || result.total !== undefined)) {
      // 后端支持分页，返回PageResponse格式
      const pageData = result || {}
      currentPageData = pageData.data || []
      totalCount = pageData.total || 0
    } else {
      // 后端返回的是数组（兼容旧版本），使用前端分页
      const allData = result || []
      totalCount = allData.length
      const startIndex = (this.config.page - 1) * this.config.pageSize
      const endIndex = startIndex + this.config.pageSize
      currentPageData = allData.slice(startIndex, endIndex)
    }

    this.config.total = totalCount
    this.config.loading = false

    if (isFirstPage) {
      this.config.data = currentPageData
    } else {
      this.config.data = [...this.config.data, ...currentPageData]
    }

    // 判断是否还有更多数据
    this.config.hasMore = this.config.data.length < this.config.total

    this._notifyDataChange()
    this._notifyStateChange()

    console.log('分页加载完成:', {
      page: this.config.page,
      pageSize: this.config.pageSize,
      loaded: this.config.data.length,
      total: this.config.total,
      hasMore: this.config.hasMore
    })
  }

  /**
   * 手动添加数据（用于本地操作）
   */
  addData(item) {
    this.config.data.unshift(item)
    this.config.total++
    this._notifyDataChange()
    this._notifyStateChange()
  }

  /**
   * 删除数据
   */
  removeData(predicate) {
    const initialLength = this.config.data.length
    this.config.data = this.config.data.filter(predicate)
    const removedCount = initialLength - this.config.data.length
    this.config.total = Math.max(0, this.config.total - removedCount)
    this._notifyDataChange()
    this._notifyStateChange()
  }

  /**
   * 更新数据
   */
  updateData(predicate, updateFn) {
    this.config.data = this.config.data.map(item =>
      predicate(item) ? updateFn(item) : item
    )
    this._notifyDataChange()
  }

  /**
   * 获取当前分页状态
   */
  getState() {
    return {
      page: this.config.page,
      pageSize: this.config.pageSize,
      total: this.config.total,
      hasMore: this.config.hasMore,
      loading: this.config.loading,
      data: this.config.data
    }
  }

  /**
   * 通知数据变化
   */
  _notifyDataChange() {
    if (this.onDataChange) {
      this.onDataChange(this.config.data)
    }
  }

  /**
   * 通知状态变化
   */
  _notifyStateChange() {
    if (this.onStateChange) {
      this.onStateChange(this.getState())
    }
  }

  /**
   * 通知搜索参数变化
   */
  _notifySearchParamsChange() {
    if (this.onSearchParamsChange) {
      this.onSearchParamsChange(this.getSearchParams())
    }
  }
}

/**
 * 搜索工具函数
 */
export class SearchManager {
  constructor() {
    this.searchKeyword = ''
    this.allData = []
    this.filteredData = []
    this.onSearchResult = null
  }

  /**
   * 设置搜索回调
   */
  setSearchCallback(callback) {
    this.onSearchResult = callback
  }

  /**
   * 设置所有数据
   */
  setAllData(data) {
    this.allData = data
    this._performSearch()
  }

  /**
   * 搜索
   */
  search(keyword, searchFields = ['name', 'description']) {
    this.searchKeyword = keyword
    this._performSearch(searchFields)
  }

  /**
   * 执行搜索
   */
  _performSearch(searchFields = ['name', 'description']) {
    if (!this.searchKeyword.trim()) {
      this.filteredData = this.allData
    } else {
      const keyword = this.searchKeyword.toLowerCase()
      this.filteredData = this.allData.filter(item => {
        return searchFields.some(field => {
          const value = item[field] || ''
          return value.toString().toLowerCase().includes(keyword)
        })
      })
    }

    if (this.onSearchResult) {
      this.onSearchResult(this.filteredData)
    }
  }

  /**
   * 获取搜索结果
   */
  getFilteredData() {
    return this.filteredData
  }

  /**
   * 清除搜索
   */
  clear() {
    this.searchKeyword = ''
    this.filteredData = this.allData
    if (this.onSearchResult) {
      this.onSearchResult(this.filteredData)
    }
  }
}

/**
 * 创建分页管理器实例
 */
export function createPaginationManager(config = {}) {
  return new PaginationManager(config)
}

/**
 * 创建搜索管理器实例
 */
export function createSearchManager() {
  return new SearchManager()
}

/**
 * 分页工具函数 - 前端分页
 */
export function frontendPagination(data, page, pageSize) {
  const startIndex = (page - 1) * pageSize
  const endIndex = startIndex + pageSize
  return {
    data: data.slice(startIndex, endIndex),
    total: data.length,
    page,
    pageSize
  }
}

/**
 * 分页工具函数 - 后端分页参数
 */
export function backendPaginationParams(page, pageSize) {
  return {
    page,
    pageSize
  }
}

/**
 * 分页工具函数 - 检查是否有更多数据
 */
export function hasMoreData(loadedCount, totalCount) {
  return loadedCount < totalCount
}

/**
 * 分页工具函数 - 计算总页数
 */
export function calculateTotalPages(total, pageSize) {
  return Math.ceil(total / pageSize)
}