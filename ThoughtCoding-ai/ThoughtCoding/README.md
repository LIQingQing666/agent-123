# ThoughtCoding CLI

ThoughtCoding CLI 是一个基于 Java 的交互式代码助手命令行工具，集成了 LangChain、OpenAI、MCP（Model Context Protocol）和终端 UI，旨在为开发者提供智能代码对话、工具调用、会话管理和项目感知能力。

## 🚀 主要特性

- 智能对话助手：支持基于模型的自然语言问答
- 流式输出：实时展示 AI 生成结果，提升交互体验
- MCP 支持：可连接 MCP 服务并动态发现工具
- 工具执行：支持文件管理、命令执行、代码执行、搜索等扩展工具
- 会话管理：支持会话保存、加载、继续与删除
- 配置驱动：基于 `config.yaml` 的自定义模型和工具配置
- 终端 UI：支持 ANSI 彩色、高亮与进度显示
- 项目感知：自动识别项目类型并加载上下文

## 📦 环境要求

- Java 17
- Maven 3.8+
- Windows / Linux / macOS

## 快速开始

### 1. 克隆代码

```bash
git clone <仓库地址>
cd ThoughtCoding
```

### 2. 构建项目

```bash
mvn clean package
```

### 3. 运行程序

```bash
java -jar target/thoughtcoding.jar
```

或者使用 Windows 启动脚本：

```powershell
bin\thought.bat
```

### 4. 编辑配置

项目根目录下的 `config.yaml` 用于模型、工具和 MCP 服务配置。你可以根据需要修改：

- 默认模型 `defaultModel`
- 模型服务地址和 API key
- 工具开关和超时
- MCP 服务列表和参数

## 🧠 常用命令

由于项目基于 Picocli，命令行支持多种参数：

```bash
java -jar target/thoughtcoding.jar -i
java -jar target/thoughtcoding.jar -p "修复项目中的空指针异常"
java -jar target/thoughtcoding.jar -c
java -jar target/thoughtcoding.jar -S latest-session
```

支持的子命令包括：

- `session`：会话管理命令
- `config`：配置查看和更新命令
- `mcp`：MCP 服务和工具管理命令

## 📁 项目结构

```
ThoughtCoding/
├── src/main/java/com/thoughtcoding/         # Java 源码
│   ├── cli/                                # 命令行命令实现
│   ├── core/                               # 核心引擎与上下文管理
│   ├── service/                            # AI 服务与会话服务
│   ├── tools/                              # 各类扩展工具实现
│   ├── mcp/                                # MCP 协议与工具适配
│   ├── ui/                                 # 终端 UI 渲染与交互组件
│   ├── config/                             # 配置加载与管理
│   ├── model/                              # 通用数据模型
│   └── util/                               # 工具类与辅助函数
├── bin/                                    # 启动脚本
├── sessions/                               # 会话持久化存储
├── config.yaml                             # 默认配置文件
├── pom.xml                                 # Maven 构建配置
└── README.md                               # 项目说明
```

## 🔧 主要模块说明

### `ThoughtCodingCLI.java`
程序入口，创建应用上下文并注册 Picocli 命令。

### `cli/`
包含 CLI 命令和参数解析逻辑，例如交互模式、会话命令、配置命令、MCP 命令等。

### `core/`
核心运行引擎和上下文管理，包括会话上下文、工具调用、流式输出和项目感知逻辑。

### `service/`
AI 服务、会话管理、上下文管理和性能监控实现。

### `tools/`
工具注册与调用实现，支持命令执行、文件管理、代码执行、搜索等功能。

### `mcp/`
MCP 协议适配层，管理 MCP 客户端、服务、工具发现与请求/响应模型。

### `ui/`
终端界面和交互组件，包括 ANSI 颜色、输入处理、进度显示等。

### `config/`
支持 YAML 配置解析与动态加载，实现模型、工具、MCP、会话和 UI 配置。

## 🧪 测试

```bash
mvn test
```

## 贡献指南

欢迎提交 Issue 和 PR：

1. Fork 仓库
2. 新建分支：`feature/xxx` 或 `fix/xxx`
3. 提交代码并创建 PR
4. 保持描述清晰，优先包含复现步骤与测试结果

