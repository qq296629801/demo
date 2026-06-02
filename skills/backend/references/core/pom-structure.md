# iidp-backend-demo-ai 工程结构与 POM 配置参考

本文档按当前仓库中的 `iidp-backend-demo-ai` 项目整理。生成、修改或审查 IIDP 后端 Demo 时，以本文件为工程拓扑、POM、`apps.json`、`app.json`、Docker Compose 与启动配置的参考。

> 安全约束：本文档不复制 `apiToken`、数据库密码、Redis 密码、内网地址对应密码等敏感明文；但会完整登记这些字段所在文件、字段名、用途和配置关系。不要为了“补全文档”把真实密钥写入 skill。

---

## 1. 开发与运行环境
### 1.1 Git 与源码获取硬性要求

生成、修改或审查 IIDP 后端 Demo 前，必须具备可执行 `git clone` 的 Git 环境，并优先从指定仓库获取最新代码。不能因为本地存在旧目录就跳过远端校验。

硬性仓库地址：

```bash
git clone http://192.168.175.55:9888/mijiuye/iidp-backend-demo-ai.git
```

Git 环境自检：

```bash
git --version
```

如果提示 `git: command not found`、`'git' 不是内部或外部命令`，或终端无法识别 `git`，必须先协助用户安装 Git，安装后重新执行 `git --version` 确认可用。

常见安装方式：

| 系统 | 安装命令或方式 |
|---|---|
| macOS | 优先执行 `xcode-select --install` 安装 Command Line Tools；也可使用 `brew install git` |
| Ubuntu / Debian | `sudo apt-get update && sudo apt-get install -y git` |
| CentOS / RHEL / Rocky / AlmaLinux | `sudo yum install -y git` 或 `sudo dnf install -y git` |
| Windows | 使用 winget：`winget install --id Git.Git -e --source winget`；或安装 Git for Windows 后重新打开终端 |

本地当前项目检查：

```bash
TARGET_REPO_URL="http://192.168.175.55:9888/mijiuye/iidp-backend-demo-ai.git"
FOUND_TARGET_REPO=0

if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  git remote -v
  git status --short
  if git remote -v | awk '{print $2}' | grep -Fx "$TARGET_REPO_URL" >/dev/null; then
    FOUND_TARGET_REPO=1
  fi
fi

if [ -d "iidp-backend-demo-ai/.git" ]; then
  git -C iidp-backend-demo-ai remote -v
  git -C iidp-backend-demo-ai status --short
  if git -C iidp-backend-demo-ai remote -v | awk '{print $2}' | grep -Fx "$TARGET_REPO_URL" >/dev/null; then
    FOUND_TARGET_REPO=1
  fi
fi

if [ "$FOUND_TARGET_REPO" -eq 0 ]; then
  git clone "$TARGET_REPO_URL"
fi
```

源码处理规则：

1. 先确认 Git 可用；没有 Git 时，必须先完成安装或给出明确安装指引，不能继续假设本地源码可信。
2. 必须先检查本地当前项目是否已经是 `http://192.168.175.55:9888/mijiuye/iidp-backend-demo-ai.git` 对应仓库：如果当前目录在 Git 工作树内，先看当前目录的 `git remote -v`；如果当前目录下存在 `iidp-backend-demo-ai/.git`，再看该子目录的远端。
3. 本地当前项目不是目标仓库，且没有 `iidp-backend-demo-ai/` 目录时，必须执行上述 `git clone` 获取仓库。
4. 本地已有 `iidp-backend-demo-ai/` 目录时，必须先执行 `git -C iidp-backend-demo-ai remote -v`、`git -C iidp-backend-demo-ai status --short` 判断是否就是该仓库，以及是否存在未提交变更。
5. 如果已有目录是干净工作区且远端匹配，可执行 `git -C iidp-backend-demo-ai pull --ff-only` 获取最新代码。
6. 如果已有目录存在未提交变更，禁止为了更新代码执行 `git reset --hard`、`git checkout -- .` 或删除目录；应先记录差异并让维护者决定如何处理。
7. 如果 Git 已安装但仓库地址不可访问，必须把克隆或拉取失败的关键信息写入交付说明；只有在明确说明“未能从远端仓库刷新代码”后，才允许退回使用当前本地工作区内容。
8. 如果 Git 无法安装或当前用户没有安装权限，必须先提示用户处理运行环境；不要继续基于不明来源源码生成最终结论。

| 项 | 当前项目要求 |
|---|---|
| Git | 必须可执行 `git clone`，并优先克隆 `http://192.168.175.55:9888/mijiuye/iidp-backend-demo-ai.git` |
| JDK | 主工程、启动模块和公共模块按 Java 8 编译，必须使用 JDK 而不是 JRE。XXL-Job 模块 POM 属性写的是 17，但当前继承的 compiler plugin 仍显式按 1.8 编译 |
| Maven | Maven 3.6+；当前验证可用 Maven 3.9.9 |
| Maven settings | 项目根 `iidp-backend-demo-ai/settings.xml` |
| 数据库 | MySQL 8.0 推荐；本地 Compose 使用 `mysql:8.0` |
| Redis | Compose 使用 `redis:7-alpine`；原始开发配置仍指向内网 Redis |
| MinIO | Compose 使用 `minio/minio:latest`，bucket 默认 `apps` |
| Docker | Docker Compose v2，`docker compose config` 必须可解析 |
| 默认应用端口 | `8060` |

构建时须先确认本地 Java 版本，再选对应命令：

```bash
java -version   # 查看当前 JDK 版本
```

**情况 A：本地已安装 JDK 8，使用 JAVA_HOME 显式指定（推荐）**

```bash
cd iidp-backend-demo-ai

# macOS（/usr/libexec/java_home 自动定位）
JAVA_HOME=$(/usr/libexec/java_home -v 1.8) mvn -s ./settings.xml -DskipTests clean package

# Linux（路径按实际安装位置调整）
JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 mvn -s ./settings.xml -DskipTests clean package

# Windows CMD（路径按实际安装位置调整）
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_xxx && mvn -s ./settings.xml -DskipTests clean package
```

**情况 B：本地只有 JDK 17 / 21，无法安装 JDK 8**

POM 已配置 `maven.compiler.source=8 target=8`，JDK 17/21 的 `javac` 可直接编译；跳过测试时一般可通过编译：

```bash
cd iidp-backend-demo-ai
mvn -s ./settings.xml -DskipTests clean package
```

> 注意：JDK 17+ 对 Spring Boot 2.x 的反射访问有额外限制，**运行时**可能出现 `InaccessibleObjectException`；如有运行需求，建议仍以 JDK 8 或 JDK 11 为主。

