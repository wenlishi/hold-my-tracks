<template>
    <view class="container">
        <!-- 地图容器 -->
        <view class="map-container">
            <!-- 轨迹名称标题栏 - 使用 cover-view 并放在 map 同级 -->
            <cover-view class="track-header-bar"  v-if="headerVisible">
                <cover-view class="back-button">
                    <cover-image class="back-icon" @tap="goBack" src="/static/collectPoint/goback.png"></cover-image>
                </cover-view>
                <cover-view class="track-title">{{ getTrackName() }}</cover-view>
            </cover-view>
            <map
                class="map"
                id="detailMap"
                ref="detailMap"
                :style="{height: mapHeight}"
                :scale="scale"
                :longitude="centerLongitude"
                :latitude="centerLatitude"
                :show-location="true"
                :enable-building="enable3D"
                :enable-zoom="true"
                :enable-scroll="true"
                :enable-rotate="false"
                :enable-satellite="enableSatellite"
                :enable-traffic="enableTraffic"
                :markers="markers"
                :polyline="polyline"
                @markertap="onMarkerTap"
                @regionchange="onRegionChange">
            </map>

            <!-- 地图控制按钮 - 也需要用 cover-view -->
            <cover-view class="map-controls">
                <cover-view class="map-control">
                    <cover-image class="control-icon" @click="toggleSatellite()" :src="enableSatellite ? '/static/collectPoint/mapcontrols/satellitese.png' : '/static/collectPoint/mapcontrols/satellite.png'"></cover-image>
                </cover-view>
                <cover-view class="map-control">
                    <cover-image class="control-icon" @click="toggle3D()" :src="enable3D ? '/static/collectPoint/mapcontrols/buildse.png' : '/static/collectPoint/mapcontrols/build.png'"></cover-image>
                </cover-view>
                <cover-view class="map-control">
                    <cover-image class="control-icon" @click="toggleTraffic()" :src="enableTraffic ? '/static/collectPoint/mapcontrols/trafficse.png' : '/static/collectPoint/mapcontrols/traffic.png'"></cover-image>
                </cover-view>
                <cover-view class="map-control">
                    <cover-image class="control-icon" @click="resetMapView()" src="/static/collectPoint/mapcontrols/rotate.png"></cover-image>
                </cover-view>
            </cover-view>
        </view>

        <!-- 轨迹信息面板 -->
        <view class="info-panel">
            <!-- 轨迹信息卡片 - 重构为图中效果 -->
            <view class="info-card track-main-info-card">
                <view class="card-header">
                    <view class="distance-section">
                        <text class="distance-value">{{ formatDistance(getTotalDistance()) }}</text>
                        <text class="date-text">{{ formatDate(getTrackCreateTime()) }}</text>
                        <button class="play-button-corner" @tap="startPlayback" :disabled="isPlaying">
                            <image class="play-icon-corner" src="/static/collectPoint/play.png"></image>
                            <text class="play-text-corner">{{ isPlaying ? '正在播放' : '动态轨迹' }}</text>
                        </button>
                    </view>
                </view>

                <!-- 速度色带图例 -->
                <view class="speed-legend">
                    <view class="color-bar-gradient"></view>
                    <view class="speed-labels-simple">
                        <span class="speed-label">慢</span>
                        <span class="speed-label">快</span>
                    </view>
                </view>

                <!-- 统计项 - 使用span在同一行显示 -->
                <view class="stats-container">
                    <span class="stat-span">
                        <image class="stat-icon" src="/static/collectPoint/time.png"></image>
                        <span class="stat-value">{{ getDuration() }}</span>
                        <span class="stat-label">采点用时</span>
                    </span>
                    <span class="stat-span">
                        <image class="stat-icon" src="/static/collectPoint/speed.png"></image>
                        <span class="stat-value">{{ formatSpeed(getAverageSpeed()) }}</span>
                        <span class="stat-label">平均速度</span>
                    </span>
                    <span class="stat-span">
                        <image class="stat-icon" src="/static/collectPoint/max-speed.png"></image>
                        <span class="stat-value">{{ formatSpeed(getMaxSpeed()) }}</span>
                        <span class="stat-label">最大速度</span>
                    </span>
                </view>
            </view>

            <!-- 控制按钮 -->
            <view class="control-buttons">
                <button class="btn btn-secondary" @tap="showHeatmap">查看热力图</button>
                <button class="btn btn-secondary" @tap="exportTrack">导出轨迹</button>
            </view>

            <!-- 轨迹点列表 -->
            <view class="info-card points-card">
                <view class="card-header">
                    <text class="card-title">轨迹点列表</text>
                    <text class="points-count">{{ getTotalPoints() }} 个点</text>
                </view>
                <scroll-view class="points-list" scroll-y="true">
                    <view
                        class="point-item"
                        :class="{ 'point-item-selected': selectedPointId === point.id }"
                        v-for="(point, index) in trackDetail.trackPoints"
                        :key="point.id"
                        @tap="focusOnPoint(point)">
                        <view class="point-index" :class="{ 'point-index-selected': selectedPointId === point.id }">{{ index + 1 }}</view>
                        <view class="point-info">
                            <text class="point-coords">{{ formatCoordinate(point.latitude) }}, {{ formatCoordinate(point.longitude) }}</text>
                            <text class="point-meta">海拔: {{ point.altitude || 0 }}m | 速度: {{ formatSpeed(point.speed) }}</text>
                            <text class="point-time" v-if="point.createTime">{{ formatTime(point.createTime) }}</text>
                        </view>
                    </view>
                </scroll-view>
            </view>
        </view>
    </view>
