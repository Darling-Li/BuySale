# 稻谷、稻种、化肥进销存系统

这是一个前后端分离的进销存项目骨架：

- 后端：Java 21、Spring Boot、MyBatis、Spring Security、Flyway、MySQL
- 前端：Vue 3、Vite、Vue Router、Pinia、Axios、ECharts
- 业务：采购登记、采购入库、销售出库、结算状态、库存汇总、月度同比/环比图表

## 目录

```text
backend/      Spring Boot API
frontend/     Vue 3 管理台
database/     数据库辅助说明
```

## 启动 MySQL

可以使用本机 MySQL，也可以用项目里的 Docker Compose：

```bash
docker compose up -d mysql
```

默认库名和账号：

```text
database: rice_trade
username: rice_user
password: rice_pass
```

## 启动后端

```bash
cd backend
mvn spring-boot:run
```

如果使用自己的 MySQL，可以覆盖环境变量：

```bash
DB_URL='jdbc:mysql://localhost:3306/rice_trade?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true' \
DB_USERNAME=root \
DB_PASSWORD=123456 \
mvn spring-boot:run
```

后端地址：`http://localhost:8848`

在 IDEA 中打开项目根目录后，Maven 面板应能直接识别 `backend/pom.xml`。

## 登录角色

默认使用 HTTP Basic 登录，前端登录页会保存本地登录态：

```text
管理员：admin / admin123
普通角色：user / user123
```

权限规则：

- 管理员：可以新增采购、自动入库、销售出库、更新结算、维护仓库，并自动写入操作日志。
- 普通角色：只能查询看板、采购、销售、库存、仓库等数据，不能做新增、修改、删除。

可以用环境变量改默认账号：

```bash
ADMIN_USERNAME=admin ADMIN_PASSWORD=your_admin_password \
VIEWER_USERNAME=user VIEWER_PASSWORD=your_user_password \
mvn spring-boot:run
```

## 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：`http://localhost:5173`

## 核心接口

- `GET /api/reference/product-types` 商品类型
- `GET /api/auth/me` 当前登录用户
- `GET /api/warehouses` 仓库列表
- `POST /api/warehouses` 新建仓库
- `GET /api/purchases` 采购记录
- `POST /api/purchases` 新增采购并自动入库
- `GET /api/sales` 销售记录
- `POST /api/sales` 新增销售并自动出库
- `PATCH /api/sales/{id}/settlement` 更新结算状态
- `GET /api/inventory` 库存汇总
- `GET /api/dashboard/monthly-trend?year=2026` 月度同比/环比
- `GET /api/audit-logs` 管理员操作日志
