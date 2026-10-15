-- H2 内存库初始化脚本（MySQL 兼容模式，启动时自动执行）
DROP TABLE IF EXISTS demo_partner;

CREATE TABLE demo_partner (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(128) NOT NULL,
    status      INT          DEFAULT 0,
    create_by   VARCHAR(64),
    create_time TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP,
    del_flag    INT          DEFAULT 0
);
