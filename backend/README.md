# 个人轨迹管理与分析系统 - 后端服务

基于Spring Boot的个人轨迹管理与分析系统后端服务，提供RESTful API接口。

## 技术栈

- **框架**: Spring Boot 2.7.18
- **安全**: Spring Security + JWT
- **数据库**: PostgreSQL
- **缓存**: Redis
- **ORM**: MyBatis-Plus
- **构建工具**: Maven
- **容器化**: Docker + Docker Compose

## 项目结构

```
backend/
├── src/main/java/com/track/
│   ├── config/          # 配置类
│   │   ├── MyBatisPlusConfig.java
│   │   ├── RedisConfig.java
│   │   └── SecurityConfig.java
│   ├── controller/      # 控制器
│   │   ├── AuthController.java
│   │   ├── MobileController.java
│   │   ├── TrackController.java
│   │   └── TrackPointController.java
│   ├── dto/            # 数据传输对象
│   ├── entity/         # 实体类
│   ├── mapper/         # MyBatis映射器
│   ├── service/        # 业务服务
│   └── util/           # 工具类
├── src/main/resources/
│   ├── application.yml # 应用配置
│   └── schema.sql      # 数据库初始化脚本
├── Dockerfile          # Docker镜像构建
├── docker-compose.yml  # 容器编排
└── pom.xml            # Maven配置
```

## 快速开始

### 环境要求

- Java 8+
- Maven 3.6+
- Docker 20.10+
- Docker Compose 1.29+

### 使用Docker Compose启动（推荐）

1. **启动所有服务**
   ```bash
   docker-compose up -d
   ```

2. **查看服务状态**
   ```bash
   docker-compose ps
   ```

3. **停止服务**
   ```bash
   docker-compose down
   ```

### 本地开发环境

1. **启动依赖服务**
   ```bash
   # 只启动数据库和Redis
   docker-compose up -d postgres redis
   ```

2. **配置环境变量**
   ```bash
   # 复制并修改配置文件
   cp src/main/resources/application.yml.example src/main/resources/application.yml
   ```

3. **运行应用**
   ```bash
   # 方式一：使用Maven
   mvn spring-boot:run

   # 方式二：打包后运行
   mvn clean package
   java -jar target/track-backend-1.0.0.jar
   ```

## API文档

### 认证接口

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/logout` - 用户登出

### 轨迹管理接口

- `GET /api/tracks` - 获取轨迹列表
- `POST /api/tracks` - 创建新轨迹
- `GET /api/tracks/{id}` - 获取轨迹详情
- `PUT /api/tracks/{id}` - 更新轨迹
- `DELETE /api/tracks/{id}` - 删除轨迹

### 轨迹点接口

- `GET /api/track-points` - 获取轨迹点列表
- `POST /api/track-points` - 添加轨迹点
- `GET /api/track-points/{trackId}` - 获取指定轨迹的所有点

## 数据库配置

### PostgreSQL连接
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/track_db
    username: postgres
    password: 123456
    driver-class-name: org.postgresql.Driver
```

### Redis配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
```

## 安全配置

- JWT Token认证
- Spring Security权限控制
- 密码加密存储
- API接口权限验证

## 部署

### 构建Docker镜像
```bash
# 构建镜像
docker build -t track-backend:latest .

# 运行容器
docker run -p 8080:8080 track-backend:latest
```

### 生产环境配置

1. 修改数据库密码
2. 配置JWT密钥
3. 设置Redis密码
4. 配置HTTPS证书

## 开发指南

### 添加新的API接口

1. 在`controller`包下创建新的控制器
2. 在`service`包下实现业务逻辑
3. 在`entity`和`mapper`包下定义数据模型
4. 在`dto`包下定义数据传输对象

### 数据库迁移

数据库初始化脚本位于：`src/main/resources/schema.sql`

### 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

## 故障排除

### 常见问题

1. **端口冲突**
   - 检查8080端口是否被占用
   - 修改`application.yml`中的端口配置

2. **数据库连接失败**
   - 确认PostgreSQL服务已启动
   - 检查数据库连接配置

3. **Redis连接失败**
   - 确认Redis服务已启动
   - 检查Redis连接配置

## 许可证

MIT License