---

## 2. 顶层目录完整结构

以下清单排除 `.git/`、`.idea/`、`target/`、`logs/`、`sie-iidp-demo-apps/apps/` 这类本地元数据或构建输出；源码、POM、配置、部署文件、文档入口和 app 加载清单全部列出。

当前项目根没有 `docs/` 目录；`README.md` 是当前随项目存在的文档入口。

运行时由 Docker 挂载的 `apps/` 目录采用 **`apps.json` + `apps/modules/*.jar`** 结构（平台 IIDP 能力包为 `sie-iidp-*-v3.x.x-RELEASE.jar`，业务包如 `sie-iidp-demo-xxljob-executor-1.0-SNAPSHOT.jar` 与清单对应关系见 §6）。

```text
iidp-backend-demo-ai/
├── .env.example
├── .gitignore
├── Dockerfile
├── README.md
├── apps/
│   ├── apps.json
│   └── modules/
│       ├── sie-iidp-app-store-app-v3.0.0-RELEASE.jar
│       ├── sie-iidp-cache-v3.0.0-RELEASE.jar
│       ├── sie-iidp-config-v3.0.0-RELEASE.jar
│       ├── sie-iidp-dict-v3.0.0-RELEASE.jar
│       ├── sie-iidp-demo-xxljob-executor-1.0-SNAPSHOT.jar
│       ├── sie-iidp-file-v3.0.0-RELEASE.jar
│       ├── sie-iidp-iam-v3.0.0-RELEASE.jar
│       ├── sie-iidp-log-v3.0.1-RELEASE.jar
│       ├── sie-iidp-tenant-v3.0.0-RELEASE.jar
│       └── sie-iidp-user-prefer-v3.0.0-RELEASE.jar
├── apps-frontend/
│   └── umdComps/
├── docker-compose.yml
├── docker/
│   └── config/
│       ├── application-dev.properties
│       ├── application.properties
│       └── dbcp.properties
├── modules/
│   └── apps/
├── pom.xml
├── settings.xml
├── sie-iidp-demo-apps/
│   ├── pom.xml
│   └── sie-iidp-demo-xxljob-executor/
│       ├── pom.xml
│       └── src/main/
│           ├── java/com/sie/iidp/demo/xxljob/executor/
│           │   ├── app.json
│           │   ├── controller/TestController.java
│           │   ├── job/SampleXxlJob.java
│           │   ├── model/SpringAppModel.java
│           │   └── spring/
│           │       ├── ServerPortCustomizer.java
│           │       ├── SpringApp.java
│           │       └── XxlJobConfig.java
│           └── resources/
│               ├── application.properties
│               └── logback.xml
├── sie-iidp-demo-common/
│   ├── pom.xml
│   └── src/main/java/com/sie/iidp/common/
│       ├── codesequence/model/ExampleCodeSequence.java
│       └── util/
│           ├── BizSettingUtil.java
│           ├── CodeGenTempUtil.java
│           ├── CodeGenUtil.java
│           ├── condition/Condition.java
│           ├── condition/ConvertType.java
│           ├── condition/QueryType.java
│           ├── consts/Constant.java
│           ├── consts/FieldConst.java
│           ├── consts/MethodConst.java
│           ├── consts/ModelConst.java
│           ├── database/BaseModelExtUtils.java
│           ├── database/DataBaseUtil.java
│           ├── database/SelectUtil.java
│           ├── database/UpdateUtil.java
│           ├── dto/PageViewDTO.java
│           ├── enums/CodeGenEnum.java
│           ├── errors/Exceptions.java
│           └── fifter/FilterBuilder.java
└── sie-iidp-demo-start/
    ├── pom.xml
    └── src/main/
        ├── java/com/sie/iidp/demo/service/
        │   ├── DemoServer.java
        │   └── WebConfig.java
        └── resources/
            ├── application-dev.properties
            ├── application.properties
            ├── dbcp.properties
            └── logback-spring.xml
```

**说明：** 仓库根下 `apps/modules` 是否出现取决于 Maven 将 jar 输出到 **`${basedir}/../apps/modules`** 后是否再同步进仓库；**容器与本地引擎实际读取** 以 `apps/modules/*.jar` 与 `apps/apps.json` 为准（见 §4.3、§6）。树中列出 `apps/modules` 表示该路径在常见集成流程中可能出现，并非要求仓库内必须提交非空目录。

---

## 3. Maven 聚合关系

### 3.1 根 POM

路径：`iidp-backend-demo-ai/pom.xml`

| 项 | 当前值 |
|---|---|
| `groupId` | `com.sie.iidp` |
| `artifactId` | `iidp-backend-demo` |
| `packaging` | `pom` |
| `version` | `${revision}` |
| `revision` | `1.0-SNAPSHOT` |

根 POM 聚合模块：

```xml
<modules>
    <module>sie-iidp-demo-apps</module>
    <module>sie-iidp-demo-start</module>
    <module>sie-iidp-demo-common</module>
</modules>
```

新增业务 App 时，通常只修改 `sie-iidp-demo-apps/pom.xml`；只有新增公共库、启动模块或新的顶层聚合模块时，才修改根 POM。

### 3.2 业务 App 聚合 POM

路径：`iidp-backend-demo-ai/sie-iidp-demo-apps/pom.xml`

| 项 | 当前值 |
|---|---|
| parent | `com.sie.iidp:iidp-backend-demo:${revision}` |
| `artifactId` | `sie-iidp-demo-apps` |
| `packaging` | `pom` |

已登记业务模块：

```xml
<modules>
    <module>sie-iidp-demo-example</module>
    <module>sie-iidp-demo-xxljob-executor</module>
</modules>
```

业务聚合 POM 自身声明的依赖：

```text
com.sie.meta:sie-snest-sdk:compile，排除 com.sie.meta:sie-snest-engine 和 com.alibaba:fastjson
com.alibaba:fastjson:1.2.83:compile
com.sie.meta:sie-snest-engine:compile
com.sie.meta:sie-iidp-dd-test:1.0.0-SNAPSHOT:test
```

业务聚合 POM 的资源配置会把 `src/main/java` 下非 `.java` 文件打包进 jar，必须保留：

```xml
<resources>
    <resource>
        <directory>src/main/resources</directory>
        <filtering>true</filtering>
    </resource>
    <resource>
        <directory>src/main/java</directory>
        <includes>
            <include>**/*.*</include>
        </includes>
        <excludes>
            <exclude>**/*.java</exclude>
        </excludes>
    </resource>
    <resource>
        <directory>src/main/resources</directory>
        <filtering>true</filtering>
        <targetPath>WEB-INF/classes</targetPath>
    </resource>
</resources>
```

