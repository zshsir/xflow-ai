# 四象限待办 (XFlow Todo)

一个基于四象限法则的待办事项管理系统，帮助你高效管理每天的任务。

## ✨ 功能特性

### 📋 四象限分类
- **重要且紧急**：立即处理的关键任务
- **重要不紧急**：需要计划安排的重要事项
- **紧急不重要**：可以委托或快速处理的杂事
- **不紧急不重要**：可考虑删除的低优先级事项

### 🎯 任务管理
- 拖拽任务在四象限间移动
- 当天完成的任务变灰显示在象限底部，**点击"已完成"标题可折叠/展开**该区域，给未完成项腾出更多空间
- 支持设置**计划完成时间**，逾期未完成的任务标红提示
- 未完成的任务**无视 `dueDate` 始终可见**，跨日不会被自动归档/隐藏

### 📅 历史记录
- 点击"查看已完成"按钮可查看**所有历史已完成**任务
- 按完成时间倒序展示
- 已完成的任务按**完成的那一天**（`completedAt`）归档，而不是原始 `dueDate`；例如 6/8 创建、6/9 勾选完成的会出现在 6/9 的日视图

### 📊 数据导出
- 按月导出所有待办为 Excel 文件
- 包含日期、象限、内容、计划完成时间、状态、完成时间等字段

### 🖥️ 悬浮窗模式
- 一键切换为悬浮窗模式，方便在专注工作时随时查看待办
- 悬浮窗可拖拽、可关闭

### 👥 人员管理（管理员）
- 用户注册由管理员在后台创建
- 支持新增、编辑、删除用户
- 支持重置用户密码
- 不能删除当前登录用户

### 🔐 安全认证
- 基于 JWT 的用户认证
- BCrypt 密码加密
- Spring Security 安全框架

### 📱 响应式设计
- 完美适配桌面端和移动端
- 移动端侧边栏抽屉式菜单
- 所有弹窗自适应屏幕宽度

## 🛠️ 技术栈

### 前端
- **Vue 3** + Composition API + `<script setup>`
- **TypeScript** 类型安全
- **Pinia** 状态管理
- **Vue Router 4** 路由
- **Element Plus** UI 组件库
- **Vue Draggable Plus** 拖拽功能
- **Axios** HTTP 客户端
- **Vite** 构建工具
- **date-fns** 日期处理

### 后端
- **Spring Boot 3.5** Java 17
- **Spring Data JPA** ORM
- **Spring Security** 安全框架
- **JWT (jjwt 0.12)** 令牌认证
- **MySQL** 数据库
- **Apache POI** Excel 导出
- **Lombok** 简化代码
- **Maven** 项目管理

## 📂 项目结构

```
xflow-ai/
├── my-admin-frontend/        # 前端项目
│   ├── src/
│   │   ├── api/             # API 调用
│   │   ├── components/      # 公共组件
│   │   │   ├── layout/      # 布局组件
│   │   │   └── todo/        # 待办组件
│   │   ├── stores/          # Pinia stores
│   │   ├── types/           # TypeScript 类型
│   │   ├── utils/           # 工具函数
│   │   └── views/           # 页面
│   ├── .env.example         # 环境变量示例
│   └── package.json
│
└── my-admin-backend/         # 后端项目
    ├── src/main/java/com/example/my_admin_backend/
    │   ├── config/          # 配置类
    │   ├── controller/      # 控制器
    │   ├── dto/             # 数据传输对象
    │   ├── entity/          # 实体类
    │   ├── repository/      # 数据访问层
    │   ├── security/        # 安全相关
    │   └── service/         # 业务逻辑层
    ├── src/main/resources/
    │   └── application.properties.example  # 配置示例
    └── pom.xml
```

## 🚀 快速开始

### 环境要求
- **Node.js** 18+
- **JDK** 17+
- **Maven** 3.8+
- **MySQL** 8.0+

### 1. 克隆项目
```bash
git clone https://github.com/your-username/xflow-ai.git
cd xflow-ai
```