</template>

<script>
const { trackApi } = require('@/utils/api.js');
import { wgs84ToGcj02, transformPoints } from "@/utils/coordinateTransform.js";
import { simpleExportTrack } from "@/utils/trackFileSaver.js";

export default {
    data() {
        return {
            trackId: null,
            trackDetail: {
                track: {},
                trackPoints: [],
                stats: {}
            },
            mapHeight: '750rpx',
            scale: 15,
            centerLongitude: 0,
            centerLatitude: 0,
            enable3D: false,
            enableSatellite: false,
            enableTraffic: false,
            markers: [],
            polyline: [],
            isPlaying: false,
            playbackInterval: null,
            currentPlaybackIndex: 0,
            selectedPointId: null,
            userHasZoomed: false,
            headerVisible: true
        }
    },

    onLoad(options) {
        if (options.id) {
            this.trackId = options.id;
            this.loadTrackDetail();
            // 强制保持标题栏可见
            this.forceHeaderVisible();
        } else {
            uni.showToast({
                title: '轨迹ID不存在',
                icon: 'none'
            });
            uni.navigateBack();
        }
    },

    onUnload() {
        if (this.playbackInterval) {
            clearInterval(this.playbackInterval);
        }
    },

    methods: {
        async loadTrackDetail() {
            try {
                uni.showLoading({
                    title: '加载中...'
                });

                // 使用压缩后的轨迹详情，提高地图渲染性能
                const res = await trackApi.getCompressedTrackDetail(this.trackId, 10.0);
                this.trackDetail = res;
                this.initMapData();

            } catch (error) {
                console.error('加载轨迹详情失败:', error);
                uni.showToast({
                    title: '加载失败',
                    icon: 'none'
                });
            } finally {
                uni.hideLoading();
            }
        },

        initMapData() {
            const points = this.trackDetail.trackPoints;
            if (!points || points.length === 0) {
                this.centerLongitude = 116.397477;
                this.centerLatitude = 39.908692;
                this.scale = 18;
                return;
            }

            const convertedPoints = transformPoints(points, wgs84ToGcj02);
            const lats = convertedPoints.map(p => p.latitude).filter(lat => lat != null);
            const lons = convertedPoints.map(p => p.longitude).filter(lon => lon != null);

            if (lats.length > 0 && lons.length > 0) {
                this.centerLatitude = lats.reduce((a, b) => a + b) / lats.length;
                this.centerLongitude = lons.reduce((a, b) => a + b) / lons.length;
            } else {
                this.centerLongitude = 116.397477;
                this.centerLatitude = 39.908692;
            }

            if (!this.userHasZoomed) {
                this.calculateOptimalZoom(convertedPoints);
            }

            const validPoints = convertedPoints.filter(p => p.latitude != null && p.longitude != null);

            if (validPoints.length <= 20) {
                this.markers = validPoints.map((point, index) => ({
                    id: point.id || index,
                    latitude: point.latitude,
                    longitude: point.longitude,
                    title: '轨迹点 ' + (index + 1),
                    iconPath: '/static/collectPoint/trackPoint.png',
                    width: 16,
                    height: 16,
                    callout: {
                        content: '点 ' + (index + 1) + ': ' + (point.latitude ? point.latitude.toFixed(6) : '0') + ', ' + (point.longitude ? point.longitude.toFixed(6) : '0'),
                        color: '#000000',
                        fontSize: 12,
                        borderRadius: 4,
                        bgColor: '#FFFFFF',
                        padding: 5,
                        display: 'BYCLICK'
                    }
                }));
            } else {
                this.markers = [
                    {
                        id: 'start',
                        latitude: validPoints[0].latitude,
                        longitude: validPoints[0].longitude,
                        title: '起点',
                        iconPath: '/static/collectPoint/startpoint.png',
                        width: 32,
                        height: 32,
                        callout: {
                            content: '起点: ' + validPoints[0].latitude.toFixed(6) + ', ' + validPoints[0].longitude.toFixed(6),
                            color: '#000000',
                            fontSize: 12,
                            borderRadius: 4,
                            bgColor: '#FFFFFF',
                            padding: 5,
                            display: 'BYCLICK'
                        }
                    },
                    {
                        id: 'end',
                        latitude: validPoints[validPoints.length - 1].latitude,
                        longitude: validPoints[validPoints.length - 1].longitude,
                        title: '终点',
                        iconPath: '/static/collectPoint/destination.png',
                        width: 32,
                        height: 32,
                        callout: {
                            content: '终点: ' + validPoints[validPoints.length - 1].latitude.toFixed(6) + ', ' + validPoints[validPoints.length - 1].longitude.toFixed(6),
                            color: '#000000',
                            fontSize: 12,
                            borderRadius: 4,
                            bgColor: '#FFFFFF',
                            padding: 5,
                            display: 'BYCLICK'
                        }
                    }
                ];
            }

            this.polyline = this.createSpeedColorPolyline(convertedPoints);
        },

        getTrackName() {
            return (this.trackDetail.track && this.trackDetail.track.trackName) || '未命名轨迹';
        },

        getTrackDescription() {
            return (this.trackDetail.track && this.trackDetail.track.description) || '暂无描述';
        },

        getTrackCreateTime() {
            return this.trackDetail.track && this.trackDetail.track.createTime;
        },

        getTrackStatus() {
            return this.trackDetail.track && this.trackDetail.track.status;
        },

        getTotalDistance() {
            return this.trackDetail.stats && this.trackDetail.stats.totalDistance;
        },

        getTotalPoints() {
            return (this.trackDetail.stats && this.trackDetail.stats.totalPoints) || 0;
        },

        getAverageSpeed() {
            return this.trackDetail.stats && this.trackDetail.stats.averageSpeed;
        },

        getMaxSpeed() {
            return this.trackDetail.stats && this.trackDetail.stats.maxSpeed;
        },

        // 新增：计算采点用时
        getDuration() {
            const points = this.trackDetail.trackPoints;
            if (!points || points.length < 2) return '00:00:00';
            
            const startTime = new Date(points[0].createTime);
            const endTime = new Date(points[points.length - 1].createTime);
            const duration = endTime - startTime;
            
            const hours = Math.floor(duration / 3600000);
            const minutes = Math.floor((duration % 3600000) / 60000);
            const seconds = Math.floor((duration % 60000) / 1000);
            
            return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
        },

        toggleSatellite() {
            console.log('切换卫星图状态:', !this.enableSatellite);
            this.enableSatellite = !this.enableSatellite;
            if (this.enableSatellite) {
                this.enable3D = false;
            }
            
            // **核心修复**：记录并轻微偏移中心点，强制地图刷新
            const tempLat = this.centerLatitude;
            const tempLng = this.centerLongitude;
            this.centerLatitude = tempLat + 0.000001; // 微小偏移
            this.centerLongitude = tempLng + 0.000001;
            
            this.$nextTick(() => {
                // 恢复原始坐标
                this.centerLatitude = tempLat;
                this.centerLongitude = tempLng;
                console.log('地图已强制刷新');
            });
        },

        toggle3D() {
            console.log('切换3D楼块状态:', !this.enable3D);
            if (this.enableSatellite) {
                uni.showToast({
                    title: '卫星图无法显示3D楼块',
                    icon: 'none'
                });
                return;
            }
            this.enable3D = !this.enable3D;
            // 强制更新地图
            this.$forceUpdate();
            // 使用地图上下文强制刷新
            this.$nextTick(() => {
                const mapCtx = uni.createMapContext('detailMap', this);
                mapCtx.moveToLocation();
            });
        },

        toggleTraffic() {
            console.log('切换实时路况状态:', !this.enableTraffic);
            this.enableTraffic = !this.enableTraffic;
            // 强制更新地图
            this.$forceUpdate();
            // 使用地图上下文强制刷新
            this.$nextTick(() => {
                const mapCtx = uni.createMapContext('detailMap', this);
                mapCtx.moveToLocation();
            });
        },

        resetMapView() {
            this.userHasZoomed = false;
            this.initMapData();
            uni.showToast({
                title: '地图已重置',
                icon: 'success'
            });
        },

        startPlayback() {
            if (this.isPlaying) return;

            this.isPlaying = true;
            this.currentPlaybackIndex = 0;

            this.markers = [];

            const totalPoints = this.trackDetail.trackPoints.length;
            const totalDuration = 10000;
            const interval = Math.max(50, totalDuration / totalPoints);

            console.log(`轨迹回放：共${totalPoints}个点，播放间隔${interval.toFixed(0)}毫秒，总时长10秒`);

            this.playbackInterval = setInterval(() => {
                if (this.currentPlaybackIndex >= totalPoints) {
                    this.stopPlayback();
                    return;
                }

                const point = this.trackDetail.trackPoints[this.currentPlaybackIndex];
                const [gcjLng, gcjLat] = wgs84ToGcj02(point.longitude, point.latitude);

                this.markers = [{
                    id: 'playback',
                    latitude: gcjLat,
                    longitude: gcjLng,
                    title: '播放点 ' + (this.currentPlaybackIndex + 1),
                    iconPath: '/static/collectPoint/trackPoint.png',
                    width: 18,
                    height: 18
                }];

                this.centerLatitude = gcjLat;
                this.centerLongitude = gcjLng;

                this.currentPlaybackIndex++;
            }, interval);
        },

        stopPlayback() {
            if (this.playbackInterval) {
                clearInterval(this.playbackInterval);
                this.playbackInterval = null;
            }
            this.isPlaying = false;
            this.selectedPointId = null;
            this.initMapData();
        },

        focusOnPoint(point) {
            this.selectedPointId = point.id;
            const [gcjLng, gcjLat] = wgs84ToGcj02(point.longitude, point.latitude);
            this.centerLatitude = gcjLat;
            this.centerLongitude = gcjLng;

            if (!this.userHasZoomed) {
                this.scale = 20;
            }

            this.showPositionIndicator(point);
        },

        showPositionIndicator(point) {
            const [gcjLng, gcjLat] = wgs84ToGcj02(point.longitude, point.latitude);
            const indicatorMarker = {
                id: 'position_indicator',
                latitude: gcjLat,
                longitude: gcjLng,
                iconPath: '/static/collectPoint/trackPoint.png',
                width: 32,
                height: 32
            };

            this.markers = this.markers.filter(marker => marker.id !== 'position_indicator');
            this.markers.push(indicatorMarker);

            setTimeout(() => {
                this.markers = this.markers.filter(marker => marker.id !== 'position_indicator');
            }, 3000);
        },

        onMarkerTap(e) {
            console.log('标记点点击:', e);
        },

        onRegionChange(e) {
            if (e.type === 'end' && e.causedBy === 'scale') {
                this.userHasZoomed = true;
            }
        },

        showHeatmap() {
            if (!this.trackId) {
                uni.showToast({
                    title: '轨迹ID不存在',
                    icon: 'none'
                });
                return;
            }

            uni.navigateTo({
                url: `/pages/heatmap/heatmap?trackMsg=${this.trackId}`,
                success: () => {
                    console.log('跳转到热力图页面成功');
                },
                fail: (err) => {
                    console.error('跳转到热力图页面失败:', err);
                    uni.showToast({
                        title: '跳转失败',
                        icon: 'none'
                    });
                }
            });
        },

       
		exportTrack() {
			// 1. 数据校验
			if (!this.trackDetail.trackPoints || this.trackDetail.trackPoints.length === 0) {
				uni.showToast({ title: '没有轨迹数据可导出', icon: 'none' });
				return;
			}

			// 2. 定义菜单
			const formatOptions = [
				{ label: 'GPX格式 (GPS交换格式)', value: 'gpx' },
				{ label: 'KML格式 (Google Earth)', value: 'kml' },
				{ label: 'CSV格式 (表格数据)', value: 'csv' },
				{ label: 'GeoJSON格式 (地理数据)', value: 'geojson' }
			];

			// 3. 弹出选择框
			uni.showActionSheet({
				itemList: formatOptions.map(o => o.label),
				success: (res) => {
					const format = formatOptions[res.tapIndex].value;
					const trackName = this.getTrackName();
					
					// 4. 【关键】直接调用工具类
					simpleExportTrack(this.trackId, format, trackName);
				},
				fail: (e) => {
					console.log('取消选择');
				}
			});
		},

        calculateOptimalZoom(points) {
            if (!points || points.length < 2) {
                this.scale = 15;
                return;
            }

            const validPoints = points.filter(p =>
                p.latitude != null && p.longitude != null
            );

            if (validPoints.length < 2) {
                this.scale = 15;
                return;
            }

            const latitudes = validPoints.map(p => p.latitude);
            const longitudes = validPoints.map(p => p.longitude);

            const minLat = Math.min(...latitudes);
            const maxLat = Math.max(...latitudes);
            const minLng = Math.min(...longitudes);
            const maxLng = Math.max(...longitudes);

            const latSpan = maxLat - minLat;
            const lngSpan = maxLng - minLng;
            const maxSpan = Math.max(latSpan, lngSpan);

            let optimalScale;
            if (maxSpan < 0.001) {
                optimalScale = 20;
            } else if (maxSpan < 0.005) {
                optimalScale = 18;
            } else if (maxSpan < 0.01) {
                optimalScale = 16;
            } else if (maxSpan < 0.05) {
                optimalScale = 14;
            } else if (maxSpan < 0.1) {
                optimalScale = 12;
            } else {
                optimalScale = 10;
            }

            const pointCount = validPoints.length;
            if (pointCount > 100) {
                optimalScale = Math.max(optimalScale - 1, 10);
            } else if (pointCount < 10) {
                optimalScale = Math.min(optimalScale + 3, 22);
            } else {
                optimalScale = Math.min(optimalScale + 1, 20);
            }

            this.scale = optimalScale;
        },

        createSpeedColorPolyline(points) {
            const validPoints = points.filter(p =>
                p.latitude != null && p.longitude != null
            );

            if (validPoints.length < 2) {
                return [];
            }

            const speeds = validPoints.map(p => p.speed || 0);
            let minSpeed = Math.min(...speeds);
            let maxSpeed = Math.max(...speeds);

            if (maxSpeed - minSpeed < 1) {
                const midSpeed = (minSpeed + maxSpeed) / 2;
                minSpeed = Math.max(0, midSpeed - 0.5);
                maxSpeed = midSpeed + 0.5;
            }

            const polylines = [];
            const useGlowEffect = validPoints.length < 100;

            for (let i = 0; i < validPoints.length - 1; i++) {
                const currentPoint = validPoints[i];
                const nextPoint = validPoints[i + 1];
                const currentSpeed = currentPoint.speed || 0;
                const nextSpeed = nextPoint.speed || 0;
                const avgSpeed = (currentSpeed + nextSpeed) / 2;
                const color = this.getSmoothSpeedColor(avgSpeed, minSpeed, maxSpeed);

                if (useGlowEffect) {
                    polylines.push({
                        points: [
                            {
                                latitude: currentPoint.latitude,
                                longitude: currentPoint.longitude
                            },
                            {
                                latitude: nextPoint.latitude,
                                longitude: nextPoint.longitude
                            }
                        ],
                        color: this.getGlowColor(color),
                        width: 16,
                        dottedLine: false,
                        arrowLine: false
                    });
                }

                polylines.push({
                    points: [
                        {
                            latitude: currentPoint.latitude,
                            longitude: currentPoint.longitude
                        },
                        {
                            latitude: nextPoint.latitude,
                            longitude: nextPoint.longitude
                        }
                    ],
                    color: color,
                    width: useGlowEffect ? 8 : 6,
                    dottedLine: false,
                    arrowLine: false
                });
            }

            return polylines;
        },

        getSmoothSpeedColor(speed, minSpeed, maxSpeed) {
            if (maxSpeed - minSpeed < 0.1) {
                return '#4A90E2';
            }

            const normalizedSpeed = Math.max(0, Math.min(1, (speed - minSpeed) / (maxSpeed - minSpeed)));
            const hue = 140 * (1 - normalizedSpeed);
            const saturation = 85 + 15 * normalizedSpeed;
            const lightness = 55 + 10 * Math.sin(normalizedSpeed * Math.PI * 2);

            return this.hslToHex(hue, saturation, lightness);
        },

        getGlowColor(baseColor) {
            const hex = baseColor.replace('#', '');
            const r = parseInt(hex.substr(0, 2), 16);
            const g = parseInt(hex.substr(2, 2), 16);
            const b = parseInt(hex.substr(4, 2), 16);

            return `rgba(${r}, ${g}, ${b}, 0.25)`;
        },

        hslToHex(h, s, l) {
            h /= 360;
            s /= 100;
            l /= 100;

            let r, g, b;

            if (s === 0) {
                r = g = b = l;
            } else {
                const hue2rgb = (p, q, t) => {
                    if (t < 0) t += 1;
                    if (t > 1) t -= 1;
                    if (t < 1/6) return p + (q - p) * 6 * t;
                    if (t < 1/2) return q;
                    if (t < 2/3) return p + (q - p) * (2/3 - t) * 6;
                    return p;
                };

                const q = l < 0.5 ? l * (1 + s) : l + s - l * s;
                const p = 2 * l - q;

                r = hue2rgb(p, q, h + 1/3);
                g = hue2rgb(p, q, h);
                b = hue2rgb(p, q, h - 1/3);
            }

            const toHex = x => {
                const hex = Math.round(x * 255).toString(16);
                return hex.length === 1 ? '0' + hex : hex;
            };

            return `#${toHex(r)}${toHex(g)}${toHex(b)}`;
        },

        getSpeedColor(speed, minSpeed, maxSpeed) {
            if (maxSpeed - minSpeed < 0.1) {
                return '#0078D4';
            }

            const normalizedSpeed = Math.max(0, Math.min(1, (speed - minSpeed) / (maxSpeed - minSpeed)));
            let r, g, b;

            if (normalizedSpeed < 0.25) {
                const ratio = normalizedSpeed * 4;
                r = Math.floor(100 * ratio);
                g = Math.floor(200 + 55 * ratio);
                b = Math.floor(100 * (1 - ratio));
            } else if (normalizedSpeed < 0.5) {
                const ratio = (normalizedSpeed - 0.25) * 4;
                r = Math.floor(100 + 155 * ratio);
                g = 255;
                b = 0;
            } else if (normalizedSpeed < 0.75) {
                const ratio = (normalizedSpeed - 0.5) * 4;
                r = 255;
                g = Math.floor(255 - 100 * ratio);
                b = 0;
            } else {
                const ratio = (normalizedSpeed - 0.75) * 4;
                r = 255;
                g = Math.floor(155 - 155 * ratio);
                b = 0;
            }

            return `#${this.componentToHex(r)}${this.componentToHex(g)}${this.componentToHex(b)}`;
        },

        componentToHex(c) {
            const hex = c.toString(16);
            return hex.length === 1 ? '0' + hex : hex;
        },

        shareFile(fileEntry) {
            try {
                uni.share({
                    provider: 'system',
                    type: 'file',
                    filePath: fileEntry.toURL(),
                    success: (res) => {
                        console.log('文件分享成功');
                    },
                    fail: (err) => {
                        console.error('文件分享失败:', err);
                        uni.showToast({
                            title: '分享失败',
                            icon: 'none'
                        });
                    }
                });
            } catch (error) {
                console.error('分享文件失败:', error);
                uni.showToast({
                    title: '分享失败',
                    icon: 'none'
                });
            }
        },

        // 新增：格式化日期
        formatDate(timeStr) {
            if (!timeStr) return '未知日期';
            const date = new Date(timeStr);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            
            return `${year}/${month}/${day} ${hours}:${minutes}`;
        },

        formatTime(timeStr) {
            if (!timeStr) return '未知';
            const date = new Date(timeStr);
            const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
            const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

            const weekday = weekdays[date.getDay()];
            const month = months[date.getMonth()];
            const day = date.getDate();
            const year = date.getFullYear();
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            const seconds = String(date.getSeconds()).padStart(2, '0');

            return `${weekday} ${month} ${day} ${year} ${hours}:${minutes}:${seconds} GMT+0800 (CST)`;
        },

        formatDistance(distance) {
            if (!distance) return '0m';
            if (distance < 1000) {
                return distance.toFixed(0) + 'm';
            } else {
                return (distance / 1000).toFixed(2) + 'km';
            }
        },

        formatSpeed(speed) {
            if (!speed) return '0m/s';
            return speed.toFixed(1) + 'm/s';
        },

        formatCoordinate(coord) {
            if (!coord) return '0.000000';
            return coord.toFixed(6);
        },

        getStatusClass(status) {
            const statusClassMap = {
                1: 'status-active',
                2: 'status-completed',
                0: 'status-ended'
            };
            return statusClassMap[status] || 'status-unknown';
        },

        getStatusText(status) {
            const statusMap = {
                1: '进行中',
                2: '已完成',
                0: '已结束'
            };
            return statusMap[status] || '未知';
        },

        // 返回上一页
        goBack(){
        	uni.navigateBack()
        },

        forceHeaderVisible() {
            // 每 500ms 检查并强制显示标题栏
            const interval = setInterval(() => {
                this.headerVisible = false;
                this.$nextTick(() => {
                    this.headerVisible = true;
                });
            }, 500);

            // 1秒后停止检查
            setTimeout(() => {
                clearInterval(interval);
            }, 1000);
        }
    }
}
</script>