业务聚合 POM 使用 `sie-snest-maven-plugin` 对业务 App jar 执行 `repackage`，当前配置的复制目录为 `${basedir}/../../apps/modules`（解析为 **仓库根目录的上一级** 下的 `apps/modules`，与根 POM `maven-dependency-plugin` 的平台 jar 落点一致；若团队将仓库放在无父目录约定的路径，须在集成说明中写明如何把 jar 同步到 Docker 使用的 `apps/modules/`）：

```xml
<plugin>
    <groupId>com.sie.meta</groupId>
    <artifactId>sie-snest-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>
        <copyDir>${basedir}/../../apps/modules</copyDir>
    </configuration>
    <executions>
        <execution>
            <id>package</id>
            <phase>package</phase>
            <goals>
                <goal>repackage</goal>
            </goals>
        </execution>
        <execution>
            <id>clean</id>
            <phase>clean</phase>
            <goals>
                <goal>repackage</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

注意：`copyDir` 与根 POM `maven-dependency-plugin` 的 `outputDirectory` 均指向 **仓库父目录** 下的 `apps/modules`（`${basedir}` 分别为 `sie-iidp-demo-apps` 与 `iidp-backend-demo-ai` 时的相对路径不同，但解析目标一致）。**Dockerfile / Compose** 挂载的是仓库内的 `./apps`，其中 **`apps/modules/`** 存放引擎加载的 jar；构建后若 jar 只出现在父目录 `apps/modules`，必须复制或同步到 `iidp-backend-demo-ai/apps/modules/`，且 `apps/apps.json` 的 `apps.SDK` 与物理文件名一致。

---

## 4. 根 POM 版本与依赖管理

### 4.1 属性版本

| 属性 | 当前值 |
|---|---|
| `engine.version` | `v3.0.0-RELEASE` |
| `sdk.version` | `v3.0.0-RELEASE` |
| `sie-iidp-plugin.version` | `0.3.1` |
| `revision` | `1.0-SNAPSHOT` |
| `maven.compiler.source` | `8` |
| `maven.compiler.target` | `8` |
| `project.build.sourceEncoding` | `UTF-8` |
| `org.springframework.boot.version` | `2.6.2` |
| `spring-boot.version` | `2.7.13` |
| `spring-boot-maven-plugin.version` | `2.7.2` |
| `commons-lang3.version` | `3.12.0` |
| `jackson-core.version` | `2.13.0` |
| `jackson-databind.version` | `2.13.0` |
| `hutool.version` | `5.8.11` |
| `guava.version` | `31.0.1-jre` |
| `amqp-client.version` | `5.16.0` |
| `easyexcel.version` | `3.1.5` |
| `forest-core.version` | `1.5.33` |
| `xxl-job-core.version` | `2.3.1` |
| `sie-iidp-iam.version` | `v3.0.0-RELEASE` |
| `sie-iidp-tenant.version` | `v3.0.0-RELEASE` |
| `sie-iidp-app-store-app.version` | `v3.0.0-RELEASE` |
| `sie-iidp-file.version` | `v3.0.0-RELEASE` |
| `sie-iidp-dict.version` | `v3.0.0-RELEASE` |
| `sie-iidp-cache.version` | `v3.0.0-RELEASE` |
| `sie-iidp-config.version` | `v3.0.0-RELEASE` |
| `sie-iidp-log.version` | `v3.0.1-RELEASE` |
| `sie-iidp-user-prefer.version` | `v3.0.0-RELEASE` |

不要为了“升级”主动替换这些版本。除非用户明确要求升级，否则以 `iidp-backend-demo-ai/pom.xml` 为准。

### 4.2 dependencyManagement 覆盖项

| groupId | artifactId | version | 备注 |
|---|---|---|---|
| `com.sie.iidp` | `sie-iidp-demo-common` | `${project.version}` | 公共模块 |
| `com.sie.iidp` | `sie-iidp-demo-example` | `${project.version}` | 历史示例依赖管理项，当前源码未包含该模块 |
| `com.sie.meta` | `sie-snest-sdk` | `${sdk.version}` | 排除 `sie-snest-engine` |
| `com.sie.meta` | `sie-snest-engine` | `${engine.version}` | 排除 `jquery-i18n-properties` |
| `com.sie.meta` | `sie-snest-script-engine` | `${engine.version}` | 可选脚本引擎 |
| `com.sie.meta` | `sie-iidp-plugin` | `${sie-iidp-plugin.version}` | IIDP 插件 |
| `org.springframework.boot` | `spring-boot-starter-web` | `${spring-boot.version}` | Web 启动 |
| `org.springframework.boot` | `spring-boot-starter` | `${spring-boot.version}` | Spring Boot |
| `com.xuxueli` | `xxl-job-core` | `${xxl-job-core.version}` | XXL-Job |
| `com.rabbitmq` | `amqp-client` | `${amqp-client.version}` | RabbitMQ 客户端 |
| `org.springframework.boot` | `spring-boot-dependencies` | `${org.springframework.boot.version}` | BOM import |
| `org.springframework.boot` | `spring-boot-maven-plugin` | `${spring-boot-maven-plugin.version}` | Boot 打包插件 |
| `cn.hutool` | `hutool-all` | `${hutool.version}` | 工具包 |
| `org.apache.commons` | `commons-lang3` | `${commons-lang3.version}` | 工具包 |
| `com.google.guava` | `guava` | `${guava.version}` | 工具包 |
| `com.alibaba` | `easyexcel` | `${easyexcel.version}` | Excel |
| `com.alibaba` | `easyexcel-core` | `${easyexcel.version}` | Excel core |
| `com.fasterxml.jackson.core` | `jackson-core` | `${jackson-core.version}` | JSON |
| `com.fasterxml.jackson.core` | `jackson-databind` | `${jackson-databind.version}` | JSON |
| `com.dtflys.forest` | `forest-core` | `${forest-core.version}` | HTTP 客户端 |

### 4.3 根 POM 插件与平台 Jar 拷贝

根 POM 使用：

| 插件 | 版本 | 作用                                                                                    |
|---|---|---------------------------------------------------------------------------------------|
| `org.apache.maven.plugins:maven-compiler-plugin` | `3.6.0` | Java 1.8 编译，开启 `-parameters`                                                          |
| `org.apache.maven.plugins:maven-dependency-plugin` | `3.6.0` | `package` 阶段复制 IIDP 平台能力 jar 到 `${basedir}/../apps/modules`（**仓库根**的上一级目录下的 `apps/modules`） |

根 POM 当前 `artifactItems` 对应坐标（版本属性见 §4.1）：

```text
com.sie.meta:sie-iidp-app-store-app:${sie-iidp-app-store-app.version}
com.sie.meta:sie-iidp-iam:${sie-iidp-iam.version}
com.sie.meta:sie-iidp-tenant:${sie-iidp-tenant.version}
com.sie.meta:sie-iidp-file:${sie-iidp-file.version}
com.sie.meta:sie-iidp-dict:${sie-iidp-dict.version}
com.sie.meta:sie-iidp-cache:${sie-iidp-cache.version}
com.sie.meta:sie-iidp-user-prefer:${sie-iidp-user-prefer.version}
com.sie.meta:sie-iidp-config:${sie-iidp-config.version}
com.sie.meta:sie-iidp-log:${sie-iidp-log.version}
```

若根 POM `artifactItems` 中对同一坐标（例如 `sie-iidp-tenant`）出现重复的 `<artifactItem>`，以仓库内实际 `pom.xml` 为准；上表为去重后的能力包清单。

该插件在 **根 POM** 执行时，`${basedir}/../apps/modules` 解析为与 `iidp-backend-demo-ai` 同级的 `apps/modules`。**Docker / 本地运行** 使用的加载目录是仓库内 `apps/modules/`（与 `apps/apps.json` 配合）；若构建产物只出现在父目录 `apps/modules`，须在交付或脚本中同步到 `iidp-backend-demo-ai/apps/modules/` 后再起容器。

历史上若存在 `sie-iidp-demo-apps/apps/` 等中间目录，以当前 `pom.xml` 为准；**Compose 挂载** 固定为项目根 `./apps`。

根 POM 的 `distributionManagement`：

| 类型 | id | url |
|---|---|---|
| release | `deploymentRepoReleases` | `http://192.168.168.156:8081/repository/maven-releases/` |
| snapshot | `deploymentRepoSnapshots` | `http://192.168.168.156:8081/repository/maven-snapshots/` |

