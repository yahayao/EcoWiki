# EcoWiki 消息通知功能实现指南

## 功能概述

本次实现为EcoWiki添加了完整的消息通知功能，用户可以：
- 发送消息给其他用户
- 查看收到和发送的消息
- 查看未读消息数量
- 标记消息为已读
- 删除消息

## 实现组件

### 1. 数据库表结构

已在 `COMPLETE_DATABASE_INIT.sql` 中添加了 `messages` 表：

```sql
CREATE TABLE IF NOT EXISTS `messages` (
  `message_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
  `recipient_user_id` INT NOT NULL COMMENT '接收用户ID',
  `sender_user_id` INT NOT NULL COMMENT '发送用户ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `send_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `status` VARCHAR(50) DEFAULT '未读' COMMENT '消息状态：未读、已读',
  INDEX `idx_recipient_user_id` (`recipient_user_id`),
  INDEX `idx_sender_user_id` (`sender_user_id`),
  INDEX `idx_send_time` (`send_time`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';
```

### 2. 后端实现

#### 实体类
- `Message.java` - 消息实体
- `MessageDto.java` - 消息传输对象
- `SendMessageRequest.java` - 发送消息请求对象

#### 服务层
- `MessageService.java` - 消息业务逻辑
- `MessageRepository.java` - 消息数据访问

#### 控制器
- `MessageController.java` - 消息API接口

#### API端点
- `POST /api/messages/send` - 发送消息
- `GET /api/messages/received` - 获取收到的消息
- `GET /api/messages/sent` - 获取发送的消息
- `GET /api/messages/all` - 获取所有消息
- `GET /api/messages/conversation/{userId}` - 获取与指定用户的对话
- `GET /api/messages/unread/count` - 获取未读消息数量
- `GET /api/messages/unread` - 获取未读消息列表
- `PUT /api/messages/{messageId}/read` - 标记消息为已读
- `PUT /api/messages/read-all` - 标记所有消息为已读
- `DELETE /api/messages/{messageId}` - 删除消息

### 3. 前端实现

#### API接口
- `message.ts` - 消息API接口定义

#### 组件
- `MessagePanel.vue` - 消息面板主组件
- `IconMessage.vue`, `IconCheck.vue`, `IconSend.vue`, `IconTrash.vue`, `IconClose.vue` - 图标组件

#### 界面集成
- 更新了 `HeaderUserArea.vue` - 在头像下拉菜单中添加消息通知入口
- 更新了 `AppHeader.vue` - 传递消息事件
- 更新了 `App.vue` - 添加消息面板模态框
- 更新了 `ClassicHome.vue` - 传递消息事件

## 功能特性

### 1. 消息列表
- 支持分页显示
- 按发送时间倒序排列
- 显示发送者/接收者信息
- 显示消息状态（已读/未读）

### 2. 消息发送
- 简洁的发送界面
- 输入验证
- 实时反馈

### 3. 未读提醒
- 头像下拉菜单显示未读数量badge
- 自动定期更新未读数量（每30秒）
- 登录状态变化时自动更新

### 4. 消息管理
- 标记单条消息为已读
- 批量标记所有消息为已读
- 删除消息功能

### 5. 响应式设计
- 支持桌面端和移动端
- 适配不同屏幕尺寸

## 使用方法

### 1. 数据库初始化
运行 `COMPLETE_DATABASE_INIT.sql` 来创建消息表

### 2. 后端启动
确保后端服务正常运行，消息API将在 `/api/messages/*` 路径下可用

### 3. 前端使用
1. 用户登录后，点击头像查看下拉菜单
2. 点击"消息通知"打开消息面板
3. 可以查看收到的消息、发送的消息等
4. 点击"发送消息"可以给其他用户发送消息

## 注意事项

### 1. 权限控制
- 所有消息API都需要用户登录
- 用户只能查看和操作自己相关的消息

### 2. 安全性
- 使用JWT认证
- 防止越权操作
- 输入内容验证

### 3. 性能优化
- 支持分页加载
- 索引优化查询
- 前端缓存未读数量

## 扩展建议

### 1. 实时推送
可以集成WebSocket实现实时消息推送

### 2. 消息类型
可以扩展支持图片、文件等多媒体消息

### 3. 群组消息
可以扩展支持群组聊天功能

### 4. 消息搜索
可以添加消息内容搜索功能

## 开发状态

✅ 数据库表结构设计
✅ 后端API实现
✅ 前端组件开发
✅ 界面集成
✅ 响应式设计
🔲 实时推送（待扩展）
🔲 多媒体消息（待扩展）

## 联系支持

如有问题，请查看项目文档或联系开发团队。
