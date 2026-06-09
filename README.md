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

可以使用本机 MySQL，也可以用项目里的 Docker Compose。DDoS 防护需要 Redis：

```bash
docker compose up -d mysql redis
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

默认使用 HTTP Basic 登录，账号和角色由数据库表 `system_users`、`system_roles`、`system_user_roles` 维护。Flyway 会初始化两个默认账号：

```text
管理员：admin / admin123
普通角色：user / user123
```

权限规则：

- 管理员：可以新增采购、自动入库、销售出库、更新结算、维护仓库，并自动写入操作日志。
- 普通角色：只能查询看板、采购、销售、库存、仓库等数据，不能做新增、修改、删除。

管理员可以通过系统用户接口维护账号和角色；生产环境上线后建议立即修改默认密码。

## 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：`http://localhost:5173`

## 生产安全配置

### Redis 布隆过滤器防护

后端内置 Redis 位图实现的布隆过滤器防护，默认关闭。开启后会按客户端 IP 做窗口计数，超过阈值后写入布隆过滤器，后续请求会快速返回 `429`：

```bash
APP_DDOS_ENABLED=true
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
APP_DDOS_WINDOW_SECONDS=60
APP_DDOS_MAX_REQUESTS_PER_WINDOW=300
APP_DDOS_BLOOM_SIZE=8388608
APP_DDOS_HASH_COUNT=7
```

如果误封，需要清空布隆过滤器 key：

```bash
redis-cli DEL rice-trade:ddos:blocked-ip:bloom
```

这个防护只在请求到达后端后生效，生产环境仍建议搭配阿里云 Anti-DDoS、WAF、CDN、SLB 限流和安全组。

### 登录 Token 缓存

登录成功后，后端会把当前用户 token 缓存在 Redis 中，默认有效期 30 分钟。每次访问 `/api/**` 时后端都会校验 token 是否仍在 Redis 中，不存在则返回 `401`，前端会清理登录态并跳回登录页。

```bash
APP_TOKEN_SESSION_ENABLED=true
APP_TOKEN_SESSION_TTL_SECONDS=1800
```

如果 Redis 中的 token 过期，需要重新登录。该机制依赖 Redis，请确保生产环境 Redis 可用。

### IP / 地区限制

后端内置了可配置访问过滤器，默认关闭，生产环境可通过环境变量启用：

```bash
APP_ACCESS_ENABLED=true \
APP_ACCESS_ALLOWED_CIDRS='203.0.113.0/24,198.51.100.10' \
APP_ACCESS_ALLOWED_REGIONS='浙江省,上海市' \
APP_ACCESS_TRUSTED_PROXY_CIDRS='10.0.0.0/8,172.16.0.0/12' \
APP_ACCESS_REGION_HEADERS='X-Client-Province,X-Client-Region' \
mvn spring-boot:run
```

`APP_ACCESS_ALLOWED_CIDRS` 用于限制来源 IP 段，`APP_ACCESS_ALLOWED_REGIONS` 用于限制地区。地区判断依赖阿里云 CDN/WAF/负载均衡等可信代理写入的请求头，因此需要同时把 `APP_ACCESS_TRUSTED_PROXY_CIDRS` 配成这些代理的内网或出口 IP 段，并在云侧安全组中禁止用户绕过代理直连后端。

### 前后端报文加密

后端和前端使用同一个共享密钥派生 AES-GCM 密钥，当前默认关闭；只有显式开启时才会加密请求和响应。生产环境如需启用，必须替换默认密钥：

```bash
APP_CRYPTO_ENABLED=true \
APP_CRYPTO_SHARED_KEY='replace-with-a-long-random-secret' \
APP_CRYPTO_REQUIRE_ENCRYPTED_REQUESTS=true \
mvn spring-boot:run
```

前端生产构建时也要显式开启，并使用同一个密钥：

```bash
VITE_CRYPTO_ENABLED=true
VITE_CRYPTO_SHARED_KEY=replace-with-a-long-random-secret
VITE_CONSOLE_GUARD=true
```

前端会加密 `POST/PUT/PATCH` 请求体，后端会加密 JSON 响应。这个机制不能替代 HTTPS，生产环境仍必须使用 HTTPS，并定期更换共享密钥。

### 管理员设备限制

如果只允许指定电脑登录管理员账号，可以开启管理员设备码校验。先在这台电脑上约定一个足够长的设备码，然后生成 SHA-256：

```bash
printf 'your-admin-device-token' | shasum -a 256
```

后端配置设备码哈希：

```bash
APP_ADMIN_DEVICE_ENABLED=true
APP_ADMIN_DEVICE_TOKEN_HASHES=上一步输出的64位哈希
```

前端登录页填写“管理员设备码”后会保存到当前浏览器，并在后续请求中自动发送 `X-Admin-Device-Token`。普通用户不校验设备码；管理员账号在其他电脑缺少正确设备码时会被拒绝。

### 控制台防护

生产构建默认会阻断常见右键、快捷键和已打开调试窗口的场景。浏览器控制台无法从技术上被前端代码绝对禁止，因此敏感数据和权限判断不能依赖控制台防护，必须依赖后端鉴权、IP/地区限制和报文加密。

## 核心接口

- `GET /api/reference/product-types` 商品类型
- `GET /api/auth/me` 当前登录用户
- `GET /api/system/roles` 管理员查询角色
- `POST /api/system/roles` 管理员新增角色
- `PUT /api/system/roles/{id}` 管理员修改角色
- `GET /api/system/users` 管理员查询用户
- `POST /api/system/users` 管理员新增用户
- `PUT /api/system/users/{id}` 管理员修改用户、密码和角色
- `GET /api/warehouses` 仓库列表
- `POST /api/warehouses` 新建仓库
- `GET /api/purchases` 采购记录
- `POST /api/purchases` 新增采购并自动入库
- `GET /api/sales` 销售记录
- `POST /api/sales` 新增销售并自动出库
- `POST /api/sales/{id}/settlements` 新增销售结账记录
- `GET /api/inventory` 库存汇总
- `GET /api/dashboard/monthly-trend?year=2026` 月度同比/环比
- `GET /api/audit-logs` 管理员操作日志
