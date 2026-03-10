# MySQL Client

一个基于 JavaFX 的 MySQL 数据库桌面客户端工具。

## 功能特性

- 连接管理：支持新建、测试、断开数据库连接
- **保存连接**：支持保存多个连接配置，下次可直接选择
- **历史记录**：自动保存执行过的 SQL 语句，支持快速填充
- SQL 执行：支持 SELECT、INSERT、UPDATE、DELETE 等 SQL 语句
- 结果展示：表格形式动态展示查询结果
- **导出 CSV**：支持将查询结果导出为 CSV 文件
- **复制/导出选中行**：支持复制选中行数据或导出为 CSV
- 元数据浏览：左侧树形显示数据库和表结构
- **右键菜单**：右键点击表名可查看表结构、查询数据
- 双击表名：自动生成 SELECT 查询（默认 100 条）

## 技术栈

- Java 17
- JavaFX 17
- MySQL Connector/J 8.0.33
- Gson 2.10.1
- Maven

## 项目结构

```
java_mysql_connect/
├── pom.xml                          # Maven 配置
├── src/main/
│   ├── java/com/xsh/
│   │   ├── Main.java                # 入口类
│   │   ├── model/                   # 数据模型
│   │   │   ├── ConnectionInfo.java
│   │   │   └── QueryResult.java
│   │   ├── db/                      # 数据库操作
│   │   │   ├── DatabaseManager.java
│   │   │   └── QueryExecutor.java
│   │   ├── controller/              # 控制器
│   │   │   ├── MainController.java
│   │   │   └── ConnectionController.java
│   │   └── util/                    # 工具类
│   │       ├── ConnectionHistoryManager.java
│   │       └── CsvExporter.java
│   └── resources/
│       ├── view/                    # FXML 界面
│       │   ├── MainView.fxml
│       │   └── ConnectionDialog.fxml
│       └── style.css                # 样式文件
├── README.md
└── AGENTS.md                        # 开发指南
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+

### 构建运行

```bash
# 编译项目
mvn clean compile

# 运行项目
mvn javafx:run

# 打包为 JAR
mvn clean package
```

### 使用方法

1. 点击「连接」按钮打开连接对话框
2. 填写 MySQL 连接信息（Host、端口、用户名、密码、数据库）
3. 勾选「保存此连接」可保存配置供下次使用
4. 点击「测试连接」验证连接是否成功
5. 点击「连接」进入主界面
6. 在 SQL 编辑器中输入 SQL 语句
7. 点击「执行」或按 Ctrl+Enter 运行查询

### 右侧菜单操作

- **右键点击表名**：
  - 查看表结构：显示字段名、类型、键等信息
  - 查询全部数据：生成 SELECT * LIMIT 100
  - 查询前10条：生成 SELECT * LIMIT 10
- **右键点击结果表格**：
  - 复制选中行：复制到剪贴板
  - 导出选中行：导出为 CSV 文件

### 数据导出

- 菜单 `文件` -> `导出CSV` 或快捷键 `Ctrl+E`
- 弹出文件选择对话框，选择保存路径

## 快捷键

| 快捷键 | 功能 |
|--------|------|
| Ctrl+N | 新建连接 |
| Ctrl+Enter | 执行 SQL |
| Ctrl+E | 导出 CSV |
| Ctrl+Q | 退出应用 |

## 数据存储

连接配置和 SQL 历史记录保存在用户主目录：
- Windows: `C:\Users\用户名\.mysql_client\`
- Linux/Mac: `/home/用户名/.mysql_client/`

文件说明：
- `connections.json` - 保存的连接配置
- `sql_history.json` - SQL 执行历史

## 许可证

MIT License
