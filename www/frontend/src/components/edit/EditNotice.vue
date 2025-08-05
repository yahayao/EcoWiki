<!--
/**
 * 编辑提示组件
 * 
 * 功能包括：
 * - 编辑页面提示信息显示
 * - 编辑规则和使用说明
 * - 贡献指导和注意事项
 * - 内容创建规范提醒
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
-->
<template>
  <div class="edit-notice">
    <div class="notice-icon">💡</div>
    <div class="notice-content">
      <p><strong>关于页面编辑：</strong> 我们很高兴您愿意为本项目做出贡献，您也是为这一次性接受新条目的贡献，我们仅能接受作者授权条目或原创。</p>
      <p><strong>关于页面内容：</strong> 此页面作为发布到互联网的条目，所以其在流程语义信息，如有雷同工作的实体条目条目，访问者应该是对同类条目至关重要的平台。</p>
    </div>
  </div>
</template>

<style scoped>
.edit-notice {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
  border: 1px solid #f59e0b;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(245, 158, 11, 0.1);
}

.notice-icon {
  font-size: 1.25rem;
  flex-shrink: 0;
  margin-top: 0.125rem;
}

.notice-content {
  flex: 1;
}

.notice-content p {
  margin: 0 0 0.75rem 0;
  font-size: 0.875rem;
  line-height: 1.5;
  color: #92400e;
}

.notice-content p:last-child {
  margin-bottom: 0;
}

.notice-content strong {
  font-weight: 600;
  color: #78350f;
}
</style>
