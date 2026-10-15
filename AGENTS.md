# AGENTS.md — AI 协作规则（DDD Lite）

本文件是给 AI 编程助手（Claude Code / Cursor / 其他 agent）的规则入口。规则以「可执行」优先，不写大道理。

## 项目是什么

一套让 AI 稳定产出可维护 Java 后端的方法论与脚手架模板。核心产物在 `scaffold/`（DDD Lite 多模块骨架）。

## 硬规则（写代码前逐项自查，违反即不合格）

| # | 规则 |
|---|------|
| 1 | 禁止新增 `service/` 包、`XxxService`、`XxxApplicationService` |
| 2 | 1 用例 = 1 Handler，Handler ≤ 100 行 |
| 3 | 单文件 ≤ 300 行；方法 ≤ 30 行 |
| 4 | 领域实体放 `domain/entity/`；domain 层仅 entity/valueobject/repository/event/port |
| 5 | 源码 UTF-8 无 BOM |

## 决策树（代码放哪，直接查表，不要猜）

- 领域规则（状态校验/变更、业务计算、不变量）→ `domain/entity/`、`domain/valueobject/`
- 用例编排（加载实体、调仓储、跨实体协作、事务、发事件）→ `application/handler/`（1 用例 1 Handler）
- 事件监听（消费领域事件，事件驱动的用例入口）→ `application/listener/`
- 持久化抽象 → 接口 `domain/repository/`；实现 `infrastructure/`
- 跨模块/外部系统能力 → 契约 `domain/port/` ＋ 实现 `infrastructure/`
- 跨模块通知 → `domain/event/`（发布）＋ 对方 `@EventListener`（消费）
- 对象落位：有身份+状态+行为 → entity；不可变业务概念 → valueobject；HTTP/消息契约 → `interfaces/request|response`

## 自检

写完后运行合规扫描（本仓库自带）：

```bash
python scripts/standards/scan-compliance.py --root scaffold
```

输出必须 0 条 fail。有任何 fail 先修再交。

## 测试

改动涉及 `scaffold/` 时，必须跑：

```bash
cd scaffold && mvn -q test
```

全部通过才算完成。
