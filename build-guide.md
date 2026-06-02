# agents

智能体提示词



#  claude code  搭建教程

打开终端
使用 Cmd+Space 搜索 "Terminal" 或在 应用程序 > 实用工具 中找到终端

2
复制并执行环境检查脚本
```shell

$ curl -fsSL https://download.aicodemirror.com/env_deploy/env-install.sh | bash

```


3
卸载已安装的Claude Code（未安装请跳过）
```shell
$ npm uninstall -g @anthropic-ai/claude-code
```


安装官方原版包

```shell
$ npm install -g @anthropic-ai/claude-code
```

5
在「API密钥」界面配置一个API Key
访问仪表板的「API密钥」页面，创建并复制一个新的API密钥

6
复制并执行环境变量配置脚本
$ curl -fsSL https://download.aicodemirror.com/env_deploy/env-deploy.sh | bash -s -- "你的API_KEY"
重启终端，验证安装结果
重启终端后运行以下命令，确认安装成功

$ claude -v


**配置密钥**

```shell
export ANTHROPIC_BASE_URL=https://api.aicodemirror.com/api/claudecode
export ANTHROPIC_API_KEY=你的密钥
export ANTHROPIC_AUTH_TOKEN=你的密钥
```



**settings.json 配置网络连接**

目录位置
```shell
/Users/mjy/.claude/settings.json/

```



```json
{
"env": {
"HTTPS_PROXY": "http://127.0.0.1:7897",
"HTTP_PROXY": "http://127.0.0.1:7897"
}
}

```



**智能体**

- agents

智能体类型	存储路径 (相对于项目根目录或家目录)	适用范围
项目级智能体	.claude/agents/	仅限当前项目，可随 Git 提交与团队共享。
个人通用智能体	~/.claude/agents/	你电脑上的所有项目都能调用。

文件示例：.claude/agents/reviewer.md



```markdown
---
name: code-reviewer
description: 专门用于代码审查，检查潜在漏洞和逻辑错误。
---

# 指令 (Instructions)
你是一个高级代码审查专家。当被调用时，请执行以下操作：
1. 检查代码是否符合 TypeScript 严格模式。
2. 确保所有的 API 调用都有错误处理。
3. 检查是否有硬编码的敏感信息。

```



目录位置
```shell
/Users/mjy/.claude/agents/

```




- CLAUDE.md
主要存储记忆


目录位置
```shell

macOS: ~/.claude/CLAUDE.md
你的项目/CLAUDE.md

```