---

## 5. 子模块 POM 明细

当前源码子模块有 `sie-iidp-demo-example`、`sie-iidp-demo-xxljob-executor`、`sie-iidp-demo-common`、`sie-iidp-demo-start`。

### 5.1 `sie-iidp-demo-example`

路径：`iidp-backend-demo-ai/sie-iidp-demo-apps/sie-iidp-demo-example/pom.xml`

| 项 | 当前值 |
|---|---|
| parent | `com.sie.iidp:sie-iidp-demo-apps:${revision}` |
| `artifactId` | `sie-iidp-demo-example` |
| Java 编译 | `8` |

依赖：

```text
org.apache.commons:commons-lang3
org.springframework:spring-core
cn.hutool:hutool-all
org.projectlombok:lombok:provided
com.sie.iidp:sie-iidp-demo-common:compile
com.sie.meta:sie-iidp-plugin:compile
```

该模块包含单元测试，测试类位于 `src/test/java/`，测试数据（JSON）与测试类同包，位于 `src/test/resources/`。详见 §7。

### 5.3 `sie-iidp-demo-xxljob-executor`

路径：`iidp-backend-demo-ai/sie-iidp-demo-apps/sie-iidp-demo-xxljob-executor/pom.xml`

| 项 | 当前值 |
|---|---|
| parent | `com.sie.iidp:sie-iidp-demo-apps:${revision}` |
| `artifactId` | `sie-iidp-demo-xxljob-executor` |
| POM 属性 Java 编译 | `17` |
| 继承的 compiler plugin 配置 | `source=1.8`、`target=1.8` |

依赖：

```text
com.xuxueli:xxl-job-core
org.springframework.boot:spring-boot-starter-web
```

注意：XXL-Job 模块 POM 显式设置 `maven.compiler.source/target=17`，但父级 `maven-compiler-plugin` 又显式配置 `source/target=1.8`。当前源码未使用 Java 17 语法；如果后续引入 Java 17 语法，需要同步调整继承的 compiler plugin 配置并重新验证构建。

### 5.4 `sie-iidp-demo-common`

路径：`iidp-backend-demo-ai/sie-iidp-demo-common/pom.xml`

| 项 | 当前值 |
|---|---|
| parent | `com.sie.iidp:iidp-backend-demo:${revision}` |
| `artifactId` | `sie-iidp-demo-common` |
| Java 编译 | `8` |

依赖：

```text
cn.hutool:hutool-all
org.projectlombok:lombok:provided
com.sie.meta:sie-snest-sdk:compile
com.sie.meta:sie-snest-engine:compile
```

`sie-snest-sdk` 排除 `sie-snest-engine`，随后单独声明 `sie-snest-engine`。

### 5.5 `sie-iidp-demo-start`

路径：`iidp-backend-demo-ai/sie-iidp-demo-start/pom.xml`

| 项 | 当前值 |
|---|---|
| parent | `com.sie.iidp:iidp-backend-demo:${revision}` |
| `artifactId` | `sie-iidp-demo-start` |
| description | `iidp-demo启动端` |
| Java 编译 | `8` |

依赖：

```text
org.springframework.boot:spring-boot-starter-web
com.sie.meta:sie-snest-engine
com.fasterxml.jackson.core:jackson-core
com.fasterxml.jackson.core:jackson-databind
```

插件：

```text
org.springframework.boot:spring-boot-maven-plugin:2.7.2
org.apache.maven.plugins:maven-compiler-plugin:3.3
```

启动 jar 路径：

```text
iidp-backend-demo-ai/sie-iidp-demo-start/target/sie-iidp-demo-start-1.0-SNAPSHOT.jar
```

Dockerfile 依赖该 jar；执行 `docker compose up -d --build` 前必须先 Maven 打包。

---

## 6. apps 加载清单

路径：`iidp-backend-demo-ai/apps/apps.json`

### 6.1 apps.json 结构与加载路径

- 清单文件：`apps/apps.json`。
- 引擎加载的 jar：**物理路径** 为 `apps/modules/`，`apps.apps.SDK` 数组中每项为 **文件名**（不含目录前缀），须与 `apps/modules/` 下实际文件名一致。

`apps.json` 的根结构（`apiToken` 为敏感值，此处不复制明文；`apps.SDK` 顺序以仓库文件为准）：

