<!--
/**
 * 用户文章管理组件
 * 
 * 功能：
 * - 提供用户文章创作和收藏管理界面
 * - 展示用户发布的文章列表
 * - 支持文章编辑、删除和状态管理
 * - 提供文章统计和分析功能
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
-->
<template>
  <div class="article-container">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M19,3H14.82C14.4,1.84 13.3,1 12,1C10.7,1 9.6,1.84 9.18,3H5A2,2 0 0,0 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V5A2,2 0 0,0 19,3M12,3A1,1 0 0,1 13,4A1,1 0 0,1 12,5A1,1 0 0,1 11,4A1,1 0 0,1 12,3" />
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">文章管理</h1>
          <p class="page-subtitle">管理您的文章创作和收藏</p>
        </div>
      </div>
      <div class="header-actions">
        <button class="create-btn" @click="createNewArticle">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z"/>
          </svg>
          新建文章
        </button>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-overview">
      <div class="stat-card clickable" :class="{ active: activeTab === 'favorites' }" @click="onTabChange('favorites')">
        <div class="stat-icon favorites">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M12,21.35L10.55,20.03C5.4,15.36 2,12.27 2,8.5C2,5.41 4.42,3 7.5,3C9.24,3 10.91,3.81 12,5.08C13.09,3.81 14.76,3 16.5,3C19.58,3 22,5.41 22,8.5C22,12.27 18.6,15.36 13.45,20.03L12,21.35Z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ favoriteCount }}</div>
          <div class="stat-label">收藏文章</div>
        </div>
      </div>
      
      <div class="stat-card clickable" :class="{ active: activeTab === 'created' }" @click="onTabChange('created')">
        <div class="stat-icon created">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ createdCount }}</div>
          <div class="stat-label">已发布</div>
        </div>
      </div>
      
      <div class="stat-card clickable" :class="{ active: activeTab === 'drafts' }" @click="onTabChange('drafts')">
        <div class="stat-icon drafts">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ draftCount }}</div>
          <div class="stat-label">草稿</div>
        </div>
      </div>

      <div class="stat-card clickable" :class="{ active: activeTab === 'liked' }" @click="onTabChange('liked')">
        <div class="stat-icon liked">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M23,10C23,8.89 22.1,8 21,8H14.68L15.64,3.43C15.66,3.33 15.67,3.22 15.67,3.11C15.67,2.7 15.5,2.32 15.23,2.05L14.17,1L7.59,7.58C7.22,7.95 7,8.45 7,9V19A2,2 0 0,0 9,21H18C18.83,21 19.54,20.5 19.84,19.78L22.86,12.73C22.95,12.5 23,12.26 23,12V10.08L23,10M1,21H5V9H1V21Z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ likedCount }}</div>
          <div class="stat-label">点赞文章</div>
        </div>
      </div>
    </div>
    
    <!-- 文章列表 -->
    <div class="article-content">
      <div v-if="activeTab === 'favorites'" class="article-grid">
        <div v-for="article in favoriteArticles" :key="article.id" class="article-card">
          <div class="card-header">
            <div class="article-status favorites">
              <svg viewBox="0 0 24 24" class="status-icon">
                <path d="M12,21.35L10.55,20.03C5.4,15.36 2,12.27 2,8.5C2,5.41 4.42,3 7.5,3C9.24,3 10.91,3.81 12,5.08C13.09,3.81 14.76,3 16.5,3C19.58,3 22,5.41 22,8.5C22,12.27 18.6,15.36 13.45,20.03L12,21.35Z"/>
              </svg>
              收藏
            </div>
          </div>
          <div class="card-content">
            <h4 class="article-title">{{ article.title }}</h4>
            <p class="article-excerpt">{{ article.excerpt }}</p>
            <div class="article-meta">
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4M12,6A6,6 0 0,1 18,12A6,6 0 0,1 12,18A6,6 0 0,1 6,12A6,6 0 0,1 12,6M12,8A4,4 0 0,0 8,12A4,4 0 0,0 12,16A4,4 0 0,0 16,12A4,4 0 0,0 12,8Z"/>
                </svg>
                {{ article.favoriteDate }}
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5M12,17C9.24,17 7,14.76 7,12C7,9.24 9.24,7 12,7C14.76,7 17,9.24 17,12C17,14.76 14.76,17 12,17M12,9C10.34,9 9,10.34 9,12C9,13.66 10.34,15 12,15C13.66,15 15,13.66 15,12C15,10.34 13.66,9 12,9Z"/>
                </svg>
                {{ article.views }} 阅读
              </span>
            </div>
          </div>
          <div class="card-actions">
            <button class="action-btn primary" @click="viewArticle(article.articleId || article.id)">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5M12,17C9.24,17 7,14.76 7,12C7,9.24 9.24,7 12,7C14.76,7 17,9.24 17,12C17,14.76 14.76,17 12,17M12,9C10.34,9 9,10.34 9,12C9,13.66 10.34,15 12,15C13.66,15 15,13.66 15,12C15,10.34 13.66,9 12,9Z"/>
              </svg>
              查看
            </button>
            <button class="action-btn secondary" @click="unfavoriteArticle(article.articleId || article.id)">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M12.1,18.55L12,18.65L11.89,18.55C7.14,14.24 4,11.39 4,8.5C4,6.5 5.5,5 7.5,5C9.04,5 10.54,6 11.07,7.36H12.93C13.46,6 14.96,5 16.5,5C18.5,5 20,6.5 20,8.5C20,11.39 16.86,14.24 12.1,18.55M16.5,3C14.76,3 13.09,3.81 12,5.08C10.91,3.81 9.24,3 7.5,3C4.42,3 2,5.41 2,8.5C2,12.27 5.4,15.36 10.55,20.03L12,21.35L13.45,20.03C18.6,15.36 22,12.27 22,8.5C22,5.41 19.58,3 16.5,3Z"/>
              </svg>
              取消收藏
            </button>
          </div>
        </div>
      </div>
      
      <div v-else-if="activeTab === 'created'" class="article-grid">
        <div v-for="article in createdArticles" :key="article.id" class="article-card">
          <div class="card-header">
            <div class="article-status published">
              <svg viewBox="0 0 24 24" class="status-icon">
                <path d="M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z"/>
              </svg>
              已发布
            </div>
          </div>
          <div class="card-content">
            <h4 class="article-title">{{ article.title }}</h4>
            <p class="article-excerpt">{{ article.excerpt }}</p>
            <div class="article-meta">
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M9,10V12H7V10H9M13,10V12H11V10H13M17,10V12H15V10H17M19,3A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5C3.89,21 3,20.1 3,19V5A2,2 0 0,1 5,3H6V1H8V3H16V1H18V3H19M19,19V8H5V19H19M19,5H5V6H19V5Z"/>
                </svg>
                {{ article.publishDate }}
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5M12,17C9.24,17 7,14.76 7,12C7,9.24 9.24,7 12,7C14.76,7 17,9.24 17,12C17,14.76 14.76,17 12,17M12,9C10.34,9 9,10.34 9,12C9,13.66 10.34,15 12,15C13.66,15 15,13.66 15,12C15,10.34 13.66,9 12,9Z"/>
                </svg>
                {{ article.views }} 阅读
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M12,21.35L10.55,20.03C5.4,15.36 2,12.27 2,8.5C2,5.41 4.42,3 7.5,3C9.24,3 10.91,3.81 12,5.08C13.09,3.81 14.76,3 16.5,3C19.58,3 22,5.41 22,8.5C22,12.27 18.6,15.36 13.45,20.03L12,21.35Z"/>
                </svg>
                {{ article.likes }} 点赞
              </span>
            </div>
          </div>
          <div class="card-actions">
            <button class="action-btn primary" @click="editArticle(article.articleId || article.id)">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
              </svg>
              编辑
            </button>
            <button class="action-btn secondary" @click="viewArticle(article.articleId || article.id)">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5M12,17C9.24,17 7,14.76 7,12C7,9.24 9.24,7 12,7C14.76,7 17,9.24 17,12C17,14.76 14.76,17 12,17M12,9C10.34,9 9,10.34 9,12C9,13.66 10.34,15 12,15C13.66,15 15,13.66 15,12C15,10.34 13.66,9 12,9Z"/>
              </svg>
              查看
            </button>
          </div>
        </div>
      </div>
      
      <div v-else-if="activeTab === 'drafts'" class="article-grid">
        <div v-for="draft in draftArticles" :key="draft.draftId" class="article-card draft-card">
          <!-- 状态徽标：右上角绝对定位 -->
          <div class="draft-status-badge" :class="`status-${draft.status}`">
            <svg v-if="draft.status === 'pending'" viewBox="0 0 24 24" class="status-icon">
              <path d="M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M16.2,16.2L11,13V7H12.5V12.2L17,14.9L16.2,16.2Z"/>
            </svg>
            <svg v-else-if="draft.status === 'rejected'" viewBox="0 0 24 24" class="status-icon">
              <path d="M13,13H11V7H13M13,17H11V15H13M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2Z"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" class="status-icon">
              <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
            </svg>
            {{ getDraftStatusLabel(draft.status) }}
          </div>

          <div class="card-content" style="padding-top: 48px;">
            <h4 class="article-title">{{ draft.title }}</h4>
            <!-- 拒稿原因提示 -->
            <div v-if="draft.status === 'rejected' && draft.rejectReason" class="reject-reason-box">
              <svg viewBox="0 0 24 24" class="reject-icon">
                <path d="M13,13H11V7H13M13,17H11V15H13M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2Z"/>
              </svg>
              <span>拒绝原因：{{ draft.rejectReason }}</span>
            </div>
            <p class="article-excerpt">{{ draft.excerpt }}</p>
            <div class="article-meta">
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M9,10V12H7V10H9M13,10V12H11V10H13M17,10V12H15V10H17M19,3A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5C3.89,21 3,20.1 3,19V5A2,2 0 0,1 5,3H6V1H8V3H16V1H18V3H19M19,19V8H5V19H19M19,5H5V6H19V5Z"/>
                </svg>
                保存于 {{ draft.lastSaved }}
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z"/>
                </svg>
                {{ draft.wordCount }} 字
              </span>
            </div>
          </div>

          <div class="card-actions">
            <template v-if="draft.status === 'pending'">
              <button class="action-btn disabled-btn" disabled>
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M16.2,16.2L11,13V7H12.5V12.2L17,14.9L16.2,16.2Z"/>
                </svg>
                审核中
              </button>
            </template>
            <template v-else>
              <button class="action-btn primary" @click="editDraft(draft.draftId)">
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
                </svg>
                {{ draft.status === 'rejected' ? '重新编辑' : '继续编辑' }}
              </button>
            </template>
            <button class="action-btn danger" @click="deleteDraft(draft.draftId)">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z"/>
              </svg>
              删除
            </button>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'liked'" class="article-grid">
        <div v-for="article in likedArticles" :key="article.id" class="article-card">
          <div class="card-header">
            <div class="article-status liked">
              <svg viewBox="0 0 24 24" class="status-icon">
                <path d="M23,10C23,8.89 22.1,8 21,8H14.68L15.64,3.43C15.66,3.33 15.67,3.22 15.67,3.11C15.67,2.7 15.5,2.32 15.23,2.05L14.17,1L7.59,7.58C7.22,7.95 7,8.45 7,9V19A2,2 0 0,0 9,21H18C18.83,21 19.54,20.5 19.84,19.78L22.86,12.73C22.95,12.5 23,12.26 23,12V10.08L23,10M1,21H5V9H1V21Z"/>
              </svg>
              已点赞
            </div>
          </div>
          <div class="card-content">
            <h4 class="article-title">{{ article.title }}</h4>
            <p class="article-excerpt">{{ article.excerpt }}</p>
            <div class="article-meta">
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4M12,6A6,6 0 0,1 18,12A6,6 0 0,1 12,18A6,6 0 0,1 6,12A6,6 0 0,1 12,6M12,8A4,4 0 0,0 8,12A4,4 0 0,0 12,16A4,4 0 0,0 16,12A4,4 0 0,0 12,8Z"/>
                </svg>
                点赞于 {{ article.likedDate }}
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5M12,17C9.24,17 7,14.76 7,12C7,9.24 9.24,7 12,7C14.76,7 17,9.24 17,12C17,14.76 14.76,17 12,17M12,9C10.34,9 9,10.34 9,12C9,13.66 10.34,15 12,15C13.66,15 15,13.66 15,12C15,10.34 13.66,9 12,9Z"/>
                </svg>
                {{ article.views }} 阅读
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M23,10C23,8.89 22.1,8 21,8H14.68L15.64,3.43C15.66,3.33 15.67,3.22 15.67,3.11C15.67,2.7 15.5,2.32 15.23,2.05L14.17,1L7.59,7.58C7.22,7.95 7,8.45 7,9V19A2,2 0 0,0 9,21H18C18.83,21 19.54,20.5 19.84,19.78L22.86,12.73C22.95,12.5 23,12.26 23,12V10.08L23,10M1,21H5V9H1V21Z"/>
                </svg>
                {{ article.likes }} 点赞
              </span>
              <span class="meta-item">
                <UserAvatar 
                  :username="article.author"
                  :avatar-url="article.authorAvatar || ''"
                  size="xs"
                  shape="circle"
                  class="meta-avatar"
                />
                {{ article.author }}
              </span>
            </div>
          </div>
          <div class="card-actions">
            <button class="action-btn primary" @click="viewArticle(article.articleId || article.id)">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5M12,17C9.24,17 7,14.76 7,12C7,9.24 9.24,7 12,7C14.76,7 17,9.24 17,12C17,14.76 14.76,17 12,17M12,9C10.34,9 9,10.34 9,12C9,13.66 10.34,15 12,15C13.66,15 15,13.66 15,12C15,10.34 13.66,9 12,9Z"/>
              </svg>
              查看
            </button>
            <button class="action-btn secondary" @click="unlikeArticle(article.articleId || article.id)">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M23,10C23,8.89 22.1,8 21,8H14.68L15.64,3.43C15.66,3.33 15.67,3.22 15.67,3.11C15.67,2.7 15.5,2.32 15.23,2.05L14.17,1L7.59,7.58C7.22,7.95 7,8.45 7,9V19A2,2 0 0,0 9,21H18C18.83,21 19.54,20.5 19.84,19.78L22.86,12.73C22.95,12.5 23,12.26 23,12V10.08L23,10M1,21H5V9H1V21Z"/>
              </svg>
              取消点赞
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 草稿重新编辑 Modal -->
  <div v-if="editingDraft" class="draft-edit-overlay" @click.self="closeDraftEditor">
    <div class="draft-edit-modal">
      <div class="modal-header">
        <h3 class="modal-title">重新编辑草稿</h3>
        <button class="modal-close" @click="closeDraftEditor">×</button>
      </div>
      <div class="modal-body">
        <div class="modal-article-title">{{ editingDraft.title }}</div>
        <div v-if="editingDraft.rejectReason" class="modal-reject-reason">
          <strong>原拒绝原因：</strong>{{ editingDraft.rejectReason }}
        </div>
        <div class="form-group">
          <label class="form-label">分类</label>
          <input v-model="editForm.category" type="text" class="form-input" placeholder="请输入文章分类" />
        </div>
        <div class="form-group">
          <label class="form-label">内容</label>
          <textarea v-model="editForm.content" class="form-textarea" rows="14" placeholder="请输入文章内容" />
        </div>
      </div>
      <div class="modal-footer">
        <button class="action-btn secondary" @click="closeDraftEditor">取消</button>
        <button class="action-btn primary" :disabled="savingDraftEdit" @click="submitDraftEdit">
          {{ savingDraftEdit ? '提交中...' : '保存并重新提交审核' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/user'
import { articleApi } from '@/api/article'
import { draftApi, type ArticleDraft } from '@/api/draft'
import toast from '@/utils/toast'
import UserAvatar from '@/components/common/UserAvatar.vue'

const router = useRouter()

const activeTab = ref('favorites')

// 数据状态
const favoriteArticles = ref<any[]>([])
const createdArticles = ref<any[]>([])
const draftArticles = ref<any[]>([])
const likedArticles = ref<any[]>([])
const loading = ref(false)
const articleStats = ref({
  totalArticles: 0,
  publishedArticles: 0,
  draftArticles: 0,
  favoriteArticles: 0,
  likedArticles: 0,
  totalViews: 0,
  totalLikes: 0
})

// 草稿总数（独立维护，避免依赖 articles 表的 status='draft' 统计）
const draftTotalCount = ref(0)

// 统计数据计算属性
const favoriteCount = computed(() => articleStats.value.favoriteArticles)
const createdCount = computed(() => articleStats.value.publishedArticles)
const draftCount = computed(() => draftTotalCount.value)
const likedCount = computed(() => articleStats.value.likedArticles)

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '无'
  try {
    return new Date(dateString).toLocaleDateString('zh-CN')
  } catch {
    return '无效日期'
  }
}

// 加载文章统计数据
const loadArticleStats = async () => {
  try {
    const stats = await userApi.getUserArticleStats()
    articleStats.value = {
      ...stats,
      likedArticles: (stats as any).likedArticles || 0  // 添加默认值以防API未返回此字段
    }
  } catch (error: any) {
    console.error('加载文章统计失败:', error)
    toast.error('加载文章统计失败', '错误')
  }
}

// 加载收藏文章
const loadFavoriteArticles = async () => {
  try {
    loading.value = true
    const result = await userApi.getFavoriteArticles(0, 20)
    favoriteArticles.value = result.content.map((article: any) => ({
      ...article,
      favoriteDate: formatDate(article.favoriteDate || article.createdAt),
      excerpt: article.content ? article.content.substring(0, 100) + '...' : '暂无摘要'
    }))
  } catch (error: any) {
    console.error('加载收藏文章失败:', error)
    toast.error('加载收藏文章失败', '错误')
    favoriteArticles.value = []
  } finally {
    loading.value = false
  }
}

// 加载用户创建的文章
const loadCreatedArticles = async () => {
  try {
    loading.value = true
    const result = await userApi.getUserArticles(0, 20, 'published')
    createdArticles.value = result.content.map((article: any) => ({
      ...article,
      publishDate: formatDate(article.createdAt),
      excerpt: article.content ? article.content.substring(0, 100) + '...' : '暂无摘要'
    }))
  } catch (error: any) {
    console.error('加载创建文章失败:', error)
    toast.error('加载创建文章失败', '错误')
    createdArticles.value = []
  } finally {
    loading.value = false
  }
}

// 加载草稿文章
const loadDraftArticles = async () => {
  try {
    loading.value = true
    const result = await draftApi.getMyDrafts(0, 20)
    draftArticles.value = result.content.map((draft: ArticleDraft) => ({
      ...draft,
      lastSaved: formatDate((draft.updatedAt || draft.createdAt || draft.submittedAt) ?? ''),
      excerpt: draft.content ? draft.content.substring(0, 100) + '...' : '暂无摘要',
      wordCount: draft.content ? draft.content.length : 0,
    }))
    draftTotalCount.value = result.totalElements
  } catch (error: any) {
    console.error('加载草稿文章失败:', error)
    toast.error('加载草稿文章失败', '错误')
    draftArticles.value = []
  } finally {
    loading.value = false
  }
}

// 加载点赞文章
const loadLikedArticles = async () => {
  try {
    loading.value = true
    const result = await userApi.getLikedArticles(0, 20)
    likedArticles.value = result.content.map((article: any) => ({
      ...article,
      likedDate: formatDate(article.likedAt || article.createdAt),
      excerpt: article.content ? article.content.substring(0, 100) + '...' : '暂无摘要'
    }))
  } catch (error: any) {
    console.error('加载点赞文章失败:', error)
    toast.error('加载点赞文章失败', '错误')
    // 使用空数组作为后备方案
    likedArticles.value = []
  } finally {
    loading.value = false
  }
}

// 文章操作方法
const createNewArticle = () => {
  router.push('/create')
}

const viewArticle = async (articleId: number) => {
  try {
    // 通过ID获取文章信息以获得title
    const article = await articleApi.getArticleById(articleId)
    router.push({ name: 'ArticleDetail', params: { title: article.title } })
  } catch (error: any) {
    console.error('获取文章信息失败:', error)
    toast.error('无法打开文章', '错误')
  }
}

const editArticle = async (articleId: number) => {
  try {
    // 通过ID获取文章信息以获得title
    const article = await articleApi.getArticleById(articleId)
    router.push({ name: 'ArticleEdit', params: { title: article.title } })
  } catch (error: any) {
    console.error('获取文章信息失败:', error)
    toast.error('无法编辑文章', '错误')
  }
}

const unfavoriteArticle = async (articleId: number) => {
  if (!confirm('确定要取消收藏这篇文章吗？')) {
    return
  }
  
  try {
    await articleApi.unfavoriteArticle(articleId)
    toast.success('已取消收藏', '操作成功')
    // 重新加载收藏文章列表
    await loadFavoriteArticles()
    // 更新统计数据
    await loadArticleStats()
  } catch (error: any) {
    console.error('取消收藏失败:', error)
    toast.error(error.message || '取消收藏失败', '错误')
  }
}

const unlikeArticle = async (articleId: number) => {
  if (!confirm('确定要取消点赞这篇文章吗？')) {
    return
  }
  
  try {
    await articleApi.unlikeArticle(articleId)
    toast.success('已取消点赞', '操作成功')
    // 重新加载点赞文章列表
    await loadLikedArticles()
    // 更新统计数据
    await loadArticleStats()
  } catch (error: any) {
    console.error('取消点赞失败:', error)
    toast.error(error.message || '取消点赞失败', '错误')
  }
}

const deleteArticle = async (articleId: number) => {
  if (!confirm('确定要删除这篇文章吗？此操作不可恢复。')) {
    return
  }
  
  try {
    loading.value = true
    await articleApi.deleteArticle(articleId)
    toast.success('文章已删除', '操作成功')
    // 重新加载文章列表和统计数据
    await loadCreatedArticles()
    await loadArticleStats()
  } catch (error: any) {
    console.error('删除文章失败:', error)
    toast.error(error.message || '删除文章失败', '错误')
  } finally {
    loading.value = false
  }
}

const deleteDraft = async (draftId: number) => {
  if (!confirm('确定要删除这个草稿吗？此操作不可恢复。')) {
    return
  }
  
  try {
    loading.value = true
    await draftApi.deleteDraft(draftId)
    toast.success('草稿已删除', '操作成功')
    // 重新加载草稿列表
    await loadDraftArticles()
  } catch (error: any) {
    console.error('删除草稿失败:', error)
    toast.error(error.message || '删除草稿失败', '错误')
  } finally {
    loading.value = false
  }
}

const publishDraft = async (draftId: number) => {
  if (!confirm('确定要发布这个草稿吗？')) {
    return
  }
  
  try {
    loading.value = true
    await articleApi.publishArticle(draftId)
    toast.success('草稿已发布', '操作成功')
    // 重新加载数据
    await loadDraftArticles()
    await loadCreatedArticles()
    await loadArticleStats()
  } catch (error: any) {
    console.error('发布草稿失败:', error)
    toast.error(error.message || '发布草稿失败', '错误')
  } finally {
    loading.value = false
  }
}

// 监听选项卡切换，按需加载数据
const onTabChange = async (newTab: string) => {
  activeTab.value = newTab
  
  if (newTab === 'favorites' && favoriteArticles.value.length === 0) {
    await loadFavoriteArticles()
  } else if (newTab === 'created' && createdArticles.value.length === 0) {
    await loadCreatedArticles()
  } else if (newTab === 'drafts' && draftArticles.value.length === 0) {
    await loadDraftArticles()
  } else if (newTab === 'liked' && likedArticles.value.length === 0) {
    await loadLikedArticles()
  }
}

// 初始化数据
onMounted(async () => {
  await loadArticleStats()
  await loadFavoriteArticles() // 默认加载收藏文章
  // 预加载草稿数量
  await loadDraftArticles()
})

// 草稿编辑方法
const editingDraft = ref<any>(null)
const editForm = ref({ content: '', category: '' })
const savingDraftEdit = ref(false)

const getDraftStatusLabel = (status: string): string => {
  const map: Record<string, string> = { pending: '审核中', rejected: '已拒绝', draft: '草稿', approved: '已通过' }
  return map[status] || '草稿'
}

const editDraft = (draftId: number) => {
  const draft = draftArticles.value.find((d: any) => d.draftId === draftId)
  if (!draft) {
    toast.error('找不到该草稿', '错误')
    return
  }
  if (draft.status === 'pending') {
    toast.warning('草稿正在审核中，审核结束前不可修改', '提示')
    return
  }
  editingDraft.value = draft
  editForm.value = { content: draft.content || '', category: draft.category || '' }
}

const closeDraftEditor = () => {
  editingDraft.value = null
  editForm.value = { content: '', category: '' }
}

const submitDraftEdit = async () => {
  if (!editingDraft.value) return
  try {
    savingDraftEdit.value = true
    await draftApi.updateDraft(editingDraft.value.draftId, {
      content: editForm.value.content,
      category: editForm.value.category,
    })
    toast.success('草稿已重新提交审核', '操作成功')
    closeDraftEditor()
    await loadDraftArticles()
  } catch (error: any) {
    toast.error(error.message || '提交失败', '错误')
  } finally {
    savingDraftEdit.value = false
  }
}
</script>

<style scoped>
/* 主容器 */
.article-container {
  padding: 24px;
  background: #f8fafb;
  min-height: 100vh;
}

