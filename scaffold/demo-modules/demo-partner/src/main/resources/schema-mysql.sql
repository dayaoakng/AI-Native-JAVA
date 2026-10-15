-- MySQL 建表脚本（mysql profile，幂等：IF NOT EXISTS，不 DROP，保护已有数据）
CREATE TABLE IF NOT EXISTS demo_partner (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(128) NOT NULL,
    status      INT          DEFAULT 0,
    create_by   VARCHAR(64),
    create_time TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP,
    del_flag    INT          DEFAULT 0,
    UNIQUE KEY uk_partner_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