```json
{
  "loaders": {
    "API": "com.sie.snest.engine.loaders.ApiLoader",
    "SDK": "com.sie.snest.sdk.loaders.SdkLoader"
  },
  "apiToken": "<敏感值：文件中存在，文档不复制>",
  "apps": {
    "SDK": [
      "sie-iidp-app-store-app-v3.0.0-RELEASE.jar",
      "sie-iidp-cache-v3.0.0-RELEASE.jar",
      "sie-iidp-config-v3.0.0-RELEASE.jar",
      "sie-iidp-dict-v3.0.0-RELEASE.jar",
      "sie-iidp-file-v3.0.0-RELEASE.jar",
      "sie-iidp-iam-v3.0.0-RELEASE.jar",
      "sie-iidp-log-v3.0.1-RELEASE.jar",
      "sie-iidp-tenant-v3.0.0-RELEASE.jar",
      "sie-iidp-user-prefer-v3.0.0-RELEASE.jar"
    ],
    "SDK-OUT": [],
    "API": [],
    "API-OUT": []
  }
}
```

物理 jar 位于 **`apps/modules/`**，文件名须与 `apps.SDK` 中条目一致（引擎按当前工程约定从该目录加载）。

当前 `apps.SDK-OUT`、`apps.API`、`apps.API-OUT` 均为空数组。

注意：

- `sie-iidp-demo-xxljob-executor-1.0-SNAPSHOT.jar` 已存在于 `apps/modules/`，但 **当前未** 列入 `apps.SDK`；若要在引擎中加载该 XXL-JOB 示例 App，须在 `apps.SDK` 中追加该文件名并确认加载顺序符合团队约定。
- 新增其它业务 App 时，必须把打包产物 jar 放入 **`apps/modules/`**（或与团队约定的等价目录），并在 `apps.SDK` 中登记，否则引擎不会加载。

---

## 7. 单元测试配置

单元测试基于 IIDP 自研的 **DDTest 数据驱动测试框架**，POM 层面的依赖与插件统一在 `sie-iidp-demo-apps/pom.xml` 中声明，所有业务 App 子模块继承。框架注解规范、Mock 配置、JSON 数据格式详见 `skills/backend/references/core/testing.md`。

### 7.1 测试依赖（`sie-iidp-demo-apps/pom.xml`）

```xml
<!-- fastjson 独立声明，同时从 sie-snest-sdk 中排除，避免版本冲突 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.83</version>
</dependency>

<!-- IIDP DDTest 框架，仅在测试阶段使用 -->
<dependency>
    <groupId>com.sie.meta</groupId>
    <artifactId>sie-iidp-dd-test</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

`sie-snest-sdk` 的 exclusion 须同步添加：

```xml
<exclusion>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
</exclusion>
```

### 7.2 测试插件（`sie-iidp-demo-apps/pom.xml`）

| 插件 | 版本 | 作用 |
|---|---|---|
| `org.apache.maven.plugins:maven-surefire-plugin` | `3.2.5` | 扫描并执行测试类 |
| `org.jacoco:jacoco-maven-plugin` | `0.8.8` | `test` 阶段生成覆盖率报告 |

`maven-surefire-plugin` 关键配置：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <skip>false</skip>
        <workingDirectory>${project.basedir}/..</workingDirectory>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Tests.java</include>
            <include>**/*TestCase.java</include>
        </includes>
    </configuration>
</plugin>
```

`workingDirectory` 设置为 `${project.basedir}/..`（即 `sie-iidp-demo-apps/` 目录），测试运行时以此为工作目录加载引擎上下文。

### 7.3 测试文件约定

- 测试类：`src/test/java/<包路径>/<业务类名>Test.java`
- 测试数据：`src/test/resources/<包路径>/<业务类名>Test.json`，与测试类同包同名
- 测试资源：`src/test/resources/` 下需提供 `application.properties`、`application-dev.properties`、`dbcp.properties`、`logback-spring.xml`

当前已有测试：

| 测试类 | 被测类 | 位置 |
|---|---|---|
| `SiteManagerTest` | `SiteManager` | `sie-iidp-demo-example/src/test/` |

执行测试（含覆盖率报告）：

```bash
cd iidp-backend-demo-ai
mvn -s ./settings.xml test
```

跳过测试打包：

```bash
mvn -s ./settings.xml -DskipTests clean package
```

---

## 8. XXL-Job App 明细

### 8.1 app.json

路径：`iidp-backend-demo-ai/sie-iidp-demo-apps/sie-iidp-demo-xxljob-executor/src/main/java/com/sie/iidp/demo/xxljob/executor/app.json`

| 字段 | 当前值 |
|---|---|
| `name` | `sie-iidp-demo-xxljob-executor` |
| `displayName` | `赛意IIDP Demo XXL-JOB APP` |
| `author` | `iidp` |
| `company` | `sie` |
| `category` | `iidp` |
| `product` | `SIE-IIDP-DEMO` |
| `productDesc` | `SIE-IIDP-DEMO` |
| `categoryDesc` | `IIDP` |
| `description` | `SIE-IIDP-DEMO-EXAMPLE` |
| `summary` | `【XXL-JOB】模块APP,以iidp平台原子能力的粒度开发样例源码，配套开发手册，供新手学习查看；` |
| `type` | `SDK` |
| `tag` | `master` |
| `resolved` | `com.sie.iidp.demo.xxljob.executor` |
| `dependencies` | `[]` |
| `application` | `true` |
| `icon` | `sie` |
| `license` | `LGPL 3.0` |
| `version` | `1.0.0` |

事件：

```json
{
  "startUp": ["xxljob_spring_app::start"],
  "unInstall": ["xxljob_spring_app::stop"]
}
```

### 8.2 源码与配置

| 文件 | 内容 |
|---|---|
| `model/SpringAppModel.java` | `@Model(name = "xxljob_spring_app", type = Model.ModelType.Data)`，提供 `start`/`stop` 事件入口 |
| `spring/SpringApp.java` | Spring 应用启动封装 |
| `spring/XxlJobConfig.java` | XXL-Job executor 配置 Bean |
| `spring/ServerPortCustomizer.java` | 端口定制类，目前代码被注释 |
| `job/SampleXxlJob.java` | 示例 Job，`@XxlJob("demoJobHandler")` |
| `controller/TestController.java` | 示例控制器 |
| `resources/application.properties` | XXL-Job 配置 |
| `resources/logback.xml` | 日志配置 |

`resources/application.properties` 当前配置项：

```properties
# server.port=8081
xxl.job.admin.addresses=http://localhost:8090/xxl-job-admin
xxl.job.accessToken=<配置值：文件中存在，生产需替换>
xxl.job.executor.appname=iidp-demo-test
xxl.job.executor.address=
xxl.job.executor.ip=
xxl.job.executor.port=9999
xxl.job.executor.logpath=/data/applogs/xxl-job/jobhandler
xxl.job.executor.logretentiondays=30
logging.config=classpath:logback.xml
```

