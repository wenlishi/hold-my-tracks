<template>
  <view class="search-form">
    <!-- 折叠状态：只显示搜索框 -->
    <view v-if="!expanded" class="collapsed-search">
      <view class="search-box" @click="expandSearch">
        <uni-icons type="search" size="20" color="#999"></uni-icons>
        <input
          class="search-input"
          placeholder="搜索轨迹名称或描述"
          :value="searchKeyword"
          readonly
        />
        <view class="expand-icon">
          <uni-icons type="arrowdown" size="16" color="#999"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 展开状态：显示完整搜索组件 -->
    <view v-else class="expanded-search">
      <!-- 关键字搜索 -->
      <view class="search-section">
        <view class="search-box">
          <uni-icons type="search" size="20" color="#999"></uni-icons>
          <input
            class="search-input"
            placeholder="搜索轨迹名称或描述"
            v-model="searchKeyword"
            @input="handleKeywordChange"
          />
          <view v-if="searchKeyword" class="clear-btn" @click="clearKeyword">
            <uni-icons type="clear" size="16" color="#999"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 日期范围搜索 -->
      <view class="date-section">
        <view class="date-row">
          <view class="date-item">
            <text class="date-label">开始日期</text>
            <view class="date-input" @click="showStartDatePicker = true">
              <text class="date-text" :class="{ 'placeholder': !startDate }">
                {{ startDate ? formatDate(startDate) : '选择开始日期' }}
              </text>
              <uni-icons type="calendar" size="18" color="#999"></uni-icons>
            </view>
          </view>

          <view class="date-item">
            <text class="date-label">结束日期</text>
            <view class="date-input" @click="showEndDatePicker = true">
              <text class="date-text" :class="{ 'placeholder': !endDate }">
                {{ endDate ? formatDate(endDate) : '选择结束日期' }}
              </text>
              <uni-icons type="calendar" size="18" color="#999"></uni-icons>
            </view>
          </view>
        </view>

      <!-- 日期选择器 -->
      <uni-datetime-picker
        v-if="showStartDatePicker"
        type="date"
        :value="startDate"
        @change="handleStartDateChange"
        @maskClick="showStartDatePicker = false"
      />

      <uni-datetime-picker
        v-if="showEndDatePicker"
        type="date"
        :value="endDate"
        @change="handleEndDateChange"
        @maskClick="showEndDatePicker = false"
      />

      <!-- 操作按钮 -->
      <view class="action-buttons">
        <view class="reset-btn" @click="resetSearch">
          <uni-icons type="refresh" size="16" color="#666"></uni-icons>
          <text>重置</text>
        </view>

        <view class="search-btn" @click="handleSearch">
          <uni-icons type="search" size="16" color="#fff"></uni-icons>
          <text>搜索</text>
        </view>

        <view class="collapse-btn" @click="collapseSearch">
          <uni-icons type="arrowup" size="16" color="#666"></uni-icons>
          <text>收起</text>
        </view>
      </view>
    </view>
    </view>

    <!-- 搜索条件提示（已移除，避免遮挡内容） -->
  </view>
</template>