<style scoped>
.container {
    flex: 1;
    background-color: #f5f5f5;
}

/* 新增：轨迹名称标题栏样式 */
.track-header-bar {
    position: absolute;
    top: 60rpx;
    left: 10rpx; /* 再向左移动10rpx */
    right: 30rpx;
    z-index: 99999 !important; /* 强制最高层级 */
    display: flex;
    align-items: center;
    background: transparent; /* 透明背景 */
    padding: 20rpx;
    box-sizing: border-box;
    /* 移除所有动画、过渡、阴影等复杂样式 */
}

/* 返回按钮样式 */
.back-button {
    width: 64rpx;
    height: 64rpx;
    background: transparent; /* 透明背景 */
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;
}

.back-icon {
    width: 48rpx;
    height: 48rpx;
}

.track-title {
    font-size: 42rpx;
    font-weight: 800; /* 加粗字体 */
    color: #1a1a1a;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    flex: 1;
}

.map-container {
    position: relative;
    height: 750rpx;
    width: 100%;
    z-index: 1; /* 确保容器本身有层级 */
}

.info-panel {
    position: relative;
    z-index: 2; /* 确保在地图容器之上 */
    background: white;
    border-radius: 20rpx 20rpx 0 0;
    margin-top: -20rpx;
    padding: 30rpx;
    min-height: calc(100vh - 730rpx);
}

