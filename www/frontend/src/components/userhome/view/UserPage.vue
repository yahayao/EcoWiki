<!-- UserPage.vue -->
<template>
  <div class="user-page-container">
    <h2 class="section-title">我的主页</h2>
    
    <div class="page-editor">
      <div class="editor-toolbar">
        <button class="btn btn-sm" :class="{ active: mode === 'template' }" @click="mode = 'template'">
          模板编辑
        </button>
        <button class="btn btn-sm" :class="{ active: mode === 'custom' }" @click="mode = 'custom'">
          自定义代码
        </button>
        <span v-if="pendingReview" class="badge badge-warning">待审核</span>
      </div>
      
      <div v-if="mode === 'template'" class="template-mode">
        <div class="template-preview">
          <div class="default-template">
            <h1>{{ username }}的主页</h1>
            <div class="user-bio">
              <textarea 
                v-model="userDescription" 
                class="bio-textarea" 
                placeholder="在这里输入您的个人描述..."
              ></textarea>
            </div>
            <div class="quick-stats">
              <div class="stat-item">
                <span class="stat-number">42</span>
                <span class="stat-label">创建页面</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">156</span>
                <span class="stat-label">编辑次数</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">3.2k</span>
                <span class="stat-label">积分</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="custom-mode">
        <div class="code-editor">
          <textarea 
            v-model="customCode" 
            class="code-textarea" 
            placeholder="输入您的HTML/CSS/JS代码..."
          ></textarea>
          <div class="editor-help">
            <p>支持HTML、CSS和JavaScript。禁止包含恶意代码。</p>
            <button class="btn btn-primary">提交审核</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const mode = ref<'template' | 'custom'>('template')
const username = ref('EcoEditor')
const userDescription = ref('欢迎来到我的主页！我是一名热衷于环保知识的编辑者...')
const customCode = ref('<h1>自定义主页</h1>\n<style>\n  body { background: #f0f8ff; }\n</style>')
const pendingReview = ref(false)
</script>

<style scoped>
.user-page-container {
  padding: 32px;
}

.section-title {
  margin: 0 0 24px 0;
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
}

.editor-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
}

.btn.active {
  background: #43a047;
  color: white;
}

.template-mode {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 24px;
}

.default-template {
  max-width: 800px;
  margin: 0 auto;
}

.default-template h1 {
  color: #2c3e50;
  margin-bottom: 20px;
}

.user-bio {
  margin-bottom: 30px;
}

.bio-textarea {
  width: 100%;
  min-height: 100px;
  padding: 12px;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  font-family: inherit;
}

.quick-stats {
  display: flex;
  gap: 40px;
  justify-content: center;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 32px;
  font-weight: bold;
  color: #43a047;
}

.stat-label {
  color: #6c757d;
  font-size: 14px;
}

.code-editor {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 24px;
}

.code-textarea {
  width: 100%;
  min-height: 300px;
  padding: 12px;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
}

.editor-help {
  margin-top: 16px;
  text-align: center;
}

.badge-warning {
  background: #fff3cd;
  color: #856404;
}
</style>