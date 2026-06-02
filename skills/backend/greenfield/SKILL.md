---
name: iidp-backend-greenfield
description: >-
  Use when the user is starting an IIDP backend from scratch, cloning the
  standard demo parent repository, has no local aggregate project yet, or
  asks for first-time local setup, Docker Compose, or an initial Maven build
  before any business App exists; also when phrases like 全新后端项目,
  从零建 IIDP 工程, or 先搭 demo 父工程 appear.
---

# IIDP 后端全新父工程（Greenfield）Skill

## 概述

本 Skill 只负责 **父工程落地、环境与首次可构建/可启动**；子模块、`app.json`、模型、视图、菜单与 `apps.json` 等增量开发 **一律** 转由同级根文件 [`../SKILL.md`](../SKILL.md)从 **Step 1** 起执行，避免与 Step 2～10 重复维护。

## 何时使用

- 工作区尚无目标 IIDP 后端聚合工程，或不确定本地目录是否为规范仓库。
- 需要先完成 **Git 克隆 / 拉取、JDK/Maven、settings、首包、可选 Compose** 再谈业务功能。

## 前置流程（须按顺序）

1. **读规范源**：完整阅读 [`../references/core/pom-structure.md`](../references/core/pom-structure.md) **§1 开发与运行环境**（Git、克隆地址、本地目录检查脚本、JDK/Maven、`settings.xml`、Docker 等）与 **§2 顶层目录完整结构**，以该文件为唯一工程拓扑与命令口径；本节不复制其中的长脚本与安全敏感细节。
2. **执行 Git 与克隆规则**：按 `pom-structure.md` 自检 `git`、核对远端是否为文档所列仓库、处理已存在目录与未提交变更；不可访问时在交付说明中记录事实，不得假定已同步最新代码。
3. **构建命令**：
   - 先执行 `java -version` 检测本地 Java 版本：
     - **JDK 8**：按 `pom-structure.md` §12.1 选择对应平台（macOS / Linux / Windows）的 `JAVA_HOME` 显式指定命令；
     - **JDK 17 / 21，无 JDK 8**：POM 已配置 `source=8 target=8`，可直接运行 `mvn -s ./settings.xml -DskipTests clean package`，但须向用户说明运行时（非编译期）可能存在 Spring Boot 2.x 兼容性风险；
     - **JRE（无 javac）**：报错 `No compiler is provided`，须提示用户安装 JDK 并重试。
   - 必须使用项目提供的 `settings.xml`（`-s ./settings.xml`），路径以 `pom-structure.md` 与宿主仓库为准。
4. **首包目标**：以 **`BUILD SUCCESS`**、无编译错误为准；私服/依赖不可达且与本次克隆无关时，在说明中区分「工程已就位」与「环境阻塞」，不伪造成功。
5. **可选本地运行**：若用户需要 Docker Compose 或启动模块，继续遵循 `pom-structure.md` 中 Dockerfile、`docker-compose.yml`、`application*.properties` 与端口等章节；与引擎、MinIO、MySQL、Redis 服务名一致。
6. **交付前自检**：能执行时，按 [`../references/core/validation-checklist.md`](../references/core/validation-checklist.md) 在可行范围内做静态与构建相关检查。

## 结束条件与下一步

- **结束条件**：规范父工程已在约定路径就位，且（在环境允许下）已完成至少一次与文档一致的打包或等价验证。
- **下一步**：打开并严格遵循 [`../SKILL.md`](../SKILL.md) 的 **Step 1：确认命名规范**，继续 Step 2～10 完成首个或后续业务 App。

## 常见错误（与 `pom-structure.md` 对齐）

| 现象 | 处理方向 |
|---|---|
| 依赖解析失败 | 是否使用了仓库根 `settings.xml`；私服/网络是否可达；与「仅缺业务代码」区分说明 |
| `No compiler is provided in this environment` | 当前 `JAVA_HOME` 指向 JRE；须切换到 JDK，按 `pom-structure.md` §12.1 选平台命令 |
| 本地只有 JDK 17 / 21 | 直接运行 `mvn -s ./settings.xml -DskipTests clean package` 可通过编译（POM 已设 source=8 target=8）；运行时需注意 Spring Boot 2.x 兼容性 |
| Compose 起不来 | 对照 `pom-structure.md` 检查服务名、端口、挂载 `apps/` 与 `apps.json` 是否一致 |
| 继续写业务却未克隆父工程 | 先回到本节步骤 1～3，再进入根 `SKILL.md` Step 1 |

## 宿主路径说明

本 Skill 与引用文件均相对于 `skills/backend/greenfield/`。若团队将 Skill 同步到其他目录，须保持 `greenfield` 与 `references/`、`SKILL.md` 的相对关系一致，或等价调整链接。
