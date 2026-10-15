# BOSS 架构文档与 DDD Lite 脚手架模板

本仓库是从全讯物联 BOSS 系统（`D:\work\V0\boss-system`）提炼出的**架构沉淀**，包含两部分：

1. **架构文档**（`docs/`）：BOSS 系统的现状架构、目标架构模板（方案 A DDD Lite）、模块成熟度清单、演进路线。
2. **后端脚手架模板**（`scaffold/`）：可直接复用的 Maven 多模块 DDD Lite 骨架，新项目可 copy 后改名启动。

> 所有规范结论均来自 BOSS 仓库的权威文档 `docs/standards/AI-RULES.md`（P0）等，已核实到源码（`boss-partner` Golden Reference）。

---

## 文档导航

| 文档 | 内容 |
|------|------|
| [01 现状架构](docs/01-现状架构.md) | BOSS 系统整体分层、模块结构、技术栈、当前真实状态（含文档与代码的差距） |
| [02 目标架构模板](docs/02-目标架构-模板.md) | 方案 A DDD Lite 目标态——分层、目录、命名、充血模型、Handler 模式、硬约束 |
| [03 模块清单与成熟度](docs/03-模块清单与成熟度.md) | 21 个业务模块清单 + L1/L2/L3 成熟度矩阵 + 实测数字 |
| [04 演进路线](docs/04-演进路线.md) | 从贫血模型/Service 到 DDD Lite 的分阶段迁移策略 |
| [scaffold/](scaffold/) | Maven 多模块 DDD Lite 脚手架（含示例 Partner 用例） |

---

## 一句话总结

> BOSS 系统是物联网卡业务的核心运营系统，正从「贫血模型 + Service」向 **DDD Lite（方案 A）** 演进：**聚合根 = `XxxEntity`（充血）、1 用例 = 1 Handler、domain 层无 service、跨模块走 Port/事件**。
