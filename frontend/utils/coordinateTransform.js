/**
 * 坐标转换工具类
 * 提供WGS84、GCJ02、BD09坐标系之间的转换
 */

/**
 * 常量定义
 */
const PI = Math.PI;
const X_PI = PI * 3000.0 / 180.0;

/**
 * 判断坐标是否在中国境内
 * @param {number} lng - 经度
 * @param {number} lat - 纬度
 * @returns {boolean}
 */
function isInChina(lng, lat) {
    // 中国大致经纬度范围
    return lng >= 72.004 && lng <= 137.8347 && lat >= 0.8293 && lat <= 55.8271;
}

/**
 * 转换经度
 * @param {number} lng - 经度
 * @param {number} lat - 纬度
 * @returns {number}
 */
function transformLng(lng, lat) {
    let ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
    ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
    ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
    return ret;
}

/**
 * 转换纬度
 * @param {number} lng - 经度
 * @param {number} lat - 纬度
 * @returns {number}
 */
function transformLat(lng, lat) {
    let ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
    ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
    ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
    return ret;
}

/**
 * WGS84转GCJ02 (火星坐标系)
 * @param {number} lng - WGS84经度
 * @param {number} lat - WGS84纬度
 * @returns {[number, number]} [gcjLng, gcjLat]
 */
export function wgs84ToGcj02(lng, lat) {
    if (!isInChina(lng, lat)) {
        return [lng, lat];
    }

    let dLat = transformLat(lng - 105.0, lat - 35.0);
    let dLng = transformLng(lng - 105.0, lat - 35.0);
    const radLat = lat / 180.0 * PI;
    let magic = Math.sin(radLat);
    magic = 1 - 0.00669342162296594323 * magic * magic;
    const sqrtMagic = Math.sqrt(magic);
    dLat = (dLat * 180.0) / ((6378245.0 * (1 - 0.00669342162296594323)) / (magic * sqrtMagic) * PI);
    dLng = (dLng * 180.0) / (6378245.0 / sqrtMagic * Math.cos(radLat) * PI);

    const mgLat = lat + dLat;
    const mgLng = lng + dLng;

    return [mgLng, mgLat];
}

/**
 * GCJ02转WGS84
 * @param {number} lng - GCJ02经度
 * @param {number} lat - GCJ02纬度
 * @returns {[number, number]} [wgsLng, wgsLat]
 */
export function gcj02ToWgs84(lng, lat) {
    if (!isInChina(lng, lat)) {
        return [lng, lat];
    }

    let dLat = transformLat(lng - 105.0, lat - 35.0);
    let dLng = transformLng(lng - 105.0, lat - 35.0);
    const radLat = lat / 180.0 * PI;
    let magic = Math.sin(radLat);
    magic = 1 - 0.00669342162296594323 * magic * magic;
    const sqrtMagic = Math.sqrt(magic);
    dLat = (dLat * 180.0) / ((6378245.0 * (1 - 0.00669342162296594323)) / (magic * sqrtMagic) * PI);
    dLng = (dLng * 180.0) / (6378245.0 / sqrtMagic * Math.cos(radLat) * PI);

    const wgsLat = lat - dLat;
    const wgsLng = lng - dLng;

    return [wgsLng, wgsLat];
}

/**
 * GCJ02转BD09 (百度坐标系)
 * @param {number} lng - GCJ02经度
 * @param {number} lat - GCJ02纬度
 * @returns {[number, number]} [bdLng, bdLat]
 */
export function gcj02ToBd09(lng, lat) {
    const z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * X_PI);
    const theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * X_PI);
    const bdLng = z * Math.cos(theta) + 0.0065;
    const bdLat = z * Math.sin(theta) + 0.006;
    return [bdLng, bdLat];
}

/**
 * BD09转GCJ02
 * @param {number} lng - BD09经度
 * @param {number} lat - BD09纬度
 * @returns {[number, number]} [gcjLng, gcjLat]
 */
export function bd09ToGcj02(lng, lat) {
    const x = lng - 0.0065;
    const y = lat - 0.006;
    const z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
    const theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
    const gcjLng = z * Math.cos(theta);
    const gcjLat = z * Math.sin(theta);
    return [gcjLng, gcjLat];
}

/**
 * WGS84转BD09
 * @param {number} lng - WGS84经度
 * @param {number} lat - WGS84纬度
 * @returns {[number, number]} [bdLng, bdLat]
 */
export function wgs84ToBd09(lng, lat) {
    const [gcjLng, gcjLat] = wgs84ToGcj02(lng, lat);
    return gcj02ToBd09(gcjLng, gcjLat);
}

/**
 * BD09转WGS84
 * @param {number} lng - BD09经度
 * @param {number} lat - BD09纬度
 * @returns {[number, number]} [wgsLng, wgsLat]
 */
export function bd09ToWgs84(lng, lat) {
    const [gcjLng, gcjLat] = bd09ToGcj02(lng, lat);
    return gcj02ToWgs84(gcjLng, gcjLat);
}

/**
 * 批量转换坐标点
 * @param {Array} points - 坐标点数组 [{longitude, latitude}, ...]
 * @param {Function} transformFn - 转换函数
 * @returns {Array} 转换后的坐标点数组
 */
export function transformPoints(points, transformFn) {
    return points.map(point => {
        const [newLng, newLat] = transformFn(point.longitude, point.latitude);
        return {
            ...point,
            longitude: newLng,
            latitude: newLat
        };
    });
}

/**
 * 坐标转换工具类
 */
export default {
    wgs84ToGcj02,
    gcj02ToWgs84,
    gcj02ToBd09,
    bd09ToGcj02,
    wgs84ToBd09,
    bd09ToWgs84,
    transformPoints
};