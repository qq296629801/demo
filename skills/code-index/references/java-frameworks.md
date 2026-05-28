# Java 快速开发框架识别与提取规则

## 框架识别矩阵

运行以下 codegraph_search 序列，根据命中情况判断框架：

| 搜索词 | 命中 → 判断 |
|-------|-----------|
| `JeecgBootApplication` | JEECG Boot |
| `YudaoApplication` 或 `yudao` | yudao-cloud |
| `MakuApplication` 或 `maku` | maku-boot |
| `BaseController` + `BaseServiceImpl` | RuoYi 系 |
| `com.ruoyi` 包路径 | RuoYi（原版或魔改） |
| `IService<T>` + `ServiceImpl<M,T>` | MyBatis-Plus（所有框架） |
| `BaseMapper<T>` | MyBatis-Plus Mapper |
| `@AutoLog` | JEECG Boot 特有注解 |
| `@SaCheckPermission` | Sa-Token（maku-boot / yudao） |
| `SecurityUtils.getUserId()` | Spring Security（RuoYi / JEECG） |

---

## RuoYi 系框架（ruoyi / ruoyi-vue / ruoyi-cloud）

### 代码分层特征

```
com.ruoyi
├── common/         工具类、注解、常量
│   ├── annotation  @Log、@RepeatSubmit、@DataScope
│   ├── core        BaseController、BaseEntity
│   └── utils       SecurityUtils、StringUtils
├── framework/      Spring Security 配置、JWT
├── system/         系统管理模块（用户、角色、菜单、部门）
│   ├── controller  SysUserController、SysRoleController
│   ├── service     ISysUserService + SysUserServiceImpl
│   ├── mapper      SysUserMapper + SysUserMapper.xml
│   └── domain      SysUser、SysRole、SysMenu
└── {module}/       业务模块（同结构）
```

### codegraph 提取规则

```
1. 搜索所有继承 BaseController 的类 → API Controller 列表
2. 搜索 @Log(title=...) → 操作日志覆盖的功能点
3. 搜索 @DataScope → 数据权限控制点
4. codegraph_trace SysUserController.list() → 用户列表完整链路
5. 搜索 extends BaseEntity 的类 → 业务数据模型
6. 搜索 *Mapper.xml 文件 → SQL 查询逻辑
```

### 典型 Spec 映射

| 代码元素 | Spec 文档位置 |
|---------|-------------|
| `@Log(title="用户管理")` | PRD 功能模块名称 |
| `@PreAuthorize("@ss.hasPermi('...')")` | API 文档权限说明 |
| `BaseEntity`（createBy/updateBy/createTime）| 数据库公共字段 |
| `SysMenu` 菜单树 | 用户故事导航结构 |

---

## JEECG Boot（jeecg-boot / JeecgBoot）

### 代码分层特征

```
org.jeecg
├── common/
│   ├── api         Result<T> 统一返回
│   ├── aspect      @AutoLog 切面
│   └── system      LoginUser、JwtUtil
├── modules/
│   └── {module}/
│       ├── controller  {Entity}Controller extends JeecgController
│       ├── service     I{Entity}Service + {Entity}ServiceImpl
│       ├── mapper      {Entity}Mapper
│       └── entity      {Entity}.java（@TableName、@ApiModel）
```

### JEECG 特有注解

```java
@Api(tags = "模块名称")              // Swagger 标签 → PRD 模块
@AutoLog(value = "操作描述")         // 操作日志 → 用户故事
@ApiOperation("接口描述")            // API 文档
@ApiImplicitParam(name, value, required)  // API 参数
@TableName("表名")                   // 数据库表名
@TableField(exist = false)           // 非数据库字段
@Dict(dicCode = "字典编码")          // 数据字典 → 枚举/下拉
@JeecgBootApplication                // 应用入口
```

### codegraph 提取规则

```
1. 搜索 @Api(tags) 注解 → 提取所有模块名称（直接映射到 PRD 章节）
2. 搜索继承 JeecgController 的类 → 完整 API Controller 列表
3. 搜索 @AutoLog → 所有用户可感知的操作（用户故事素材）
4. 搜索 @ApiOperation → 提取 API 描述（直接写入 API 文档）
5. 搜索 @TableName → 所有数据库表（数据库结构文档）
6. codegraph_trace JeecgController.add() → 新增接口完整链路
7. 搜索 queryWrapper → 了解查询条件（SRS 筛选需求）
```

### JEECG 特有：Online 报表 / 低代码

```
搜索 OnlineTable / JimuReport → 说明存在低代码配置，
在规格书中注明"此功能由低代码配置生成，无业务代码"
```

---

## yudao-cloud（芋道源码）

### 代码分层特征（微服务架构）

