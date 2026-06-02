# mcp-jenkins：安装与 Cursor MCP 配置

本文档补充 **安装步骤**、**Jenkins 侧准备** 与 **Cursor `mcp.json` 配置**。工具用法见同目录 [`SKILL.md`](SKILL.md)。

---

## 一、安装

### 1. 环境要求

- **Python**：建议 3.11+（本文档验证环境为 3.13；需与你在 `mcp.json` 里写的 `mcp-jenkins` 路径一致）。
- **网络**：运行 Cursor 的机器能访问 Jenkins 的 HTTP(S) 地址。
- **Jenkins**：已创建用于 API 访问的账号（见下文「Jenkins 侧准备」）。

### 2. 使用 pip 安装

在打算作为 MCP 进程使用的解释器下安装：

```bash
python3 -m pip install -U mcp-jenkins
```

安装后确认入口可用：

```bash
mcp-jenkins --help
```

若命令未找到，可使用绝对路径（与 `mcp.json` 中 `command` 保持一致），例如：

```bash
/path/to/python3 -m pip show mcp-jenkins
which mcp-jenkins
```

### 3. 版本说明

可通过 `python3 -m pip show mcp-jenkins` 查看当前安装的 **Name / Version**。升级：

```bash
python3 -m pip install -U mcp-jenkins
```

---

## 二、Jenkins 侧准备

1. **用户与权限**  
   使用专用机器人账号或你的账号，确保对目标 Job、队列、节点等有与操作相匹配的权限（只读排查与触发构建、改配置所需权限不同）。

2. **密码或 API Token**  
   推荐使用 **API Token**（Jenkins：用户 → Configure → API Token），将 Token 当作 `jenkins_password` 传入，避免使用明文登录密码（仍以密钥对待，勿写入仓库）。

3. **URL 格式**  
   `jenkins_url` 一般为 Jenkins **根地址**，带尾部 `/`，例如 `https://jenkins.example.com/`。若使用自签名证书，需在 MCP 侧关闭 SSL 校验或导入信任链（见下文环境变量 `jenkins_verify_ssl`）。

4. **Crumb / CSRF**  
   `mcp-jenkins` 通过 HTTP 客户端与 Jenkins 交互，一般可与标准 Jenkins 配置配合；若遇 403 与 crumb 相关错误，需在 Jenkins 安全管理中核对 CSRF 与代理设置。

---

## 三、连接参数来源（env 与命令行）

进程启动时，`mcp-jenkins` 会读取环境变量（**全小写**）。若在命令行传入 `--jenkins-url` 等选项，会写入同名环境变量后再启动服务。

Cursor 中最稳妥的做法是在 **`mcp.json` 的 `env` 块**中设置 `jenkins_url`、`jenkins_username`、`jenkins_password`，避免把秘密写进 `args` 数组（易进历史记录与日志）。

### 可选环境变量

| 变量 | 含义 | 默认 |
|------|------|------|
| `jenkins_url` | Jenkins 根 URL | 必填 |
| `jenkins_username` | 用户名 | 必填 |
| `jenkins_password` | 密码或 API Token | 必填 |
| `jenkins_timeout` | HTTP 超时（秒） | `5` |
| `jenkins_verify_ssl` | 是否校验 SSL证书（字符串 `true`/`false`） | `true` |
| `jenkins_session_singleton` | 会话内复用请求实例 | `true` |

自签名证书可设 `jenkins_verify_ssl` 为 `false`（仅建议在受信内网评估风险后使用）。

### 只读模式

`--read-only` 仅在命令行中提供：在 `mcp.json` 的 `args` 中加入 `"--read-only"` 可限制为只暴露只读类工具（具体以当前版本 `mcp-jenkins` 的 tag 区分为准）。

### 传输方式

默认 **`stdio`**（与 Cursor 本地 MCP 子进程匹配）。若使用 `sse` 或 `streamable-http`，需额外配置 `--host`、`--port`，一般桌面 Cursor 场景**保持默认 stdio 即可**。

---

## 四、Cursor `mcp.json` 配置示例

配置文件路径一般为 **用户级**：`~/.cursor/mcp.json`；也可按项目使用项目级配置（以 Cursor 当前行为为准）。

### 方式 A：推荐 — `command` + `env`

将秘密放在 `env` 中，**勿**把真实密码提交到 Git。

```json
{
  "mcpServers": {
    "jenkins-py": {
      "command": "mcp-jenkins",
      "args": [],
      "env": {
        "jenkins_url": "https://jenkins.example.com/",
        "jenkins_username": "your-user",
        "jenkins_password": "your-api-token",
        "jenkins_timeout": "30",
        "jenkins_verify_ssl": "true"
      }
    }
  }
}
```

若 `mcp-jenkins` 不在 `PATH` 中，将 `command` 改为该脚本所在目录的**绝对路径**（安装目录因系统而异，可用 `which mcp-jenkins` 或查阅对应 Python 的 `bin` 目录）。

### 方式 B：命令行传入 Jenkins 参数（不推荐写入仓库）

敏感信息仍建议通过环境注入；若临时调试，可使用：

```json
{
  "mcpServers": {
    "jenkins-py": {
      "command": "mcp-jenkins",
      "args": [
        "--jenkins-url", "https://jenkins.example.com/",
        "--jenkins-username", "your-user",
        "--jenkins-password", "your-api-token",
        "--jenkins-timeout", "30"
      ]
    }
  }
}
```

### 配置后

保存 `mcp.json`，在 Cursor 中 **重启 MCP 或重启 Cursor**，在 MCP 面板中确认 `jenkins-py`（或你自定义的 server 名）已连接且无认证错误。

---

## 五、常见问题

- **401/403**：检查用户、Token、Job 权限与 Jenkins 安全矩阵。  
- **SSL 错误**：内网自签证书可尝试 `jenkins_verify_ssl`=`false`，或改为信任系统 CA。  
- **超时**：调大 `jenkins_timeout`。  
- **找不到 `mcp-jenkins`**：在 `mcp.json` 里写 Python 的绝对路径 + `-m pip show mcp-jenkins` 确认安装位置，或改用该 Python 下的 `Scripts`/`bin` 中的 `mcp-jenkins`。
