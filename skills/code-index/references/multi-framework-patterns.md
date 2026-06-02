# 多框架识别与提取规则（后端）

涵盖 Python、TypeScript/Node.js、Go 后端框架。  
Java 后端框架规则见 `java-frameworks.md`。  
前端框架规则（Vue/React/Angular）见 `frontend-frameworks.md`。

---

## Python 框架

### Django（含 Django REST Framework）

**检测信号：**

```
codegraph_search("models.Model", kind="class")     → Django ORM 模型
codegraph_search("APIView", kind="class")          → DRF 视图
codegraph_search("ViewSet", kind="class")          → DRF ViewSet
codegraph_search("ModelSerializer", kind="class")  → DRF 序列化器
codegraph_search("urlpatterns")                    → URL 路由配置
```

**代码分层提取：**

```
# 模型层（对应数据库表）
codegraph_search("models.Model") → 找到所有模型文件路径
Read(models.py) → 提取字段定义（CharField/IntegerField/ForeignKey 等）

# 视图层（API 入口）
codegraph_search("@api_view" / "APIView") → 获取视图文件路径
Read(views.py) → 提取 HTTP 方法（get/post/put/delete）+ URL 映射

# 序列化器（相当于 DTO）
codegraph_search("ModelSerializer") → 序列化器文件路径
Read(serializers.py) → 提取字段声明 + validators 校验规则

# URL 路由
Read(urls.py) → 提取 path() / re_path() → path + view 映射

# 权限/认证
codegraph_search("IsAuthenticated" / "IsAdminUser") → 权限类
codegraph_search("permission_classes") → 接口权限配置
```

**Codebook 映射：**

| 代码元素 | Codebook 文档位置 |
|---------|-------------|
| `models.Model` 子类字段 | 数据库结构文档 DDL |
| `ModelSerializer` fields | API 文档 Request/Response 字段 |
| `urlpatterns` path() | API 文档接口路径 |
| `validators=` 参数 | API 字段校验矩阵 |
| `permission_classes` | API 权限说明 |

---

### FastAPI

**检测信号：**

```
codegraph_search("@app.get" / "@router.get")       → FastAPI 路由装饰器
codegraph_search("BaseModel", kind="class")         → Pydantic 模型（DTO）
codegraph_search("Depends", kind="function")        → 依赖注入（权限/DB）
codegraph_search("APIRouter", kind="variable")      → 路由器实例
```

**代码分层提取：**

```
# Pydantic 模型（请求/响应 DTO）
codegraph_search("BaseModel") → 找到所有 Pydantic 模型文件
Read(schemas.py / models.py) → 提取字段 + Field(... , min_length=, regex=) 校验

# 路由端点
codegraph_search("@app.post" / "@router.post") → 路由文件路径
Read(routers/*.py) → 装饰器路径 + 函数签名（入参类型 = Request DTO）

# 依赖注入
codegraph_callers("get_current_user" / "get_db") → 找出哪些端点需要认证/DB
```

**FastAPI 特有：**

```python
# 字段校验（直接映射到 API 字段校验矩阵）
Field(..., min_length=3, max_length=50, regex=r"^[a-zA-Z0-9_]+$")
Field(None, description="可选字段")

# 响应模型声明（映射到 API 文档 Response）
@router.get("/users", response_model=List[UserResponse])
```

---

### Flask（含 Flask-RESTful / Flask-RESTx）

**检测信号：**

```
codegraph_search("Flask", kind="variable")         → Flask 应用实例
codegraph_search("@app.route" / "@blueprint.route")→ 路由定义
codegraph_search("Resource", kind="class")         → Flask-RESTful 资源
codegraph_search("Namespace", kind="variable")     → Flask-RESTx 命名空间
```

**提取规则：**

```
Read(app.py / views.py) → 提取 @app.route + methods 参数
codegraph_search("Resource") → 获取 API 资源类路径
Read(resources.py) → get/post/put/delete 方法 → API 文档
codegraph_search("fields.String" / "ma.Schema") → Marshmallow 序列化器字段
```

---

## TypeScript / Node.js 后端框架

### NestJS

**检测信号：**

```
codegraph_search("@Controller", kind="class")      → NestJS 控制器
codegraph_search("@Injectable", kind="class")      → NestJS 服务/Provider
codegraph_search("@Module", kind="class")          → 模块定义
codegraph_search("@Entity", kind="class")          → TypeORM 实体
codegraph_search("@ApiProperty")                   → Swagger DTO 属性
```

**代码分层提取：**

```
# 控制器层（API 入口）
codegraph_search("@Controller") → 获取 .controller.ts 文件列表
Read(*.controller.ts) → @Get/@Post 装饰器路径 + @UseGuards 守卫 + @Roles 权限

# DTO 层（请求/响应体）
codegraph_search("@ApiProperty", kind="class") → DTO 文件
Read(*.dto.ts) → 字段名 + @IsString/@IsNotEmpty/@MinLength 校验装饰器

# Entity 层（数据库表）
codegraph_search("@Entity") → TypeORM 实体文件
Read(*.entity.ts) → @Column({ type, length, nullable }) + @Index/@Unique

# 服务层
codegraph_callers("XxxService.create") → 找调用方（Controller + EventEmitter + Cron）
codegraph_trace("XxxController.create", "XxxRepository.save") → 流程图
```

