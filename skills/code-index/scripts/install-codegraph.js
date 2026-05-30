#!/usr/bin/env node
// code-index 一键安装脚本（跨平台 Node.js 版，支持 Windows / macOS / Linux）
// 用法：node install-codegraph.js [项目路径]
//
// 前提：Node.js >= 18（codegraph 本身也要求此版本）

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const projectPath = path.resolve(process.argv[2] || '.');

const run = (cmd, opts = {}) =>
  execSync(cmd, { stdio: 'inherit', cwd: projectPath, ...opts });

const say = (msg) => console.log(msg);

say('========================================');
say('  code-index — codegraph 安装脚本');
say('========================================');
say(`目标项目：${projectPath}`);
say('');

// 检查 Node.js 版本
const major = parseInt(process.versions.node.split('.')[0], 10);
if (major < 18) {
  console.error(`❌ Node.js ${process.versions.node} 过旧，需要 >= 18`);
  process.exit(1);
}
say(`✅ Node.js ${process.versions.node}`);

// 安装 codegraph
say('');
say('📦 安装 @colbymchenry/codegraph ...');
run('npm install -g @colbymchenry/codegraph --quiet');
say('✅ codegraph 已安装');

// 确认项目目录存在
if (!fs.existsSync(projectPath)) {
  console.error(`❌ 目录不存在：${projectPath}`);
  process.exit(1);
}
say('');
say(`📁 进入项目：${projectPath}`);

// 写入 .codegraph/config.json（Java 项目优化配置）
const configDir = path.join(projectPath, '.codegraph');
const configFile = path.join(configDir, 'config.json');
if (!fs.existsSync(configDir)) {
  fs.mkdirSync(configDir, { recursive: true });
  const config = {
    version: 1,
    exclude: [
      'target/**', '.git/**', 'node_modules/**',
      'dist/**', 'build/**', '*.min.js',
      '**/*.class', '**/.mvn/**', '**/generated-sources/**'
    ],
    extractDocstrings: true,
    trackCallSites: true,
    maxFileSize: 2097152
  };
  fs.writeFileSync(configFile, JSON.stringify(config, null, 2), 'utf8');
  say('✅ 已创建 .codegraph/config.json（已优化 Java 项目排除规则）');
}

// 初始化并建立索引（-i = index immediately）
say('');
say('🔍 初始化并建立代码索引（大型项目可能需要 1-2 分钟）...');
run('codegraph init -i');
say('✅ 索引完成');

// 输出统计
say('');
say('📊 索引统计：');
run('codegraph status');

say('');
say('========================================');
say('✅ 安装完成！');
say('');
say('接下来可以运行以下命令分析代码：');
say('  codegraph query "Controller" --kind class --json');
say('  codegraph query "Service" --kind class --json');
say('  codegraph context "用户登录流程" --format markdown');
say('========================================');