---

## 9. Common 公共模块明细

路径：`iidp-backend-demo-ai/sie-iidp-demo-common`

### 9.1 公共模型

| 文件 | 模型 |
|---|---|
| `codesequence/model/ExampleCodeSequence.java` | `@Model(tableName = "example_code_sequence", name = "example_code_sequence", displayName = "Example-编码序列表")` |

### 9.2 工具类清单

| 文件 | 作用 |
|---|---|
| `util/BizSettingUtil.java` | 业务设置读取 |
| `util/CodeGenTempUtil.java` | 示例编码生成临时工具 |
| `util/CodeGenUtil.java` | 编码规则工具 |
| `util/condition/Condition.java` | Filter 构建注解 |
| `util/condition/ConvertType.java` | 条件值转换类型 |
| `util/condition/QueryType.java` | 查询操作类型 |
| `util/consts/Constant.java` | 通用常量 |
| `util/consts/FieldConst.java` | 字段名常量 |
| `util/consts/MethodConst.java` | 内置服务名常量 |
| `util/consts/ModelConst.java` | 示例模型名常量 |
| `util/database/BaseModelExtUtils.java` | BaseModel 查询、转换、计数工具 |
| `util/database/DataBaseUtil.java` | 原生 SQL 查询、更新、分页工具 |
| `util/database/SelectUtil.java` | SQL SELECT 条件构造器 |
| `util/database/UpdateUtil.java` | SQL UPDATE 条件构造器 |
| `util/dto/PageViewDTO.java` | 分页结果 DTO |
| `util/enums/CodeGenEnum.java` | 编码生成枚举 |
| `util/errors/Exceptions.java` | 通用异常常量与异常工厂 |
| `util/fifter/FilterBuilder.java` | 基于注解构建 IIDP Filter |

---

## 10. 启动模块明细

路径：`iidp-backend-demo-ai/sie-iidp-demo-start`

| 文件 | 作用 |
|---|---|
| `src/main/java/com/sie/iidp/demo/service/DemoServer.java` | Spring Boot 启动类；启动前加载 IIDP 配置、创建 Loader、启动 Engine |
| `src/main/java/com/sie/iidp/demo/service/WebConfig.java` | Web 配置 |
| `src/main/resources/application.properties` | 激活 `dev` profile |
| `src/main/resources/application-dev.properties` | 原始开发环境配置 |
| `src/main/resources/dbcp.properties` | 原始数据库连接配置 |
| `src/main/resources/logback-spring.xml` | 日志配置 |

### 10.1 DemoServer 启动链路

`DemoServer.main` 的启动顺序：

```text
ConfigUtils.loadAllConfig()
读取 engine.store.meta.mode
读取 engine.run.mode
LoaderFactory.getAppLoader(WebMode.of(runMode))
IMetaFactory.getMetaStoreManager(IMetaType.fromString(metaStoreType))
Loader.setLoader(loader)
Engine.start()
SpringApplication.run(DemoServer.class, args)
```

`DemoServer` 排除 Spring Boot 默认数据源自动配置：

```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "com.sie.snest")
```

### 10.2 原始开发配置

`application.properties`：

```properties
spring.profiles.active=dev
```

`application-dev.properties` 关键项：

| 配置项 | 当前含义                        |
|---|-----------------------------|
| `server.port=8060` | 应用端口                        |
| `engine.run.mode=SINGLE` | 单机运行模式                      |
| `engine.store.meta.mode=CLOUD` | 元数据存储模式                     |
| `engine.model2ddl.mode` | 当前注释，示例值 `CREATE`           |
| `minio.endpoint` | 原始开发环境 MinIO 地址，当前为内网地址,示例：http://192.168.175.54:9000 |
| `redis.host` / `redis.port` / `redis.db` / `redis.password` | 原始开发环境 Redis 配置，密码不复制到本文档   |
| `cas.*` | CAS 单点登录配置                  |
| `getModel.whiteList` | 模型白名单                       |
| `kkfile.host` | 文件预览服务                      |
| `server.host` | 服务域名                        |

`dbcp.properties` 关键项：

| 配置项 | 当前含义 |
|---|---|
| `driverClassName=com.mysql.cj.jdbc.Driver` | MySQL 驱动 |
| `url` | 原始本机 MySQL `snest` 库连接 |
| `username` | 原始数据库用户名 |
| `password` | 原始数据库密码，本文档不复制明文 |
| `initialSize=5` | 初始连接数 |
| `maxActive=30` | 最大活跃连接数 |
| `minIdle=5` | 最小空闲连接数 |
| `maxWait=6000` | 最大等待毫秒 |
| `filters=stat` | Druid 统计 |
| `testWhileIdle=true` | 空闲检测 |
| `poolPreparedStatements=true` | Docker 配置已修正为 `=`；原始文件中存在冒号写法 |
| `connectionProperties` | Druid SQL 统计参数 |

---

## 11. Docker 与 Compose 部署

### 11.1 Dockerfile

路径：`iidp-backend-demo-ai/Dockerfile`

| 项 | 当前值 |
|---|---|
| 基础镜像 | `eclipse-temurin:8-jre-alpine` |
| `APP_JAR` | `sie-iidp-demo-start/target/sie-iidp-demo-start-1.0-SNAPSHOT.jar` |
| `APP_HOME` | `/opt/iidp` |
| 默认 `JAVA_OPTS` | `-Xms512m -Xmx1024m` |
| 工作目录 | `/opt/iidp` |
| jar 复制 | `COPY ${APP_JAR} /app.jar` |
| apps 复制 | `COPY apps/ /apps/` |
| 暴露端口 | `8060` |
| 外置配置 | `-Dspring.config.additional-location=file:/config/` 和 `-Xbootclasspath/a:/config` |

启动命令：

```sh
java ${JAVA_OPTS} \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.config.additional-location=file:/config/ \
  -Xbootclasspath/a:/config \
  -Dspring.output.ansi.enabled=always \
  -Dfile.encoding=UTF-8 \
  -Duser.timezone=Asia/Shanghai \
  -jar /app.jar
```

### 11.2 docker-compose.yml

路径：`iidp-backend-demo-ai/docker-compose.yml`

Compose 项目名：

```yaml
name: iidp-backend-demo-ai
```

服务完整清单：

