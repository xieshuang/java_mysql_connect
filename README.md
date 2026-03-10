# MySQL Client

一个基于 JavaFX 的 MySQL 数据库桌面客户端工具。

## 功能特性

- 连接管理：支持新建、测试、断开数据库连接
- SQL 执行：支持 SELECT、INSERT、UPDATE、DELETE 等 SQL 语句
- 结果展示：表格形式动态展示查询结果
- 元数据浏览：左侧树形显示数据库和表结构
- 双击表名：自动生成 SELECT 查询

## 技术栈

- Java 17
- JavaFX 17
- MySQL Connector/J 8.0.33
- Maven

## 项目结构

```
mysql_connector/
├── pom.xml                          # Maven 配置
├── src/main/
│   ├── java/com/mysqlclient/
│   │   ├── Main.java                # 入口类
│   │   ├── model/                   # 数据模型
│   │   ├── db/                      # 数据库操作
│   │   └── controller/              # 控制器
│   └── resources/
│       ├── view/                    # FXML 界面
│       └── style.css                # 样式文件
└── README.md
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
```

### 使用方法

1. 点击「连接」按钮打开连接对话框
2. 填写 MySQL 连接信息（Host、端口、用户名、密码）
3. 点击「测试连接」验证连接是否成功
4. 点击「连接」进入主界面
5. 在 SQL 编辑器中输入 SQL 语句
6. 点击「执行」或按 Ctrl+Enter 运行查询

## 快捷键

| 快捷键 | 功能 |
|--------|------|
| Ctrl+N | 新建连接 |
| Ctrl+Enter | 执行 SQL |
| Ctrl+Q | 退出应用 |

## 许可证

MIT License