/* 页面标题区域 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.header-icon .icon {
  width: 24px;
  height: 24px;
  fill: white;
}

.header-text {
  flex: 1;
}

.page-title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  letter-spacing: -0.5px;
}

.page-subtitle {
  margin: 4px 0 0 0;
  color: #718096;
  font-size: 16px;
  font-weight: 400;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.create-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.create-btn .icon {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

/* 统计概览 */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.stat-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.stat-card.clickable {
  cursor: pointer;
  position: relative;
}

.stat-card.clickable:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.stat-card.active {
  border-color: #667eea;
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.3);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card.active .stat-content .stat-number {
  color: white;
}

.stat-card.active .stat-content .stat-label {
  color: rgba(255, 255, 255, 0.9);
}

.stat-card.active::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  pointer-events: none;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon.favorites {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
}

.stat-icon.created {
  background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
}

.stat-icon.drafts {
  background: linear-gradient(135deg, #feca57 0%, #ff9ff3 100%);
}

.stat-icon.liked {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon .icon {
  width: 24px;
  height: 24px;
  fill: white;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1a202c;
  line-height: 1;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 12px;
  color: #718096;
  font-weight: 500;
}

/* 文章网格 */
.article-content {
  min-height: 400px;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
}

/* 文章卡片 */
.article-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  height: 320px; /* 调整为4:3比例，约320px高度 */
  display: flex;
  flex-direction: column;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-header {
  padding: 20px 24px 0;
}

.article-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.article-status.favorites {
  background: #fff5f5;
  color: #c53030;
  border: 1px solid #feb2b2;
}

.article-status.published {
  background: #f0fff4;
  color: #22543d;
  border: 1px solid #9ae6b4;
}

.article-status.draft {
  background: #fffbeb;
  color: #d69e2e;
  border: 1px solid #fbd38d;
}

.article-status.liked {
  background: #edf2f7;
  color: #4a5568;
  border: 1px solid #cbd5e0;
}

.status-icon {
  width: 12px;
  height: 12px;
  fill: currentColor;
}

.card-content {
  padding: 20px 24px;
  flex: 1; /* 让内容区域占据剩余空间 */
  display: flex;
  flex-direction: column;
}

.article-title {
  margin: 0 0 8px 0; /* 减少底部间距 */
  font-size: 20px;
  font-weight: 600;
  color: #1a202c;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-excerpt {
  margin: 0 0 12px 0; /* 减少底部间距 */
  color: #718096;
  font-size: 14px;
  line-height: 1.5;
  flex: 1; /* 让摘要占据剩余空间 */
  display: -webkit-box;
  -webkit-line-clamp: 3; /* 减少显示行数适应更小高度 */
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  white-space: pre-wrap; /* 保持换行和空格 */
  word-wrap: break-word; /* 长单词换行 */
  overflow-wrap: break-word; /* 确保换行兼容性 */
  height: calc(1.5em * 3); /* 精确控制高度，避免半行显示 */
  max-height: calc(1.5em * 3); /* 最大高度限制 */
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 20px;
  margin-top: auto; /* 推到底部 */
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #718096;
  font-size: 13px;
}

.meta-icon, .meta-avatar {
  width: 14px;
  height: 14px;
  fill: currentColor;
}

.card-actions {
  padding: 0 24px 24px;
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.action-btn.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.action-btn.secondary {
  background: #f7fafc;
  color: #4a5568;
  border: 1px solid #e2e8f0;
}

.action-btn.secondary:hover {
  background: #edf2f7;
  border-color: #cbd5e0;
}

.action-btn.danger {
  background: #fed7d7;
  color: #c53030;
  border: 1px solid #feb2b2;
}

.action-btn.danger:hover {
  background: #fec4c4;
  border-color: #f56565;
}

.action-btn .icon {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-container {
    padding: 16px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .article-tabs {
    flex-direction: column;
    gap: 4px;
  }
  
  .tab-btn {
    justify-content: flex-start;
  }
  
  .article-grid {
    grid-template-columns: 1fr;
  }
  
  .article-card {
    height: 300px; /* 移动端保持4:3比例 */
  }
  
  .card-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .stat-card {
    flex-direction: column;
    text-align: center;
    gap: 8px;
    padding: 12px;
  }
  
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }
}

/* ── 草稿卡片状态徽标 ── */
.draft-card {
  position: relative;
}

.draft-status-badge {
  position: absolute;
  top: 14px;
  right: 14px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.4px;
  z-index: 2;
}

.draft-status-badge .status-icon {
  width: 13px;
  height: 13px;
  fill: currentColor;
}

.draft-status-badge.status-pending {
  background: #fffbeb;
  color: #d69e2e;
  border: 1px solid #fbd38d;
}

.draft-status-badge.status-rejected {
  background: #fff5f5;
  color: #c53030;
  border: 1px solid #feb2b2;
}

.draft-status-badge.status-draft {
  background: #ebf8ff;
  color: #2b6cb0;
  border: 1px solid #bee3f8;
}

.draft-status-badge.status-approved {
  background: #f0fff4;
  color: #276749;
  border: 1px solid #9ae6b4;
}

/* 拒绝原因提示框 */
.reject-reason-box {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  background: #fff5f5;
  border: 1px solid #fed7d7;
  border-radius: 8px;
  padding: 8px 12px;
  margin-bottom: 10px;
  font-size: 13px;
  color: #c53030;
  line-height: 1.5;
}

.reject-icon {
  width: 15px;
  height: 15px;
  fill: currentColor;
  flex-shrink: 0;
  margin-top: 1px;
}

/* 审核中禁用按钮 */
.action-btn.disabled-btn {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: not-allowed;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: #f7fafc;
  color: #a0aec0;
}

.action-btn.disabled-btn .icon {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

/* ── 草稿编辑 Modal ── */
.draft-edit-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 24px;
}

.draft-edit-modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 720px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.modal-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a202c;
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  background: #f7fafc;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  color: #4a5568;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.modal-close:hover {
  background: #edf2f7;
}

.modal-body {
  padding: 20px 24px;
  overflow-y: auto;
  flex: 1;
}

.modal-article-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 12px;
  padding: 10px 14px;
  background: #f7fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.modal-reject-reason {
  background: #fff5f5;
  border: 1px solid #fed7d7;
  border-radius: 8px;
  padding: 10px 14px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #c53030;
  line-height: 1.5;
}

.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #4a5568;
  margin-bottom: 6px;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  color: #1a202c;
  background: white;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  color: #1a202c;
  background: white;
  resize: vertical;
  font-family: inherit;
  line-height: 1.6;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 16px 24px;
  border-top: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.modal-footer .action-btn {
  flex: 0;
  min-width: 120px;
}

.modal-footer .action-btn.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}
</style>