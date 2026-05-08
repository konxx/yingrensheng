# 映人生 Android 模块目录结构与包名规范

## 1. 目的

这份规范用于统一映人生 Android 项目的：

- Gradle 模块目录结构
- 包名命名方式
- 源码子目录组织
- 类名、资源名、路由名规范

目标很简单：

**让项目从第一天开始就有清晰边界，避免后面出现模块乱依赖、包名混杂、文件四处飘的情况。**

---

## 2. 根包名建议

建议主包名统一为：

```text
com.yingrensheng.app
```

说明：

- `com.yingrensheng.app` 足够直观，适合当前从 0 到 1 的项目
- 如果未来公司已有固定域名体系，也可以迁移为 `com.xxx.yingrensheng`
- 在没有明确公司域名前，不建议用过长包名

统一原则：

- 包名全小写
- 不使用拼音缩写组合怪词
- 不使用 `util2`、`newui`、`temp` 这类临时命名

---

## 3. Gradle 模块目录结构

建议目录采用“物理目录”和“Gradle 路径”一一对应：

```text
yingrensheng-android/
├── app/
├── benchmark/
├── build-logic/
├── core/
│   ├── analytics/
│   ├── common/
│   ├── database/
│   ├── datastore/
│   ├── designsystem/
│   ├── media/
│   ├── model/
│   ├── navigation/
│   ├── network/
│   ├── testing/
│   ├── ui/
│   └── upload/
├── data/
│   ├── agency/
│   ├── creation/
│   ├── member/
│   ├── order/
│   ├── project/
│   ├── user/
│   └── work/
├── feature/
│   ├── agency/
│   ├── auth/
│   ├── create/
│   ├── editor/
│   ├── home/
│   ├── member/
│   ├── onboarding/
│   ├── order/
│   ├── profile/
│   └── works/
├── gradle/
├── sync/
└── docs/
```

对应的 Gradle module path：

```text
:app
:benchmark
:core:analytics
:core:common
:core:database
:core:datastore
:core:designsystem
:core:media
:core:model
:core:navigation
:core:network
:core:testing
:core:ui
:core:upload
:data:agency
:data:creation
:data:member
:data:order
:data:project
:data:user
:data:work
:feature:agency
:feature:auth
:feature:create
:feature:editor
:feature:home
:feature:member
:feature:onboarding
:feature:order
:feature:profile
:feature:works
:sync
```

---

## 4. `settings.gradle.kts` 模块包含建议

推荐按下面这种方式维护：

```kotlin
include(":app")
include(":benchmark")
include(":sync")

include(":core:analytics")
include(":core:common")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:media")
include(":core:model")
include(":core:navigation")
include(":core:network")
include(":core:testing")
include(":core:ui")
include(":core:upload")

include(":data:agency")
include(":data:creation")
include(":data:member")
include(":data:order")
include(":data:project")
include(":data:user")
include(":data:work")

include(":feature:agency")
include(":feature:auth")
include(":feature:create")
include(":feature:editor")
include(":feature:home")
include(":feature:member")
include(":feature:onboarding")
include(":feature:order")
include(":feature:profile")
include(":feature:works")
```

要求：

- 模块名使用单数，不要一会儿 `users` 一会儿 `user`
- 模块名尽量短，但必须可读
- `feature:create` 和 `feature:editor` 分开，不建议揉成一个超大创作模块

---

## 5. 各模块包名映射

## 5.1 总体映射规则

统一映射方式：

```text
模块路径 :feature:home
包名     com.yingrensheng.feature.home
```

也就是：

```text
com.yingrensheng.<layer>.<module>
```

### 样例

| 模块 | 包名 |
|---|---|
| `:app` | `com.yingrensheng.app` |
| `:core:common` | `com.yingrensheng.core.common` |
| `:core:designsystem` | `com.yingrensheng.core.designsystem` |
| `:core:network` | `com.yingrensheng.core.network` |
| `:data:user` | `com.yingrensheng.data.user` |
| `:data:creation` | `com.yingrensheng.data.creation` |
| `:feature:home` | `com.yingrensheng.feature.home` |
| `:feature:create` | `com.yingrensheng.feature.create` |
| `:feature:editor` | `com.yingrensheng.feature.editor` |
| `:sync` | `com.yingrensheng.sync` |
| `:benchmark` | `com.yingrensheng.benchmark` |

---

