# 个人轨迹管理与分析系统 - 后端服务

基于Spring Boot的个人轨迹管理与分析系统后端服务，提供RESTful API接口。

## 技术栈

- **框架**: Spring Boot 2.7.18
- **安全**: Spring Security + JWT
- **数据库**: PostgreSQL
- **缓存**: Redis
- **ORM**: MyBatis-Plus
- **AOP**: Spring AOP + 自定义注解
- **构建工具**: Maven
- **容器化**: Docker + Docker Compose

## 项目结构

```
backend/
├── src/main/java/com/track/
│   ├── annotation/     # 自定义注解
│   │   ├── RequirePermission.java
│   │   ├── LogOperation.java
│   │   └── ValidateParam.java
│   ├── aspect/         # AOP切面
│   │   ├── PermissionAspect.java
│   │   ├── LogAspect.java
│   │   └── GlobalExceptionHandler.java
│   ├── config/         # 配置类
│   │   ├── MyBatisPlusConfig.java
│   │   ├── RedisConfig.java
│   │   └── SecurityConfig.java
│   ├── controller/     # 控制器
│   │   ├── AuthController.java
│   │   ├── TrackController.java
│   │   └── TrackPointController.java
│   ├── dto/           # 数据传输对象
│   ├── entity/        # 实体类
│   ├── mapper/        # MyBatis映射器
│   ├── service/       # 业务服务
│   ├── util/          # 工具类
│   └── validator/     # 校验工具
│       └── ParamValidator.java
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
   cp src/main/resources/application.yml.txt.example src/main/resources/application.yml.txt
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

## 自定义注解系统

本项目实现了基于Spring AOP的自定义注解系统，提供声明式的权限验证、操作日志记录和参数校验功能。

### 权限验证注解 `@RequirePermission`

自动验证用户对资源的访问权限，支持多种资源类型和操作类型。

**使用示例：**
```java
@RequirePermission(resourceType = "track", resourceIdParam = "id")
@GetMapping("/{id}")
public ResponseEntity<Track> getTrack(@PathVariable Long id) {
    // 权限验证已通过AOP处理
    Track track = trackService.getById(id);
    return ResponseEntity.ok(track);
}
```

**参数说明：**
- `resourceType`: 资源类型（如：track, user, point等）
- `resourceIdParam`: 资源ID参数名（方法参数名）
- `operation`: 权限操作类型（如：read, write, delete等）

### 操作日志注解 `@LogOperation`

自动记录方法执行日志，支持SpEL表达式动态获取资源ID。

**使用示例：**
```java
@LogOperation(operation = "查询轨迹", resourceId = "#id", logParams = true)
@GetMapping("/{id}")
public ResponseEntity<Track> getTrack(@PathVariable Long id) {
    // 操作日志已自动记录
    Track track = trackService.getById(id);
    return ResponseEntity.ok(track);
}
```

**参数说明：**
- `operation`: 操作描述
- `resourceId`: 资源ID表达式（支持SpEL，如："#id"）
- `logParams`: 是否记录请求参数
- `logResult`: 是否记录执行结果

### 参数校验注解 `@ValidateParam`

自定义参数校验规则，支持多种校验类型。

**使用示例：**
```java
@ValidateParam(type = "username", message = "用户名格式不正确")
private String username;

@ValidateParam(type = "email", message = "邮箱格式不正确")
private String email;
```

**支持的校验类型：**
- `username`: 用户名校验（字母、数字、下划线、中文，长度3-50位）
- `email`: 邮箱格式校验
- `phone`: 手机号格式校验
- `password`: 密码强度校验

## 安全配置

- JWT Token认证
- Spring Security权限控制
- 密码加密存储
- API接口权限验证
- 基于自定义注解的声明式权限管理

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

### 使用自定义注解

#### 权限验证
```java
@RequirePermission(resourceType = "track", resourceIdParam = "id", operation = "read")
@GetMapping("/{id}")
public ResponseEntity<Track> getTrack(@PathVariable Long id) {
    // 权限验证已自动处理
    return ResponseEntity.ok(trackService.getById(id));
}
```

#### 操作日志
```java
@LogOperation(operation = "创建轨迹", resourceId = "#track.id", logParams = true)
@PostMapping
public ResponseEntity<Track> createTrack(@RequestBody Track track) {
    // 操作日志已自动记录
    return ResponseEntity.ok(trackService.save(track));
}
```

#### 参数校验
```java
@Data
public class RegisterRequest {
    @ValidateParam(type = "username", message = "用户名格式不正确")
    private String username;

    @ValidateParam(type = "password", message = "密码强度不足")
    private String password;
}
```

### 数据库迁移

数据库初始化脚本位于：`src/main/resources/schema.sql`

### 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

## 注解系统优势

### 代码简洁性
- **减少70%** 的重复权限验证代码
- **消除** 手动日志记录代码
- **统一** 参数校验逻辑

### 可维护性
- 权限逻辑集中管理，修改只需改一处
- 日志格式统一，便于分析和监控
- 校验规则可复用，易于扩展

### 开发效率
- 声明式编程，代码意图更清晰
- 自动处理横切关注点，专注业务逻辑
- 减少错误，提高代码质量

### 扩展性
- 易于添加新的注解和切面
- 支持多种资源类型和操作类型
- 可与其他Spring组件无缝集成

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