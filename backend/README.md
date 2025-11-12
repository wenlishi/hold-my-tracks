# 个人轨迹管理与分析系统 - 后端服务

基于Spring Boot的后端服务，提供用户管理、轨迹采集、数据分析等功能。

## 技术栈

- **框架**: Spring Boot 2.7+
- **安全**: Spring Security + JWT
- **数据库**: PostgreSQL
- **ORM**: MyBatis Plus
- **缓存**: Redis
- **API文档**: Swagger/OpenAPI 3.0

## 功能模块

1. **用户管理模块**
   - 用户注册
   - 用户登录
   - JWT认证
   - 密码加密

2. **轨迹管理模块**
   - 轨迹创建
   - 轨迹点采集
   - 轨迹查询
   - 轨迹分析

3. **系统管理模块**
   - 权限管理
   - 数据统计
   - 系统监控

## 数据库设计

### 用户表 (users)
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    real_name VARCHAR(50),
    status INTEGER DEFAULT 1, -- 1:正常 0:禁用
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 轨迹表 (tracks)
```sql
CREATE TABLE tracks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    track_name VARCHAR(100) NOT NULL,
    description TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    total_distance DECIMAL(10,2),
    total_points INTEGER,
    status INTEGER DEFAULT 1, -- 1:进行中 2:已完成
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 轨迹点表 (track_points)
```sql
CREATE TABLE track_points (
    id BIGSERIAL PRIMARY KEY,
    track_id BIGINT NOT NULL,
    longitude DECIMAL(10,6) NOT NULL, -- 经度
    latitude DECIMAL(10,6) NOT NULL,  -- 纬度
    altitude DECIMAL(8,2),            -- 海拔
    speed DECIMAL(6,2),               -- 速度
    accuracy DECIMAL(6,2),            -- 精度
    satellite_count INTEGER,          -- 卫星数量
    address VARCHAR(255),             -- 地址信息
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (track_id) REFERENCES tracks(id)
);
```

### 设备表 (devices)
```sql
CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_name VARCHAR(100),
    device_type VARCHAR(50),
    device_id VARCHAR(100) UNIQUE,
    system_version VARCHAR(50),
    app_version VARCHAR(50),
    status INTEGER DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## API接口设计

### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/refresh` - 刷新token
- `POST /api/auth/logout` - 用户登出

### 用户相关
- `GET /api/user/info` - 获取用户信息
- `PUT /api/user/info` - 更新用户信息
- `PUT /api/user/password` - 修改密码

### 轨迹相关
- `POST /api/tracks` - 创建新轨迹
- `GET /api/tracks` - 获取轨迹列表
- `GET /api/tracks/{id}` - 获取轨迹详情
- `PUT /api/tracks/{id}` - 更新轨迹
- `DELETE /api/tracks/{id}` - 删除轨迹
- `POST /api/tracks/{id}/points` - 添加轨迹点
- `GET /api/tracks/{id}/points` - 获取轨迹点列表
- `GET /api/tracks/{id}/heatmap` - 获取热力图数据

### 设备相关
- `POST /api/devices` - 注册设备
- `PUT /api/devices/{id}` - 更新设备信息
- `GET /api/devices` - 获取设备列表

## 部署说明

### 环境要求
- JDK 11+
- PostgreSQL 12+
- Redis 6+

### 配置文件
主要配置文件：`application.yml`

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/track_db
    username: postgres
    password: your_password
    driver-class-name: org.postgresql.Driver

  redis:
    host: localhost
    port: 6379
    password: your_redis_password
    database: 0

jwt:
  secret: your-jwt-secret-key
  expiration: 86400
```

## 开发说明

### 项目结构
```
backend/
├── src/main/java/com/track/
│   ├── config/          # 配置类
│   ├── controller/      # 控制器
│   ├── service/         # 服务层
│   ├── mapper/          # 数据访问层
│   ├── entity/          # 实体类
│   ├── dto/             # 数据传输对象
│   ├── security/        # 安全相关
│   └── util/            # 工具类
├── src/main/resources/
│   ├── mapper/          # MyBatis映射文件
│   └── application.yml  # 配置文件
└── pom.xml              # Maven配置
```