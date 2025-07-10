<template>
  <div class="version-comparison">
    <div class="comparison-header">
      <div class="header-title">
        <h2>版本比较</h2>
        <p class="subtitle">对比两个版本的差异</p>
      </div>
      <div class="header-actions">
        <button @click="swapVersions" class="swap-btn" :disabled="loading">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M7 16V4m0 0L3 8m4-4l4 4"/>
            <path d="M17 8v12m0 0l4-4m-4 4l-4-4"/>
          </svg>
          交换版本
        </button>
        <button @click="$emit('close')" class="close-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 版本信息对比 -->
    <div class="version-info-comparison" v-if="comparison">
      <div class="version-info-item">
        <h3>源版本</h3>
        <div class="version-details">
          <div class="detail-item">
            <span class="label">版本号:</span>
            <span class="value">{{ comparison.sourceVersion.versionNumber }}</span>
          </div>
          <div class="detail-item">
            <span class="label">作者:</span>
            <span class="value">{{ comparison.sourceVersion.author }}</span>
          </div>
          <div class="detail-item">
            <span class="label">创建时间:</span>
            <span class="value">{{ formatDate(comparison.sourceVersion.createdAt) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">存储类型:</span>
            <span class="value">{{ getStorageTypeLabel(comparison.sourceVersion.storageType) }}</span>
          </div>
        </div>
      </div>
      
      <div class="version-info-item">
        <h3>目标版本</h3>
        <div class="version-details">
          <div class="detail-item">
            <span class="label">版本号:</span>
            <span class="value">{{ comparison.targetVersion.versionNumber }}</span>
          </div>
          <div class="detail-item">
            <span class="label">作者:</span>
            <span class="value">{{ comparison.targetVersion.author }}</span>
          </div>
          <div class="detail-item">
            <span class="label">创建时间:</span>
            <span class="value">{{ formatDate(comparison.targetVersion.createdAt) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">存储类型:</span>
            <span class="value">{{ getStorageTypeLabel(comparison.targetVersion.storageType) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 变更统计 -->
    <div class="change-stats" v-if="comparison">
      <div class="stats-item addition">
        <div class="stats-icon">+</div>
        <div class="stats-content">
          <div class="stats-number">{{ comparison.changeStats.additions }}</div>
          <div class="stats-label">新增行</div>
        </div>
      </div>
      
      <div class="stats-item deletion">
        <div class="stats-icon">-</div>
        <div class="stats-content">
          <div class="stats-number">{{ comparison.changeStats.deletions }}</div>
          <div class="stats-label">删除行</div>
        </div>
      </div>
      
      <div class="stats-item modification">
        <div class="stats-icon">~</div>
        <div class="stats-content">
          <div class="stats-number">{{ comparison.changeStats.modifications }}</div>
          <div class="stats-label">修改行</div>
        </div>
      </div>
      
      <div class="stats-item total">
        <div class="stats-icon">=</div>
        <div class="stats-content">
          <div class="stats-number">{{ getTotalChanges() }}</div>
          <div class="stats-label">总变更</div>
        </div>
      </div>
    </div>

    <!-- 差异内容 -->
    <div class="diff-content" v-if="!loading && comparison">
      <div class="diff-toolbar">
        <div class="view-options">
          <button 
            @click="viewMode = 'unified'" 
            :class="{ active: viewMode === 'unified' }"
            class="view-btn"
          >
            统一视图
          </button>
          <button 
            @click="viewMode = 'split'" 
            :class="{ active: viewMode === 'split' }"
            class="view-btn"
          >
            分割视图
          </button>
        </div>
        
        <div class="diff-options">
          <label class="option-label">
            <input 
              type="checkbox" 
              v-model="showWhitespace"
              @change="updateDiffView"
            >
            显示空白字符
          </label>
          <label class="option-label">
            <input 
              type="checkbox" 
              v-model="ignoreWhitespace"
              @change="updateDiffView"
            >
            忽略空白差异
          </label>
        </div>
      </div>
      
      <div class="diff-viewer" :class="viewMode">
        <div class="diff-html" v-html="comparison.diffHtml"></div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在比较版本差异...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-state">
      <div class="error-icon">❌</div>
      <h3>比较失败</h3>
      <p>{{ error }}</p>
      <button @click="loadComparison" class="retry-btn">重试</button>
    </div>

    <!-- 底部操作栏 -->
    <div class="comparison-footer" v-if="comparison">
      <div class="footer-info">
        <span>比较模式: {{ viewMode === 'unified' ? '统一视图' : '分割视图' }}</span>
        <span>变更时间跨度: {{ getTimeDiff() }}</span>
      </div>
      <div class="footer-actions">
        <button @click="exportDiff" class="export-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="7,10 12,15 17,10"/>
            <line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
          导出差异
        </button>
        <button @click="copyDiff" class="copy-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
            <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
          </svg>
          复制差异
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { articleApi, type VersionComparison } from '../../api/article'

interface Props {
  sourceVersionId: number | null
  targetVersionId: number | null
  visible: boolean
}

interface Emits {
  (e: 'close'): void
  (e: 'swap'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 响应式状态
const loading = ref(false)
const error = ref<string | null>(null)
const comparison = ref<VersionComparison | null>(null)
const viewMode = ref<'unified' | 'split'>('unified')
const showWhitespace = ref(false)
const ignoreWhitespace = ref(false)

// 计算属性
const canCompare = computed(() => props.sourceVersionId !== null && props.targetVersionId !== null)

// 格式化日期
const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取存储类型标签
const getStorageTypeLabel = (type: string): string => {
  const labels = {
    'FULL_CONTENT': '完整内容',
    'DIFF_FROM_BASE': '基于基础版本的差异',
    'DIFF_FROM_PREV': '基于前一版本的差异'
  }
  return labels[type as keyof typeof labels] || type
}

// 获取总变更数
const getTotalChanges = (): number => {
  if (!comparison.value) return 0
  const { additions, deletions, modifications } = comparison.value.changeStats
  return additions + deletions + modifications
}

// 获取时间差
const getTimeDiff = (): string => {
  if (!comparison.value) return ''
  
  const sourceTime = new Date(comparison.value.sourceVersion.createdAt)
  const targetTime = new Date(comparison.value.targetVersion.createdAt)
  const diffMs = Math.abs(targetTime.getTime() - sourceTime.getTime())
  
  const days = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diffMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
  
  if (days > 0) {
    return `${days}天${hours}小时`
  } else if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

// 加载比较结果
const loadComparison = async () => {
  if (!canCompare.value) return
  
  try {
    loading.value = true
    error.value = null
    
    comparison.value = await articleApi.compareVersions(
      props.sourceVersionId!,
      props.targetVersionId!
    )
    
  } catch (err) {
    console.error('加载版本比较失败:', err)
    error.value = err instanceof Error ? err.message : '比较失败'
  } finally {
    loading.value = false
  }
}

// 交换版本
const swapVersions = () => {
  emit('swap')
}

// 更新差异视图
const updateDiffView = () => {
  // 这里可以添加根据选项重新渲染差异的逻辑
  console.log('更新差异视图', { showWhitespace: showWhitespace.value, ignoreWhitespace: ignoreWhitespace.value })
}

// 导出差异
const exportDiff = () => {
  if (!comparison.value) return
  
  const diffContent = comparison.value.diffHtml
  const blob = new Blob([diffContent], { type: 'text/html' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `version-diff-${comparison.value.sourceVersion.versionNumber}-${comparison.value.targetVersion.versionNumber}.html`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

// 复制差异
const copyDiff = async () => {
  if (!comparison.value) return
  
  try {
    // 提取纯文本差异
    const tempDiv = document.createElement('div')
    tempDiv.innerHTML = comparison.value.diffHtml
    const textContent = tempDiv.textContent || tempDiv.innerText || ''
    
    await navigator.clipboard.writeText(textContent)
    alert('差异内容已复制到剪贴板')
  } catch (err) {
    console.error('复制失败:', err)
    alert('复制失败')
  }
}

// 监听版本ID变化
watch([() => props.sourceVersionId, () => props.targetVersionId], () => {
  if (props.visible && canCompare.value) {
    loadComparison()
  }
})

// 监听可见性变化
watch(() => props.visible, (visible) => {
  if (visible && canCompare.value) {
    loadComparison()
  }
})

onMounted(() => {
  if (props.visible && canCompare.value) {
    loadComparison()
  }
})
</script>

<style scoped>
.version-comparison {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.comparison-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.header-title h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
}

.subtitle {
  margin: 4px 0 0 0;
  font-size: 0.9rem;
  opacity: 0.9;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.swap-btn, .close-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
}

.swap-btn:hover, .close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.version-info-comparison {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 20px 24px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.version-info-item h3 {
  margin: 0 0 16px 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #495057;
}

.version-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e9ecef;
}

.detail-item:last-child {
  border-bottom: none;
}

.label {
  font-weight: 500;
  color: #6c757d;
}

.value {
  color: #495057;
}

.change-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 20px 24px;
  background: white;
  border-bottom: 1px solid #e9ecef;
}

.stats-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  transition: transform 0.2s;
}

.stats-item:hover {
  transform: translateY(-2px);
}

.stats-item.addition {
  background: #d4edda;
  border: 1px solid #c3e6cb;
}

.stats-item.deletion {
  background: #f8d7da;
  border: 1px solid #f5c6cb;
}

.stats-item.modification {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
}

.stats-item.total {
  background: #e2e3e5;
  border: 1px solid #d6d8db;
}

.stats-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-weight: bold;
  font-size: 1.2rem;
}

.addition .stats-icon {
  background: #28a745;
  color: white;
}

.deletion .stats-icon {
  background: #dc3545;
  color: white;
}

.modification .stats-icon {
  background: #ffc107;
  color: #212529;
}

.total .stats-icon {
  background: #6c757d;
  color: white;
}

.stats-content {
  display: flex;
  flex-direction: column;
}

.stats-number {
  font-size: 1.4rem;
  font-weight: 600;
  color: #495057;
}

.stats-label {
  font-size: 0.85rem;
  color: #6c757d;
}

.diff-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.diff-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.view-options {
  display: flex;
  gap: 8px;
}

.view-btn {
  padding: 8px 16px;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
}

.view-btn:hover {
  background: #f8f9fa;
}

.view-btn.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.diff-options {
  display: flex;
  gap: 16px;
}

.option-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
  color: #495057;
  cursor: pointer;
}

.option-label input[type="checkbox"] {
  cursor: pointer;
}

.diff-viewer {
  flex: 1;
  overflow: auto;
  padding: 16px 24px;
  background: #fafafa;
}

.diff-html {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9rem;
  line-height: 1.5;
}

.diff-viewer.split .diff-html {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.diff-html :deep(.diff-line) {
  display: block;
  padding: 2px 8px;
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.diff-html :deep(.diff-line-added) {
  background: #d4edda;
  border-left: 4px solid #28a745;
}

.diff-html :deep(.diff-line-removed) {
  background: #f8d7da;
  border-left: 4px solid #dc3545;
}

.diff-html :deep(.diff-line-modified) {
  background: #fff3cd;
  border-left: 4px solid #ffc107;
}

.diff-html :deep(.diff-line-context) {
  background: #f8f9fa;
  color: #6c757d;
}

.loading-state, .error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  flex: 1;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.retry-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.retry-btn:hover {
  background: #5a6fd8;
}

.comparison-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.footer-info {
  display: flex;
  gap: 16px;
  font-size: 0.85rem;
  color: #6c757d;
}

.footer-actions {
  display: flex;
  gap: 8px;
}

.export-btn, .copy-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.export-btn {
  background: #e9ecef;
  color: #495057;
}

.export-btn:hover {
  background: #dee2e6;
}

.copy-btn {
  background: #667eea;
  color: white;
}

.copy-btn:hover {
  background: #5a6fd8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .version-info-comparison {
    grid-template-columns: 1fr;
  }
  
  .change-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .diff-toolbar {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .comparison-footer {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .footer-info {
    flex-direction: column;
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .change-stats {
    grid-template-columns: 1fr;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