## ⭐ 备注

- 项目当前使用 Java 17 和 Picocli 作为 CLI 框架
- 主要依赖包括 LangChain4j、OpenAI 连接器、JLine、Jackson、OkHttp 和 SLF4J
- 通过 `config.yaml` 可以灵活切换模型和启用/禁用 MCP 服务

### `src/main/java/com/thoughtcoding/core/` - 核心功能

**功能**: 提供核心业务逻辑

`ThoughtCodingContext.java`

- **功能**：应用上下文容器（依赖注入）
- **特性**：统一管理所有服务组件，提供全局访问入口

`AgentLoop.java`

- **功能**：Agent 循环实现类
- **特性**：基于 LangChain4j 实现智能对话，支持工具调用和选项管理

`MessageHandler.java`

- **功能**：消息处理器
- **特性**：处理流式输出，实时显示 AI 响应

`StreamingOutput.java`

- **功能**：流式输出处理类
- **特性**：Token-by-Token 实时输出，优化用户体验

`ProjectContext.java`

- **功能**：项目上下文检测
- **特性**：自动识别项目类型（Maven/Gradle/NPM等），提供项目相关信息

`OptionManager.java`

- **功能**：选项管理器
- **特性**：从 AI 响应中提取多选项，支持用户选择（1/2/3）

`ToolExecutionConfirmation.java`

- **功能**：工具执行确认
- **特性**：在执行工具前进行用户确认，提高安全性

`DirectCommandExecutor.java`

- **功能**：直接命令执行器
- **特性**：支持直接执行系统命令，无需通过工具调用

### `src/main/java/com/thoughtcoding/service/` - 服务层

**功能**: 业务逻辑和服务实现

**主要服务**:

- `LangChainService.java` - AI 服务核心实现
  - **特性**：集成 LangChain4j，支持流式响应和工具调用
- `SessionService.java` - 会话数据管理
  - **特性**：会话持久化、加载、自动保存
- `AIService.java` - AI 服务接口
  - **特性**：定义统一的 AI 服务接口，支持多模型切换
- `ContextManager.java` - 上下文管理器
  - **特性**：管理对话历史窗口，控制 Token 使用，实现滑动窗口策略
- `PerformanceMonitor.java` - 性能监控
  - **特性**：Token 使用统计、执行时间监控、性能指标收集

### `src/main/java/com/thoughtcoding/tools/` - 工具集合

**功能**: 各种功能工具的实现

`ToolProvider.java`

- **功能**：工具提供接口

`ToolRegistry.java`

- **功能**：工具注册中心

**主要工具**:

- **文件管理工具**: 文件读写、目录操作 (`FileManagerTool.java`)
- **命令执行工具**: 执行系统命令 (`CommandExecutorTool.java`)
- **代码执行工具**: 执行代码片段 (`CodeExecutorTool.java`)
- **搜索工具**: 文件内容搜索 (`GrepSearchTool.java`)
- **扩展性**: 容易添加新工具，基于 `BaseTool` 基类
- **工具提供者**: `ToolProvider.java` 定义工具提供接口，支持动态注册

### `src/main/java/com/thoughtcoding/mcp/` - MCP 功能

**功能**: 实现 Model Context Protocol 客户端功能，连接和管理外部 MCP 服务器

`MCPService.java` - MCP 服务管理器

- **功能**: MCP 服务的核心管理器
- **特性**: 管理多个 MCP 服务器连接，统一工具注册

`MCPClient.java` - MCP 客户端

- **功能**: 单个 MCP 服务器的客户端实现
- **特性**: JSON-RPC 通信，进程管理，错误处理

`MCPToolManager.java` - MCP 工具管理器

- **功能**: 管理所有 MCP 工具的统一入口
- **特性**: 工具发现、注册、调用路由

`MCPToolAdapter.java` - MCP 工具适配器

- **功能**: 将 MCP 工具适配为内部 BaseTool 格式
- **特性**: 统一工具接口，隐藏 MCP 通信细节

**`mcp/model/`** - MCP 协议数据模型

**功能**: 定义 MCP 协议的数据结构和类型（位于 `mcp` 包下的子包）