## 6. 模块内部目录规范

## 6.1 `app` 模块

推荐结构：

```text
app/
└── src/main/java/com/yingrensheng/app/
    ├── AppApplication.kt
    ├── MainActivity.kt
    ├── di/
    ├── navigation/
    ├── ui/
    └── startup/
```

职责：

- Application
- 根导航
- 全局主题注入
- App Startup 初始化
- 只放壳层能力，不放具体业务页面实现

---

## 6.2 `core` 模块

### `core:common`

```text
core/common/
└── src/main/java/com/yingrensheng/core/common/
    ├── coroutine/
    ├── error/
    ├── result/
    ├── extension/
    ├── util/
    └── dispatcher/
```

### `core:model`

```text
core/model/
└── src/main/java/com/yingrensheng/core/model/
    ├── user/
    ├── project/
    ├── material/
    ├── creation/
    ├── work/
    ├── order/
    └── member/
```

建议按业务域再分子包，不要所有 model 平铺。

### `core:designsystem`

```text
core/designsystem/
└── src/main/java/com/yingrensheng/core/designsystem/
    ├── component/
    ├── theme/
    ├── token/
    ├── icon/
    └── preview/
```

### `core:ui`

```text
core/ui/
└── src/main/java/com/yingrensheng/core/ui/
    ├── component/
    ├── scaffold/
    ├── permission/
    ├── media/
    ├── state/
    └── loading/
```

### `core:navigation`

```text
core/navigation/
└── src/main/java/com/yingrensheng/core/navigation/
    ├── route/
    ├── graph/
    ├── navigator/
    └── argument/
```

### `core:network`

```text
core/network/
└── src/main/java/com/yingrensheng/core/network/
    ├── api/
    ├── client/
    ├── interceptor/
    ├── serializer/
    ├── response/
    └── di/
```

### `core:database`

```text
core/database/
└── src/main/java/com/yingrensheng/core/database/
    ├── dao/
    ├── entity/
    ├── converter/
    ├── migration/
    └── di/
```

### `core:datastore`

```text
core/datastore/
└── src/main/java/com/yingrensheng/core/datastore/
    ├── datasource/
    ├── serializer/
    ├── model/
    └── di/
```

### `core:media`

```text
core/media/
└── src/main/java/com/yingrensheng/core/media/
    ├── player/
    ├── extractor/
    ├── thumbnail/
    ├── audio/
    └── di/
```

### `core:upload`

```text
core/upload/
└── src/main/java/com/yingrensheng/core/upload/
    ├── worker/
    ├── queue/
    ├── model/
    ├── uploader/
    ├── observer/
    └── di/
```

### `core:analytics`

```text
core/analytics/
└── src/main/java/com/yingrensheng/core/analytics/
    ├── event/
    ├── tracker/
    ├── logger/
    └── di/
```

### `core:testing`

```text
core/testing/
└── src/main/java/com/yingrensheng/core/testing/
    ├── data/
    ├── fake/
    ├── rule/
    ├── coroutine/
    └── util/
```

---

## 6.3 `data` 模块

每个 `data` 模块统一采用同一套结构。

以 `data:user` 为例：

```text
data/user/
└── src/main/java/com/yingrensheng/data/user/
    ├── datasource/
    │   ├── local/
    │   └── remote/
    ├── di/
    ├── mapper/
    ├── model/
    │   ├── dto/
    │   ├── entity/
    │   └── param/
    ├── repository/
    └── source/
```

说明：

- `model/dto` 放接口模型
- `model/entity` 放数据库实体或缓存实体
- `model/param` 放请求参数模型
- `repository` 放仓储实现与接口
- `datasource/remote` 和 `datasource/local` 分清远端与本地

如果觉得 `source` 与 `datasource` 重复，可以直接删掉 `source`，统一保留 `datasource`。

更推荐最终定为：

```text
datasource/
repository/
mapper/
model/
di/
```

### 其他 `data` 模块包名

| 模块 | 包名 |
|---|---|
| `:data:user` | `com.yingrensheng.data.user` |
| `:data:project` | `com.yingrensheng.data.project` |
| `:data:creation` | `com.yingrensheng.data.creation` |
| `:data:work` | `com.yingrensheng.data.work` |
| `:data:order` | `com.yingrensheng.data.order` |
| `:data:member` | `com.yingrensheng.data.member` |
| `:data:agency` | `com.yingrensheng.data.agency` |

