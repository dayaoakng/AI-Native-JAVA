# AI 开发规范 × DDD Lite 架构方法论

一套让 AI 能稳定写出可维护、可交接、可落地的企业级 Java 后端的方法论。

<p align="center">
  <a href="#"><img src="https://img.shields.io/badge/Java-1.8-007396?logo=java&logoColor=white" alt="Java 8"></a>
  <a href="#"><img src="https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 2.7.18"></a>
  <a href="#"><img src="https://img.shields.io/badge/MyBatis--Plus-3.5.2-blue" alt="MyBatis-Plus"></a>
  <a href="#"><img src="https://img.shields.io/badge/测试-Spock%202.3-brightgreen" alt="Spock"></a>
  <a href="#"><img src="https://img.shields.io/badge/状态-生产验证-success" alt="生产验证"></a>
  <a href="#"><img src="https://img.shields.io/badge/门禁-pre--commit%20%2B%20CI-green" alt="门禁"></a>
  <a href="#"><img src="https://img.shields.io/badge/规则-AI%20可执行-blueviolet" alt="AI 可执行"></a>
</p>

---

## 目录

- [一句话看懂](#一句话看懂)
- [架构总览](#架构总览)
- [方法论：三件套](#方法论三件套)
- [三个关键数字](#三个关键数字)
- [文档导航](#文档导航)

---

## 一句话看懂

> 传统开发：人写代码，AI 打辅助。  
> 我们的做法：**AI 按规范写代码，人做审查与决策**——因为规则足够明确，AI 的输出才能被信任。

## 架构总览

我们采用 **DDD Lite**：保留 DDD 的核心价值（充血模型、分层、领域边界），砍掉让 AI 和人都犯迷糊的抽象（聚合根直接用 `Entity`，不造 `Aggregate`）。

```mermaid
flowchart TB
    subgraph I["interfaces · 接口层"]
        direction LR
        CTRL["Controller"] --> RQ["Request / Response / VO"]
    end

    subgraph A["application · 应用层"]
        direction LR
        H["Handler（1 用例 = 1 类，≤100 行）"] --> AS["Assembler / Support"]
    end

    subgraph D["domain · 领域层（纯业务，无框架）"]
        direction LR
        E["Entity（充血）"] & V["ValueObject"]
        REPO["Repository 接口"] & EV["Event / Port"]
    end

    subgraph INF["infrastructure · 基础设施层"]
        direction LR
        MP["Mapper / PO"] & RI["RepositoryImpl"]
    end

    I -->|"调用"| A
    A -->|"编排"| D
    D -.->|"实现接口"| INF

    style D fill:#e8f4ff,stroke:#2f80ed
    style I fill:#f6f8fa,stroke:#57606a
    style A fill:#f6f8fa,stroke:#57606a
    style INF fill:#f6f8fa,stroke:#57606a
```

**依赖方向单一**：外层依赖内层，内层不依赖外层；domain 层不碰 Spring/MyBatis/Redis，可移植、可独立测试。

## 方法论：三件套

让 AI 输出可控，靠的不是「讲道理」，而是三件可执行的东西：

```mermaid
flowchart LR
    R1["① 硬规则<br/>违反即拒绝生成"] --> OUT["AI 输出稳定、<br/>可审查、可交接"]
    R2["② 决策树<br/>AI 直接查表，不猜"] --> OUT
    R3["③ 自动检查<br/>commit 门禁拦截违规"] --> OUT

    style OUT fill:#dcffe4,stroke:#1a7f37
```

| 三件套 | 例子 |
|--------|------|
| **① 硬规则** | 禁止 `service/`；文件 ≤ 300 行、Handler ≤ 100 行、方法 ≤ 30 行；实体必须进 `domain/entity/` |
| **② 决策树** | 「这段代码是领域规则？→ 写 Entity；是用例编排？→ 写 Handler；是持久化？→ 写 infrastructure」 |
| **③ 自动检查** | 合规扫描 `scripts/standards/scan-compliance.py` 一键出报告；pre-commit hook（`.githooks/`）+ GitHub Action（`.github/workflows/ci.yml`）双门禁拦截违规 |

## 三个关键数字（来自生产系统实测）

| 指标 | 数值 | 说明 |
|------|------|------|
| 业务模块 Handler | **462 个** | 1 用例 = 1 Handler，全程可追踪 |
| 假 Handler / 遗留 Service / DomainService / Aggregate | **全部为 0** | 反模式被规则彻底挡住 |
| Controller 直调 Handler（口径 A） | **近 100%** | 没有「假分层」，每一层都真的在工作 |

## 文档导航

| 文档 | 内容 | 读者 |
|------|------|------|
| [AI 开发手册](docs/AI开发手册.md) | **通用开发规范实操版**：分层、命名、职责边界、质量门禁、事件驱动、审查清单 | 开发者 / AI 助手 |
| [AI 开发规范方法论](docs/AI开发规范方法论.md) | 为什么 AI 写不好代码、我们的解法是什么 | 决策者 / 推广对象 |
| [架构优势](docs/架构优势.md) | DDD Lite 架构的设计与真实收益 | 决策者 / 推广对象 |
| [scaffold/](scaffold/) | 可直接运行的最小骨架（`mvn spring-boot:run` 一键启动，7 个 Spock 测试通过，含事件示例） | 开发者 |
| [AGENTS.md](AGENTS.md) | 喂给 AI 编程助手的规则入口（Claude Code / Cursor 通用） | AI 助手 |

> **文档关系**：[AI 开发手册](docs/AI开发手册.md) 是「怎么做」（实操规范），[AI 开发规范方法论](docs/AI开发规范方法论.md) 是「为什么」（价值与方法论），[架构优势](docs/架构优势.md) 是「结果」（真实成效）。

---

**License**：[MIT](LICENSE) · 一份被生产系统验证过的 AI 开发范式。
