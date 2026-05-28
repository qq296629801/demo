# 前端框架识别与提取规则

不依赖预先枚举框架名称。通过读取 `package.json` 依赖键自动识别前端技术栈组合，
再通过符号搜索处理 package.json 缺失的情况。
适用于任何前端项目（无论后端语言是 Java/Go/Python/TS）。

---

## 第一节：通用前端框架发现流程

```
Step 1 — Read package.json
  → 提取 dependencies + devDependencies 的所有包名

Step 2 — 对比依赖键映射表（见第二节）
  → 识别出核心框架 + 状态管理库 + 路由库 + HTTP 库 + UI 组件库 + 构建工具

Step 3 — 执行对应提取策略
  → 详细提取规则见本文件第五节（React）、第六节（Vue.js）、第七节（Angular）

Step 4 — package.json 缺失或依赖未命中 → 退回符号搜索（见第三节）
```

---

## 第二节：依赖键映射表

> **原则**：本表只负责"识别框架是什么"，不涉及 Spec 输出路径（输出路径由 SKILL.md 工作流决定）。

### 核心框架层

| 依赖键 | 识别结论 | 详细提取规则 |
|--------|---------|------------|
| `vue` | Vue 3 / Vue 2 | 本文件第六节 §Vue.js |
| `react` + `react-dom` | React | 本文件第五节 §React |
| `@angular/core` | Angular | 本文件第七节 §Angular |
| `next` | Next.js（React SSR）| 本文件第五节 §React（Next.js 适用）|
| `nuxt` | Nuxt.js（Vue SSR）| 本文件第六节 §Vue.js（Nuxt.js 适用）|
| `svelte` | Svelte | 通用前端规则 |
| `solid-js` | SolidJS | 通用前端规则 |

### 状态管理层

| 依赖键 | 识别结论 | 关键符号 |
|--------|---------|---------|
| `pinia` | Pinia（Vue 3 官方）| `defineStore()` → store 名称/state 字段/actions |
| `vuex` | Vuex（Vue 2/3 旧）| `createStore()` → mutations/actions |
| `redux` / `@reduxjs/toolkit` | Redux / RTK | `createSlice()` → slice 名称/actions |
| `zustand` | Zustand | `create()` |
| `@ngrx/store` | NgRx（Angular）| `createReducer/createAction` |
| `jotai` / `recoil` | Jotai / Recoil | `atom()` |
| `mobx` | MobX | `makeObservable/@observable` |

### 路由层

| 依赖键 | 识别结论 | 关键符号 |
|--------|---------|---------|
| `vue-router` | Vue Router | `createRouter()` → routes 数组（path/component/meta）|
| `react-router-dom` | React Router v6 | `createBrowserRouter()` |
| `@angular/router` | Angular Router | `RouterModule.forRoot()` |
| `next`（已含路由）| Next.js 文件路由 | `pages/` 或 `app/` 目录结构即路由 |

### HTTP 请求层

| 依赖键 | 识别结论 | 关键符号 |
|--------|---------|---------|
| `axios` | Axios | `axios.create()` → 实例；`axios.get/post` → 调用端点 |
| `@tanstack/react-query` / `@tanstack/vue-query` | TanStack Query | `useQuery/useMutation` |
| `swr` | SWR（React）| `useSWR()` |
| `ky` / `got` | 轻量 HTTP 库 | 同 Axios 方式提取调用端点 |

### UI 组件库（辅助识别技术栈定位）

| 依赖键 | 识别结论 |
|--------|---------|
| `element-plus` | Element Plus（Vue 3，国内后台常见）|
| `ant-design-vue` | Ant Design Vue |
| `antd` | Ant Design（React）|
| `@mui/material` | Material UI（React）|
| `@angular/material` | Angular Material |
| `naive-ui` / `arco-design` | 国内 Vue 组件库 |
| `vant` / `nutui` | 移动端 H5 项目 |
| `uni-app` / `taro` | 小程序/跨端项目 |

### 构建工具（辅助识别项目配置）

| 依赖键 | 识别结论 |
|--------|---------|
| `vite` | Vite（现代构建工具）|
| `webpack` / `@vue/cli-service` | Webpack（旧版构建）|
| `turbopack` / `@next/swc-*` | Next.js 内置构建 |

---

## 第三节：package.json 缺失时的符号搜索

当 `package.json` 不存在或依赖键无法命中时，退回到符号搜索：

```
# Vue 项目特征
codegraph_search("defineComponent")   → Vue Options/Composition API
codegraph_search("defineStore")       → Pinia store
codegraph_search("createRouter")      → Vue Router

# React 项目特征
codegraph_search("useState")          → React Hooks
codegraph_search("createSlice")       → Redux Toolkit
codegraph_search("React.FC")          → 函数组件

# Angular 项目特征
codegraph_search("@NgModule")         → Angular 模块
codegraph_search("@Component")        → Angular 组件

# 跨框架通用
codegraph_search("axios.create")      → Axios 实例（API 调用层入口）
codegraph_search("router.beforeEach") → 路由守卫（权限控制）
```

