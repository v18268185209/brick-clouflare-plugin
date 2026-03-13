# brick-cloudflare-plugin

基于 `Spring Boot 3.5 + JDK17 + Brick Bootkit` 的 Cloudflare 可视化管理插件。  
插件采用与 `eqadmin/eq-admin-business` 一致的插件打包方式（`pluginInfo + repackage`），并按共享模式运行。

## 功能范围
- Cloudflare 账号管理
  - 多账号维护
  - API Token / Global Key 双模式鉴权
  - 凭证加密存储（AES-GCM）
  - 连接验证、Zone 查询
- DNS 管理
  - 记录列表/新增/更新/删除
- R2 管理
  - 存储桶列表/创建/删除
- 防火墙管理
  - 规则列表/创建/更新/删除
  - 规则启用/暂停
- 缓存管理
  - 缓存设置读取与更新
  - Purge Cache
- SSL/TLS 管理
  - SSL 模式、Always HTTPS、TLS 最低版本、TLS1.3
- Workers 管理
  - Script 列表/读取/上传/删除
  - Route 列表/创建/删除
- KV 管理
  - Namespace 列表/创建/删除
  - Key 列表、Value 读写删
- 扩展功能
  - Page Rules 管理
  - Page Rules 启用/禁用
  - Analytics Dashboard 查询
  - 操作审计日志
  - 管理总览面板
  - 多账号/Zone 选择持久化（浏览器本地存储）
  - Cloudflare API 瞬时失败重试（429/5xx/网络抖动）
  - Cloudflare JSON 响应 `success=false` 自动识别为失败并统一返回错误

## 技术栈
- 后端：Spring Boot 3.5.5、MyBatis-Plus、SQLite、Brick Bootkit Plugin
- 前端：Vite + Vue3 + Pinia + Element Plus（按需加载）+ ECharts
- JDK：17

## 工程结构
```text
brick-cloudflare-plugin/
├─ pom.xml
├─ src/main/java/.../cloudflare
│  ├─ CloudflarePluginApplication.java
│  ├─ controller/             # 各业务模块 REST API
│  ├─ service/                # 账号、加密、Cloudflare 网关、日志
│  ├─ domain/entity/          # SQLite 实体
│  └─ mapper/                 # MyBatis-Plus Mapper
├─ src/main/resources
│  ├─ cloudflare-application.yaml
│  ├─ db/schema.sql
│  └─ static/console/         # Vite 构建产物
└─ ui/                        # 前端源码（Vite + Vue）
```

## 前端开发与构建
在 `ui` 目录下：

```bash
npm install
npm run dev
npm run build
```

构建产物默认输出到：
`src/main/resources/static/console`

插件静态访问路径（默认）：
`/plugins/eqadminPluginsCloudflare/console/index.html`

## 后端接口前缀
默认通过 Brick Bootkit 的插件前缀挂载：

`/plugins/{pluginId}/cloudflare/**`

本项目 `pluginId` 默认：
`eqadminPluginsCloudflare`

示例：
- `/plugins/eqadminPluginsCloudflare/cloudflare/accounts`
- `/plugins/eqadminPluginsCloudflare/cloudflare/dns/records`

## 数据库
- 使用 SQLite
- 默认文件：
  `${user.home}/.eqadmin/cloudflare-plugin/cloudflare.db`
- 初始化脚本：
  `src/main/resources/db/schema.sql`

## 配置说明
`src/main/resources/cloudflare-application.yaml`

关键配置：
- `cloudflare.plugin.api-base-url`
- `cloudflare.plugin.encryption-key`
- `cloudflare.plugin.sqlite-path`
- `cloudflare.plugin.max-retries`
- `cloudflare.plugin.retry-backoff-ms`
- `plugin.resources.static-locations`

建议通过环境变量覆盖密钥：
`CLOUDFLARE_PLUGIN_SECRET`

## 与 EqAdmin 集成方式
参考 `eq-admin-business` 的插件打包配置，当前插件已在 `pom.xml` 中配置：
- `spring-boot3-brick-bootkit-maven-packager`
- `pluginInfo.id=eqadminPluginsCloudflare`
- `pluginInfo.bootstrapClass=cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.CloudflarePluginApplication`
- `pluginInfo.configFileName=cloudflare-application.yaml`

