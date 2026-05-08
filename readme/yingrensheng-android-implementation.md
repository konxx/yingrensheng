# 映人生 Android 实施方案

## 1. 目标与边界

这份文档用于把产品方案继续拆到 Android 研发可执行的粒度，重点解决三件事：

- App 应该怎么拆模块
- 页面与路由应该怎么组织
- 技术栈、依赖边界、后台协作与非功能要求应该怎么定

先明确一个关键边界：

**映人生 Android 端不负责重型 AI 生成和最终视频渲染。**

Android 端主要负责：

- 用户登录与会员身份
- 创作流程引导
- 素材采集、整理、上传
- AI 结果展示与轻编辑
- 成片预览、导出、支付
- 作品管理与消息触达

后端负责：

- 素材分析
- AI 叙事生成
- 分镜生成
- 音频转写
- 配音与字幕处理
- 最终视频渲染
- 合规审核
- 水印与导出

这条边界要尽早定死，否则 Android 端会被拖进本地渲染、复杂媒体处理和设备兼容泥潭。

---

## 2. 架构总原则

建议采用：

```text
Kotlin First
+ Single Activity
+ Jetpack Compose
+ MVVM + UDF
+ Repository 分层
+ 按 feature 拆多模块
```

具体原则：

- UI 使用 Compose，不新做 View/XML 体系
- 导航使用单 Activity 承载所有页面
- 页面状态以 `ViewModel + StateFlow/Compose State` 为主
- 数据访问统一经过 Repository
- 优先按功能域拆 feature 模块，不按“工具类堆一起”的方式长大
- 复杂业务逻辑可按需引入 UseCase，但不要在 V1 过度设计 domain 层

---

## 3. 工程模块拆分

## 3.1 推荐模块树

```text
app
core:common
core:model
core:designsystem
core:ui
core:navigation
core:network
core:database
core:datastore
core:media
core:upload
core:analytics
core:testing
data:user
data:project
data:creation
data:work
data:order
data:member
data:agency
sync
feature:onboarding
feature:auth
feature:home
feature:create
feature:editor
feature:works
feature:order
feature:member
feature:profile
feature:agency
benchmark
```

## 3.2 模块职责说明

### `app`

应用壳模块，负责：

- `Application` 初始化
- Hilt 根注入
- 根导航装配
- build variant 注入
- 全局主题与启动流程拼装

### `core:common`

纯 Kotlin 公共能力：

- Result/Error 封装
- 时间、文件、格式化工具
- 常量、枚举、扩展函数
- Dispatcher/Coroutine 统一注入接口

尽量做成 Kotlin library，不带 Android 依赖。

### `core:model`

全局共享数据模型定义：

- User
- Project
- MaterialItem
- StoryDraft
- Storyboard
- RenderTask
- Work
- Order
- MemberInfo

要求：

- 区分 API DTO、DB Entity、UI Model，不要让一个类跑完整条链路
- `core:model` 放领域模型与轻量值对象，不直接放 Retrofit DTO

### `core:designsystem`

统一设计系统：

- 颜色、字体、间距、圆角、阴影
- 品牌主题
- Button/Card/Dialog/Tag 等统一组件
- 空态、错误态、加载态组件

这是“映人生气质”落地的关键模块，不要散在各 feature 里各写各的。

### `core:ui`

跨页面复用 UI 容器：

- 通用 Scaffold
- 顶栏/底栏
- 图片选择器壳层
- 权限请求弹层
- 进度条、骨架屏、媒体网格
- 通用表单组件

### `core:navigation`

导航协议层：

- Route 常量或 typed route 定义
- 各 feature 对外暴露的导航入口
- Deep link 规则
- 参数编码/解码

要求：

- feature 不直接互相依赖页面实现
- 导航只通过 `core:navigation` 暴露能力

### `core:network`

网络基础设施：

- OkHttpClient
- Retrofit
- 序列化配置
- 鉴权拦截器
- 日志拦截器
- 错误码转换
- 通用分页/响应包装解析

### `core:database`

本地结构化存储：

- Room Database
- DAO
- migration
- 本地草稿、上传记录、缓存索引

### `core:datastore`

轻量 KV 存储：

- token 元信息
- 首启状态
- 用户偏好
- 最近使用风格
- 本地开关配置

### `core:media`

媒体能力封装：

- Media3 播放器封装
- 缩略图与封面帧抽取
- 时长/分辨率读取
- 本地素材预览能力
- 音频播放与波形基础数据接口

### `core:upload`

大文件上传基础设施：

