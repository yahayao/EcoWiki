# EcoWiki 消息模块实现指南（Python 版）

## 1. 模块现状
消息模块已切换到 Python 技术栈并投入使用：
- 后端：FastAPI + SQLAlchemy
- 前端：Vue 3 + TypeScript
- 认证：JWT（所有消息接口需要登录）

## 2. 代码结构（真实路径）
### 后端
- `www/backend/models/message.py`：消息模型与状态定义（`UNREAD`/`READ`/`DELETED`）
- `www/backend/schemas/message.py`：消息请求/响应 Schema
- `www/backend/routers/messages.py`：消息路由实现

### 前端
- `www/frontend/src/api/message.ts`：消息 API 封装
- `www/frontend/src/views/Messages.vue`：消息中心页面（收件箱/已发送/系统通知）
- `www/frontend/src/components/layout/HeaderUserArea.vue`：头部铃铛、未读角标、未读下拉预览

## 3. API 列表（当前实现）
基础前缀：`/api/messages`

- `GET /api/messages`：收件箱（分页，可按 `msg_type` 过滤）
- `GET /api/messages/received`：收件箱别名（兼容）
- `GET /api/messages/sent`：发件箱（分页）
- `GET /api/messages/all`：收件+发件汇总（分页）
- `GET /api/messages/conversation/{user_id}`：与指定用户对话（分页）
- `GET /api/messages/unread/count`：未读数
- `GET /api/messages/unread`：未读列表
- `POST /api/messages`：发送消息
- `POST /api/messages/send`：发送别名（兼容）
- `POST /api/messages/broadcast`：群发消息
- `PUT /api/messages/{message_id}/read`：单条标记已读
- `PUT /api/messages/read-all`：全部标记已读
- `DELETE /api/messages/{message_id}`：删除（软删除，状态置为 `DELETED`）

## 4. 前端功能现状
- 消息中心页面可查看收件箱、已发送、系统通知
- 支持分页、详情查看、回复、删除、全部已读
- 头部铃铛显示未读数量
- 鼠标悬停铃铛展示未读预览（最多 5 条）
- 未读计数默认每 30 秒轮询

## 5. 注意点处理结果（已清理）
### 已处理
1. 路由兼容问题：已提供 `/received` 与 `/send` 别名，兼容旧前端调用。
2. 删除语义不一致：已统一为软删除（`DELETED`），避免误删数据。
3. 未读提醒入口缺失：头部已接入铃铛角标和未读下拉预览。
4. 文档技术栈不一致：已移除 Java/Spring 结构描述，改为 Python/FastAPI 实际结构。
5. 群发请求校验不足：已新增 `BroadcastMessageRequest` 强类型请求模型。
6. 未读接口调试打印：已移除 `unread/count` 调试 `print` 输出。

### 当前约束（需知晓）
1. 消息发送页当前使用“收件人 ID”输入，尚未做用户名搜索选择器。

## 6. 快速联调
### 启动后端
```bash
cd www/backend
uvicorn main:app --host 0.0.0.0 --port 8080 --reload
```

### 启动前端
```bash
cd www/frontend
pnpm install
pnpm dev
```

### 验证路径
- 接口文档：`http://localhost:8080/api/docs`
- 消息中心：`http://localhost:5173/messages`

## 7. 结论
消息模块已完成 Python 化主流程，当前重点在体验增强与细节收敛，不是基础功能缺失。