.map {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 1; /* 确保在底层 */
}

.map-controls {
    position: absolute;
    top: 150rpx; /* 向下移动130rpx */
    right: 20rpx;
    z-index: 99999 !important; /* 与标题栏同级 */
}

.map-control {
    width: 60rpx;
    height: 60rpx;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 30rpx;
    margin-bottom: 15rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.control-icon {
    width: 36rpx;
    height: 36rpx;
}

/* 重构：轨迹信息卡片样式 - 缩短卡片长度 */
.track-main-info-card {
    background: #ffffff;
    border-radius: 16rpx;
    padding: 24rpx; /* 减小内边距从30rpx到24rpx */
    margin-bottom: 24rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
    border: 1rpx solid #f0f0f0;
}

.track-main-info-card .card-header {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx; /* 减小下边距从24rpx到20rpx */
}

.distance-section {
    position: relative;
    flex: 1;
    padding-right: 200rpx; /* 为更宽的右上角按钮留出空间 */
}

.distance-value {
    font-size: 56rpx; /* 减小字体从64rpx到56rpx */
    font-weight: 700;
    color: #1a1a1a;
    display: block;
    line-height: 1.2;
}

.date-text {
    font-size: 26rpx; /* 减小字体从28rpx到26rpx */
    color: #8c8c8c;
    display: block;
    margin-top: 6rpx; /* 减小上边距从8rpx到6rpx */
}

/* 播放按钮样式 - 右上角固定大小按钮 */
.play-button-corner {
    position: absolute;
    top: 24rpx; /* 调整位置从30rpx到24rpx */
    right: 24rpx; /* 调整位置从30rpx到24rpx */
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: flex-start;  /* 改为左对齐 */
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
    padding: 14rpx 24rpx 14rpx 18rpx;  /* 减小内边距 */
    border-radius: 32rpx;
    border: none;
    margin: 0;
    z-index: 5;
    width: 200rpx;
    height: 60rpx; /* 减小高度从64rpx到60rpx */
    box-sizing: border-box;
}

.play-icon-corner {
    width: 26rpx;  /* 减小图标从28rpx到26rpx */
    height: 26rpx;
    margin-right: 8rpx;
}

.play-text-corner {
    font-size: 26rpx;  /* 减小字体从28rpx到26rpx */
    color: white;
    font-weight: 500;
    white-space: nowrap;
}

.play-button-corner[disabled] {
    opacity: 0.6;
}

/* 统计项布局 - 使用span在同一行显示 */
.stats-container {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    margin-top: 16rpx;
}

.stat-span {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex: 1;
    text-align: center;
    padding: 8rpx;
}

.stat-icon {
    width: 32rpx;
    height: 32rpx;
    margin-bottom: 6rpx;
}

.stat-value {
    font-size: 28rpx;
    font-weight: 600;
    color: #1a1a1a;
    line-height: 1.2;
    margin-bottom: 2rpx;
}

.stat-label {
    font-size: 22rpx;
    color: #8c8c8c;
    line-height: 1.2;
}

/* 速度色带图例样式 */
.speed-legend {
    margin-top: 16rpx;
    margin-bottom: 16rpx;
}

/* 平滑渐变色带 */
.color-bar-gradient {
    height: 8rpx;
    border-radius: 4rpx;
    margin-bottom: 6rpx;
    background: linear-gradient(90deg,
        #5CB85C 0%,    /* 绿色 - 慢速 */
        #F0AD4E 50%,   /* 橙色 - 中速 */
        #D9534F 100%   /* 红色 - 快速 */
    );
}

/* 简化的速度标签 */
.speed-labels-simple {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
}

.speed-label {
    font-size: 20rpx;
    color: #666666;
}

/* 控制按钮样式 */
.control-buttons {
    flex-direction: row;
    justify-content: space-between;
    margin-bottom: 30rpx;
}

.btn {
    flex: 1;
    margin: 0 5rpx;
    border-radius: 12rpx;
    font-size: 28rpx;
    height: 88rpx;
    line-height: 88rpx;
    font-weight: 500;
}

.btn-primary {
    background: #0078D4;
    color: white;
    border: none;
}

.btn-secondary {
    background: #f8f9fa;
    color: #1a1a1a;
    border: 1rpx solid #e8e8e8;
}

/* 轨迹点列表样式 */
.points-card {
    margin-bottom: 0;
}

.points-card .card-header {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
}

.card-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #1a1a1a;
}

