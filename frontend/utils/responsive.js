// 响应式布局工具类
export default {
  // 获取设备信息
  getDeviceInfo() {
    return new Promise((resolve, reject) => {
      uni.getSystemInfo({
        success: (res) => {
          resolve(res);
        },
        fail: (err) => {
          reject(err);
        }
      });
    });
  },

  // 判断设备类型
  getDeviceType() {
    const systemInfo = uni.getSystemInfoSync();
    const { platform, screenWidth } = systemInfo;

    // 平板设备判断（屏幕宽度大于600px）
    if (screenWidth > 600) {
      return 'tablet';
    }

    // 手机设备
    return 'phone';
  },

  // 响应式单位转换
  rpxToPx(rpx) {
    const systemInfo = uni.getSystemInfoSync();
    return (rpx * systemInfo.screenWidth) / 750;
  },

  pxToRpx(px) {
    const systemInfo = uni.getSystemInfoSync();
    return (px * 750) / systemInfo.screenWidth;
  },

  // 响应式字体大小
  getFontSize(baseSize = 14) {
    const deviceType = this.getDeviceType();
    switch (deviceType) {
      case 'tablet':
        return baseSize * 1.2;
      case 'phone':
      default:
        return baseSize;
    }
  },

  // 响应式间距
  getSpacing(baseSpacing = 10) {
    const deviceType = this.getDeviceType();
    switch (deviceType) {
      case 'tablet':
        return baseSpacing * 1.5;
      case 'phone':
      default:
        return baseSpacing;
    }
  },

  // 响应式布局类
  getLayoutClass() {
    const deviceType = this.getDeviceType();
    return {
      'responsive-container': true,
      [`device-${deviceType}`]: true
    };
  },

  // 响应式网格布局
  getGridColumns() {
    const deviceType = this.getDeviceType();
    switch (deviceType) {
      case 'tablet':
        return 3; // 平板显示3列
      case 'phone':
      default:
        return 1; // 手机显示1列
    }
  },

  // 响应式图片尺寸
  getImageSize(baseSize = 100) {
    const deviceType = this.getDeviceType();
    switch (deviceType) {
      case 'tablet':
        return baseSize * 1.5;
      case 'phone':
      default:
        return baseSize;
    }
  }
};