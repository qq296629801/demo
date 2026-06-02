# AI 辅助大型项目开发最佳实践
 
> 本文整合 Cursor 与 Claude Code 在大型项目中的核心最佳实践，涵盖项目配置、上下文管理、工作流规范和团队协作四大维度。
 
---
 
## 概览：两大工具对比
 
| 维度 | Cursor | Claude Code |
|---|---|---|
| 项目配置 | .cursorignore + Rules | CLAUDE.md（自动读取） |
| 上下文理解 | 本地向量化索引 | 按需读取相关文件 |
| 规范定义 | .cursor/rules/*.mdc | CLAUDE.md 多级目录 |
| 命令复用 | 无内置 | Slash Commands |
| 规划模式 | 手动告知 | Plan Mode (Shift+Tab×2) |
| 适合场景 | 日常编辑、多文件联动 | 复杂任务、自动化工作流 |
 
---
 
## 第一部分：Cursor 大型项目最佳实践
 
### 1. 配置 .cursorignore（第一步必做）
 
排除无关文件，避免 Cursor 把构建产物、依赖包等加入索引，既提升性能，又减少上下文噪音。
 
> 🔴 **必做**：项目初始化后第一件事就是创建 .cursorignore，否则索引会包含大量无关文件。
 
```bash
# .cursorignore
node_modules/
dist/  build/  target/
*.log  *.lock  *.class
coverage/  .git/
```
 
---
 
### 2. 写好 Cursor Rules（项目规范核心）
 
Rules 存放在 `.cursor/rules/` 目录下，以 `.mdc` 格式保存，是让 AI 理解项目规范的关键文件。
 
- 每个规则文件建议控制在 500 行以内
- 按模块拆分：`java.mdc` / `lock.mdc` / `api.mdc`
- 包含：技术栈版本、命名规范、禁止事项、代码示例
 
```
.cursor/rules/
├── java.mdc        # Java 编码规范
├── api.mdc         # 接口规范
└── database.mdc    # 数据库规范
```
 
 
### 3. 维护 AI 专用文档目录
 
类似给新员工的 onboarding 文档，让 AI 快速理解项目背景，AI 跑偏时也便于重新对齐。
 
```
docs/
├── architecture.md     # 整体架构说明
├── lock-guide.md       # 分布式锁使用规范
├── api-conventions.md  # 接口规范
└── database.md         # 数据库设计说明
```
 
---
 
### 4. 控制对话窗口长度
 
> ⚠️ **关键原则**：一个 Composer 窗口对话越长，AI 越容易遗忘前面的指令。完成一个独立功能后，开新窗口。
 
| | 推荐做法 | 避免做法 |
|---|---|---|
| 窗口管理 | 每个功能点开新窗口 | 一个窗口聊几十轮 |
| 任务拆分 | 复杂任务分步骤执行 | 一次让 AI 做所有事 |
| 上下文 | 开窗口前先贴相关代码 | 让 AI 自己猜代码在哪 |
 
---
 
### 5. 选对模型
 
- **复杂任务**：Claude Sonnet 4 / Gemini 2.5 Pro（上下文理解更强）
- **简单补全**：轻量模型，节省额度
- **代码审查**：优先选推理能力强的模型
 
---
 
### 6. 让 AI 编写并运行测试
 
AI 写完代码后，同步让它写测试并执行，通过测试结果自动发现和修复 Bug，比单纯生成代码效果更好。
 
```
流程：提需求 → AI 写代码 → AI 写测试 → AI 执行测试 → AI 修复失败 → 你 Review
```
 
---
 
### 7. Git 频繁提交
 
- 完成每个小功能后立即 commit，AI 跑偏时 `git stash` 一键回滚
- 提 PR 前必须通过 lint + build + test
 
---
 
## 第二部分：Claude Code 大型项目最佳实践
 
### 1. CLAUDE.md —— 最重要的配置文件
 
CLAUDE.md 是 Claude Code 自动读取的特殊文件，相当于项目的「永久大脑」，每次启动 Session 都会加载。
 
> ✅ **核心价值**：写一次，永久生效。所有 Session 都会遵守，不用每次重复告诉 AI 项目规范。
 
推荐包含的内容：
- 常用命令（构建、测试、代码检查）
- 技术栈版本（Java 17、Spring Boot 3.x 等）
- 代码规范（工具类使用、异常处理、命名约定）
- 项目结构（每层职责说明）
- 测试要求（覆盖率、禁止注释掉失败测试）
 
示例：
 
```markdown
# CLAUDE.md
 
## 常用命令
- 构建：mvn clean package -DskipTests
- 测试：mvn test
- 代码检查：mvn checkstyle:check
 
## 代码规范
- Java 17，Spring Boot 3.x
- 分布式锁统一用 RedissonUtil.executeWithLock
- 异常统一抛 BizException
- key 格式：{业务域}:{资源类型}:{resourceId}
 
## 项目结构
- controller/  只做参数校验和返回值封装
- service/     核心业务逻辑
- mapper/      数据库操作，禁止写业务逻辑
 
## 测试要求
- 新功能必须附带单元测试
- 禁止注释掉失败的测试，必须修复
```
 
多级 CLAUDE.md 结构：
 
```
项目根目录/CLAUDE.md          # 全局规范（所有 Session 生效）
backend/CLAUDE.md             # 后端专属规范
frontend/CLAUDE.md            # 前端专属规范
backend/service/CLAUDE.md     # 更细粒度的规范
```
 
> 💡 子目录的 CLAUDE.md 会覆盖父目录的同名配置，Claude 优先使用最具体的规范。
 
---
 
### 2. 上下文窗口管理（核心瓶颈）
 
上下文窗口是最重要的资源，存储了整个对话、读取的文件和命令输出。随着上下文填满，Claude 性能会明显下降。
 
| ✅ 推荐做法 | ❌ 避免做法 |
|---|---|
| 每个功能开新 Session | 一个 Session 处理所有功能 |
| 复杂任务拆成小步骤 | 一次提交几百行需求 |
| 用 /clear 定期清空上下文 | 让上下文无限增长 |
| 只引入必要的文件 | 把整个项目都贴进来 |
 
---
 
### 3. 先规划再执行（Plan Mode）
 
对于较大的功能，先让 Claude 进入规划模式分析需求，规划确认后再开新 Session 执行，避免方向跑偏。
 
> 🔧 **操作方式**：按两次 `Shift+Tab` 进入 Plan Mode，Claude 只分析不修改文件，直到你批准方案。
 
```
Step 1: Plan Mode 规划 → Claude 提问边界情况、技术方案
Step 2: 确认方案
Step 3: 开新 Session → 携带规划结果执行
Step 4: 运行测试验证
```
 
---
 
### 4. 自定义 Slash Commands
 
在 `.claude/commands/` 目录下创建 Markdown 文件，封装常用操作为可复用命令，避免每次重复描述需求。
 
```
.claude/commands/
├── new-controller.md   # /new-controller OrderController
├── new-service.md      # /new-service OrderService
├── add-lock.md         # /add-lock 给方法加分布式锁
└── review.md           # /review 代码 Review
```
 
示例命令内容（`new-controller.md`）：
 
```markdown
创建 Controller 类，名称为 $ARGUMENTS，要求：
- 继承 BaseController
- 参数使用 @Valid 注解校验
- 返回值统一用 Result<T> 包装
```
 
---
 
### 5. 模块边界清晰
 
清晰的架构边界是高效 AI 协作的前提。当模块职责明确时，Claude 可以在单个模块内自信地工作，无需理解整个系统。
 
| 层级 | 职责 | 禁止事项 |
|---|---|---|
| Controller | 参数校验 + 返回值封装 | 包含业务逻辑 |
| Service | 核心业务逻辑 | 直接写 SQL |
| Mapper | 数据库操作 | 包含业务逻辑 |
| Util | 无状态工具方法 | 注入 Spring Bean |
 
---
 
### 6. 让 Claude 自我 Review
 
另起新 Session，让 Claude 以资深工程师视角审查刚生成的代码，效果出奇地好。
 
> 💡 **推荐 Prompt**：「以资深工程师视角 review 这段代码，重点关注：并发安全、异常处理、性能问题、是否符合项目规范」
 
---
 
## 第三部分：通用最佳实践
 
### 1. 上下文补充策略
 
无论使用哪个工具，提供给 AI 的上下文质量直接决定输出质量。
 
- 提需求时主动附上相关文件路径，不让 AI 猜
- 涉及工具类，直接贴工具类代码
- 说明已有约束（用了哪个锁、哪种异常），避免 AI 重新发明轮子
- 提供反例：「不要用 `lock()`，要用 `tryLock()`」
 
---
 
### 2. Git 工作流
 
- 每完成一个小功能立即 commit，粒度越小越安全
- AI 跑偏时：`git stash --include-untracked` 一键还原
- 提 PR 前必须通过完整的 lint + build + test
 
---
 
### 3. 测试门禁
 
```bash
# 提交前必须全部通过
mvn compile checkstyle:check test -q
```
 
> ⚠️ **原则**：禁止注释掉失败的测试来让构建通过，必须修复根本原因。
 
---
 
## 第四部分：优先级清单
 
### Cursor
 
| 优先级 | 事项 | 说明 |
|---|---|---|
| 🔴 必做 | 配置 .cursorignore | 排除 node_modules、target 等无关目录 |
| 🔴 必做 | 编写 Cursor Rules | 将项目规范写入 .cursor/rules/*.mdc |
| 🟡 推荐 | 维护 docs/ 文档目录 | 类似 onboarding 文档，AI 跑偏时重新对齐 |
| 🟡 推荐 | 编写测试并验证 | 让 AI 生成代码的同时生成测试 |
| 🟡 推荐 | 每个功能开新窗口 | 保持上下文干净，减少遗忘 |
| 🟢 习惯 | Git 频繁提交 | 每个小功能完成即 commit |
 
### Claude Code
 
| 优先级 | 事项 | 说明 |
|---|---|---|
| 🔴 必做 | 写好 CLAUDE.md | 项目规范、常用命令全部写进去 |
| 🔴 必做 | 控制 Session 长度 | 功能完成后开新 Session |
| 🔴 必做 | 先规划再执行 | 用 Plan Mode 确认方案再动手 |
| 🟡 推荐 | 自定义 Slash Commands | 封装常用操作，一键复用 |
| 🟡 推荐 | 让 Claude 自我 Review | 新 Session 让 AI 审查自己写的代码 |
| 🟡 推荐 | 子目录配置 CLAUDE.md | 按模块细化规范 |
| 🟢 习惯 | Git 频繁提交 + 测试门禁 | 所有测试通过再提 PR |
 
---
 
## 总结
 
**核心思路：把项目规范「固化」到配置文件里，而不是每次靠 Prompt 描述。**
 
- **Cursor**：`.cursorignore` + Rules 文件
- **Claude Code**：`CLAUDE.md` + Slash Commands
- **通用**：短 Session + 频繁 Git commit + 测试门禁