<template>
    <view class="container">
        <!-- WebView容器 -->
        <web-view
            ref="mywebview"
            v-if="showWebview"
            :src="webviewUrl"
            style="width: 100%; height: 100vh;">
        </web-view>

        <!-- 加载状态 -->
        <view class="loading-overlay" v-if="loading">
            <view class="loading-content">
                <image class="loading-icon" src="/static/collectPoint/loading.gif"></image>
                <text class="loading-text">{{ loadingText }}</text>
            </view>
        </view>

        <!-- 错误状态 -->
        <view class="error-overlay" v-if="showError">
            <view class="error-content">
                <text class="error-title">加载失败</text>
                <text class="error-message">{{ errorMessage }}</text>
                <button class="retry-btn" @tap="retryLoad">重试</button>
            </view>
        </view>
    </view>
</template>

<script>
const { trackPointApi } = require('@/utils/api.js');
const { convertToHeatmapData } = require('@/utils/heatmapUtils.js');

export default {
    data() {
        return {
            trackId: null,
            heatmapData: [],
            webviewUrl: '',
            showWebview: false,
            loading: false,
            loadingText: '加载中...',
            showError: false,
            errorMessage: ''
        }
    },

    onLoad(options) {
        if (options.trackMsg) {
            this.trackId = options.trackMsg;
            this.loadHeatmapData();
        } else {
            this.showError = true;
            this.errorMessage = '轨迹ID不存在';
        }
    },

    methods: {
        async loadHeatmapData() {
            try {
                this.loading = true;
                this.showError = false;
                this.loadingText = '正在加载轨迹点数据...';

                // 使用后端API获取轨迹点数据
                const trackPoints = await trackPointApi.getTrackPoints(this.trackId);
                console.log('获取到的轨迹点数据:', trackPoints);

                // 检查是否有轨迹点数据
                if (trackPoints && trackPoints.length > 0) {
                    // 转换为热力图数据格式（指定使用GCJ02坐标系）
                    const heatmapData = convertToHeatmapData(trackPoints, {
                        coordinateSystem: 'gcj02'
                    });
                    console.log('转换后的热力图数据:', heatmapData);

                    // 存储数据到本地存储
                    uni.setStorageSync('heatmapData', JSON.stringify({
                        data: heatmapData,
                        trackId: this.trackId
                    }));

                    // 设置WebView URL
                    this.webviewUrl = '/static/html/shili.html';
                    this.showWebview = true;

                } else {
                    throw new Error('没有轨迹点数据');
                }

            } catch (error) {
                console.error('加载热力图数据失败:', error);
                this.showError = true;
                this.errorMessage = '加载热力图数据失败：' + error.message;

                // 降级方案：使用原有的外部热力图API
                await this.loadFallbackData();

            } finally {
                this.loading = false;
            }
        },

        async loadFallbackData() {
            try {
                this.loadingText = '使用备用数据源...';

                // 使用原有的外部API
                const res = await new Promise((resolve, reject) => {
                    uni.request({
                        url: `https://5141ea69.r10.cpolar.top/api/getData?lineid=${this.trackId}`,
                        header: {
                            'Access-Control-Allow-Origin': '*'
                        },
                        success: (res) => resolve(res),
                        fail: (err) => reject(err)
                    });
                });

                if (res.data && res.data.length > 0) {
                    // 存储数据到本地存储
                    uni.setStorageSync('heatmapData', JSON.stringify({
                        data: res.data,
                        trackId: this.trackId
                    }));

                    // 设置WebView URL
                    this.webviewUrl = '/static/html/shili.html';
                    this.showWebview = true;
                } else {
                    throw new Error('没有热力图数据');
                }

            } catch (error) {
                console.error('备用数据源也失败了:', error);
                this.showError = true;
                this.errorMessage = '所有数据源都加载失败';
            }
        },

        retryLoad() {
            this.showError = false;
            this.loadHeatmapData();
        }
    }
}
</script>

<style scoped>
.container {
    flex: 1;
    background-color: #f5f5f5;
    position: relative;
}

.loading-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    z-index: 9999;
    display: flex;
    align-items: center;
    justify-content: center;
}

.loading-content {
    background: white;
    border-radius: 20rpx;
    padding: 60rpx 40rpx;
    align-items: center;
    min-width: 300rpx;
}

.loading-icon {
    width: 80rpx;
    height: 80rpx;
    margin-bottom: 30rpx;
}

.loading-text {
    font-size: 28rpx;
    color: #333;
    text-align: center;
}

.error-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.95);
    z-index: 9999;
    display: flex;
    align-items: center;
    justify-content: center;
}

.error-content {
    background: white;
    border-radius: 20rpx;
    padding: 60rpx 40rpx;
    align-items: center;
    min-width: 400rpx;
    box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
    border: 1rpx solid #e0e0e0;
}

.error-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #ff4444;
    margin-bottom: 20rpx;
    display: block;
    text-align: center;
}

.error-message {
    font-size: 28rpx;
    color: #666;
    text-align: center;
    margin-bottom: 40rpx;
    display: block;
    line-height: 1.5;
}

.retry-btn {
    background: #0078D4;
    color: white;
    border-radius: 10rpx;
    padding: 20rpx 40rpx;
    font-size: 28rpx;
    border: none;
}

.retry-btn:active {
    background: #005a9e;
}
</style>