.points-count {
    font-size: 26rpx;
    color: #8c8c8c;
    font-weight: 500;
}

.points-list {
    max-height: 400rpx;
}

.point-item {
    flex-direction: row;
    align-items: flex-start;
    padding: 20rpx 24rpx;
    border-bottom: 1rpx solid #f8f9fa;
    background: white;
}

.point-item:last-child {
    border-bottom: none;
}

.point-item:active {
    background: #f8f9fa;
    border-radius: 12rpx;
}

.point-index {
    width: 40rpx;
    height: 40rpx;
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
    color: white;
    border-radius: 20rpx;
    text-align: center;
    line-height: 40rpx;
    font-size: 20rpx;
    font-weight: 600;
    margin-right: 16rpx;
    flex-shrink: 0;
    margin-top: 2rpx;
}

.point-info {
    flex: 1;
}

.point-coords {
    font-size: 24rpx;
    color: #1a1a1a;
    display: block;
    margin-bottom: 4rpx;
    font-weight: 500;
}

.point-meta {
    font-size: 24rpx;
    color: #8c8c8c;
    display: block;
    margin-bottom: 2rpx;
}

.point-time {
    font-size: 24rpx;
    color: #bfbfbf;
    display: block;
}

.point-item-selected {
    background: #f0f8ff !important;
    border-radius: 12rpx;
    padding: 20rpx}

.point-index-selected {
    background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%) !important;
    box-shadow: 0 2rpx 8rpx rgba(255, 77, 79, 0.3);
}
</style>