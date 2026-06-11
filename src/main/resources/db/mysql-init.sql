-- ============================================================
-- MySQL 业务数据库初始化脚本
-- ============================================================
CREATE DATABASE IF NOT EXISTS agent_db
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE agent_db;

-- 强制使用 utf8mb4，避免加载脚本时中文乱码
SET NAMES utf8mb4;

-- 用户表 -----------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  DEFAULT 'user' COMMENT 'user / agent / admin',
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 会话表 -----------------------------------------------------
CREATE TABLE IF NOT EXISTS conversation (
    id              VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
    user_id         BIGINT      NOT NULL,
    status          TINYINT     DEFAULT 0 COMMENT '0进行中 1已结束 2已转人工',
    confidence_low  TINYINT     DEFAULT 0 COMMENT '是否触发低置信',
    deleted         TINYINT     DEFAULT 0,
    create_time     DATETIME    DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 消息表 -----------------------------------------------------
CREATE TABLE IF NOT EXISTS message (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id VARCHAR(36) NOT NULL,
    role            VARCHAR(10) NOT NULL COMMENT 'user / assistant / tool',
    content         TEXT        NOT NULL,
    tool_name       VARCHAR(50) COMMENT '若为工具调用结果则记录工具名',
    create_time     DATETIME    DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conv_id (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 本地消息表（Outbox 模式） -----------------------------------
CREATE TABLE IF NOT EXISTS outbox_message (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id VARCHAR(36) NOT NULL,
    payload         JSON        NOT NULL,
    status          TINYINT     DEFAULT 0 COMMENT '0待发送 1已发送',
    retry_count     INT         DEFAULT 0,
    create_time     DATETIME    DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Outbox 本地消息表';

-- 知识库文档元数据 -------------------------------------------
CREATE TABLE IF NOT EXISTS kb_document (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(200) NOT NULL,
    file_type   VARCHAR(20)  COMMENT 'pdf / markdown / txt',
    file_path   VARCHAR(500),
    chunk_count INT          DEFAULT 0,
    deleted     TINYINT      DEFAULT 0,
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- 订单表 -----------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_order (
    order_id     VARCHAR(40) PRIMARY KEY,
    product_name VARCHAR(200) NOT NULL,
    status       VARCHAR(20)  NOT NULL COMMENT '待付款/待发货/已发货/已签收/退款中/已退款',
    amount       DECIMAL(10,2) NOT NULL,
    address      VARCHAR(300),
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 物流表 -----------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_logistics (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id       VARCHAR(40) NOT NULL UNIQUE,
    carrier        VARCHAR(50),
    tracking_no    VARCHAR(50),
    current_status VARCHAR(50),
    events_json    TEXT COMMENT '物流轨迹 JSON 数组',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流表';

-- 退款记录表 -------------------------------------------------
CREATE TABLE IF NOT EXISTS refund_record (
    refund_id   VARCHAR(40) PRIMARY KEY,
    order_id    VARCHAR(40) NOT NULL,
    reason      VARCHAR(500),
    status      VARCHAR(20) DEFAULT '处理中' COMMENT '处理中/已退款/已驳回',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_refund_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';
