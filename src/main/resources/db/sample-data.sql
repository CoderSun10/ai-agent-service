-- ============================================================
-- 示例数据（用于演示 / 联调）
-- 密码为明文 "123456"（演示用，生产请使用 BCrypt）
-- ============================================================
USE agent_db;

SET NAMES utf8mb4;

INSERT INTO sys_user (username, password, role) VALUES
    ('demo',  '123456', 'user'),
    ('alice', '123456', 'user'),
    ('agent01', '123456', 'agent'),
    ('admin', '123456', 'admin')
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- 知识库文档不在此插入：由 KnowledgeSeeder 在应用启动时解析 resources/docs
-- 下的真实文档、向量化后写入向量库（真实内容，可直接 RAG 检索）。

-- 订单示例数据
INSERT INTO biz_order (order_id, product_name, status, amount, address) VALUES
    ('O20260601001', '机械键盘 Cherry 红轴', '已发货', 399.00, '北京市海淀区中关村大街1号'),
    ('O20260601002', '27 寸 4K 显示器',      '待发货', 1599.00, '上海市浦东新区世纪大道100号'),
    ('O20260530009', '无线鼠标',             '已签收', 129.00, '广州市天河区天河路200号')
ON DUPLICATE KEY UPDATE product_name = VALUES(product_name);

-- 物流示例数据
INSERT INTO biz_logistics (order_id, carrier, tracking_no, current_status, events_json) VALUES
    ('O20260601001', '顺丰速运', 'SF1234567890', '运输中',
     '["2026-06-01 11:00 商家已发货","2026-06-01 18:30 【北京分拨中心】已收入","2026-06-02 08:15 已到达【北京海淀营业点】，派送中"]'),
    ('O20260530009', '中通快递', 'ZT9876543210', '已签收',
     '["2026-05-30 12:00 商家已发货","2026-05-31 20:00 已到达【广州天河营业点】","2026-06-01 09:30 已签收，签收人：本人"]')
ON DUPLICATE KEY UPDATE carrier = VALUES(carrier);
