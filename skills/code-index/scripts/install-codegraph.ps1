# code-index 一键安装脚本（Windows PowerShell 版）
# 用法：.\install-codegraph.ps1 [-ProjectPath <项目路径>]
#
# 功能：
#   1. 检查 Node.js 环境（需要 >= 18）
#   2. 全局安装 @colbymchenry/codegraph
#   3. 对目标项目初始化并建立索引
#   4. 输出 codegraph status 概览

param(
    [string]$ProjectPath = "."
)

$ErrorActionPreference = 'Stop'

# 解析为绝对路径
$ProjectPath = (Resolve-Path $ProjectPath).Path

Write-Host "========================================"
Write-Host "  code-index — codegraph 安装脚本"
Write-Host "========================================"
Write-Host "目标项目：$ProjectPath"
Write-Host ""

# 检查 Node.js
if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    Write-Host "❌ 未找到 Node.js，请先安装 Node.js >= 18"
    Write-Host "   下载地址：https://nodejs.org/"
    exit 1
}

$nodeVer = node -e "process.stdout.write(process.versions.node)"
$major = [int]($nodeVer -split '\.')[0]
if ($major -lt 18) {
    Write-Host "❌ Node.js 版本 $nodeVer 过旧，需要 >= 18"
    exit 1
}
Write-Host "✅ Node.js $nodeVer"

# 安装 codegraph
Write-Host ""
Write-Host "📦 安装 @colbymchenry/codegraph ..."
npm install -g @colbymchenry/codegraph --quiet
Write-Host "✅ codegraph 已安装"

# 进入项目目录
Set-Location $ProjectPath
Write-Host ""
Write-Host "📁 进入项目：$ProjectPath"

# 生成 .codegraph/config.json（Java 项目优化配置）
if (-not (Test-Path ".codegraph")) {
    New-Item -ItemType Directory -Path ".codegraph" -Force | Out-Null

    $config = @'
{
  "version": 1,
  "exclude": [
    "target/**",
    ".git/**",
    "node_modules/**",
    "dist/**",
    "build/**",
    "*.min.js",
    "**/*.class",
    "**/.mvn/**",
    "**/generated-sources/**"
  ],
  "extractDocstrings": true,
  "trackCallSites": true,
  "maxFileSize": 2097152
}
'@
    $config | Set-Content -Path ".codegraph\config.json" -Encoding UTF8
    Write-Host "✅ 已创建 .codegraph\config.json（已优化 Java 项目排除规则）"
}

# 初始化并建立索引
Write-Host ""
Write-Host "🔍 建立代码索引（大型项目可能需要 1-2 分钟）..."
codegraph index . --quiet
Write-Host "✅ 索引完成"

# 输出统计
Write-Host ""
Write-Host "📊 索引统计："
codegraph status .

Write-Host ""
Write-Host "========================================"
Write-Host "✅ 安装完成！"
Write-Host ""
Write-Host "接下来可以运行以下命令分析代码："
Write-Host "  codegraph query `"Controller`" --kind class --json"
Write-Host "  codegraph query `"Service`" --kind class --json"
Write-Host "  codegraph context `"用户登录流程`" --format markdown"
Write-Host "========================================"
