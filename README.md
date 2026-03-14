# brick-cloudflare-plugin

基于 `Spring Boot 3.5 + JDK 17 + Brick Bootkit` 的 Cloudflare 管理插件。

当前项目已经完成以下方向的重构：

- 前端统一为 `Vue 3 + Vite`，实际源码目录为 `sources/`
- 插件业务数据从 SQLite 切换为 MySQL
- 插件业务表落在 `cloudflare` 库
- 主应用菜单、权限、API 绑定、角色授权落在 `eqadmin` 主库
- 同时支持“独立测试模式”和“作为 EqAdmin 插件挂载模式”
- 前端界面文案以中文为主，独立测试时可直接显示左侧菜单

## 主要功能

- 账号管理：Cloudflare 账号维护、凭证校验、站点加载
- 总览面板：账号数量、站点数量、关键状态概览
- DNS 管理：记录查询、新增、编辑、删除
- R2 管理：存储桶查询、新建、删除
- 防火墙规则：规则查询、创建、启停、删除
- 安全规则集：Rulesets 查询、创建、启停、删除
- 缓存策略：缓存设置读取、保存、清理缓存
- SSL/TLS：SSL 模式、Always HTTPS、TLS 设置管理
- 站点高级设置：常用 Zone Settings 统一管理
- Workers：脚本查询、发布、删除、路由管理
- KV：命名空间与键值读取、写入、删除
- 页面规则：Page Rules 查询、创建、启停、删除
- 分析报表：站点分析数据展示
- 操作日志：插件内部操作日志查看与清理
- 插件市场：外部插件市场管理能力
- 授权中心：Worker Admin 相关管理能力

## 项目结构

```text
brick-clouflare-plugin/
├─ pom.xml
├─ README.md
├─ sources/                               # Vue3 + Vite 前端源码
│  ├─ src/
│  ├─ static/
│  ├─ dist/                               # Vite 构建产物
│  └─ vite.config.js
├─ src/main/java/cn/net/rjnetwork/qixiaozhu/plugins/cloudflare
│  ├─ bootstrap/                          # 独立启动默认配置、静态资源同步、微应用注册
│  ├─ config/                             # 插件双数据源、Liquibase、MyBatis 等配置
│  ├─ controller/                         # Cloudflare 各模块接口
│  ├─ domain/                             # 实体、DTO、VO
│  ├─ mapper/                             # MyBatis-Plus Mapper
│  ├─ service/                            # 业务逻辑与 Cloudflare API 网关
│  └─ support/                            # JDBC URL 推导、不可用数据源兜底
└─ src/main/resources
   ├─ cloudflare-application.yaml         # 插件主配置
   └─ db/changelog/                       # 当前生效的 Liquibase 脚本
      ├─ db.changelog-master.yaml         # 插件业务库初始化
      ├─ db.changelog-host-bootstrap.yaml # 主库菜单/权限/API/角色初始化
      ├─ changelog/v1.0.0/
      └─ sql/v1.0.0/
```

说明：

- `ui/` 目录已经废弃并删除，当前只有 `sources/` 是有效前端目录
- Maven 会把 `sources/dist` 打包到 `eqadmin-cloudflare/web/childrens/cloudflare`
- 插件启动时会将静态资源同步到 EqAdmin Web 根目录下的 `childrens/cloudflare`

## 数据库设计

项目现在采用双数据源：

- 插件业务数据源：`cloudflare.plugin.datasource.*`
  - 目标库：`cloudflare`
  - 负责插件自己的业务表、账号信息、日志等数据
- 主应用桥接数据源：`cloudflare.plugin.host-datasource.*`
  - 目标库：`eqadmin`
  - 负责菜单、按钮权限、API 权限、系统角色授权、微应用信息同步

当前 Liquibase 入口拆分如下：

- `src/main/resources/db/changelog/db.changelog-master.yaml`
  - 只作用于插件业务库 `cloudflare`
- `src/main/resources/db/changelog/db.changelog-host-bootstrap.yaml`
  - 只作用于主库 `eqadmin`

如果独立启动时没有显式指定插件业务库地址，插件会基于主库 JDBC URL 自动推导出 `cloudflare` 库地址。

## 运行模式

### 独立测试模式

适合本插件单独开发、联调和页面测试。

- 后端启动命令：

```bash
mvn spring-boot:run -Dspring-boot.run.fork=false
```

- 前端开发命令：

```bash
cd sources
npm install
npm run dev
```

