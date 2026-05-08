# 映人生前后端接口契约（V1）

## 1. 目标

这份文档用于把映人生 Android 客户端与后端服务的边界固定下来，确保项目按前后端分离方式推进。

原则：

- Android 端只依赖 API 契约，不直接假设后端实现细节
- 生成、渲染、上传统一走异步任务模型
- 原始媒体文件只放对象存储，不进业务数据库
- 业务主链路优先保证一致性：项目、任务、订单、权益、授权记录要能互相对上

---

## 2. 总体约定

### 2.1 基础信息

- Base URL：`/api/v1`
- 数据格式：`application/json`
- 认证方式：`Authorization: Bearer <accessToken>`
- 时间格式：ISO 8601，示例 `2026-05-08T14:30:00+08:00`
- ID 规则：字符串 ID，不要求前端感知自增主键

### 2.2 通用响应体

成功响应：

```json
{
  "requestId": "req_20260508_0001",
  "data": {}
}
```

失败响应：

```json
{
  "requestId": "req_20260508_0001",
  "error": {
    "code": "PROJECT_NOT_FOUND",
    "message": "Project does not exist",
    "details": {}
  }
}
```

分页响应：

```json
{
  "requestId": "req_20260508_0002",
  "data": {
    "items": [],
    "page": 1,
    "pageSize": 20,
    "hasMore": true,
    "nextCursor": "cursor_xxx"
  }
}
```

### 2.3 通用任务模型

所有长耗时流程统一返回：

```json
{
  "taskId": "task_xxx",
  "status": "RUNNING",
  "progress": 72,
  "currentStage": "GENERATING_STORYBOARD",
  "estimatedRemainingSeconds": 45,
  "result": null,
  "error": null,
  "updatedAt": "2026-05-08T14:35:12+08:00"
}
```

建议状态枚举：

- `PENDING`
- `RUNNING`
- `SUCCEEDED`
- `FAILED`
- `CANCELLED`

---

## 3. 认证与用户

### 3.1 发送验证码

- `POST /auth/sms/send`

请求：

```json
{
  "phone": "13800138000",
  "purpose": "LOGIN"
}
```

### 3.2 登录

- `POST /auth/login`

请求：

```json
{
  "phone": "13800138000",
  "smsCode": "123456",
  "deviceId": "android_xxx"
}
```

响应：

```json
{
  "requestId": "req_login_001",
  "data": {
    "accessToken": "token_xxx",
    "refreshToken": "refresh_xxx",
    "expiresInSeconds": 7200,
    "user": {
      "userId": "user_001",
      "nickname": "林青",
      "phone": "13800138000",
      "avatarUrl": ""
    }
  }
}
```

### 3.3 刷新 Token

- `POST /auth/refresh`

### 3.4 获取当前用户

- `GET /users/me`

### 3.5 获取当前会员信息

- `GET /member/me`

---

## 4. 场景、项目与素材

### 4.1 获取场景列表

- `GET /scenes`

响应字段建议：

- `sceneId`
- `title`
- `subtitle`
- `recommendedDurationLabel`
- `estimatedTimeLabel`
- `coverUrl`
- `supportedMaterialTypes`

### 4.2 创建项目

- `POST /projects`

请求：

```json
{
  "sceneId": "scene_travel",
  "mode": "QUICK_FILM",
  "title": "未命名旅行纪念"
}
```

### 4.3 获取项目列表

- `GET /projects?status=DRAFT&page=1&pageSize=20`

### 4.4 获取项目详情

- `GET /projects/{projectId}`

### 4.5 更新项目信息

- `PATCH /projects/{projectId}`

用于更新：

- 标题
- 一句话主题
- 选择的情绪风格
- 当前创作步骤

### 4.6 添加素材记录

- `POST /projects/{projectId}/materials`

请求：

```json
{
  "uploadId": "upload_xxx",
  "materialType": "VIDEO",
  "fileName": "travel.mp4",
  "mimeType": "video/mp4",
  "durationMillis": 18000,
  "sizeBytes": 305812345
}
```

### 4.7 删除素材

- `DELETE /projects/{projectId}/materials/{materialId}`

### 4.8 提交授权确认

- `POST /projects/{projectId}/authorization-confirmations`

请求：

```json
{
  "selfOwnedConfirmed": true,
  "containsThirdPartyPortrait": true,
  "containsMinor": false,
  "aiGeneratedNoticeAccepted": true
}
```

---

## 5. 上传接口

## 5.1 初始化上传

- `POST /uploads/initiate`

请求：

```json
{
  "fileName": "travel.mp4",
  "mimeType": "video/mp4",
  "sizeBytes": 305812345,
  "sha256": "abc123",
  "projectId": "project_001"
}
```

响应：

```json
{
  "requestId": "req_upload_001",
  "data": {
    "uploadId": "upload_001",
    "storageProvider": "S3_COMPATIBLE",
    "bucket": "yingrensheng-materials",
    "objectKey": "project_001/upload_001/travel.mp4",
    "uploadMode": "MULTIPART",
    "partSizeBytes": 5242880,
    "alreadyUploaded": false,
    "presignedUrls": []
  }
}
```

## 5.2 上报分片完成

