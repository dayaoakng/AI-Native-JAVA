# 贡献指南

感谢关注本项目。这是一个方法论 + 脚手架模板仓库，贡献方式主要有两类。

## 贡献方式

### 1. 改进规则与文档

- 硬规则、决策树、架构说明的表述是否清晰、是否有遗漏 → 欢迎提 Issue / PR
- 文档错别字、链接失效、表述歧义 → 直接 PR

### 2. 改进脚手架与工具

- `scaffold/` 代码问题、新示例模块
- `scripts/standards/scan-compliance.py` 规则漏报 / 误报

## 提 Issue

请包含：

- 环境：JDK 版本（8 / 17）、Maven 版本、操作系统
- 复现步骤（能贴命令最好）
- 期望行为 vs 实际行为

## 提 PR

1. Fork 本仓库
2. 建分支（`feat/xxx`、`fix/xxx`、`docs/xxx`）
3. 改动 `scaffold/` 时，本地必须通过：

   ```bash
   cd scaffold && mvn test
   python scripts/standards/scan-compliance.py --root scaffold
   ```

4. 提交信息遵循 [Conventional Commits](https://www.conventionalcommits.org/zh-hans/)（`feat:` / `fix:` / `docs:` / `ci:` / `refactor:` 等）

## 行为准则

- 尊重他人，对事不对人
- 就技术问题讨论，不进行人身攻击
- 维护者有权关闭偏离主题或违反准则的 Issue / PR

## 许可

提交 PR 即表示你同意你的贡献以本仓库的 [MIT License](LICENSE) 发布。