- 分片/断点续传策略封装
- 上传队列管理
- 前后台切换后的任务恢复
- 上传进度状态流

这个模块最好单独存在，不要把上传逻辑塞进某个 feature。

### `core:analytics`

埋点与日志抽象：

- 页面曝光
- 创作漏斗
- 支付漏斗
- 错误事件
- 性能事件

只定义接口与事件模型，具体第三方 SDK 放实现层。

### `core:testing`

测试公共依赖：

- fake repository
- 测试数据构造器
- Compose test helper
- coroutine test rule

### `data:*`

按业务域拆 repository 与数据源：

- `data:user`：用户、登录态、资料
- `data:project`：项目草稿、项目详情、继续创作
- `data:creation`：场景、素材分析、故事生成、分镜生成、任务状态
- `data:work`：作品列表、作品详情、分享信息
- `data:order`：支付、订单、导出规格
- `data:member`：会员权益、套餐、权益扣减
- `data:agency`：机构申请、批量任务、模板管理

每个 `data` 模块内部建议包含：

- repository interface / implementation
- remote datasource
- local datasource
- mapper

### `sync`

后台同步与任务编排：

- WorkManager 任务注册
- 上传重试
- 生成状态轮询
- 导出状态轮询
- 草稿自动同步

### `feature:onboarding`

首启与新手引导。

### `feature:auth`

登录、协议确认、授权引导。

### `feature:home`

首页、推荐场景、继续创作、活动卡片。

### `feature:create`

创作主流程前半段：

- 场景选择
- 素材导入
- AI 引导问答
- 风格选择
- 故事草稿确认
- 分镜生成状态

### `feature:editor`

创作主流程后半段：

- 故事板
- 视频预览
- 轻编辑
- 字幕/音乐/封面调整
- 导出前确认

### `feature:works`

作品库、草稿箱、作品详情。

### `feature:order`

订单列表、订单详情、支付状态页。

### `feature:member`

会员中心、套餐页、权益说明。

### `feature:profile`

我的页面、设置、帮助、授权记录。

### `feature:agency`

机构入口、机构申请、批量任务入口。

V1 可以先做空壳或 H5 承接。

### `benchmark`

性能专项模块：

- Macrobenchmark
- Baseline Profile 生成

---

## 4. 模块依赖规则

建议遵守下面这组硬规则：

```text
app -> feature / sync / core / data
feature -> data / core
data -> core
core -> 不依赖 feature 与 data
benchmark -> app
```

补充规则：

- `feature` 之间禁止直接互相依赖实现
- 导航跨模块通过 `core:navigation`
- 公共 UI 进入 `core:ui` 或 `core:designsystem`
- 纯逻辑优先放 Kotlin library，减少 Android module 数量
- Gradle 统一用 version catalog 和 convention plugin 管理

---

## 5. 页面与路由清单

## 5.1 路由组织建议

采用单 Activity + nested navigation graph：

```text
root
├── splashGraph
├── onboardingGraph
├── authGraph
├── mainGraph
│   ├── homeGraph
│   ├── createGraph
│   ├── worksGraph
│   └── profileGraph
└── modalGraph
    ├── payment
    ├── share
    └── fullscreenPreview
```

## 5.2 页面清单

### A. 启动与登录

| 页面 | 所属模块 | MVP | 说明 |
|---|---|---:|---|
| SplashRoute | `app` | 是 | 冷启动、路由分发、登录态检查 |
| OnboardingRoute | `feature:onboarding` | 是 | 新手引导、核心价值说明 |
| AgreementRoute | `feature:auth` | 是 | 用户协议、隐私协议、AI 生成说明 |
| LoginRoute | `feature:auth` | 是 | 手机号登录、验证码登录 |
| ProfileSetupRoute | `feature:auth` | 否 | 补资料、头像昵称 |

### B. 首页

| 页面 | 所属模块 | MVP | 说明 |
|---|---|---:|---|
| HomeRoute | `feature:home` | 是 | 首页聚合页 |
| SceneFeedRoute | `feature:home` | 是 | 热门场景更多 |
| TemplateFeedRoute | `feature:home` | 否 | 模板广场 |
| CampaignDetailRoute | `feature:home` | 否 | 活动详情 |

### C. 创作流程

