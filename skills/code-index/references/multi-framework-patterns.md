# 多框架识别与提取规则

涵盖 Python、TypeScript/Go 后端框架，以及 React/Vue/Angular 前端框架。  
Java 框架规则见 `java-frameworks.md`。

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

**Spec 映射：**

| 代码元素 | Spec 文档位置 |
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

**Spec 映射：**

| 代码元素 | Spec 文档位置 |
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

## 前端框架（TypeScript/JavaScript）

> 前端规格书输出目录：`spec/frontend/`  
> 与后端独立，核心维度：页面路由、组件树、状态管理、API 调用

**前端项目识别（优先读取 package.json）：**

```
Read("package.json") → 检查 dependencies：
  "react" / "react-dom"      → React 项目
  "vue"                      → Vue.js 项目
  "@angular/core"            → Angular 项目
  "next"                     → Next.js（React SSR）
  "nuxt"                     → Nuxt.js（Vue SSR）
  "vite" / "webpack"         → 构建工具（前端确认）
```

---

### React（含 Next.js）

**检测信号：**

```
codegraph_search("React.FC", kind="function")      → React 函数组件
codegraph_search("useState", kind="function")      → React Hooks 使用
codegraph_search("useEffect", kind="function")     → 副作用 Hook
codegraph_search("createSlice")                    → Redux Toolkit
codegraph_search("create()", kind="function")      → Zustand store
```

**提取规则：**

```
# 路由表
# React Router v6
codegraph_search("createBrowserRouter" / "Route path=")
Read(src/router/*.tsx) → path + element（组件）+ loader（权限守卫）

# Next.js：直接扫描目录结构
ls src/app/ 或 src/pages/ → 文件路径即路由（file-based routing）

# 状态管理（Redux Toolkit）
codegraph_search("createSlice") → slice 文件路径
Read(src/store/*.ts) → name + initialState 字段 + reducers/actions 方法名

# 状态管理（Zustand）
codegraph_search("create(", kind="function") → store 文件
Read(*.store.ts) → state 字段类型 + action 函数签名

# API 调用层
codegraph_search("axios.get" / "axios.post") → API 服务文件
Read(src/api/*.ts) → 函数名 + 调用路径 + 请求类型 + 响应类型

# React Query（TanStack Query）
codegraph_search("useQuery" / "useMutation") → 查询 key + 调用的 API 函数

# 权限
codegraph_callers("useAuth" / "usePermission") → 找出使用权限 Hook 的组件
```

**前端 Spec 输出字段：**

```
spec/frontend/01-pages.md：
  路径 | 组件文件 | 布局 | 权限守卫 | 功能描述

spec/frontend/03-state.md（Redux/Zustand）：
  Store 名称 | State 字段（字段名/类型/初始值）| Actions 列表

spec/frontend/04-api-client.md：
  函数名 | HTTP 方法 | 端点路径 | 请求参数类型 | 响应类型 | 使用的页面
```

---

### Vue.js（含 Nuxt.js）

**检测信号：**

```
codegraph_search("defineComponent", kind="function") → Vue 组件（Options API）
codegraph_search("defineStore", kind="function")     → Pinia store
codegraph_search("createRouter")                     → Vue Router 实例
codegraph_search("createStore" / "defineStore")      → Vuex / Pinia
```

**提取规则：**

```
# 路由表（Vue Router）
codegraph_search("createRouter") → 路由文件路径
Read(src/router/index.ts) → routes 数组 → path + component + meta.requiresAuth

# Nuxt.js：扫描 pages/ 目录结构（文件路由）

# Pinia Store
codegraph_search("defineStore") → 获取所有 store 文件路径
Read(src/store/*.ts) → store id + state() 函数返回字段 + actions 方法签名

# Vuex Store（旧项目）
codegraph_search("createStore") → Vuex store 文件
Read(src/store/index.ts) → modules → state 字段 + mutations + actions

# API 调用
codegraph_search("request(" / "http.get" / "useHttp") → API 封装文件
Read(src/api/*.ts) → 函数名 + url 字符串 + method + 参数类型

# 权限守卫
codegraph_search("router.beforeEach") → 路由守卫逻辑
Read(permission.ts / guard.ts) → 权限判断逻辑 → 补充到 pages.md

# 组件 Props/Emits
codegraph_search("defineProps" / "defineEmits") → 组件文件
Read(*.vue) → 提取 props 类型定义 + emits 事件列表
```

**yudao-ui-admin-vue3 特有（Element Plus + Vue 3）：**

```
codegraph_search("usePermission" / "hasPermi") → 按钮级权限控制
codegraph_search("ElTable" / "ElForm")          → 识别列表/表单组件
Read(src/api/**.ts) → 提取所有 defHttp() / request() 调用 → API 客户端文档
```

---

### Angular

**检测信号：**

```
codegraph_search("@Component", kind="class")       → Angular 组件
codegraph_search("@Injectable", kind="class")      → Angular 服务
codegraph_search("@NgModule", kind="class")        → 模块定义
codegraph_search("HttpClient", kind="class")       → HTTP 服务
codegraph_search("RouterModule")                   → 路由模块
```

**提取规则：**

```
# 路由配置
codegraph_search("RouterModule.forRoot" / "RouterModule.forChild")
Read(app-routing.module.ts / *.routing.ts) → Routes 数组
  → path + component + canActivate（守卫）+ loadChildren（懒加载）

# 服务层（相当于 API Client + Store）
codegraph_search("HttpClient") → 服务文件路径
Read(*.service.ts) → this.http.get/post 调用 → 端点路径 + 返回类型 Observable<T>

# NgRx 状态管理
codegraph_search("createReducer" / "createEffect" / "createAction")
Read(*.reducer.ts + *.effects.ts) → State 接口字段 + Action 类型 + Effect（副作用）

# 组件 Input/Output
codegraph_search("@Input" / "@Output", kind="class") → 组件文件
Read(*.component.ts) → @Input() 字段类型 + @Output() EventEmitter<T>

# 权限守卫
codegraph_search("CanActivate", kind="class") → 守卫文件
Read(auth.guard.ts) → 判断逻辑 → 补充到路由表权限列
```

**Angular 模块化分析：**

```
codegraph_search("@NgModule") → 所有模块文件
Read(*.module.ts) → imports/declarations/providers → 模块依赖图
# 对应 spec/frontend/02-components.md 模块树
```

---

## 通用跨框架模式

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

### 认证中间件模式识别

```
# 后端
JWT 验证：codegraph_search("JwtAuthGuard" / "verifyToken" / "parseToken")
Sa-Token：codegraph_search("SaTokenUtils" / "@SaCheckLogin")
Session：codegraph_search("HttpSession" / "session.getAttribute")

# 前端
Token 存储：codegraph_search("localStorage.setItem" / "Cookies.set")
请求拦截：codegraph_search("request.interceptors" / "axios.interceptors")
路由守卫：codegraph_search("router.beforeEach" / "canActivate")
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
