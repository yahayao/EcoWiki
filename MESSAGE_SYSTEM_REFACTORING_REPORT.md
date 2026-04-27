# EcoWiki 消息模块重构报告（Python 版）

## 1. 报告信息
- 模块：消息系统
- 分支定位：`python-refactor`
- 技术栈：FastAPI + SQLAlchemy + Vue 3 + TypeScript
- 报告日期：2026-04-15

## 2. 重构目标与结果
### 目标
- 将历史 Java/Spring 消息文档与实现描述统一迁移为 Python 结构
- 对齐前后端接口命名与数据字段
- 完成消息提醒入口（头部铃铛）和消息中心页面联动

### 结果
- 已完成后端消息路由 Python 化并稳定运行
- 已完成前端消息页面与 API 对接
- 已完成头部未读数 + 下拉未读预览
- 已完成文档从 Java 描述到 Python 真实结构的清理

## 3. 当前架构
### 后端
- `www/backend/routers/messages.py`：统一消息路由
- `www/backend/models/message.py`：消息模型、状态、关系
- `www/backend/schemas/message.py`：请求/响应对象

### 前端
- `www/frontend/src/api/message.ts`：消息接口封装
- `www/frontend/src/views/Messages.vue`：消息中心
- `www/frontend/src/components/layout/HeaderUserArea.vue`：铃铛入口与未读预览

## 4. 能力清单
- 单聊发送
- 群发
- 收件箱/发件箱分页
- 会话查询
- 未读计数
- 单条已读/全部已读
- 软删除
- 头部提醒与消息中心联动

## 5. 历史注意点处理结论
以下历史“注意点”已在当前结构中处理：
1. 前端适配消息类型：已支持 `msg_type` 过滤与系统通知视图。
2. 未读入口体验不足：已接入铃铛角标和未读下拉。
3. 接口兼容问题：保留 `/received`、`/send` 别名。
4. 文档与实现不一致：已更新为 Python 实际代码路径和端点。

## 6. 风险与建议
### 当前风险
1. 发送页仍以收件人 ID 输入为主，缺少用户名检索/选择体验。

### 建议优先级
1. 增加收件人用户名检索和下拉选择。
2. 补一组消息模块集成测试（发送、已读、软删除、未读计数）。

## 7. 总结
消息系统已完成 Python 主线重构，核心功能和前端体验可用；后续工作主要是类型收敛、日志规范化和测试补齐。