| 页面 | 所属模块 | MVP | 说明 |
|---|---|---:|---|
| CreateEntryRoute | `feature:create` | 是 | 选择“快速成片”或“故事成片” |
| SceneSelectRoute | `feature:create` | 是 | 旅行、回忆、节庆等场景选择 |
| MaterialImportRoute | `feature:create` | 是 | 相册/视频/音频/文字导入 |
| MaterialReviewRoute | `feature:create` | 是 | 素材清单、去重结果、授权确认 |
| InterviewRoute | `feature:create` | 是 | AI 轻访谈问答 |
| StyleSelectRoute | `feature:create` | 是 | 情绪、片长、旁白风格、配乐方向 |
| StoryDraftRoute | `feature:create` | 是 | 故事主线与文案初稿确认 |
| StoryGeneratingRoute | `feature:create` | 是 | 生成中状态、任务进度 |
| StoryboardRoute | `feature:editor` | 是 | 段落式故事板 |
| PreviewRoute | `feature:editor` | 是 | 首版成片预览 |
| ClipAdjustRoute | `feature:editor` | 是 | 删除片段、替换素材、重排序 |
| SubtitleEditRoute | `feature:editor` | 是 | 调整字幕与局部文案 |
| AudioEditRoute | `feature:editor` | 是 | 音乐/配音切换 |
| CoverEditRoute | `feature:editor` | 是 | 封面选择与标题修改 |
| ExportPlanRoute | `feature:editor` | 是 | 规格选择、权益提示 |
| PaymentRoute | `feature:order` | 是 | 支付方式、会员解锁 |
| ExportProcessingRoute | `feature:order` | 是 | 导出中、结果轮询 |

### D. 作品与项目

| 页面 | 所属模块 | MVP | 说明 |
|---|---|---:|---|
| WorksRoute | `feature:works` | 是 | 作品列表 |
| DraftsRoute | `feature:works` | 是 | 草稿与进行中项目 |
| WorkDetailRoute | `feature:works` | 是 | 成片详情 |
| ProjectDetailRoute | `feature:works` | 是 | 项目详情、继续编辑 |
| ShareRoute | `feature:works` | 否 | 分享页/海报页 |

### E. 我的 / 商业化

| 页面 | 所属模块 | MVP | 说明 |
|---|---|---:|---|
| ProfileRoute | `feature:profile` | 是 | 我的首页 |
| MemberCenterRoute | `feature:member` | 是 | 会员权益与套餐 |
| OrdersRoute | `feature:order` | 是 | 订单列表 |
| OrderDetailRoute | `feature:order` | 是 | 订单详情与售后状态 |
| AuthRecordRoute | `feature:profile` | 是 | 授权与合规记录 |
| PointsRoute | `feature:member` | 否 | 积分与兑换 |
| SettingsRoute | `feature:profile` | 是 | 设置页 |
| HelpRoute | `feature:profile` | 是 | 帮助、FAQ、客服入口 |

### F. 机构合作

| 页面 | 所属模块 | MVP | 说明 |
|---|---|---:|---|
| AgencyEntryRoute | `feature:agency` | 否 | 机构介绍页 |
| AgencyApplyRoute | `feature:agency` | 否 | 机构合作申请 |
| BatchTaskRoute | `feature:agency` | 否 | 批量任务入口 |

## 5.3 底部导航最终建议

底部导航保留 4 项：

- 首页
- 创作
- 作品
- 我的

不建议 V1 就把“订单”独立到底部导航，会让主流程变重。

---

## 6. 关键页面的数据与状态要求

## 6.1 首页

至少需要拉取并缓存：

- 用户基础信息
- 最近项目
- 推荐场景
- 推荐模板
- 会员权益摘要

状态要求：

- 支持离线显示上次缓存
- 继续创作入口必须秒开

## 6.2 素材导入页

需要支持：

- 系统相册多选
- 视频、图片、音频混合导入
- URI 持久权限处理
- 大批量素材列表懒加载

状态要求：

- 导入后立即本地入库
- 生成缩略图与基础元数据
- 即使 App 被杀，草稿也能恢复

## 6.3 生成中页面

需要展示：

- 当前任务阶段
- 百分比或阶段性状态
- 预估等待时间
- 离开后后台继续的说明

状态要求：

- 页面退出后任务不中断
- 支持重新进入查看状态
- 支持推送或轮询刷新

## 6.4 预览页

需要支持：

- 网络视频播放
- 本地缓存封面与首帧
- 切换字幕显示
- 替换音乐和配音后的二次生成请求

状态要求：

- 横竖屏与折叠态切换不丢播放位置
- 返回列表后可恢复最近预览位置

---

## 7. 技术栈建议

## 7.1 基础栈

