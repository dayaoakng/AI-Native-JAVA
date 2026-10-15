# DDD Lite 后端脚手架模板

从 BOSS 系统 `boss-partner`（Golden Reference）提炼的**最小可编译** Maven 多模块模板。新项目复制本目录 → 全局替换包名/groupId → 直接开发。

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

Java 8 · Spring Boot 2.7.18 · MyBatis-Plus 3.5.2 · Spock 2.3 + Groovy 3.0 · Lombok

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

```bash
# 全量编译（跳过测试）
mvn clean install -DskipTests

# 单模块测试
mvn test -pl demo-modules/demo-partner -am
```

## 合规自查

```bash
# 在仓库根目录运行（扫描 scaffold/）
python scripts/standards/scan-compliance.py --root scaffold
```

输出 `0 条 fail` 即合规。CI（`.github/workflows/ci.yml`）与 pre-commit hook（`.githooks/`）会自动执行同一扫描。
