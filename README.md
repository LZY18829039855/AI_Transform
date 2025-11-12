# AI Transform Backend

基于 Spring Boot + MyBatis + MySQL 5.7 的后端服务框架

## 技术栈

- **Java**: 1.8
- **Spring Boot**: 2.7.18
- **MyBatis**: 2.3.1
- **MySQL**: 5.7
- **Maven**: 项目构建工具

## 项目结构

```
ai-transform/
├── pom.xml                          # Maven配置文件
├── README.md                        # 项目说明文档
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/huawei/aitransform/
│   │   │       ├── Application.java              # 主启动类
│   │   │       ├── config/                       # 配置类
│   │   │       │   └── MyBatisConfig.java
│   │   │       ├── controller/                   # 控制器层（待开发）
│   │   │       ├── service/                      # 服务层（待开发）
│   │   │       ├── mapper/                       # Mapper接口层（待开发）
│   │   │       ├── entity/                       # 实体类（待开发）
│   │   │       └── common/                       # 公共类（待开发）
│   │   └── resources/
│   │       ├── application.yml                   # 应用配置文件
│   │       ├── mapper/                           # MyBatis XML映射文件（待开发）
│   │       └── db/                               # 数据库脚本
│   │           └── schema.sql
│   └── test/                                     # 测试代码
```

## 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+

### 2. 数据库配置

1. 创建数据库并执行初始化脚本：
```sql
-- 执行 src/main/resources/db/schema.sql
```

2. 修改 `src/main/resources/application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_transform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

### 3. 运行项目

#### 方式一：使用 Maven 命令
```bash
mvn spring-boot:run
```

#### 方式二：打包后运行
```bash
mvn clean package
java -jar target/ai-transform-1.0.0.jar
```

#### 方式三：IDE 运行
直接运行 `Application.java` 的 main 方法

### 4. 验证启动

服务启动后，访问地址：`http://localhost:8080/api`

如果看到404或空响应，说明框架已成功启动，可以开始开发业务逻辑。

## 开发规范

1. **代码规范**:
   - if、for、do、while 等语句的执行体必须加大括号 `{}`
   - 对象和数组定义时使用拖尾逗号

2. **包结构说明**:
   - `controller`: 处理HTTP请求，调用Service层
   - `service`: 业务逻辑处理，调用Mapper层
   - `mapper`: MyBatis接口定义
   - `entity`: 实体类，对应数据库表
   - `config`: 配置类
   - `common`: 公共工具类

3. **MyBatis使用**:
   - Mapper接口放在 `mapper` 包下
   - XML映射文件放在 `resources/mapper` 目录下
   - XML文件命名与Mapper接口名称一致

## 注意事项

1. 确保MySQL服务已启动
2. 数据库连接信息需要根据实际情况修改
3. 首次运行前需要执行数据库初始化脚本
4. 日志级别可在 `application.yml` 中配置

## 后续开发建议

1. 根据业务需求创建实体类（entity包）
2. 创建Mapper接口和XML映射文件
3. 实现Service层业务逻辑
4. 创建Controller层处理HTTP请求
5. 添加统一响应格式和异常处理
6. 添加参数校验（使用 `@Valid` 注解）
7. 添加分页功能
8. 添加单元测试
9. 添加API文档（Swagger/OpenAPI）