- `MCPRequest.java` - MCP 请求模型
- `MCPResponse.java` - MCP 响应模型
- `MCPError.java` - MCP 错误模型
- `MCPTool.java` - MCP 工具定义
- `InputSchema.java` - 输入模式定义

**注意**: `mcp/model/` 是 MCP 协议专用的数据模型，与独立的 `model/` 包（通用数据模型）不同。

### `src/main/java/com/thoughtcoding/ui/` - 用户界面

**功能**: 终端用户界面管理

**主要组件**:

`ThoughtCodingUI.java`

- **功能**：UI 主类

`TerminalManager.java`

- **功能**：终端管理器

`AnsiColors.java`

- **功能**：ANSI 颜色工具类

**`component/`**

- **`ChatRenderer.java`**：聊天渲染器
  - **特性**：实时渲染 AI 响应，支持代码高亮
- **`InputHandler.java`**：输入处理器
  - **特性**：处理用户输入，支持命令补全和历史记录
- **`ProgressIndicator.java`**：进度指示器
  - **特性**：显示任务执行进度，提供视觉反馈
- **`ToolDisplay.java`**：工具显示类
  - **特性**：格式化显示工具调用和执行结果
- **`StatusBar.java`**：状态栏类
  - **特性**：显示当前状态信息（模型、会话、Token 使用等）

**`themes/`**

- **`ColorScheme.java`**：颜色方案类
  - **特性**：定义终端颜色主题，支持自定义配色

## ⚙ 配置说明

`config.yaml` 是项目的核心配置文件，用于定义模型、工具、会话、UI 和 MCP 服务。当前项目默认配置位于根目录下的 `config.yaml`。

### 关键配置项

- `models`：AI 模型列表
  - `name`：模型标识
  - `baseURL`：模型服务地址
  - `apiKey`：访问密钥
  - `streaming`：是否启用流式输出
  - `maxTokens`：最大 Token 限制
  - `temperature`：生成随机性

- `defaultModel`：默认模型名称

- `tools`：工具模块配置
  - `fileManager`：文件读写管理
  - `commandExec`：命令执行工具
  - `codeExecutor`：代码执行工具
  - `search`：文本搜索工具

- `session`：会话管理配置
  - `autoSave`：是否自动保存会话
  - `maxSessions`：最大会话数
  - `sessionTimeout`：会话超时时间（毫秒）

- `ui`：终端显示配置
  - `theme`、`showTimestamps`、`colorfulOutput`、`progressAnimation`

- `performance`：性能监控配置
  - `enableMonitoring`、`logLevel`、`cacheSize`

- `mcp`：MCP 服务配置
  - `enabled`：是否启用 MCP
  - `autoDiscover`：是否自动发现 MCP 工具
  - `connectionTimeout`：连接超时
  - `servers`：MCP 服务列表及启动参数

## 🛠️ 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- Node.js 16+
- 2GB 以上可用内存

### 克隆仓库

```bash
git clone <仓库地址>
cd ThoughtCoding
```

### 构建项目

```bash
mvn clean package
```

### 运行应用

```bash
java -jar target/thoughtcoding.jar
```

或使用启动脚本：

```bash
./bin/thought
```

Windows:

```powershell
.\bin\thought.bat
```

## 💡 示例命令

- 交互模式：`./bin/thought`
- 单次提问：`./bin/thought -p "帮我写一个Java类"`
- 继续会话：`./bin/thought -c`
- 指定会话：`./bin/thought -S <session-id>`
- 查看帮助：`./bin/thought help`

## 📌 常用 MCP 命令

- `/mcp list`：查看已连接工具
- `/mcp predefined`：列出预定义工具
- `/mcp tools filesystem,sqlite,github`：快速连接预定义工具
- `/mcp disconnect filesystem`：断开工具
- `/mcp connect filesystem npx @modelcontextprotocol/server-filesystem`：动态连接工具

## 🧪 测试

```bash
mvn test
```

## 🤝 贡献

欢迎提交 Issue 和 PR，请参考 `CONTRIBUTING.md`。

## 许可证

本项目采用 MIT 许可证，详见 `LICENSE`。

**ThoughtCoding CLI** - 让 AI 编程助手更智能、更易用。 🚀

