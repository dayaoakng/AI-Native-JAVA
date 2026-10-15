# AGENTS.md — DDD Lite 规则（随脚手架一起复制）

新项目复制本目录后自带这份规则，AI 编程助手按此执行。

## 硬规则

| # | 规则 |
|---|------|
| 1 | 禁止新增 `service/` 包、`XxxService`、`XxxApplicationService` |
| 2 | 1 用例 = 1 Handler，Handler ≤ 100 行 |
| 3 | 单文件 ≤ 300 行；方法 ≤ 30 行 |
| 4 | 领域实体放 `domain/entity/`；domain 层仅 entity/valueobject/repository/event/port |
| 5 | 源码 UTF-8 无 BOM |

## 代码放哪（查表）

- 领域规则 → `domain/entity/`、`domain/valueobject/`
- 用例编排 → `application/handler/`（1 用例 1 Handler）
- 事件监听 → `application/listener/`
- 持久化抽象 → 接口 `domain/repository/`；实现 `infrastructure/`
- 跨模块/外部能力 → 契约 `domain/port/` ＋ 实现 `infrastructure/`
- 跨模块通知 → `domain/event/`（发布）＋ 对方 `@EventListener`（消费）

## 自检与测试

```bash
mvn -q test                                  # 必须全绿
python ../scripts/standards/scan-compliance.py --root .   # 必须 0 fail（仓库根自带脚本）
```
