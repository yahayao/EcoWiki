# EcoWiki 消息模块测试指南（Python 版）

## 1. 测试前准备
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

### 获取 Token
在登录接口成功后，复制 `Bearer` Token，用于后续 `Authorization` 请求头。

## 2. API 冒烟测试
以下示例均基于：`http://localhost:8080`。

### 2.1 发送消息
```bash
curl -X POST "http://localhost:8080/api/messages" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "recipient_user_id": 2,
    "subject": "测试消息",
    "content": "这是 FastAPI 消息接口测试",
    "message_type": "USER",
    "priority": 1
  }'
```

### 2.2 收件箱分页
```bash
curl -X GET "http://localhost:8080/api/messages?page=0&size=20" \
  -H "Authorization: Bearer <TOKEN>"
```

### 2.3 发件箱分页
```bash
curl -X GET "http://localhost:8080/api/messages/sent?page=0&size=20" \
  -H "Authorization: Bearer <TOKEN>"
```

### 2.4 未读数
```bash
curl -X GET "http://localhost:8080/api/messages/unread/count" \
  -H "Authorization: Bearer <TOKEN>"
```

### 2.5 未读列表
```bash
curl -X GET "http://localhost:8080/api/messages/unread" \
  -H "Authorization: Bearer <TOKEN>"
```

### 2.6 标记已读
```bash
curl -X PUT "http://localhost:8080/api/messages/1/read" \
  -H "Authorization: Bearer <TOKEN>"
```

### 2.7 全部已读
```bash
curl -X PUT "http://localhost:8080/api/messages/read-all" \
  -H "Authorization: Bearer <TOKEN>"
```

### 2.8 删除消息（软删除）
```bash
curl -X DELETE "http://localhost:8080/api/messages/1" \
  -H "Authorization: Bearer <TOKEN>"
```

### 2.9 群发消息
```bash
curl -X POST "http://localhost:8080/api/messages/broadcast" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "recipient_user_ids": [2, 3],
    "subject": "群发通知",
    "content": "请查收测试通知"
  }'
```

## 3. 前端联动测试
1. 登录后进入首页，确认头部铃铛显示未读角标。
2. 悬停铃铛，确认出现未读预览列表（最多 5 条）。
3. 点击“查看全部消息”，跳转 `/messages`。
4. 在消息中心执行“单条已读、全部已读、删除”，确认与角标同步。
5. 在“撰写消息”中发送后，切换到“已发送”确认可见。

## 4. 回归检查清单
- [ ] 未登录访问消息接口返回 401
- [ ] 只能读取自己的收件消息
- [ ] 只能操作自己可见的消息（已读/删除）
- [ ] 删除后消息状态为 `DELETED`，而非物理删除
- [ ] `/received` 与 `/send` 兼容端点可用
- [ ] `msg_type=SYSTEM` 过滤正确
- [ ] 未读数在读消息后减少
- [ ] 铃铛轮询（30 秒）能刷新计数

## 5. 注意点（已处理/待优化）
### 已处理
1. 历史 Java 路径与接口名已全部替换为 Python 真实路径。
2. 会话认证示例已统一为 `Bearer Token`。
3. 接口示例已统一为 `/api/messages` 命名空间。
4. `broadcast` 已切换为强类型请求模型。
5. `unread/count` 已移除调试打印。

### 待优化（非阻塞）
1. 发送页可补充用户名检索，减少对收件人 ID 的依赖。

## 6. 结论
当前消息模块可进行完整联调与回归，文档已与 Python 实现对齐。