- `POST /uploads/{uploadId}/parts`

请求：

```json
{
  "partNumber": 1,
  "etag": "etag_xxx",
  "sizeBytes": 5242880
}
```

## 5.3 完成上传

- `POST /uploads/{uploadId}/complete`

## 5.4 查询上传状态

- `GET /uploads/{uploadId}`

响应至少包含：

- `uploadId`
- `status`
- `uploadedPartCount`
- `totalPartCount`
- `deduplicated`
- `materialId`

---

## 6. AI 创作接口

## 6.1 获取 AI 访谈问题配置

- `GET /creation/interview-prompts?sceneId=scene_memory`

## 6.2 保存访谈回答

- `POST /projects/{projectId}/interview-answers`

请求：

```json
{
  "answers": [
    {
      "promptId": "prompt_1",
      "answer": "想送给妈妈"
    }
  ]
}
```

## 6.3 生成故事草稿

- `POST /projects/{projectId}/story-draft/generate`

请求：

```json
{
  "styleId": "style_warm",
  "themeLine": "想把那天的晚风和笑声留下来"
}
```

响应：

```json
{
  "requestId": "req_story_001",
  "data": {
    "taskId": "task_story_001"
  }
}
```

## 6.4 查询故事草稿

- `GET /projects/{projectId}/story-draft`

## 6.5 生成故事板

- `POST /projects/{projectId}/storyboard/generate`

## 6.6 局部重写段落

- `POST /projects/{projectId}/storyboard/sections/{sectionId}/rewrite`

请求：

```json
{
  "instruction": "让结尾更克制一点"
}
```

## 6.7 生成预览版本

- `POST /projects/{projectId}/preview/generate`

## 6.8 查询任务状态

- `GET /tasks/{taskId}`

### 当前阶段枚举建议

- `ANALYZING_MATERIALS`
- `TRANSCRIBING_AUDIO`
- `GENERATING_STORY_DRAFT`
- `GENERATING_STORYBOARD`
- `RENDERING_PREVIEW`
- `RENDERING_EXPORT`
- `AUDITING_CONTENT`

---

## 7. 作品预览与导出

### 7.1 获取预览结果

- `GET /projects/{projectId}/preview`

响应字段建议：

- `previewId`
- `videoUrl`
- `coverUrl`
- `title`
- `subtitleSummary`
- `musicLabel`
- `version`

### 7.2 创建导出任务

- `POST /projects/{projectId}/exports`

请求：

```json
{
  "exportPlanId": "plan_single",
  "resolution": "1080P",
  "removeWatermark": true
}
```

响应：

```json
{
  "requestId": "req_export_001",
  "data": {
    "exportId": "export_001",
    "taskId": "task_export_001",
    "payRequired": true,
    "orderId": "order_001"
  }
}
```

### 7.3 查询导出状态

- `GET /exports/{exportId}`

### 7.4 获取作品列表

- `GET /works?page=1&pageSize=20`

### 7.5 获取作品详情

- `GET /works/{workId}`

---

## 8. 订单、支付与会员

### 8.1 创建订单

- `POST /orders`

请求：

```json
{
  "projectId": "project_001",
  "productType": "EXPORT",
  "productId": "plan_single"
}
```

### 8.2 获取支付签名

- `POST /orders/{orderId}/pay-signature`

### 8.3 查询订单列表

- `GET /orders?page=1&pageSize=20`

### 8.4 查询订单详情

- `GET /orders/{orderId}`

### 8.5 获取会员套餐

- `GET /member/plans`

### 8.6 购买会员

- `POST /member/purchases`

---

## 9. 通知与轮询

### 9.1 拉取通知列表

- `GET /notifications?cursor=xxx`

### 9.2 标记通知已读

- `POST /notifications/{notificationId}/read`

### 9.3 客户端最少要支持的轮询接口

如果推送未接通，至少保证下面三个接口稳定：

- `GET /tasks/{taskId}`
- `GET /uploads/{uploadId}`
- `GET /exports/{exportId}`

---

## 10. Android 端接入原则

Android 端后续只通过 repository 接口访问数据层。

建议映射：

- `UserRepository` -> `/auth/*`, `/users/me`, `/member/me`
- `ProjectRepository` -> `/projects/*`
- `CreationRepository` -> `/creation/*`, `/tasks/*`, `/projects/{id}/story-draft/*`, `/projects/{id}/storyboard/*`
- `OrderRepository` -> `/orders/*`
- `WorkRepository` -> `/works/*`
- `AgencyRepository` -> `/agency/*` 或机构合作相关接口

当前 Android 工程里的 fake repository 只是占位实现，后续替换为 `Network*Repository` 即可，不需要再改 UI 路由结构。

---

## 11. V1 必须先落的后端接口

优先顺序建议：

1. 登录与用户信息
2. 场景列表
3. 项目创建 / 详情 / 素材记录
4. 上传初始化 / 分片完成 / 完成上传 / 上传状态
5. 故事草稿生成任务 / 任务状态
6. 故事板生成任务 / 任务状态
7. 预览结果查询
8. 导出任务 / 导出状态
9. 订单创建 / 订单查询 / 会员信息

这 9 组接口打通后，Android 端就可以从假数据平滑切到真实后端。