| 类别 | 建议 |
|---|---|
| 开发语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 架构 | Single Activity + MVVM + UDF |
| 状态管理 | `ViewModel` + `StateFlow` + Compose state |
| 并发 | Kotlin Coroutines + Flow |
| 导航 | Navigation Compose |
| DI | Hilt |
| 本地数据库 | Room |
| 轻量存储 | DataStore |
| 播放器 | Media3 ExoPlayer |
| 图片加载 | Coil |
| 网络 | OkHttp + Retrofit |
| 后台任务 | WorkManager |
| 启动初始化 | App Startup |
| 测试 | JUnit + Compose UI Test + MockWebServer |
| 性能 | Macrobenchmark + Baseline Profile |

## 7.2 版本与构建要求

建议：

- 使用 Gradle Kotlin DSL
- 使用 `libs.versions.toml`
- Compose 相关依赖统一走 Compose BOM
- Room、Hilt、序列化统一用 KSP，不走 kapt
- 至少配置 `dev / staging / prod` 三套环境

不建议：

- V1 上来就做 Play Dynamic Feature
- 同时维护 XML 和 Compose 双 UI 体系
- 每个 feature 都重复造网络、播放器、上传逻辑

## 7.3 SDK 建议

建议初始参数：

- `minSdk = 26`
- `targetSdk = 当前最新稳定版`
- `compileSdk = 当前最新稳定版`

原因：

- 能明显减少低版本存储、权限和媒体兼容成本
- 更适合大文件上传、现代媒体能力和 Compose 工程实践
- 对中国市场 2026 年的新项目来说，兼容收益已经不高于维护成本

如果业务方极度强调覆盖率，可以评估 `minSdk 24`，但会显著增加测试成本。

---

## 8. 页面实现建议

## 8.1 首页与底部框架

建议：

- 使用 `NavigationSuiteScaffold` 思路做自适应导航
- 手机小屏走 bottom bar
- 大屏与折叠屏走 navigation rail

## 8.2 创作流程页

建议：

- 采用 Wizard 风格，不要做成一大页堆满表单
- 所有步骤支持草稿自动保存
- 每一页只要求用户做一个核心决策

## 8.3 故事板页

建议：

- 优先做段落卡片式故事板
- 暂不做专业多轨时间线
- “重新生成某段”是主操作，不是隐藏菜单

## 8.4 预览页

建议：

- 视频区域优先稳定播放和首帧速度
- 右侧或底部操作栏只保留高频编辑
- 重编辑行为走二次任务生成，不本地实时复杂渲染

---

## 9. 后端接口配合要求

Android 端开工前，后端至少要提供以下接口域。

## 9.1 认证与用户

- 登录/验证码
- 用户信息
- 会员信息
- token 刷新

## 9.2 项目与素材

- 创建项目
- 获取项目详情
- 获取草稿详情
- 添加/删除素材
- 提交授权确认

## 9.3 上传

建议支持：

- 分片上传
- 断点续传
- 上传完成回调
- 文件哈希去重

Android 侧非常需要后端提供：

- 上传任务 ID
- 分片确认接口
- 失败重试规则
- 秒传/去重能力

## 9.4 AI 创作

- 场景列表
- AI 访谈问题配置
- 生成故事草稿
- 生成分镜
- 局部重写
- 局部重生

生成类接口必须是异步任务模型，不要做同步长请求。

建议统一：

- `taskId`
- `status`
- `progress`
- `currentStage`
- `estimatedRemainingSeconds`

## 9.5 渲染与导出

- 创建导出任务
- 查询导出状态
- 获取成片地址
- 获取封面地址
- 获取分享地址

## 9.6 商业化

- 订单创建
- 支付签名
- 支付结果查询
- 会员购买
- 权益扣减

## 9.7 通知

- 导出完成通知
- 生成失败通知
- 会员权益通知

如果推送体系暂时不完整，至少要保证状态轮询接口稳定。

---

## 10. 本地存储设计

## 10.1 Room 建议表

V1 可先有这些核心表：

- `project_draft`
- `material_local`
- `upload_task`
- `generation_task`
- `work_cache`
- `user_cache`

## 10.2 DataStore 建议字段

- `isFirstLaunch`
- `hasAcceptedAgreement`
- `lastLoginUserId`
- `recentSceneType`
- `recentNarrativeStyle`
- `notificationEnabledHintShown`

## 10.3 文件缓存策略

缓存分三层：

- 原始素材引用层：保存 `content://` 或本地文件映射
- 缩略图缓存层：本地小图与首帧
- 预览缓存层：短时缓存预览视频/封面

