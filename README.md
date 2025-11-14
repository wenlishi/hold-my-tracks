# 个人轨迹管理与分析系统

一个基于uni-app开发的个人轨迹管理与分析移动应用，包含前端移动端和后端服务。

## 项目结构

```
├── frontend/              # uni-app前端项目
│   ├── components/        # 组件
│   ├── pages/            # 页面
│   ├── static/           # 静态资源
│   ├── utils/            # 工具函数
│   ├── uni_modules/      # uni-app模块
│   ├── common/           # 公共代码
│   ├── nativeplugins/    # 原生插件
│   ├── App.vue           # 应用入口
│   ├── main.js           # 主程序
│   ├── manifest.json     # 应用配置
│   ├── pages.json        # 页面配置
│   ├── package.json      # 依赖配置
│   └── vue.config.js     # Vue配置
├── backend/              # Java后端服务
│   ├── src/              # 源代码
│   ├── pom.xml           # Maven配置
│   ├── Dockerfile        # 容器配置
│   └── docker-compose.yml # 容器编排
├── unpackage/            # 构建产物（不上传）
├── .gitignore            # Git忽略配置
└── README.md             # 项目说明
```

## 技术栈

### 前端
- uni-app (Vue 2)
- 高德地图API
- ECharts图表
- Axios网络请求

### 后端
- Java
- Spring Boot
- Maven

## 环境配置

### 前端配置（基于uni-app）

**重要：本项目是基于uni-app开发的移动应用，需要使用HBuilderX进行开发和构建。**

1. **安装HBuilderX**
   - 下载并安装 [HBuilderX](https://www.dcloud.io/hbuilderx.html)
   - 建议使用标准版或App开发版

2. **打开项目**
   - 使用HBuilderX打开 `frontend` 目录
   - 项目会自动识别为uni-app项目

3. **安装第三方依赖**
   ```bash
   cd frontend
   npm install
   ```

   **第三方依赖说明：**
   - `@amap/amap-jsapi-loader` - 高德地图JavaScript API加载器
   - `axios` - HTTP请求库
   - `cheerio` - 服务器端HTML解析（用于数据抓取）
   - `cordova` - 混合应用开发框架
   - `echarts` - 数据可视化图表库
   - `htmlparser2` - HTML解析器
   - `http` - Node.js HTTP模块

4. **配置开发环境**
   - 在 `manifest.json` 中配置高德地图API密钥
   - 在`config.js`中配置后端api的url
   - 根据需要配置其他应用设置
   

5. **运行项目**
   - 在HBuilderX中选择运行平台（H5、微信小程序、App等）
   - 点击运行按钮启动开发环境

### 后端配置
1. 安装Java 8+
2. 安装Maven
3. 运行：`mvn spring-boot:run`

## APK打包教程

**重要：APK打包必须使用HBuilderX，uni-app的构建和打包由HBuilderX统一管理。**

### 使用HBuilderX云打包（推荐）

1. **准备工作**
   - 确保已安装 [HBuilderX](https://www.dcloud.io/hbuilderx.html)
   - 使用HBuilderX打开 `frontend` 目录
   - 确保项目能正常运行

2. **配置应用信息**
   - 打开 `manifest.json` 文件
   - 在 "基础配置" 中设置应用名称、版本等
   - 在 "App图标配置" 中上传应用图标
   - 在 "App启动图配置" 中设置启动画面
   - 在 "App模块配置" 中确保已勾选所需模块（地图、定位等）

3. **配置高德地图**
   - 在 `manifest.json` → "App SDK配置" → "地图" → "高德地图"
   - 配置Android和iOS平台的API密钥

4. **生成签名证书**
   - 点击 "发行" → "原生App-云打包"
   - 选择 "Android" 平台
   - 点击 "证书" 生成或选择现有证书
   - 填写证书别名、密码等信息（妥善保管）

5. **执行云打包**
   - 选择打包模式："传统打包" 或 "安心打包"
   - 勾选需要的模块和权限
   - 点击 "打包" 等待云端构建完成
   - 下载生成的APK文件到 `unpackage/release/apk/` 目录

### 方式二：使用命令行

1. **安装uni-app CLI**
   ```bash
   npm install -g @dcloudio/uni-cli
   ```

2. **构建Android项目**
   ```bash
   # 构建Android项目
   npm run build:app-plus
   
   # 或使用HBuilderX CLI
   hbx build --platform android
   ```

3. **使用Android Studio打包**
   - 生成的Android项目位于 `unpackage/dist/build/android`
   - 用Android Studio打开项目
   - 配置签名证书
   - 执行 Build → Generate Signed Bundle / APK

### 方式三：使用GitHub Actions自动构建

创建 `.github/workflows/build-android.yml`：

```yaml
name: Build Android APK

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '16'

    - name: Install dependencies
      run: npm install

    - name: Build APK
      run: |
        # 这里需要配置HBuilderX云打包API
        # 或使用其他构建方式
        echo "构建APK"

    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-release
        path: unpackage/release/apk/*.apk
```

### 打包注意事项

1. **证书安全**
   - 妥善保管签名证书和密码
   - 不要将证书文件上传到Git仓库
   - 建议使用不同的证书用于调试和发布

2. **权限配置**
   - 在 `manifest.json` 中正确配置应用权限
   - 确保位置权限、存储权限等已开启

3. **API密钥配置**
   - 配置高德地图Android平台API密钥
   - 确保密钥与打包证书的SHA1指纹匹配

4. **版本管理**
   - 每次发布更新 `versionName` 和 `versionCode`
   - `versionCode` 必须递增

## API密钥配置

项目使用高德地图服务，需要配置API密钥：

1. 申请高德地图开发者账号
2. 获取Web服务API Key和Android/iOS Key
3. 在manifest.json中配置对应的密钥

## 功能特性

- 个人轨迹记录
- 轨迹数据展示
- 地图轨迹可视化
- 轨迹点热力图

## 许可证

MIT License