| 服务 | 镜像/构建 | container_name | 端口 | volume | healthcheck |
|---|---|---|---|---|---|
| `mysql` | `mysql:8.0` | `iidp-demo-mysql` | `${MYSQL_PORT:-3306}:3306` | `mysql_data:/var/lib/mysql` | `mysqladmin ping` |
| `redis` | `redis:7-alpine` | `iidp-demo-redis` | `${REDIS_PORT:-6379}:6379` | `redis_data:/data` | `redis-cli ping` |
| `minio` | `minio/minio:latest` | `iidp-demo-minio` | `${MINIO_API_PORT:-9000}:9000`、`${MINIO_CONSOLE_PORT:-9001}:9001` | `minio_data:/data` | `/minio/health/live` |
| `minio-init` | `minio/mc:latest` | `iidp-demo-minio-init` | 无 | 无 | 依赖 `minio` healthy |
| `iidp-app` | build `Dockerfile` | `iidp-demo-app` | `${APP_PORT:-8060}:8060` | `./apps:/apps`、`./docker/config:/config:ro`、`iidp_logs:/logs` | 依赖 MySQL、Redis、MinIO 初始化 |

Compose volume 完整清单：

```yaml
volumes:
  mysql_data:
  redis_data:
  minio_data:
  iidp_logs:
```

### 11.3 .env.example

路径：`iidp-backend-demo-ai/.env.example`

| 变量 | 默认值 |
|---|---|
| `APP_PORT` | `8060` |
| `JAVA_OPTS` | `-Xms512m -Xmx1024m` |
| `MYSQL_PORT` | `3306` |
| `MYSQL_DATABASE` | `iidp_demo` |
| `MYSQL_ROOT_PASSWORD` | 示例默认值，生产必须替换 |
| `MYSQL_USER` | `iidp` |
| `MYSQL_PASSWORD` | 示例默认值，生产必须替换 |
| `REDIS_PORT` | `6379` |
| `REDIS_PASSWORD` | 示例默认值，生产必须替换 |
| `MINIO_API_PORT` | `9000` |
| `MINIO_CONSOLE_PORT` | `9001` |
| `MINIO_ROOT_USER` | `snest` |
| `MINIO_ROOT_PASSWORD` | 示例默认值，生产必须替换 |
| `MINIO_BUCKET` | `apps` |

### 11.4 Docker 外置配置

路径：`iidp-backend-demo-ai/docker/config/application.properties`

```properties
spring.profiles.active=dev
```

路径：`iidp-backend-demo-ai/docker/config/application-dev.properties`

| 配置项 | Docker 当前值 |
|---|---|
| `server.port` | `8060` |
| `engine.run.mode` | `SINGLE` |
| `engine.store.meta.mode` | `CLOUD` |
| `engine.model2ddl.mode` | `CREATE` |
| `appAuth.Server` | `http://iidp-app:8060` |
| `minio.endpoint` | `http://minio:9000` |
| `minio.accessKey` | `snest` |
| `minio.secretKey` | 示例默认值，生产必须替换 |
| `minio.bucketName` | `apps` |
| `redis.host` | `redis` |
| `redis.port` | `6379` |
| `redis.db` | `0` |
| `redis.password` | 示例默认值，生产必须替换 |
| `kkfile.host` | `http://host.docker.internal:8012` |
| `server.host` | `http://localhost:8060` |

路径：`iidp-backend-demo-ai/docker/config/dbcp.properties`

| 配置项 | Docker 当前值 |
|---|---|
| `driverClassName` | `com.mysql.cj.jdbc.Driver` |
| `url` | `jdbc:mysql://mysql:3306/iidp_demo?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai` |
| `username` | `iidp` |
| `password` | 示例默认值，生产必须替换 |
| `initialSize` | `5` |
| `maxActive` | `30` |
| `minIdle` | `5` |
| `maxWait` | `6000` |
| `filters` | `stat` |
| `testWhileIdle` | `true` |
| `poolPreparedStatements` | `true` |
| `maxOpenPreparedStatements` | `20` |

Docker 配置必须使用 compose 服务名 `mysql`、`redis`、`minio`，不要引用原始开发配置中的内网地址。

---

## 12. 构建、运行与验证命令

### 12.1 Maven 打包

执行打包前，先检查本地 Java 版本并选择对应命令：

```bash
java -version   # 确认是 JDK（含 javac）而非 JRE
```

**JDK 8 可用时（推荐，完整兼容）**

```bash
cd iidp-backend-demo-ai

# macOS
JAVA_HOME=$(/usr/libexec/java_home -v 1.8) mvn -s ./settings.xml -DskipTests clean package

# Linux（路径按实际安装位置调整）
JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 mvn -s ./settings.xml -DskipTests clean package

# Windows CMD（路径按实际安装位置调整）
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_xxx && mvn -s ./settings.xml -DskipTests clean package
```

**仅有 JDK 17 / 21 时（编译可通过，运行存在风险）**

POM 已设置 `maven.compiler.source=8 target=8`，可直接构建：

```bash
cd iidp-backend-demo-ai
mvn -s ./settings.xml -DskipTests clean package
```

当前验证过的 Reactor Summary（JDK 8 环境）：

```text
Reactor Summary for iidp-backend-demo 1.0-SNAPSHOT:
iidp-backend-demo: SUCCESS
sie-iidp-demo-apps: SUCCESS
sie-iidp-demo-xxljob-executor: SUCCESS
sie-iidp-demo-start: SUCCESS
sie-iidp-demo-common: SUCCESS
BUILD SUCCESS
```

常见错误：
- `No compiler is provided in this environment`：当前 `JAVA_HOME` 指向 JRE，须切换到 JDK。
- `InaccessibleObjectException` / 模块访问异常：JDK 17/21 运行 Spring Boot 2.x 的已知问题，改用 JDK 8 或 11 解决。

### 12.2 Docker Compose 静态检查

```bash
cd iidp-backend-demo-ai
docker compose config --quiet
```

### 12.3 Docker Compose 启动

```bash
cd iidp-backend-demo-ai
cp .env.example .env
docker compose up -d --build
docker compose logs -f iidp-app
```

默认访问：

```text
IIDP 后端：http://localhost:8060
Druid：http://localhost:8060/druid
MinIO Console：http://localhost:9001
```

当前环境限制：`docker compose build iidp-app` 需要拉取 `eclipse-temurin:8-jre-alpine`，若 Docker Hub token 获取超时，属于网络/镜像源问题，不是 POM 或 Dockerfile 路径问题。

---

## 13. 新增业务 App 操作规范

新增业务 App 时必须同时完成以下文件登记：

1. 在 `iidp-backend-demo-ai/sie-iidp-demo-apps/` 下创建 `sie-iidp-demo-{appName}/`。
2. 在 `iidp-backend-demo-ai/sie-iidp-demo-apps/pom.xml` 的 `<modules>` 中追加 `<module>sie-iidp-demo-{appName}</module>`，禁止重复。
3. 子模块 POM parent 使用：