---

## 第四节：通用前端分层提取规则（与框架无关）

识别到框架后，按以下规则提取前端规格信息：

### 页面路由层

```
codegraph_search("router" / "routes", kind="variable") → 路由配置文件路径
Read(src/router/index.ts) → 提取路由表：
  - path → 页面 URL
  - component → 对应组件文件
  - meta.auth / meta.roles → 权限守卫
  - children → 嵌套路由

Next.js / Nuxt.js 文件路由：
  直接列举 pages/ 或 app/ 目录结构即为路由表
```

### 状态管理层

```
codegraph_search("defineStore" / "createStore" / "createSlice") → store 文件路径
Read(store 文件) → 提取：
  - store 名称（唯一标识）
  - state 字段及类型
  - actions/mutations 方法签名
  - getters/selectors
```

### API 调用层

```
codegraph_search("axios.create" / "request" / "http", kind="function") → API 服务文件
Read(src/api/*.ts) → 提取：
  - 函数名 → 对应业务操作
  - HTTP 方法 + 端点路径
  - 请求参数类型
  - 响应类型

codegraph_callers("useUserStore" / "usePermissionStore")
  → 找出哪些页面/组件依赖权限 Store → 补充路由守卫文档
```

### 登录/权限流程

```
codegraph_trace(from="LoginPage.submit", to="useUserStore.setToken")
  → 提取完整登录调用链 → 驱动前端流程图生成
```

---

## 使用示例：Vue 3 + Pinia 项目（yudao-ui-admin-vue3）

```
Step 1 — Read package.json，发现：
  "vue": "^3.x"
  "pinia": "^2.x"
  "vue-router": "^4.x"
  "element-plus": "^2.x"
  "axios": "^1.x"
  "vite": "^5.x"

Step 2 — 命中依赖键：
  vue → Vue 3
  pinia → Pinia（defineStore()）
  vue-router → Vue Router（createRouter()）
  element-plus → Element Plus（国内后台）
  axios → Axios（axios.create() → API 实例）
  vite → Vite 构建

Step 3 — 执行 Vue.js 提取策略：
  → 详细规则见本文件第六节 §Vue.js

Step 4 — 按第四节分层提取：
  codegraph_search("createRouter") → Read(src/router/index.ts) → 路由表
  codegraph_search("defineStore")  → Read(src/store/*.ts)      → State 模块
  codegraph_search("axios.create") → Read(src/utils/request.ts) → HTTP 实例
  codegraph_search("useUserStore") → callers → 权限相关页面列表
```

---

## 第五节：React / Next.js 详细提取规则

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

**Spec 输出字段：**

```
spec/10-pages.md：
  路径 | 组件文件 | 布局 | 权限守卫 | 功能描述

spec/12-state.md（Redux/Zustand）：
  Store 名称 | State 字段（字段名/类型/初始值）| Actions 列表

spec/05-api.md（"前端调用层对照"章节，由 Prompt 5 生成）：
  函数名 | HTTP 方法 | 端点路径 | TS 请求类型 | TS 响应类型 | 使用的页面
  （原 spec/frontend/04-api-client.md 的内容，已并入后端 API 文档同一接口下）
```

---

## 第六节：Vue.js / Nuxt.js 详细提取规则

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

## 第七节：Angular 详细提取规则

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

# 模块依赖图
codegraph_search("@NgModule") → 所有模块文件
Read(*.module.ts) → imports/declarations/providers → 模块树
```

---

## 第八节：通用前端跨框架模式

### 认证中间件模式识别

```
# Token 存储
codegraph_search("localStorage.setItem" / "Cookies.set")

# 请求拦截（Axios）
codegraph_search("request.interceptors" / "axios.interceptors")

# 路由守卫
codegraph_search("router.beforeEach" / "canActivate")
```

### HTTP 动词识别（前端视角）

```
匹配 API 服务函数中的调用模式：
  axios.get / http.get    → 查询/列表/详情
  axios.post / http.post  → 创建/登录/提交
  axios.put               → 全量更新
  axios.patch             → 部分更新
  axios.delete            → 删除

路径规范识别：
  /api/v1/{resource}           → RESTful 资源集合
  /api/v1/{resource}/{id}      → 资源单项
  /api/v1/{resource}/{action}  → 资源动作
```

### 字段类型注解提取（TypeScript 前端 DTO）

```typescript
// 从接口/类型定义提取字段信息：
interface CreateUserReq {
  username: string      // → 字段名/类型
  password?: string     // → 可选字段（? 号）
  roleIds: number[]     // → 数组类型
}

// Zod / Yup 运行时校验（可选）
const schema = z.object({
  username: z.string().min(4).max(30),
  email: z.string().email(),
})
// → 提取校验规则写入字段校验矩阵
```
