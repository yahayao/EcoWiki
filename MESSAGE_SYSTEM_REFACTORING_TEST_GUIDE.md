# EcoWiki 消息系统重构测试指南

## 概述
本文档提供重构后消息系统的测试指南，帮助验证新功能和性能改进。

## 版本信息
- **系统版本**: 2.0.0
- **重构日期**: 2025-09-14
- **主要改进**: 类型安全、性能优化、功能增强

---

## 1. 数据库升级测试

### 1.1 运行数据库升级脚本
```bash
# 进入项目目录
cd d:\Desktop\Code\EcoWiki\www\backend

# 连接MySQL数据库
mysql -u root -p ecowiki

# 执行升级脚本
source src/main/resources/db/migration/message_system_upgrade_v2.sql
```

### 1.2 验证数据库结构
```sql
-- 检查新增字段
DESCRIBE messages;

-- 验证索引创建
SHOW INDEX FROM messages;

-- 检查数据迁移情况
SELECT 
    COUNT(*) as total_messages,
    COUNT(CASE WHEN status = 'UNREAD' THEN 1 END) as unread_count,
    COUNT(CASE WHEN message_type = 'SYSTEM_NOTIFICATION' THEN 1 END) as system_notifications
FROM messages;

-- 查看升级通知消息
SELECT * FROM messages WHERE subject = '消息系统升级通知' LIMIT 5;
```

---

## 2. API接口测试

### 2.1 启动后端服务
```bash
cd d:\Desktop\Code\EcoWiki\www\backend
mvn spring-boot:run
```

### 2.2 测试发送消息接口

#### 基本消息发送
```bash
curl -X POST http://localhost:8080/api/v2/messages/send \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=你的会话ID" \
  -d '{
    "recipientUserId": 2,
    "content": "这是重构后的消息系统测试",
    "subject": "系统测试",
    "messageType": "USER_PRIVATE",
    "priority": 2
  }'
```

#### 系统通知发送
```bash
curl -X POST http://localhost:8080/api/v2/messages/system-notification \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=你的会话ID" \
  -d '{
    "recipientUserId": 2,
    "content": "重要系统通知：消息系统已升级",
    "subject": "系统升级通知",
    "priority": 4,
    "expireHours": 72,
    "metadata": {
      "notificationType": "UPGRADE",
      "version": "2.0.0"
    }
  }'
```

#### 群发消息
```bash
curl -X POST http://localhost:8080/api/v2/messages/broadcast \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=你的会话ID" \
  -d '{
    "recipientUserIds": [2, 3, 4],
    "content": "群发测试消息",
    "subject": "重要通知",
    "messageType": "ANNOUNCEMENT",
    "priority": 3
  }'
```

### 2.3 测试消息查询接口

#### 获取收到的消息
```bash
curl -X GET "http://localhost:8080/api/v2/messages/received?page=0&size=10" \
  -H "Cookie: JSESSIONID=你的会话ID"
```

#### 按类型查询消息
```bash
curl -X GET "http://localhost:8080/api/v2/messages/by-type/SYSTEM_NOTIFICATION?page=0&size=5" \
  -H "Cookie: JSESSIONID=你的会话ID"
```

#### 获取未读消息数量
```bash
curl -X GET "http://localhost:8080/api/v2/messages/unread-count" \
  -H "Cookie: JSESSIONID=你的会话ID"
```

#### 获取对话记录
```bash
curl -X GET "http://localhost:8080/api/v2/messages/conversation/2?page=0&size=20" \
  -H "Cookie: JSESSIONID=你的会话ID"
```

### 2.4 测试消息操作接口

#### 标记单条消息已读
```bash
curl -X PUT "http://localhost:8080/api/v2/messages/1/mark-read" \
  -H "Cookie: JSESSIONID=你的会话ID"
```

#### 批量标记已读
```bash
curl -X PUT "http://localhost:8080/api/v2/messages/batch-mark-read" \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=你的会话ID" \
  -d '[1, 2, 3]'
```

#### 全部标记已读
```bash
curl -X PUT "http://localhost:8080/api/v2/messages/mark-all-read" \
  -H "Cookie: JSESSIONID=你的会话ID"
```

#### 撤回消息
```bash
curl -X PUT "http://localhost:8080/api/v2/messages/1/recall" \
  -H "Cookie: JSESSIONID=你的会话ID"
```

#### 删除消息
```bash
curl -X DELETE "http://localhost:8080/api/v2/messages/1" \
  -H "Cookie: JSESSIONID=你的会话ID"
```

---

## 3. 性能测试

### 3.1 批量消息性能测试
```bash
# 创建测试脚本
cat > test_batch_messages.sh << 'EOF'
#!/bin/bash
JSESSIONID="你的会话ID"
BASE_URL="http://localhost:8080/api/v2/messages"

echo "开始批量发送测试..."
start_time=$(date +%s)

for i in {1..100}; do
  curl -s -X POST "$BASE_URL/send" \
    -H "Content-Type: application/json" \
    -H "Cookie: JSESSIONID=$JSESSIONID" \
    -d "{\"recipientUserId\": 2, \"content\": \"批量测试消息 #$i\", \"messageType\": \"USER_PRIVATE\"}" > /dev/null
  if [ $((i % 10)) -eq 0 ]; then
    echo "已发送 $i 条消息..."
  fi
done

end_time=$(date +%s)
duration=$((end_time - start_time))
echo "批量发送完成，耗时: ${duration}秒"
EOF

chmod +x test_batch_messages.sh
./test_batch_messages.sh
```

