# Changelog

本项目遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)，格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/)。

> 当前仍处于 0.x 早期阶段，接口与结构可能变动。

## [Unreleased]

### Added
- JDK 17 分支（`jdk17`），与 JDK 8 的 `main` 分支并列
- 开箱即启动：启动类 + H2 内存库（默认）/ MySQL（`mysql` profile）
- 跨模块同步调用示例：`PartnerCreditPort` + `PartnerCreditPortImpl`
- CI 按分支自动选择 JDK（8 / 17）
- `CONTRIBUTING.md`、`CODE_OF_CONDUCT.md`、`CHANGELOG.md`

### Changed
- `docs/架构优势.md` 重构为 `docs/架构说明.md`（单体定位、MVC/DDD 对比、组合边界）

### Security
- MySQL 密码默认值移除，改为 `MYSQL_PASSWORD` 环境变量传入

## [0.1.0] - 2026-10-15

### Added
- DDD Lite 多模块脚手架（framework / shared / modules / partner）
- AI 开发规范方法论、架构优势、AI 开发手册
- 合规扫描脚本 `scan-compliance.py`、pre-commit hook、CI 门禁
- `AGENTS.md` / `.cursor/rules` AI 规则入口