```
yudao-cloud
├── yudao-framework/        公共框架层
│   ├── yudao-spring-boot-starter-security   OAuth2
│   ├── yudao-spring-boot-starter-web        WebFilter
│   └── yudao-spring-boot-starter-mybatis    MyBatis-Plus
├── yudao-module-system/    系统管理（用户、角色、租户）
├── yudao-module-infra/     基础设施（代码生成、定时任务）
├── yudao-module-bpm/       工作流（Flowable）
├── yudao-module-pay/       支付
└── yudao-module-{domain}/  业务模块
    └── yudao-module-{domain}-biz/
        ├── controller/admin/  管理后台 API
        ├── controller/app/    App 端 API
        ├── service/
        ├── dal/               数据访问层
        │   ├── mysql/entity/
        │   └── mysql/mapper/
        └── convert/           MapStruct 转换
```

### yudao 特有注解与模式

```java
@Tag(name = "管理后台 - 模块名")   // SpringDoc → API 分组
@Operation(summary = "接口名")     // 接口描述
@PreAuthorize("@ss.hasPermission('...')")  // 权限注解
@Schema(description = "字段描述")  // DTO 字段
// VO / ReqVO / RespVO / CreateReqVO / UpdateReqVO → DTO 命名规范
// Convert 接口（MapStruct）→ 对象转换层
// ErrorCodeConstants → 错误码文档
```

### codegraph 提取规则

```
1. 搜索 @Tag(name) → 提取所有模块（admin/app 分层）
2. 搜索 *RespVO → 接口返回结构（直接生成 API 文档 Response）
3. 搜索 *CreateReqVO / *UpdateReqVO → 接口请求体
4. 搜索 ErrorCodeConstants → 错误码列表（写入 API 文档）
5. 搜索 *Mapper extends BaseMapperX → 数据访问层
6. codegraph_trace admin Controller → 完整调用链
7. 搜索 BpmTaskService → 了解工作流节点（流程图素材）
```

### yudao 多模块分析策略

```
# 先了解模块拓扑
codegraph_search("@FeignClient")  → 微服务调用关系（HLA 架构图素材）
codegraph_search("DubboService")  → RPC 服务（如有）
# 然后按模块逐个分析
```

---

## maku-boot（maku 框架）

### 代码分层特征

```
net.maku
├── framework/          公共框架（Sa-Token、MyBatis-Plus）
│   ├── common/         BaseEntity、PageResult、Result
│   ├── security/       SaTokenUtils
│   └── exception/      ErrorCode
├── system/             系统模块
│   └── controller/     SysUserController extends BaseController
└── {module}/           业务模块
    ├── controller/
    ├── service/
    ├── dao/             extends BaseDao<T>
    ├── entity/
    ├── dto/             *Query、*DTO
    └── convert/         MapStruct
```

### maku 特有注解与模式

```java
@SaCheckPermission("system:user:page")   // Sa-Token 权限
@Operation(summary = "...")              // SpringDoc
@LogOperation("操作名称")               // 操作日志
BaseDao<T>                              // 基础 DAO
PageResult<T>                           // 分页返回
```

### codegraph 提取规则

```
1. 搜索 @SaCheckPermission → 权限矩阵（SRS 权限章节）
2. 搜索 @LogOperation → 审计日志功能清单
3. 搜索 extends BaseDao → 数据访问层完整列表
4. 搜索 *Query extends PageQuery → 分页查询条件（SRS 查询需求）
5. codegraph_callers BaseController.getPage() → 分页接口覆盖面
```

---

## 通用 MyBatis-Plus 提取规则（所有框架适用）

```
# 数据库表结构提取
codegraph_search("@TableName") → 所有数据库表
codegraph_node(entity_id, include_source=true) → 字段列表（即建表字段）

# 枚举/字典
codegraph_search("@EnumValue") → 枚举字段（写入数据库文档）
codegraph_search("DictType")   → 数据字典类型

# 数据权限
codegraph_search("DataScope")  → 多租户/数据隔离模式

# 代码生成器产物识别
codegraph_search("@Generated") → 自动生成代码，Spec 中标注
```

---

## 框架对应的 Spec 生成策略差异

| 维度 | RuoYi | JEECG Boot | yudao-cloud | maku-boot |
|-----|-------|-----------|-------------|-----------|
| API 文档来源 | @ApiOperation | @ApiOperation | @Operation | @Operation |
| 权限来源 | @PreAuthorize | @RequiresPermissions | @PreAuthorize | @SaCheckPermission |
| 模块识别 | 包名 | @Api(tags) | @Tag(name) | 包名 |
| DTO 命名 | domain/Entity | entity | *ReqVO/*RespVO | *DTO/*Query |
| 工作流 | 无 | Activiti（可选）| Flowable | 无 |
| 微服务 | ruoyi-cloud 版 | 否 | 是（Spring Cloud）| 否 |
| 前端技术 | Vue2/Vue3 | Ant Design Vue | Vue3 + Element Plus | Vue3 |