### 3.2 查询性能测试
```bash
# 测试分页查询性能
echo "测试分页查询性能..."
time curl -s "http://localhost:8080/api/v2/messages/received?page=0&size=50" \
  -H "Cookie: JSESSIONID=你的会话ID" > /dev/null

# 测试未读消息统计性能
echo "测试未读消息统计性能..."
time curl -s "http://localhost:8080/api/v2/messages/unread-count" \
  -H "Cookie: JSESSIONID=你的会话ID" > /dev/null
```

---

## 4. 功能验证清单

### 4.1 核心功能验证
- [ ] 用户登录系统
- [ ] 发送私人消息
- [ ] 发送系统通知
- [ ] 群发消息功能
- [ ] 查看收到的消息
- [ ] 查看发送的消息
- [ ] 按类型筛选消息
- [ ] 按状态筛选消息
- [ ] 查看对话记录
- [ ] 标记消息已读
- [ ] 批量操作消息
- [ ] 撤回消息功能
- [ ] 删除消息功能
- [ ] 未读消息统计
- [ ] 消息搜索功能

### 4.2 新功能验证
- [ ] 消息类型枚举（USER_PRIVATE, SYSTEM_NOTIFICATION等）
- [ ] 消息优先级（1-4级别）
- [ ] 消息过期机制
- [ ] 消息元数据支持
- [ ] 软删除和撤回
- [ ] 消息状态转换
- [ ] 自动清理过期消息
- [ ] 消息统计和分析
- [ ] 批量标记已读
- [ ] 高优先级消息提醒

### 4.3 性能验证
- [ ] 分页查询响应时间 < 200ms
- [ ] 未读消息统计响应时间 < 100ms
- [ ] 批量操作支持（100条以内）
- [ ] 数据库索引优化效果
- [ ] N+1查询问题解决
- [ ] 用户信息缓存效果

---

## 5. 错误处理测试

### 5.1 权限验证测试
```bash
# 测试未登录访问
curl -X GET "http://localhost:8080/api/v2/messages/received" 
# 预期：401 Unauthorized

# 测试访问他人消息
curl -X PUT "http://localhost:8080/api/v2/messages/999/mark-read" \
  -H "Cookie: JSESSIONID=你的会话ID"
# 预期：403 Forbidden 或 404 Not Found
```

### 5.2 参数验证测试
```bash
# 测试无效的接收者ID
curl -X POST http://localhost:8080/api/v2/messages/send \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=你的会话ID" \
  -d '{"recipientUserId": -1, "content": "测试"}'
# 预期：400 Bad Request

# 测试空消息内容
curl -X POST http://localhost:8080/api/v2/messages/send \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=你的会话ID" \
  -d '{"recipientUserId": 2, "content": ""}'
# 预期：400 Bad Request
```

### 5.3 业务逻辑测试
```bash
# 测试撤回超时的消息（假设消息ID 1是很久之前的消息）
curl -X PUT "http://localhost:8080/api/v2/messages/1/recall" \
  -H "Cookie: JSESSIONID=你的会话ID"
# 预期：400 Bad Request（撤回时间已过期）

# 测试删除不存在的消息
curl -X DELETE "http://localhost:8080/api/v2/messages/99999" \
  -H "Cookie: JSESSIONID=你的会话ID"
# 预期：404 Not Found
```

---

## 6. 兼容性测试

### 6.1 与前端的兼容性
- [ ] 检查现有MessagePanel.vue组件是否正常工作
- [ ] 验证API响应格式兼容性
- [ ] 测试消息显示和交互功能

### 6.2 与数据库的兼容性
- [ ] 验证现有数据完整性
- [ ] 检查索引性能提升
- [ ] 确认触发器和存储过程正常工作

---

## 7. 后续优化建议

### 7.1 前端集成
1. 更新MessagePanel.vue以支持新的消息类型
2. 添加消息优先级显示
3. 实现消息过期提醒
4. 优化批量操作界面

### 7.2 系统监控
1. 添加消息发送量监控
2. 实现消息处理性能监控
3. 添加错误率统计
4. 设置消息积压报警

### 7.3 功能扩展
1. 消息模板系统
2. 消息推送集成
3. 消息审核机制
4. 消息分析报表

---

## 8. 问题排查

### 8.1 常见问题
1. **编译错误**: 检查依赖和导入
2. **数据库连接**: 确认数据库服务运行
3. **权限问题**: 验证用户登录状态
4. **性能问题**: 检查数据库索引

### 8.2 日志查看
```bash
# 查看应用日志
tail -f d:\Desktop\Code\EcoWiki\www\backend\logs\ecowiki-error.log

# 查看数据库慢查询日志（如果启用）
# 在MySQL中执行：
# SET GLOBAL slow_query_log = 'ON';
# SET GLOBAL long_query_time = 1;
```

---

## 结论

重构后的消息系统提供了：
- ✅ 更好的类型安全性
- ✅ 增强的功能特性  
- ✅ 优化的性能表现
- ✅ 完善的错误处理
- ✅ 向后兼容性

按照本指南进行测试，确保系统稳定运行后即可投入生产使用。