```xml
<parent>
    <groupId>com.sie.iidp</groupId>
    <artifactId>sie-iidp-demo-apps</artifactId>
    <version>${revision}</version>
</parent>
```

4. `app.json` 必须放在 `resolved` 对应包路径下，例如：

```text
src/main/java/com/sie/iidp/{appPkg}/app.json
```

5. `app.json.view` 必须登记所有视图文件路径。
6. `app.json.data` 必须登记 `data/menus.json` 和其它种子数据文件。

**完整 `app.json` 示例**（来自 `sie-iidp-demo-example`，已补充 `view`/`data` 数组）：

```json
{
  "name": "sie-iidp-demo-example",
  "displayName": "赛意IIDP Demo Example APP",
  "author": "iidp",
  "company": "sie",
  "category": "iidp",
  "product": "sie-iidp-demo",
  "productDesc": "SIE IIDP Demo",
  "categoryDesc": "IIDP",
  "description": "SIE-IIDP-DEMO-EXAMPLE",
  "summary": "示例模块 APP，以 IIDP 平台原子能力为粒度开发示例源码",
  "type": "SDK",
  "tag": "master",
  "resolved": "com.sie.iidp.example",
  "dependencies": [],
  "application": true,
  "icon": "sie",
  "license": "LGPL 3.0",
  "version": "1.0.0",
  "view": [
    "classmgr/views/example_class_view.json",
    "studentmgr/views/example_student_view.json",
    "studentprofile/views/demo_student_profile_view.json",
    "itemmgr/views/example_item_view.json"
  ],
  "data": [
    "views/menus.json",
    "data/yes_no.json",
    "data/unit_type.json",
    "data/example_order.json",
    "data/job_level.json"
  ],
  "events": {}
}
```

规则：
- `view` 数组：每项为相对于 `resolved` 包路径的视图 JSON 路径，引擎按此列表加载视图。
- `data` 数组：每项为相对于 `resolved` 包路径的种子数据路径，`menus.json` 必须在此登记。
- 两个数组如有遗漏，对应视图或数据在运行时不会被加载，但不会报启动错误，只会静默缺失。

7. 视图 JSON、菜单 JSON 放在 `src/main/java` 下，依赖父 POM 资源配置进入 jar。
8. 执行 Maven 打包，确认业务 jar 的复制目录（`sie-snest-maven-plugin` 的 `copyDir`）；若要随当前 Dockerfile 部署，须将需加载的 jar 置于 **`iidp-backend-demo-ai/apps/modules/`**（若构建先落在仓库父目录 `apps/modules`，须同步到此处）。
9. 在 `iidp-backend-demo-ai/apps/apps.json` 的 `apps.SDK` 中追加新业务 jar **文件名**；仅有物理 jar、未登记 `apps.SDK` 时，引擎不会加载。
10. 如果要 Docker 运行，确认 `docker/config` 连接的是 compose 服务名，并执行 `docker compose config --quiet`。
11. **必须为每个新 App 模块添加单元测试**，测试文件结构与 `sie-iidp-demo-example` 保持一致：

```text
src/test/
├── java/<包路径>/<业务类名>Test.java
└── resources/
    ├── application.properties          ← spring.profiles.active=dev
    ├── application-dev.properties      ← 测试环境连接配置
    ├── dbcp.properties                 ← 测试数据库连接
    ├── logback-spring.xml              ← 日志配置
    └── <包路径>/<业务类名>Test.json    ← 数据驱动用例，与测试类同包同名
```

测试框架使用 DDTest（`@IIDPTest`、`@DDTest`、`@DDArgs`、`@DDExpected`），父 POM 已声明依赖和插件，子模块无需额外配置。详见 `skills/backend/references/core/testing.md`。

---

## 14. 交付前检查清单

- [ ] 已执行 `git --version`，确认 Git 环境可用；如不可用，已协助用户安装并复验。
- [ ] 已检查本地当前项目是否为目标仓库：当前目录在 Git 工作树内时已执行 `git remote -v`，当前目录下存在 `iidp-backend-demo-ai/.git` 时已执行 `git -C iidp-backend-demo-ai remote -v`。
- [ ] 已优先执行 `git clone http://192.168.175.55:9888/mijiuye/iidp-backend-demo-ai.git`，或在已有目录中确认 `git remote -v` 匹配该仓库。
- [ ] 如果未能从远端仓库克隆或拉取代码，已在交付说明中记录失败原因，并明确本次基于当前本地工作区内容整理。
- [ ] 根 POM 的三层聚合关系与本文档一致。
- [ ] `sie-iidp-demo-apps/pom.xml` 已登记当前全部源码业务模块：`sie-iidp-demo-xxljob-executor`，以及本次新增模块。
- [ ] 新增业务 jar 已进入 `iidp-backend-demo-ai/apps/modules/`（或与团队脚本同步后的等价路径）。
- [ ] `apps/apps.json` 的 `apps.SDK` 已登记新增业务 jar。
- [ ] `app.json.resolved` 与实际 Java 包路径一致。
- [ ] `app.json.view` 与真实视图文件一致。
- [ ] `app.json.data` 与真实种子文件一致。
- [ ] 菜单 `view` 中的每个 view key 都能在视图 JSON 中找到。
- [ ] Dockerfile 的 `APP_JAR` 指向真实存在的 `sie-iidp-demo-start` 启动 jar。
- [ ] Docker Compose 包含 `mysql`、`redis`、`minio`、`minio-init`、`iidp-app`。
- [ ] Docker 外置配置使用 `mysql`、`redis`、`minio` 服务名。
- [ ] `engine.model2ddl.mode=CREATE` 只用于本地首次建表；测试/生产改为团队约定值。
- [ ] 没有把真实 `apiToken`、数据库密码、Redis 密码、MinIO 密钥写入 skill 文档。
- [ ] 新增业务 App 模块已在 `src/test/` 下创建测试类、测试 JSON 数据及测试资源文件（`application.properties`、`application-dev.properties`、`dbcp.properties`、`logback-spring.xml`）。
- [ ] 测试类使用 `@IIDPTest` + `@DDTest`，覆盖所有 public 方法的正常路径和异常路径。
- [ ] 已执行 `mvn -s ./settings.xml test` 确认测试通过。
- [ ] 已执行 `git diff --check`。
- [ ] 已执行 `docker compose config --quiet`。
- [ ] 能访问私服并配置 JDK 时，已执行 `JAVA_HOME=$(/usr/libexec/java_home -v 1.8.0_451) mvn -s ./settings.xml -DskipTests clean package`。