---

## 6.4 `feature` 模块

每个 `feature` 模块统一按“导航、容器、UI、状态、组件”组织。

以 `feature:home` 为例：

```text
feature/home/
└── src/main/java/com/yingrensheng/feature/home/
    ├── component/
    ├── model/
    ├── navigation/
    ├── state/
    ├── ui/
    └── viewmodel/
```

更细一点的推荐结构：

```text
feature/home/
└── src/main/java/com/yingrensheng/feature/home/
    ├── component/
    │   ├── HomeHeroCard.kt
    │   ├── RecentProjectCard.kt
    │   └── SceneShortcutRow.kt
    ├── model/
    │   └── HomeUiModel.kt
    ├── navigation/
    │   ├── HomeNavigation.kt
    │   └── HomeRoute.kt
    ├── state/
    │   ├── HomeUiAction.kt
    │   ├── HomeUiEvent.kt
    │   └── HomeUiState.kt
    ├── ui/
    │   ├── HomeRoute.kt
    │   └── HomeScreen.kt
    └── viewmodel/
        └── HomeViewModel.kt
```

说明：

- `Route` 负责拿 `ViewModel`、收集状态、处理导航
- `Screen` 负责纯 Compose UI
- `component` 放页面局部组件
- `state` 放状态、动作、事件
- `viewmodel` 只放该页面或该 feature 的 ViewModel

### 推荐的 `feature` 包名

| 模块 | 包名 |
|---|---|
| `:feature:onboarding` | `com.yingrensheng.feature.onboarding` |
| `:feature:auth` | `com.yingrensheng.feature.auth` |
| `:feature:home` | `com.yingrensheng.feature.home` |
| `:feature:create` | `com.yingrensheng.feature.create` |
| `:feature:editor` | `com.yingrensheng.feature.editor` |
| `:feature:works` | `com.yingrensheng.feature.works` |
| `:feature:order` | `com.yingrensheng.feature.order` |
| `:feature:member` | `com.yingrensheng.feature.member` |
| `:feature:profile` | `com.yingrensheng.feature.profile` |
| `:feature:agency` | `com.yingrensheng.feature.agency` |

---

## 6.5 `sync` 模块

```text
sync/
└── src/main/java/com/yingrensheng/sync/
    ├── worker/
    ├── scheduler/
    ├── observer/
    ├── model/
    └── di/
```

职责：

- 任务调度
- 轮询策略
- WorkManager 注册
- 后台同步编排

---

## 7. 包名分层规范

## 7.1 推荐分层词汇

统一只使用这些高频分层词：

- `ui`
- `component`
- `navigation`
- `viewmodel`
- `state`
- `model`
- `repository`
- `datasource`
- `mapper`
- `di`
- `worker`
- `util`

不建议随意出现：

- `manager`
- `helper`
- `base`
- `impl2`
- `temp`
- `misc`

这些名字一旦泛滥，后面模块会非常难维护。

## 7.2 `model` 子包再细分规则

只有在确实有必要时再细分：

- `dto`：接口响应/请求模型
- `entity`：本地数据库模型
- `param`：请求参数
- `vo`：谨慎使用，只有明确值对象语义时才用

不建议整个项目到处出现：

- `bean`
- `pojo`
- `data`

这些命名语义都太弱。

---

## 8. 类名规范

## 8.1 Compose 页面命名

统一规则：

- 页面容器：`XxxRoute`
- 纯 UI 页：`XxxScreen`
- 局部组件：`XxxCard`、`XxxSection`、`XxxDialog`

样例：

- `HomeRoute`
- `HomeScreen`
- `MemberCenterRoute`
- `MemberCenterScreen`
- `StoryDraftCard`
- `ExportPlanSection`

## 8.2 状态类命名

统一规则：

- 页面状态：`XxxUiState`
- 用户动作：`XxxUiAction`
- 一次性事件：`XxxUiEvent`

样例：

- `HomeUiState`
- `CreateEntryUiAction`
- `PreviewUiEvent`

## 8.3 ViewModel 命名

统一规则：

- `XxxViewModel`

样例：

- `HomeViewModel`
- `MaterialImportViewModel`
- `PreviewViewModel`

## 8.4 Repository 命名

接口和实现建议分开：

- `UserRepository`
- `OfflineFirstUserRepository`

或：

- `UserRepository`
- `DefaultUserRepository`