**NestJS 特有校验注解（class-validator）：**

```typescript
@IsString()
@IsNotEmpty()
@MinLength(4)
@MaxLength(30)
@Matches(/^[a-zA-Z0-9_]+$/, { message: '仅允许字母数字下划线' })
@IsEmail()
@IsEnum(UserStatus)
```

**Codebook 映射：**

| 代码元素 | Codebook 文档位置 |
|---------|-------------|
| `@ApiTags("模块名")` | API 文档模块分组 |
| `@ApiOperation({ summary })` | API 接口描述 |
| `@ApiProperty({ description, required })` | API 字段说明 |
| `class-validator` 装饰器 | API 字段校验矩阵 |
| `@UseGuards(JwtAuthGuard)` | API 权限说明 |
| `@Column({ unique: true })` | 数据库唯一索引 |

---

### Express.js

**检测信号：**

```
codegraph_search("router.get" / "router.post")     → Express 路由定义
codegraph_search("express.Router")                 → 路由器实例
codegraph_search("mongoose.Schema" / "Model")      → Mongoose 模型（MongoDB）
codegraph_search("DataTypes.STRING" / "sequelize") → Sequelize 模型（SQL）
```

**提取规则：**

```
Read(routes/*.ts) → router.get/post/put/delete 路径 + 中间件链
codegraph_search("mongoose.Schema") → 获取 Schema 文件
Read(models/*.ts) → 字段定义 + required/validate 校验

# 中间件（权限/验证）
codegraph_search("passport.authenticate" / "verifyToken") → 认证中间件
codegraph_callers("authMiddleware") → 找出受保护的路由
```

---

## Go 后端框架

### Gin

**检测信号：**

```
codegraph_search("gin.Default" / "gin.New")        → Gin 实例创建
codegraph_search("*gin.Context", kind="function")  → Gin Handler 函数
codegraph_search("router.GET" / "r.POST")          → 路由注册
codegraph_search("gorm.Model", kind="struct")      → GORM 模型
```

**代码分层提取：**

```
# 路由层
Read(router/*.go / main.go) → router.GET("/path", handler) 路径 + Handler 函数名

# Handler 层（相当于 Controller）
codegraph_search("*gin.Context") → 所有 Handler 函数
Read(handler/*.go) → c.ShouldBindJSON / c.Param / c.Query 参数提取

# 模型层（数据库表 + DTO）
codegraph_search("gorm:\"column") → GORM struct tag 字段
Read(model/*.go) → struct 字段 + json/form/validate tag

# 权限中间件
codegraph_callers("AuthMiddleware" / "JWTMiddleware") → 受保护路由
```

**Go struct tag 提取规则：**

```go
// 从 struct tag 提取三类信息：
type User struct {
    Name  string `json:"name" gorm:"column:name;size:30;not null" validate:"required,min=2,max=30"`
    // json tag  → API 字段名
    // gorm tag  → 数据库字段定义（column/size/not null/unique）
    // validate tag → 字段校验矩阵（required/min/max/email）
}
```

---

### Echo

**检测信号：**

```
codegraph_search("echo.New" / "e.GET")             → Echo 实例/路由
codegraph_search("echo.Context", kind="function")  → Echo Handler
codegraph_search("echo.HandlerFunc")               → Handler 类型
```

**提取规则（同 Gin，路由注册方式不同）：**

```
Read(server.go / router.go) → e.GET/POST/PUT/DELETE("/path", handler)
codegraph_trace("handler", "db.Create") → 调用链 → 流程图
```

---

## 通用跨框架模式（后端）

### HTTP 动词 + 路径识别（跨语言通用）

```
匹配模式（任意语言）：
  GET    → 查询/列表/详情
  POST   → 创建/登录/提交
  PUT    → 全量更新
  PATCH  → 部分更新
  DELETE → 删除

路径规范识别：
  /api/v1/{resource}         → RESTful 资源集合
  /api/v1/{resource}/{id}    → 资源单项
  /api/v1/{resource}/{id}/{action}  → 资源动作
```

### 认证中间件模式识别（后端）

```
JWT 验证：codegraph_search("JwtAuthGuard" / "verifyToken" / "parseToken")
Sa-Token：codegraph_search("SaTokenUtils" / "@SaCheckLogin")
Session：codegraph_search("HttpSession" / "session.getAttribute")
```

### 错误响应结构识别

```
# 统一响应体模式（Read 异常处理类后判断）
codegraph_search("GlobalExceptionHandler" / "ControllerAdvice")
codegraph_search("Result<T>" / "R<T>" / "CommonResult<T>")
Read(GlobalExceptionHandler.java 或等价文件)
  → 提取错误码字段名 + 消息字段名 → API 文档统一响应格式
```

### 字段 tag/注解三类信息提取模板

读取 Entity/DTO/Struct 文件后，识别三类信息：

| 类型 | Java | Python | TypeScript | Go |
|------|------|--------|----------|-----|
| **序列化名** | `@JsonProperty("name")` | 字段名（snake_case） | `@ApiProperty` | `json:"name"` |
| **数据库映射** | `@Column(name="user_name")` | `db_column="user_name"` | `@Column({ name })` | `gorm:"column:user_name"` |
| **校验规则** | `@NotBlank @Pattern` | `Field(min_length=)` | `@IsNotEmpty @MinLength` | `validate:"required,min=2"` |
