# 映人生 Server

映人生后端服务，按前后端分离方式为 Android 客户端提供认证、项目、上传、AI 任务、导出、订单与会员接口。

当前版本先实现最小可联调骨架：

- 登录
- 当前用户
- 场景列表
- 项目创建 / 查询
- 上传初始化 / 查询
- AI 任务查询

## 本地启动

```powershell
python -m uvicorn app.main:app --host 127.0.0.1 --port 8080 --reload
```

## 健康检查

```text
GET /health
GET /api/v1/scenes
POST /api/v1/auth/login
```

