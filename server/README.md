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
python run_server.py
```

默认监听：

- Host: `127.0.0.1`
- Port: `3000`

## 环境变量

可参考：

- [server/.env.example](D:/Projects/yingrensheng/server/.env.example)

当前默认数据库预留为本机 MySQL：

- Host: `127.0.0.1`
- Port: `3306`
- Database: `yingrensheng`

当前版本已经接入 SQLAlchemy 持久化层，支持：

- `DB_DRIVER=mysql`：连接本机 MySQL
- `DB_DRIVER=sqlite`：本地单文件调试

如果你本机 MySQL 的 `root` 密码不是示例值，需要先在 `server/.env` 里填写真实账号密码，否则服务启动时会因为认证失败而退出。

## 推荐本地配置

如果你要直接接本机 MySQL：

```env
DB_DRIVER=mysql
APP_HOST=127.0.0.1
APP_PORT=3000
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_USER=root
MYSQL_PASSWORD=你的真实密码
MYSQL_DATABASE=yingrensheng
```

如果你只是想先本地联调接口：

```env
DB_DRIVER=sqlite
APP_HOST=127.0.0.1
APP_PORT=3000
SQLITE_PATH=./yingrensheng.db
```

## 外接 AI 接口

当前生成链路使用阿里云百炼 DashScope Python SDK。暂时不配置 key 时，服务端会自动返回本地演示结果，保证 App 创作流程能先跑通。

```env
DASHSCOPE_API_KEY=你的百炼APIKey
DASHSCOPE_BASE_HTTP_API_URL=https://dashscope.aliyuncs.com/api/v1

AI_TEXT_MODEL=qwen3.6-plus
AI_IMAGE_MODEL=qwen-image-2.0-pro
AI_VIDEO_MODEL=wan2.7-i2v
AI_TTS_MODEL=qwen-tts-realtime
```

如需为不同能力使用不同 key，也可以分别配置：

```env
AI_TEXT_API_KEY=文本模型Key
AI_IMAGE_API_KEY=图片模型Key
AI_VIDEO_API_KEY=视频模型Key
AI_TTS_API_KEY=TTS模型Key
```

## 健康检查

```text
GET /health
GET /api/v1/scenes
POST /api/v1/auth/login
```

## Admin 页面

## Web 工作台

后端会同时托管淡色系 Web 工作台，启动服务后访问：

```text
http://127.0.0.1:3000/web
http://127.0.0.1:3000/web/create
http://127.0.0.1:3000/web/projects
http://127.0.0.1:3000/web/works
```

默认账号：`demo / 123456`；系统管理员：`admin / admin`。

浏览器访问：

```text
http://127.0.0.1:3000/admin
```

默认后台账号：

- 用户名：`admin`
- 密码：`admin`
