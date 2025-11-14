// 统一的API请求工具类

// 导入配置文件
import config from '../config.js';

// API基础URL - 从配置文件读取
const BASE_URL = config.baseUrl;

// 请求拦截器
const requestInterceptor = (config) => {
  // 添加认证token
  const token = uni.getStorageSync('token');
  if (token) {
    config.header = config.header || {};
    config.header['Authorization'] = `Bearer ${token}`;
  }

  // 添加内容类型
  config.header = config.header || {};
  config.header['content-type'] = 'application/json';

  return config;
};

// 响应拦截器
const responseInterceptor = (response) => {
  if (response.statusCode === 200) {
    return response.data;
  } else if (response.statusCode === 401) {
    // Token过期，跳转到登录页
    uni.removeStorageSync('token');
    uni.removeStorageSync('user');
    uni.showToast({
      title: '登录已过期，请重新登录',
      icon: 'none'
    });
    uni.reLaunch({
      url: '/pages/login/login'
    });
    return Promise.reject(new Error('登录已过期'));
  } else {
    return Promise.reject(new Error('网络请求失败'));
  }
};

// 通用请求方法
const request = (options) => {
  return new Promise((resolve, reject) => {
    // 请求拦截
    const config = requestInterceptor(options);

    uni.request({
      url: `${BASE_URL}${config.url}`,
      method: config.method || 'GET',
      data: config.data,
      header: config.header,
      success: (res) => {
        // 响应拦截
        try {
          const result = responseInterceptor(res);
          resolve(result);
        } catch (error) {
          reject(error);
        }
      },
      fail: (err) => {
        console.error('请求失败:', err);
        uni.showToast({
          title: '网络请求失败',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
};

// 兼容前端原有接口的请求方法
const mobileRequest = (action, params = {}) => {
  const queryString = Object.keys(params)
    .map(key => `${key}=${encodeURIComponent(params[key])}`)
    .join('&');

  return request({
    url: `?action=${action}&${queryString}`,
    method: 'GET'
  });
};

// 认证相关API
export const authApi = {
  // 登录
  login: (username, password) => {
    return request({
      url: '/auth/login',
      method: 'POST',
      data: {
        username,
        password
      }
    });
  },

  // 注册
  register: (userData) => {
    return request({
      url: '/auth/register',
      method: 'POST',
      data: userData
    });
  },

  // 兼容前端登录接口
  mobileLogin: (username, password) => {
    return mobileRequest('login', { username, password });
  }
};

// 轨迹相关API
export const trackApi = {
  // 获取用户轨迹列表
  getTracks: () => {
    return request({
      url: '/tracks',
      method: 'GET'
    });
  },

  // 获取分页轨迹列表
  getTracksWithPagination: (page, pageSize) => {
    return request({
      url: `/tracks?page=${page}&pageSize=${pageSize}`,
      method: 'GET'
    });
  },

  // 搜索轨迹（关键字和日期范围）
  searchTracks: (params) => {
    const { page = 1, pageSize = 10, keyword = '', startDate = '', endDate = '' } = params;

    let url = `/tracks/search?page=${page}&pageSize=${pageSize}`;

    if (keyword) {
      url += `&keyword=${encodeURIComponent(keyword)}`;
    }

    if (startDate) {
      url += `&startDate=${encodeURIComponent(startDate)}`;
    }

    if (endDate) {
      url += `&endDate=${encodeURIComponent(endDate)}`;
    }

    return request({
      url,
      method: 'GET'
    });
  },

  // 创建轨迹
  createTrack: (trackData) => {
    return request({
      url: '/tracks',
      method: 'POST',
      data: trackData
    });
  },

  // 获取轨迹详情
  getTrack: (id) => {
    return request({
      url: `/tracks/${id}`,
      method: 'GET'
    });
  },

  // 更新轨迹
  updateTrack: (id, trackData) => {
    return request({
      url: `/tracks/${id}`,
      method: 'PUT',
      data: trackData
    });
  },

  // 删除轨迹
  deleteTrack: (id) => {
    return request({
      url: `/tracks/${id}`,
      method: 'DELETE'
    });
  },

  // 获取轨迹详情
  getTrackDetail: (id) => {
    return request({
      url: `/tracks/${id}/detail`,
      method: 'GET'
    });
  },

  // 导出轨迹
  exportTrack: (id, format) => {
    return new Promise((resolve, reject) => {
      const token = uni.getStorageSync('token');

      uni.request({
        url: `${BASE_URL}/tracks/${id}/export/${format}`,
        method: 'GET',
        header: {
          'Authorization': token ? `Bearer ${token}` : '',
          'content-type': 'application/json'
        },
        responseType: 'arraybuffer',
        success: (res) => {
          if (res.statusCode === 200) {
            // 将ArrayBuffer转换为Uint8Array
            const arrayBuffer = res.data;
            const uint8Array = new Uint8Array(arrayBuffer);
            resolve(uint8Array);
          } else if (res.statusCode === 401) {
            // Token过期，跳转到登录页
            uni.removeStorageSync('token');
            uni.removeStorageSync('user');
            uni.showToast({
              title: '登录已过期，请重新登录',
              icon: 'none'
            });
            uni.reLaunch({
              url: '/pages/login/login'
            });
            reject(new Error('登录已过期'));
          } else {
            reject(new Error('导出失败'));
          }
        },
        fail: (err) => {
          console.error('导出请求失败:', err);
          uni.showToast({
            title: '网络请求失败',
            icon: 'none'
          });
          reject(err);
        }
      });
    });
  }
};

// 轨迹点相关API
export const trackPointApi = {
  // 添加轨迹点
  addPoint: (pointData) => {
    return request({
      url: '/track-points',
      method: 'POST',
      data: pointData
    });
  },

  // 获取轨迹的轨迹点列表
  getTrackPoints: (trackId) => {
    return request({
      url: `/tracks/${trackId}/points`,
      method: 'GET'
    });
  },

  // 兼容前端添加轨迹点接口
  mobileAddPoint: (liid, x, y, z, speed, address) => {
    return mobileRequest('addcoordpoint', {
      liid,
      x,
      y,
      z,
      speed: speed || '',
      address: address || ''
    });
  },

  // 更新轨迹状态
  updateRoute: (liid) => {
    return mobileRequest('updateroute', { liid });
  }
};

// 热力图相关API
export const heatmapApi = {
  // 获取轨迹热力图数据
  getTrackHeatmap: (trackId) => {
    return request({
      url: `/tracks/${trackId}/heatmap`,
      method: 'GET'
    });
  },

  // 获取用户所有轨迹的热力图数据
  getUserHeatmap: () => {
    return request({
      url: '/heatmap/user',
      method: 'GET'
    });
  },

  // 获取时间范围热力图数据
  getTimeRangeHeatmap: (startTime, endTime) => {
    return request({
      url: '/heatmap/time-range',
      method: 'POST',
      data: {
        startTime,
        endTime
      }
    });
  },

  // 兼容前端热力图数据接口
  mobileGetHeatmapData: (lineid) => {
    return mobileRequest('getheatmapdata', { lineid });
  }
};

// 用户相关API
export const userApi = {
  // 获取用户信息
  getUserInfo: () => {
    return request({
      url: '/users/profile',
      method: 'GET'
    });
  },

  // 更新用户设备信息
  updateDeviceInfo: (deviceInfo) => {
    return request({
      url: '/users/device',
      method: 'PUT',
      data: deviceInfo
    });
  },

  // 兼容前端获取用户信息接口
  mobileGetStaffInfo: (username) => {
    return mobileRequest('getstaffinfo', { username });
  },

  // 兼容前端更新设备信息接口
  mobileUpdateStaffInfo: (username, phonetype) => {
    return mobileRequest('updatestaffinfo', { username, phonetype });
  },

  // 兼容前端修改密码接口
  mobileUpdatePassword: (username, newpwd) => {
    return mobileRequest('updatepassword', { username, newpwd });
  }
};

export default {
  request,
  mobileRequest,
  authApi,
  trackApi,
  trackPointApi,
  heatmapApi,
  userApi
};