<script>
export default {
  name: 'SearchForm',
  props: {
    // 初始搜索参数
    initialParams: {
      type: Object,
      default: () => ({
        keyword: '',
        startDate: '',
        endDate: ''
      })
    },
    // 初始展开状态
    initiallyExpanded: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      searchKeyword: '',
      startDate: '',
      endDate: '',
      showStartDatePicker: false,
      showEndDatePicker: false,
      expanded: false // 是否展开
    }
  },

  computed: {
    // 是否有搜索条件
    hasSearchConditions() {
      return this.searchKeyword || this.startDate || this.endDate
    }
  },

  watch: {
    // 监听初始参数变化
    initialParams: {
      handler(newParams) {
        this.searchKeyword = newParams.keyword || ''
        this.startDate = newParams.startDate || ''
        this.endDate = newParams.endDate || ''
      },
      immediate: true
    },
    // 监听初始展开状态
    initiallyExpanded: {
      handler(newValue) {
        this.expanded = newValue
      },
      immediate: true
    }
  },

  methods: {
    // 展开搜索
    expandSearch() {
      this.expanded = true
      this.$emit('expand')
    },

    // 收起搜索
    collapseSearch() {
      this.expanded = false
      this.$emit('collapse')
    },

    // 关键字变化
    handleKeywordChange() {
      this.$emit('keyword-change', this.searchKeyword)
    },

    // 清除关键字
    clearKeyword() {
      this.searchKeyword = ''
      this.$emit('keyword-change', '')
    },

    // 开始日期变化
    handleStartDateChange(date) {
      this.startDate = date
      this.showStartDatePicker = false
      this.$emit('start-date-change', date)
    },

    // 结束日期变化
    handleEndDateChange(date) {
      this.endDate = date
      this.showEndDatePicker = false
      this.$emit('end-date-change', date)
    },

    // 清除开始日期
    clearStartDate() {
      this.startDate = ''
      this.$emit('start-date-change', '')
    },

    // 清除结束日期
    clearEndDate() {
      this.endDate = ''
      this.$emit('end-date-change', '')
    },

    // 重置搜索
    resetSearch() {
      this.searchKeyword = ''
      this.startDate = ''
      this.endDate = ''
      this.$emit('reset')
    },

    // 执行搜索
    handleSearch() {
      const params = {
        keyword: this.searchKeyword,
        startDate: this.startDate,
        endDate: this.endDate
      }
      this.$emit('search', params)

      // 搜索完成后自动收起
      this.collapseSearch()
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return ''

      try {
        const d = new Date(date)
        const year = d.getFullYear()
        const month = String(d.getMonth() + 1).padStart(2, '0')
        const day = String(d.getDate()).padStart(2, '0')
        return `${year}-${month}-${day}`
      } catch (error) {
        return date
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.search-form {
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
  border: 1rpx solid #e9ecef;
  overflow: hidden;
}

/* 折叠状态样式 */
.collapsed-search {
  padding: 20rpx 30rpx;
}

.collapsed-search .search-box {
  cursor: pointer;
}

.expand-icon {
  padding: 8rpx;
  background: #f8f9fa;
  border-radius: 50%;
}

/* 展开状态样式 */
.expanded-search {
  padding: 30rpx;
}

.search-section {
  margin-bottom: 30rpx;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border-radius: 50rpx;
  padding: 20rpx 30rpx;
  border: 1rpx solid #e9ecef;
}

.search-input {
  flex: 1;
  margin: 0 20rpx;
  font-size: 28rpx;
  color: #333;
}

.clear-btn {
  padding: 8rpx;
  background: #e9ecef;
  border-radius: 50%;
}

.date-section {
  border-top: 1rpx solid #f0f0f0;
  padding-top: 30rpx;
}

.date-row {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.date-item {
  flex: 1;
}

.date-label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 16rpx;
  font-weight: 500;
}

.date-input {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  border: 1rpx solid #e9ecef;
  cursor: pointer;
}

.date-text {
  font-size: 28rpx;
  color: #333;
}

.date-text.placeholder {
  color: #999;
}

.action-buttons {
  display: flex;
  gap: 15rpx;
}

.reset-btn,
.search-btn,
.collapse-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  padding: 20rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 500;
}

.reset-btn {
  background: #f8f9fa;
  color: #666;
  border: 1rpx solid #e9ecef;
}

.search-btn {
  background: #007AFF;
  color: #fff;
}

.collapse-btn {
  background: #f8f9fa;
  color: #666;
  border: 1rpx solid #e9ecef;
}

.search-conditions {
  border-top: 1rpx solid #f0f0f0;
  padding-top: 30rpx;
  margin-top: 30rpx;
}

.conditions-text {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 20rpx;
  font-weight: 500;
}

.condition-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.condition-tag {
  display: flex;
  align-items: center;
  gap: 10rpx;
  background: #e6f7ff;
  border: 1rpx solid #91d5ff;
  border-radius: 20rpx;
  padding: 12rpx 20rpx;
  font-size: 24rpx;
  color: #1890ff;
}

.condition-tag text {
  white-space: nowrap;
}
</style>