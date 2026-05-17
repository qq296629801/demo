# IIDP 后端测试参考

IIDP 服务测试通过 `@ExtendWith(SieEngineTestExtension.class)` 启动引擎上下文，
再用 `BaseContextHandler.getMeta()` 获取 Meta，直接调用模型服务。

> H2 内存数据库：IIDP 引擎**不支持** H2 方言，不可用。

---

## 基本结构

```java
package com.sie.iidp.{appPkg}.{moduleName};

import com.sie.snest.engine.container.Meta;
import com.sie.snest.engine.context.BaseContextHandler;
import com.sie.snest.engine.rule.Filter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import server.annotation.SieEngineTestExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SieEngineTestExtension.class)
class {ModelName}ServiceTest {

    @Test
    void {testMethodName}() {
        Meta meta = BaseContextHandler.getMeta();
        assertNotNull(meta);

        Object result = meta.get("{model_name}").call("{serviceName}", /* args... */);
        assertNotNull(result);
    }
}
```

关键点：
- `@ExtendWith(SieEngineTestExtension.class)` 放在**类级别**，测试前启动引擎
- `BaseContextHandler.getMeta()` 获取当前请求级 Meta 上下文
- `meta.get("{model_name}")` 获取目标模型的 RecordSet
- `.call("{serviceName}", args...)` 调用 `@MethodService(name="...")`

---

## 状态机服务测试（最常用）

```java
@ExtendWith(SieEngineTestExtension.class)
class {ModelName}ServiceTest {

    @Test
    void {serviceName}_success_fromStatusToStatus() {
        Meta meta = BaseContextHandler.getMeta();

        // 前置：确认记录存在且状态为 {FROM_STATUS}
        Object record = meta.get("{model_name}").call("find",
            Filter.equal("id", testId), null, null, null, null);
        assertNotNull(record);

        // 执行状态变更服务
        Object result = meta.get("{model_name}").call("{serviceName}", testId);
        assertNotNull(result);

        // 验证后置状态
        List<?> updated = (List<?>) meta.get("{model_name}").call("find",
            Filter.equal("id", testId), null, null, null, null);
        assertEquals("{TO_STATUS}", ((Map<?, ?>) ((List<?>) updated).get(0)).get("status"));
    }

    @Test
    void {serviceName}_reject_illegalStatus() {
        Meta meta = BaseContextHandler.getMeta();
        // 前置：记录状态为非合法前置状态
        assertThrows(Exception.class, () ->
            meta.get("{model_name}").call("{serviceName}", illegalStatusId)
        );
    }
}
```

---

## 指定租户上下文

```java
@Test
void testWithTenantContext() {
    Meta outerMeta = BaseContextHandler.getMeta();
    assertNotNull(outerMeta);

    try (Meta meta = new Meta("{tenantId}", outerMeta.getContext())) {
        Object result = meta.get("{model_name}").search(
            new Filter(), null, null, null, null);
        assertNotNull(result);
    } catch (Exception e) {
        fail("测试失败：" + e.getMessage());
    }
}
```

---

## 异步服务测试

```java
@Test
void testAsync() {
    Meta meta = BaseContextHandler.getMeta();
    assertNotNull(meta);

    Object result = meta.get("{model_name}").executeAsync(
        () -> Meta.getCurrentMeta().get("{model_name}"),
        "search"
    );
    assertNotNull(result);
}
```

---

## 注意事项

- `@ExtendWith(SieEngineTestExtension.class)` 需要**引擎可启动**（数据库、配置就绪）
- 无法启动引擎时注释掉 `@Test`，保留方法体作为手动验证脚本
- 测试方法名格式：`{serviceName}_{场景}_{预期结果}`，如 `release_success_draftToReleased`
- 测试文件路径：`src/test/java/com/sie/iidp/{appPkg}/{moduleName}/{ModelName}ServiceTest.java`
- 内置服务直接用 RecordSet 对应方法；自定义服务用 `.call("{serviceName}", args...)`

---

## TC-BE 覆盖优先级

每个 `@MethodService` 至少覆盖以下场景：

| 场景 | 测试方法命名示例 |
|---|---|
| 正常流程 | `release_success_draftToReleased` |
| 状态拒绝（非法前置状态） | `release_reject_nonDraftStatus` |
| 必填参数缺失 | `release_reject_missingId` |
| 副作用字段验证 | `release_sideEffect_actualStartTimeSet` |