要求：

- 不复制大体积原始文件，优先持有 URI 与受控缓存
- 缓存要有清理策略
- 草稿删除时要能回收关联缓存

---

## 11. 上传与后台任务设计

## 11.1 上传策略

建议：

- 大文件上传走 `WorkManager + foreground` 模式
- 支持 Wi-Fi 限制与仅充电时后台继续等策略预留
- 网络波动时自动重试
- 上传任务支持队列与并发数限制

## 11.2 任务轮询策略

生成与导出任务建议：

- 前台页面内短周期轮询
- 退后台后切换 WorkManager 周期轮询
- 完成后本地通知提示

轮询间隔建议分层：

- 生成中前 30 秒：3 到 5 秒
- 之后：8 到 15 秒
- 后台：15 到 30 秒

---

## 12. 权限与系统能力清单

按 MVP 需要，至少涉及：

- 网络权限
- 媒体读取权限
- 通知权限
- 录音权限

按 Android 版本拆分：

- Android 13+：`READ_MEDIA_IMAGES`、`READ_MEDIA_VIDEO`、`READ_MEDIA_AUDIO`
- Android 12 及以下：旧存储读取权限兼容处理

如果需要：

- 语音录入：`RECORD_AUDIO`
- 上传/导出状态提醒：`POST_NOTIFICATIONS`
- 长时上传：前台服务相关声明

不建议 V1 默认请求：

- 相机权限
- 通讯录
- 精确位置

除非产品流程真的依赖。

---

## 13. 安全与合规要求

## 13.1 账号与凭证

要求：

- HTTPS only
- token 短期有效 + refresh
- 本地敏感信息使用系统安全存储方案

## 13.2 素材合规

前端流程中必须有：

- 本人授权确认
- 第三方肖像提醒
- 未成年人提示
- AI 生成标识说明

## 13.3 审计留痕

客户端至少要记录：

- 授权确认时间
- 素材上传时间
- 生成请求时间
- 导出请求时间
- 支付结果回写时间

---

## 14. 测试要求

## 14.1 单元测试

覆盖：

- ViewModel 状态流转
- Repository 错误分支
- mapper 转换
- 上传队列与重试策略

## 14.2 UI 测试

覆盖：

- 登录流程
- 创作主链路
- 支付前关键跳转
- 作品列表回流

## 14.3 接口测试

建议使用 MockWebServer 做：

- 成功/失败响应
- 超时与重试
- 大文件上传状态模拟

## 14.4 性能测试

至少关注：

- 冷启动时间
- 首页首屏时间
- 大量素材导入时卡顿
- 作品列表滚动流畅度
- 视频预览首帧时间

建议首版就接入：

- Macrobenchmark
- Baseline Profile

---

## 15. 性能与体验指标

建议把下面这些指标写进研发验收标准：

- 冷启动到首页可交互 < 2.0s
- 首页首屏接口失败时仍能展示缓存骨架
- 100 个素材导入后列表可滚动，无明显 ANR 风险
- 单个 300MB 视频可稳定加入上传队列
- 从作品列表进入成片预览首帧 < 1.2s
- 生成/导出页离开再进入，状态恢复正确

---

## 16. V1 实施优先级

## P0 必做

- 单 Activity + Compose 工程骨架
- 多模块基础结构
- 登录与协议确认
- 首页
- 创作主流程
- 草稿本地保存
- 素材上传
- 故事草稿/分镜/预览/导出流程
- 作品页
- 我的页
- 订单与会员基础页

## P1 建议尽早补上

- Baseline Profile
- 上传重试与断点续传
- 推送或通知提醒
- 授权记录页
- 埋点体系

## P2 可后置

- 模板广场
- 积分商城
- 机构批量页
- 深度分享页
- 大屏双栏深度优化

---

## 17. 最终建议

如果这项目现在从 0 开始，最稳的 Android 落地方式不是“先写一堆页面”，而是先搭好下面这个顺序：

1. 先搭工程骨架和模块边界
2. 再串登录、首页、创作、作品四条主导航
3. 再补上传、生成轮询、预览、导出四个关键技术闭环
4. 最后再往会员、订单、机构和运营能力扩

对映人生来说，真正的技术难点不在“页面多”，而在这四件事：

- 大素材导入和上传稳定性
- 异步 AI 任务状态管理
- 视频预览与轻编辑体验
- 草稿、任务、支付三条状态链的一致性

只要这四条打稳，产品就能跑起来；如果这四条没打稳，前面再漂亮的 UI 也会很快露馅。
