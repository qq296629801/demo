# IIDP 实现、验证与复盘

## 测试用例规格（从 AC 派生）

### 触发时机

Step 1.5a（backend-spec.md 生成后）立即执行 AC 提取，将 `requirements.md` 验收标准转化为结构化测试用例，写入当前 feature 的 `validation.md` 测试用例规格节。tasks.md 中的测试任务块通过 TC-ID 与此节对应。

### AC → TC 提取 Prompt
