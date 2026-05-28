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
  → 详细提取规则见 multi-framework-patterns.md 对应章节

Step 4 — package.json 缺失或依赖未命中 → 退回符号搜索（见第三节）
```

---

## 第二节：依赖键映射表

> **原则**：本表只负责"识别框架是什么"，不涉及 Spec 输出路径（输出路径由 SKILL.md 工作流决定）。

### 核心框架层

| 依赖键 | 识别结论 | 详细提取规则 |
|--------|---------|------------|
| `vue` | Vue 3 / Vue 2 | multi-framework-patterns.md §Vue.js |
| `react` + `react-dom` | React | multi-framework-patterns.md §React |
| `@angular/core` | Angular | multi-framework-patterns.md §Angular |
| `next` | Next.js（React SSR）| multi-framework-patterns.md §React |
| `nuxt` | Nuxt.js（Vue SSR）| multi-framework-patterns.md §Vue.js |
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
  → 详细规则见 multi-framework-patterns.md §Vue.js

Step 4 — 按第四节分层提取：
  codegraph_search("createRouter") → Read(src/router/index.ts) → 路由表
  codegraph_search("defineStore")  → Read(src/store/*.ts)      → State 模块
  codegraph_search("axios.create") → Read(src/utils/request.ts) → HTTP 实例
  codegraph_search("useUserStore") → callers → 权限相关页面列表
```
