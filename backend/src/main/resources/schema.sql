-- 创建数据库
-- CREATE DATABASE track_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    real_name VARCHAR(50),
    phone_type VARCHAR(100), -- 设备型号
    status INTEGER DEFAULT 1, -- 1:正常 0:禁用
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建轨迹表
CREATE TABLE IF NOT EXISTS tracks (
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

-- 创建轨迹点表
CREATE TABLE IF NOT EXISTS track_points (
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

-- 创建设备表
CREATE TABLE IF NOT EXISTS devices (
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

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_tracks_user_id ON tracks(user_id);
CREATE INDEX IF NOT EXISTS idx_track_points_track_id ON track_points(track_id);
CREATE INDEX IF NOT EXISTS idx_devices_user_id ON devices(user_id);
CREATE INDEX IF NOT EXISTS idx_devices_device_id ON devices(device_id);