### 2. 数据库准备
```sql
CREATE DATABASE my_xflow_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 后端启动

```bash
cd my-admin-backend

# 复制配置文件
cp src/main/resources/application.properties.example src/main/resources/application.properties

# 修改 application.properties 中的数据库和 JWT 配置
# spring.datasource.url=
# spring.datasource.username=
# spring.datasource.password=
# jwt.secret=

# 启动
./mvnw spring-boot:run
```

后端启动时会自动：
- 创建数据库表（`users`、`todos`）
- 创建默认管理员账户（用户名 `admin` / 密码 `admin123`）

> ⚠️ **首次登录后请立即修改默认密码！**

### 4. 前端启动

```bash
cd my-admin-frontend

# 安装依赖
npm install

# 复制环境变量
cp .env.example .env

# 启动开发服务器
npm run dev
```

访问 http://localhost:5173

> 💡 启动后端（默认 8080 端口）后，前端通过 `vite.config.ts` 中配置的代理将 `/api/*` 自动转发到 `http://localhost:8080`，本地开发无需手动配 CORS。生产环境走 `nginx.conf` 的反向代理。

### 5. 默认登录

| 字段 | 值 |
|------|------|
| 用户名 | `admin` |
| 密码 | `admin123` |

登录后请进入"人员管理"创建新用户并修改默认密码。

## 📝 配置文件说明

### 后端 `application.properties`

| 配置项 | 说明 |
|--------|------|
| `spring.datasource.url` | MySQL 数据库连接地址 |
| `spring.datasource.username` | 数据库用户名 |
| `spring.datasource.password` | 数据库密码 |
| `jwt.secret` | JWT 签名密钥（至少 32 字符） |
| `jwt.expiration` | Token 过期时间（毫秒） |
| `server.port` | 后端服务端口 |

### 前端 `.env`

| 配置项 | 说明 |
|--------|------|
| `VITE_API_BASE_URL` | 后端 API 地址 |

## 🔌 API 端点

### 认证
- `POST /api/auth/login` - 登录
- `POST /api/auth/register` - 注册（默认禁用，仅供管理员使用）

### 待办
- `GET /api/todos?date=YYYY-MM-DD` - 获取指定日期待办（返回：所有未完成 + 当天已完成）
- `POST /api/todos` - 创建待办
- `PUT /api/todos/{id}` - 更新待办
- `DELETE /api/todos/{id}` - 删除待办
- `PATCH /api/todos/{id}/complete` - 标记完成
- `PATCH /api/todos/{id}/uncomplete` - 取消完成
- `PATCH /api/todos/{id}/quadrant` - 移动到其他象限
- `GET /api/todos/completed` - 获取所有已完成待办
- `GET /api/todos/export?month=YYYY-MM` - 导出当月待办

### 用户管理
- `GET /api/users` - 获取所有用户
- `GET /api/users/{id}` - 获取单个用户
- `POST /api/users` - 创建用户
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户
- `PATCH /api/users/{id}/password` - 重置密码

## 🖼️ 界面预览

### 主页面
- 2x2 网格四象限布局
- 顶部工具栏：日期选择、快捷导航、新建/导出/查看已完成
- 侧边栏：菜单导航

### 新建待办
- 点击象限标题直接在该象限创建
- 支持设置计划完成时间
- 自动识别逾期任务

### 移动端
- 侧边栏抽屉式菜单
- 工具栏垂直堆叠
- 弹窗自适应屏幕
- 触摸友好的交互

## 🛡️ 安全提示

在部署到生产环境前，请务必：
1. 修改默认管理员密码
2. 修改 JWT 密钥（使用强随机字符串）
3. 配置 HTTPS
4. 限制 CORS 允许的域名
5. 不要将 `application.properties` 和 `.env` 提交到版本控制
6. 定期备份数据库

## 📄 开源协议

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📮 联系方式

- GitHub Issues: [提交问题](https://github.com/your-username/xflow-ai/issues)
- Email: your-email@example.com

---

⭐ 如果这个项目对你有帮助，请给个 Star！