主应用（EqAdmin）示例配置：

```yaml
plugin:
  enable: true
  runMode: dev
  mainPackage: cn.net.rjnetwork.qixiaozhu.QixiaozhuApplication
  sortInitPluginIds:
    - eqadminPluginsCloudflare
  pluginPath:
    - C:\usersoft\codes\brick-clouflare-plugin\target
```

## 说明
- 当前实现以 Cloudflare Open API 为主，针对不同套餐/权限账户，部分接口返回会有差异。
- 建议先完成账号验证，再逐步开启各模块能力。
- 账号“验证凭据”已兼容两种鉴权模式：
  - `API_TOKEN` -> 调用 `/user/tokens/verify`
  - `GLOBAL_KEY` -> 调用 `/user`
- 非业务异常统一返回 `internal error`，详细堆栈仅写入服务端日志。
基于brick-boot-springboot的cloudflare的插件运维，包含了r2管理等。

## Auto Menu Injection (EqAdmin)
- This plugin includes an idempotent Liquibase changelog for EqAdmin menu and permission seeding:
  - `src/main/resources/liquibase/changelog/cloudflare-menu.changelog.xml`
  - `src/main/resources/liquibase/sql/cloudflare-menu-permissions.sql`
- On plugin startup, it resolves host `mainDataSource` and executes this changelog automatically.
- It injects:
  - Cloudflare menu tree (directory + module entries)
  - Menu permissions
  - API permissions for `/plugins/eqadminPluginsCloudflare/cloudflare/**` and `/plugins/eqadminPluginsCloudflare/console/**`
  - module-level API permissions (accounts/dns/r2/firewall/cache/ssl/zone-settings/workers/kv/rulesets/page-rules/analytics/logs)
  - button-level permissions (e.g. `btn:cf:acct:create`, `btn:cf:dns:delete`, `btn:cf:worker:publish`)
  - role binding for role code `sys`

## Button Permission UI Binding
- Frontend buttons are bound by `v-permission` directive.
- Permission sources:
  - `localStorage.permissions`
  - `localStorage.btnConPomentsPerms`
- Super users (`accountType=super/supper` or role code `sys`) bypass button checks.
- When permission payload is temporarily empty (for compatibility before session refresh), UI falls back to visible.

Config (default enabled):

```yaml
cloudflare:
  plugin:
    menu-injection:
      enabled: true
      change-log: liquibase/changelog/cloudflare-menu.changelog.xml
      database-change-log-table: databasechangelog_cloudflare_plugin
      database-change-log-lock-table: databasechangeloglock_cloudflare_plugin
```

## Newly Added Expansion Modules
- Zone advanced settings:
  - brotli, http3, always_online, automatic_https_rewrites, rocket_loader, security_level, minify
  - API path: `/plugins/eqadminPluginsCloudflare/cloudflare/zone-settings/**`
- WAF Rulesets (Rulesets API):
  - entrypoint query/update
  - rules create/update/delete
  - API path: `/plugins/eqadminPluginsCloudflare/cloudflare/rulesets/**`

## 2026 Update: Shared DataSource + Worker Admin Migration

This plugin now supports two runtime modes:

- `embedded-shared` (recommended): use host `mainDataSource` from EqAdmin.
- `standalone`: fallback to local SQLite.

Important config:

```yaml
cloudflare:
  plugin:
    runtime-mode: embedded-shared
    host-data-source-bean-name: mainDataSource
    market:
      enabled: true
      base-url: ${CLOUDFLARE_PLUGIN_MARKET_BASE_URL:}
      signature-enabled: true
      access-key: ${CLOUDFLARE_PLUGIN_MARKET_ACCESS_KEY:}
      access-secret: ${CLOUDFLARE_PLUGIN_MARKET_ACCESS_SECRET:}
      admin-username: ${CLOUDFLARE_PLUGIN_MARKET_ADMIN_USERNAME:}
      admin-password: ${CLOUDFLARE_PLUGIN_MARKET_ADMIN_PASSWORD:}
```

New console modules:

- `#/plugin-market`: external plugin market operations.
- `#/worker-admin`: migrated worker `/admin` capabilities
  (plan/payment/email/menu/misc/licenses).
