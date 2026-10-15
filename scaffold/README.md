# DDD Lite 后端脚手架模板

从 BOSS 系统 `boss-partner`（Golden Reference）提炼的**最小可运行** Maven 多模块模板。内置 H2 内存库，`mvn spring-boot:run` 一键启动。新项目复制本目录 → 全局替换包名/groupId → 直接开发。

## 结构

```
scaffold/
├── pom.xml                         # 根聚合 pom（统一版本管理）
├── demo-framework/                 # 框架层：Result / ResultCode / BusinessException / 全局异常
├── demo-shared/                    # 共享层：BaseEntity（审计字段）
└── demo-modules/
    ├── pom.xml                     # 业务模块聚合
    └── demo-partner/               # 示例模块：Partner 完整用例
        ├── interfaces/             # controller / request / response
        ├── application/            # handler / assembler / listener（事件监听）
        ├── domain/                 # entity / valueobject / repository / event
        ├── infrastructure/         # mapper / persistence/repository
        └── src/test/groovy/        # Spock 单元测试
```

> 本目录自带 `AGENTS.md`（DDD Lite 规则），AI 编程助手复制后自动生效。
> 跨模块协作示例：`ActivatePartnerHandler` 发布 `PartnerStatusChangedEvent`，
> `PartnerStatusListener` 用 `@EventListener` 消费（支柱 4：跨模块走事件）。

## 技术栈（与 BOSS 对齐）

Java 8 · Spring Boot 2.7.18 · MyBatis-Plus 3.5.2 · H2（默认，开箱启动）/ MySQL 8（profile）· Spock 2.3 + Groovy 3.0 · Lombok

> **本分支（`main`）用 JDK 8**。需要 JDK 17 请看 `jdk17` 分支。

## 硬约束（写代码前逐项自查）

| # | 规则 |
|---|------|
| 1 | 禁止新增 `service/`、`XxxService`、`XxxApplicationService` |
| 2 | 1 用例 = 1 Handler，≤ 100 行 |
| 3 | Entity 充血（≥3 业务方法），放 `domain/entity/`，≤ 300 行 |
| 4 | domain 层仅 entity/valueobject/repository/event（+port），**无 service/** |
| 5 | 跨模块走 Port 或领域事件 |
| 6 | 单文件 ≤ 300 行；方法 ≤ 30 行；UTF-8 无 BOM |

## 新建一个业务模块的步骤

1. 复制 `demo-modules/demo-partner/` → 改名（如 `demo-order/`）
2. 全局替换 `com.demo.partner` → `com.demo.order`、`Partner` → `Order`
3. 在 `demo-modules/pom.xml` 注册 `<module>`
4. 在 `demo-partner/pom.xml` 同级补充 Mapper 接口与 XML
5. 为每个用例写 `XxxHandler` + 对应 `XxxHandlerSpec.groovy`

## 运行

> 前置：JDK 8 + Maven 3.6+。无需安装任何数据库（H2 内存库，MySQL 兼容模式）。

```bash
# 1. 安装到本地仓库（首次必须，让 spring-boot:run 能解析 demo-framework/demo-shared）
mvn clean install -DskipTests

# 2. 启动服务（默认 8080；示例模块 demo-partner 是唯一的可启动模块）
cd demo-modules/demo-partner
mvn spring-boot:run

# 3. 验证（另开终端）
curl -X POST http://localhost:8080/partners \
  -H "Content-Type: application/json" \
  -d '{"code":"P001","name":"测试伙伴"}'
# → {"code":"200","data":{"id":1,"code":"P001","status":0,"statusDesc":"未激活"},...}

curl -X POST http://localhost:8080/partners/1/activate
# → status 变为 1（已激活）

# 单模块测试
mvn test -pl demo-modules/demo-partner -am
```

> 端口被占用：`mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8090`（或改 `application.yml` 的 `server.port`）。
> H2 控制台：启动后访问 `http://localhost:8080/h2-console`（JDBC URL 见 `application.yml`）。

## 连接真实 MySQL（mysql profile）

默认跑 H2 内存库。要连你本机的 MySQL，切 `mysql` profile 即可，**无需改任何代码**：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

- 连接参数在 `application-mysql.yml`，已用环境变量占位（有默认值）：
  - `MYSQL_HOST`（默认 localhost）· `MYSQL_PORT`（默认 3306）
  - `MYSQL_DATABASE`（默认 ddd_lite，**不存在会自动创建**）
  - `MYSQL_USER`（默认 root）· `MYSQL_PASSWORD`（默认 123456）
- 建表走 `schema-mysql.sql`：`CREATE TABLE IF NOT EXISTS`，**绝不 DROP**，每次启动幂等、不清空业务数据。
- 覆盖任意项：`-DMYSQL_PASSWORD=xxx` 或设置环境变量，**不要把真实密码写进仓库**（模板默认值仅演示用）。

## 合规自查

```bash
# 在仓库根目录运行（扫描 scaffold/）
python scripts/standards/scan-compliance.py --root scaffold
```

输出 `0 条 fail` 即合规。CI（`.github/workflows/ci.yml`）与 pre-commit hook（`.githooks/`）会自动执行同一扫描。