推荐优先使用更有语义的实现名，例如：

- `OfflineFirstProjectRepository`
- `NetworkCreationRepository`

不要大量使用没有信息量的 `RepositoryImpl`。

## 8.5 DataSource 命名

推荐：

- `UserRemoteDataSource`
- `UserLocalDataSource`
- `CreationRemoteDataSource`

## 8.6 Worker 命名

统一用动作前缀：

- `UploadMaterialWorker`
- `SyncProjectDraftWorker`
- `PollRenderStatusWorker`

---

## 9. 资源命名规范

虽然项目以 Compose 为主，但资源命名仍要统一。

## 9.1 drawable / mipmap

推荐前缀：

- 图标：`ic_`
- 插图：`illu_`
- 图片：`img_`
- 背景：`bg_`
- 占位图：`placeholder_`

样例：

- `ic_home_filled.xml`
- `ic_create_outline.xml`
- `illu_empty_works.xml`
- `img_default_cover.webp`
- `bg_member_gradient.xml`

## 9.2 string

建议按模块前缀命名：

- `home_title`
- `home_continue_create`
- `create_select_scene`
- `editor_export_hd`
- `member_buy_now`

避免：

- `title1`
- `text_ok`
- `content_desc_1`

## 9.3 color

如果颜色来自设计系统，优先进入 `core:designsystem` 的 token，不要在 feature 下随意新增。

样例：

- `brand_amber`
- `surface_primary`
- `text_secondary`

## 9.4 文件名

规则：

- Kotlin 文件：PascalCase
- 资源文件：snake_case
- 包名目录：lowercase

---

## 10. 路由与导航命名规范

## 10.1 Route 常量命名

推荐按功能域命名：

```text
home
home/scene_feed
create/entry
create/scene_select
create/material_import
editor/preview
works/detail
member/center
profile/settings
```

## 10.2 导航方法命名

推荐：

- `navigateToHome()`
- `navigateToSceneSelect()`
- `navigateToPreview(projectId)`
- `navigateToWorkDetail(workId)`

不建议：

- `goPage()`
- `jump()`
- `open2()`

## 10.3 参数命名

统一使用业务语义命名：

- `projectId`
- `workId`
- `orderId`
- `sceneType`

不要使用：

- `id`
- `type`
- `data`

因为上下文一多就会混乱。

---

## 11. 测试目录规范

所有模块统一保留：

```text
src/test/
src/androidTest/
```

包结构尽量镜像主源码结构。

例如：

```text
feature/home/src/test/java/com/yingrensheng/feature/home/viewmodel/
feature/home/src/androidTest/java/com/yingrensheng/feature/home/ui/
```

要求：

- 单元测试尽量跟随被测类同路径
- Compose UI 测试放 `androidTest`
- fake / fixture 可复用时放 `core:testing`

---

## 12. 是否需要 `domain` 层

当前阶段建议：

**先不单独拆 `domain` 模块。**

原因：

- V1 重点是快速建立闭环
- 现在业务域虽多，但真正复杂的是上传、轮询、预览和支付状态，不是算法编排本身
- 过早拆 `domain`，容易让工程复杂度先膨胀

如果后续这些逻辑显著变复杂，再考虑新增：

```text
domain/creation
domain/project
domain/member
```

但那应该发生在 V2 或 V3，而不是 V1。

---

## 13. 最终推荐落地方式

如果现在开始搭工程，建议严格按下面顺序执行：

1. 先按文档创建 Gradle 模块目录
2. 再统一根包名为 `com.yingrensheng.app`
3. `core`、`data`、`feature` 全部按固定包路径创建
4. 每个 `feature` 先建 `navigation/ui/state/viewmodel/component`
5. 每个 `data` 先建 `datasource/repository/mapper/model/di`
6. 最后再往里面填实现

不要反过来先写页面，再回头想模块怎么拆。那样项目一旦长起来，基本都会进入迁移重构期。

---

## 14. 一句话结论

映人生 Android 项目最适合的工程命名方式是：

- 目录按 `app / core / data / feature / sync / benchmark` 分层
- 包名按 `com.yingrensheng.<layer>.<module>` 统一
- 页面按 `Route + Screen + UiState + ViewModel` 固定模板组织
- 数据层按 `datasource + repository + mapper + model` 统一

这样后续不管是加页面、加开发、加功能，工程都还能保持可读、可拆、可维护。
