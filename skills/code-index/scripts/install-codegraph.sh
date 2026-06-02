#!/usr/bin/env bash
# code-index 一键安装脚本
# 用法：bash install-codegraph.sh [项目路径]
# 
# 功能：
#   1. 检查 Node.js 环境（需要 >= 18）
#   2. 全局安装 @colbymchenry/codegraph
#   3. 对目标项目初始化并建立索引
#   4. 输出 codegraph status 概览

set -e

PROJECT_PATH="${1:-.}"
PROJECT_PATH=$(realpath "$PROJECT_PATH")

echo "========================================"
echo "  code-index — codegraph 安装脚本"
echo "========================================"
echo "目标项目：$PROJECT_PATH"
echo ""

# 检查 Node.js
if ! command -v node &> /dev/null; then
    echo "❌ 未找到 Node.js，请先安装 Node.js >= 18"
    echo "   下载地址：https://nodejs.org/"
    exit 1
fi

NODE_VER=$(node -e "process.stdout.write(process.versions.node)")
MAJOR=$(echo "$NODE_VER" | cut -d. -f1)
if [ "$MAJOR" -lt 18 ]; then
    echo "❌ Node.js 版本 $NODE_VER 过旧，需要 >= 18"
    exit 1
fi
echo "✅ Node.js $NODE_VER"

# 安装 codegraph
echo ""
echo "📦 安装 @colbymchenry/codegraph ..."
npm install -g @colbymchenry/codegraph --quiet
echo "✅ codegraph 已安装"

# 进入项目目录
cd "$PROJECT_PATH"
echo ""
echo "📁 进入项目：$PROJECT_PATH"

# 生成 .codegraph/config.json（Java 项目优化配置）
if [ ! -d ".codegraph" ]; then
    mkdir -p .codegraph
    cat > .codegraph/config.json << 'EOF'
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
EOF
    echo "✅ 已创建 .codegraph/config.json（已优化 Java 项目排除规则）"
fi

# 初始化并建立索引（-i = index immediately）
echo ""
echo "🔍 初始化并建立代码索引（大型项目可能需要 1-2 分钟）..."
codegraph init -i
echo "✅ 索引完成"

# 输出统计
echo ""
echo "📊 索引统计："
codegraph status

echo ""
echo "========================================"
echo "✅ 安装完成！"
echo ""
echo "接下来可以运行以下命令分析代码："
echo "  codegraph query \"Controller\" --kind class --json"
echo "  codegraph query \"Service\" --kind class --json"
echo "  codegraph context \"用户登录流程\" --format markdown"
echo "========================================"
