// 热力图工具函数
import { wgs84ToGcj02 } from './coordinateTransform.js';

/**
 * 将轨迹点数据转换为热力图数据格式
 * @param {Array} trackPoints - 轨迹点数组
 * @param {Object} options - 配置选项
 * @returns {Array} 热力图数据
 */
export function convertToHeatmapData(trackPoints, options = {}) {
  const {
    clusterRadius = 0.001, // 聚类半径（度）
    maxPoints = 1000,      // 最大点数
    weightField = null,    // 权重字段
    coordinateSystem = 'gcj02' // 坐标系：'wgs84' 或 'gcj02'
  } = options;

  if (!trackPoints || trackPoints.length === 0) {
    return [];
  }

  // 如果点数过多，进行采样
  let processedPoints = trackPoints;
  if (trackPoints.length > maxPoints) {
    processedPoints = samplePoints(trackPoints, maxPoints);
  }

  // 转换为热力图格式
  return processedPoints.map(point => {
    const weight = weightField ? (point[weightField] || 1) : 1;

    // 根据坐标系进行坐标转换
    let lng = point.longitude || point.x;
    let lat = point.latitude || point.y;

    // 如果后端存储的是WGS84坐标，但热力图需要GCJ02坐标，则进行转换
    if (coordinateSystem === 'gcj02') {
      [lng, lat] = wgs84ToGcj02(lng, lat);
    }

    return {
      lng: lng,
      lat: lat,
      count: weight,
      // 保留原始数据用于交互
      originalData: point
    };
  });
}

/**
 * 轨迹点采样（减少数据量）
 * @param {Array} points - 原始轨迹点
 * @param {Number} maxCount - 最大点数
 * @returns {Array} 采样后的点
 */
function samplePoints(points, maxCount) {
  if (points.length <= maxCount) {
    return points;
  }

  const step = Math.ceil(points.length / maxCount);
  const sampledPoints = [];

  for (let i = 0; i < points.length; i += step) {
    sampledPoints.push(points[i]);
  }

  return sampledPoints;
}

/**
 * 计算热力图中心点和缩放级别
 * @param {Array} heatmapData - 热力图数据
 * @returns {Object} 中心点和缩放级别
 */
export function calculateHeatmapView(heatmapData) {
  if (!heatmapData || heatmapData.length === 0) {
    return {
      center: { longitude: 116.397477, latitude: 39.908692 },
      scale: 10
    };
  }

  const lngs = heatmapData.map(point => point.lng);
  const lats = heatmapData.map(point => point.lat);

  const minLng = Math.min(...lngs);
  const maxLng = Math.max(...lngs);
  const minLat = Math.min(...lats);
  const maxLat = Math.max(...lats);

  const centerLng = (minLng + maxLng) / 2;
  const centerLat = (minLat + maxLat) / 2;

  // 计算合适的缩放级别
  const lngSpan = maxLng - minLng;
  const latSpan = maxLat - minLat;
  const maxSpan = Math.max(lngSpan, latSpan);

  let scale;
  if (maxSpan < 0.001) scale = 18;
  else if (maxSpan < 0.005) scale = 16;
  else if (maxSpan < 0.01) scale = 14;
  else if (maxSpan < 0.05) scale = 12;
  else if (maxSpan < 0.1) scale = 10;
  else scale = 8;

  return {
    center: { longitude: centerLng, latitude: centerLat },
    scale
  };
}

/**
 * 生成热力图渐变配置
 * @param {String} type - 渐变类型
 * @returns {Object} 渐变配置
 */
export function getHeatmapGradient(type = 'default') {
  const gradients = {
    default: {
      0.4: 'blue',
      0.6: 'cyan',
      0.7: 'lime',
      0.8: 'yellow',
      1.0: 'red'
    },
    cool: {
      0.4: '#1E3A8A',
      0.6: '#3B82F6',
      0.7: '#10B981',
      0.8: '#F59E0B',
      1.0: '#EF4444'
    },
    warm: {
      0.3: '#FBBF24',
      0.5: '#F59E0B',
      0.7: '#DC2626',
      0.9: '#7C2D12',
      1.0: '#450A0A'
    },
    traffic: {
      0.2: '#10B981',
      0.4: '#F59E0B',
      0.6: '#F97316',
      0.8: '#EF4444',
      1.0: '#7F1D1D'
    }
  };

  return gradients[type] || gradients.default;
}

/**
 * 获取热力图配置
 * @param {Object} options - 配置选项
 * @returns {Object} 热力图配置
 */
export function getHeatmapConfig(options = {}) {
  const {
    radius = 25,
    opacity = [0, 0.8],
    gradientType = 'default',
    max = 100
  } = options;

  return {
    radius,
    opacity,
    gradient: getHeatmapGradient(gradientType),
    max
  };
}

/**
 * 按时间过滤轨迹点
 * @param {Array} trackPoints - 轨迹点数组
 * @param {String} startTime - 开始时间
 * @param {String} endTime - 结束时间
 * @returns {Array} 过滤后的轨迹点
 */
export function filterPointsByTime(trackPoints, startTime, endTime) {
  if (!startTime && !endTime) {
    return trackPoints;
  }

  const start = startTime ? new Date(startTime).getTime() : 0;
  const end = endTime ? new Date(endTime).getTime() : Date.now();

  return trackPoints.filter(point => {
    const pointTime = point.createTime ? new Date(point.createTime).getTime() : 0;
    return pointTime >= start && pointTime <= end;
  });
}

/**
 * 计算热力图统计数据
 * @param {Array} heatmapData - 热力图数据
 * @returns {Object} 统计数据
 */
export function calculateHeatmapStats(heatmapData) {
  if (!heatmapData || heatmapData.length === 0) {
    return {
      totalPoints: 0,
      maxDensity: 0,
      avgDensity: 0,
      coverageArea: 0
    };
  }

  const counts = heatmapData.map(point => point.count || 1);
  const maxDensity = Math.max(...counts);
  const avgDensity = counts.reduce((sum, count) => sum + count, 0) / counts.length;

  // 估算覆盖面积（简化计算）
  const lngs = heatmapData.map(point => point.lng);
  const lats = heatmapData.map(point => point.lat);
  const lngSpan = Math.max(...lngs) - Math.min(...lngs);
  const latSpan = Math.max(...lats) - Math.min(...lats);
  const coverageArea = lngSpan * latSpan * 111 * 111; // 转换为平方公里（近似）

  return {
    totalPoints: heatmapData.length,
    maxDensity,
    avgDensity: Math.round(avgDensity * 100) / 100,
    coverageArea: Math.round(coverageArea * 100) / 100
  };
}