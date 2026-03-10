# AGENTS.md - 开发指南

本文档为 AI 代理和开发人员提供项目开发指南。

---

## 1. 构建与运行命令

### 基本命令

```bash
# 编译项目
mvn clean compile

# 运行应用程序
mvn javafx:run

# 打包为 JAR
mvn clean package

# 清理构建
mvn clean
```

### 测试命令

当前项目**未配置单元测试框架**，暂无测试命令。

如需添加测试，建议使用 JUnit 5：

```bash
# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=ClassNameTest

# 运行单个测试方法
mvn test -Dtest=ClassNameTest#methodName

# 生成测试报告
mvn test -DgenerateReports=true
```

### 代码检查

```bash
# 编译检查
mvn compile

# 完整验证（包含测试）
mvn verify
```

---

## 2. 项目技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Java 17 |
| GUI框架 | JavaFX 17 |
| 数据库驱动 | MySQL Connector/J 8.0.33 |
| JSON处理 | Gson 2.10.1 |
| 构建工具 | Maven 3.6+ |
| JavaFX插件 | javafx-maven-plugin 0.0.8 |

---

## 3. 代码风格指南

### 3.1 项目结构

```
src/main/java/com/xsh/
├── Main.java              # 应用程序入口
├── model/                 # 数据模型层
│   ├── ConnectionInfo.java
│   └── QueryResult.java
├── db/                    # 数据库操作层
│   ├── DatabaseManager.java
│   └── QueryExecutor.java
└── controller/             # 控制器层
    ├── MainController.java
    └── ConnectionController.java

src/main/resources/
├── view/                  # FXML 界面文件
│   ├── MainView.fxml
│   └── ConnectionDialog.fxml
└── style.css              # 样式文件
```

### 3.2 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 类名 | PascalCase | `DatabaseManager`, `ConnectionInfo` |
| 方法名 | camelCase | `connect()`, `executeQuery()` |
| 变量名 | camelCase | `connectionInfo`, `queryExecutor` |
| 常量 | UPPER_SNAKE_CASE | `MAX_CONNECTIONS` |
| 包名 | lowercase | `com.xsh.model` |
| FXML ID | camelCase | `sqlEditor`, `resultTable` |

### 3.3 Import 规范

- **导入顺序**（ideally）：
  1. Java 标准库 (`java.*`)
  2. 第三方库 (`javafx.*`, `com.mysql.*`)
  3. 项目内部包 (`com.xsh.*`)
- **避免使用通配符导入**：`import java.util.*` 不推荐
- **按字母排序**（Maven/IDE 自动处理）

```java
// 正确示例
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.xsh.model.ConnectionInfo;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
```

### 3.4 代码格式化

- **缩进**：4 空格（不使用 Tab）
- **行长度**：建议不超过 120 字符
- **空行**：类方法之间空一行，逻辑块之间可空行
- **大括号**：K&R 风格（行尾开，行尾闭）

```java
// 正确示例
public class DatabaseManager {
    
    public boolean connect(ConnectionInfo connectionInfo) {
        try {
            // code here
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public void disconnect() {
        // code here
    }
}
```

### 3.5 类型规范

| 类型 | 使用建议 |
|------|----------|
| 字符串 | 使用 `String`，避免 `StringBuffer`（单线程） |
| 集合 | 优先使用接口 `List<T>`, `Map<K,V>` |
| 日期时间 | 使用 `java.time` API (Java 8+) |
| Optional | 方法返回值可能为空时使用 `Optional<T>` |

```java
// 推荐
List<String> databases = new ArrayList<>();
Map<String, String> config = new HashMap<>();

// 不推荐
ArrayList<String> databases = new ArrayList<>();
```

### 3.6 错误处理规范

- **异常捕获**：优先捕获具体异常，避免 `catch (Exception e)`
- **资源关闭**：使用 try-with-resources
- **日志记录**：使用 `System.err` 或日志框架（建议添加 SLF4J）
- **用户提示**：GUI 异常应显示友好提示，不直接暴露堆栈

```java
// 推荐：try-with-resources
try (Connection conn = DriverManager.getConnection(url, user, pass);
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery(sql)) {
    // 使用资源
} catch (SQLException e) {
    System.err.println("数据库错误: " + e.getMessage());
    // 用户友好的错误提示
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setContentText("连接失败，请检查数据库配置");
    alert.showAndWait();
}

// 推荐：具体异常类型
} catch (ClassNotFoundException e) {
    // 处理驱动未找到
} catch (SQLException e) {
    // 处理 SQL 错误
}
```

### 3.7 FXML 规范

- 使用有意义的 fx:id 命名
- 事件处理方法使用 `@FXML` 注解
- 样式类名使用小写连字符

```xml
<!-- 正确示例 -->
<TextArea fx:id="sqlEditor" onAction="#executeQuery" styleClass="sql-editor"/>

<!-- Controller 中 -->
@FXML
private TextArea sqlEditor;

@FXML
public void executeQuery() {
    // handle event
}
```

### 3.8 JavaFX 特定规范

- **FXML 路径**：`/view/xxx.fxml`（从 resources 根目录开始）
- **Controller 注解**：所有 `@FXML` 注入的字段和方法必须是 `public` 或 `protected`
- **初始化**：`initialize()` 方法在 FXML 加载后自动调用
- **线程安全**：数据库操作应在后台线程执行，结果更新到 UI 线程

```java
@FXML
public void initialize() {
    // 初始化 UI 组件
    databaseManager = new DatabaseManager();
    updateConnectionStatus(false);
}
```

---

## 4. 提交规范

### Git 提交信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type 类型

| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | Bug 修复 |
| refactor | 代码重构 |
| docs | 文档更新 |
| style | 格式调整（不影响功能） |
| test | 测试相关 |
| chore | 构建/工具变动 |

### 示例

```bash
# 正确示例
git commit -m "feat(连接): 添加连接测试功能"
git commit -m "fix(UI): 修复 FXML padding 属性错误"
git commit -m "refactor(db): 提取 QueryExecutor 为独立类"

# 不推荐
git commit -m "更新代码"
git commit -m "fix bug"
```

---

## 5. 常用快捷操作

### 在 IDE 中运行

```bash
# 直接运行 Main 类（IDEA/Eclipse）
# VM options: --module-path "${env:JAVAFX_HOME}/lib" --add-modules javafx.controls,javafx.fxml
```

### 添加新依赖

在 `pom.xml` 的 `<dependencies>` 节点添加：

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>library-name</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 6. 注意事项

1. **JavaFX 模块**：运行需要 JavaFX 模块路径配置
2. **数据库连接**：确保 MySQL 服务正常运行
3. **编码**：所有源码文件使用 UTF-8 编码
4. **平台兼容性**：代码应兼容 Windows/Linux/macOS

---

> 本文件由 AI 生成，最后更新：2026-03-10
