-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(255) NOT NULL COMMENT '密码（BCrypt 加密）',
    nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除标识（0-正常，1-已删除）',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';