- Vite 默认端口：`30001`
- Vite 默认代理目标：`http://127.0.0.1:8080`
- 独立测试模式下，左侧菜单由前端内置菜单脚本提供
  - 文件：`sources/src/router/menus.js`
- 独立测试模式下，接口默认访问前缀：
  - `/cloudflare/**`

独立模式下，插件会优先尝试从同级 EqAdmin 工作区自动读取默认配置，主要参考：

- `C:\usersoft\codes\eqadmin\config\brick-bootkit-admin-install-overrides.properties`
- `C:\usersoft\codes\eqadmin\eq-admin-bases\eq-admin-start\src\main\resources\application-dev.yml`

### 主应用插件模式

适合交付到 EqAdmin 主应用中运行。

- 前端先构建：

```bash
cd sources
npm install
npm run build
```

- 再执行 Maven 打包：

```bash
mvn -DskipTests package
```

- 产物示例：
  - `target/eqadminPluginsCloudflare-0.1.0-repackage.jar`

挂载到主应用后：

- 菜单展示依赖主应用框架壳层
- 插件会自动向主库补齐菜单、按钮权限、API 权限和 `sys` 角色授权
- 插件接口前缀通常为：
  - `/plugins/eqadminPluginsCloudflare/cloudflare/**`
- 微应用访问基地址为：
  - `/childrens/cloudflare`

## 前端说明

前端实际有效目录只有 `sources/`，并且已经切换到 `Vue 3 + Vite`。

关键说明：

- 路由使用 `vue-router`
- 状态管理使用 `pinia`
- UI 组件使用 `element-plus`
- 图表使用 `echarts`
- 独立测试模式显示插件自带侧边菜单
- 微应用模式通过 `window.__MICRO_APP_ENVIRONMENT__` 判断
- 微应用模式下由主应用负责外层菜单，插件内部只处理页面路由切换

## 菜单、权限与角色授权

当前主库初始化脚本已经改为通过 `db/changelog-host-bootstrap.yaml` 执行，包含：

- 菜单目录：`边缘云平台`
- 页面菜单：总览、账号管理、解析记录、对象存储、防火墙规则、安全规则集、缓存策略、证书与加密、站点高级设置、边缘脚本、键值存储、分析报表、页面规则、操作日志、插件市场、授权中心
- 页面按钮权限
- 插件 API 权限绑定
- `sys` 角色默认授权

这部分数据只应该写入 `eqadmin` 主库，不应该写入 `cloudflare` 业务库。

## 关键环境变量

### 插件业务库

- `CLOUDFLARE_PLUGIN_DB_URL`
- `CLOUDFLARE_PLUGIN_DB_USERNAME`
- `CLOUDFLARE_PLUGIN_DB_PASSWORD`

### 主应用桥接库

- `CLOUDFLARE_PLUGIN_HOST_DB_URL`
- `CLOUDFLARE_PLUGIN_HOST_DB_USERNAME`
- `CLOUDFLARE_PLUGIN_HOST_DB_PASSWORD`

### 主库兜底环境变量

- `EQADMIN_MYSQL_URL`
- `EQADMIN_MYSQL_USERNAME`
- `EQADMIN_MYSQL_PASSWORD`

### 插件市场

- `CLOUDFLARE_PLUGIN_MARKET_BASE_URL`
- `CLOUDFLARE_PLUGIN_MARKET_ACCESS_KEY`
- `CLOUDFLARE_PLUGIN_MARKET_ACCESS_SECRET`
- `CLOUDFLARE_PLUGIN_MARKET_ADMIN_USERNAME`
- `CLOUDFLARE_PLUGIN_MARKET_ADMIN_PASSWORD`

### 加密密钥

- `CLOUDFLARE_PLUGIN_SECRET`

## 当前推荐验证命令

前端构建验证：

```bash
cd sources
npm run build
```

后端构建验证：

```bash
mvn -DskipTests package
```

本地联调验证：

```bash
mvn spring-boot:run -Dspring-boot.run.fork=false
```

## 注意事项

- 本项目已经不再使用 SQLite，后续新增表结构请统一维护到 `db/changelog/`
- `src/main/resources/liquibase/` 那套旧版 XML/SQL 资源已废弃，不应继续使用
- `src/main/resources/db/schema.sql` 已废弃，不应再作为初始化入口
- 该插件的交付产物是 Brick Bootkit 插件包，不建议按普通独立 Spring Boot 可执行 Jar 的方式理解部署
- 若需要完整主应用联调，请确保同级 `eqadmin` 工作区存在或显式传入上述数据库配置
