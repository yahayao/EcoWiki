<!--
API优化使用示例组件
展示如何使用优化后的API
-->
<template>
  <div class="api-optimized-component">
    <div class="stats-panel">
      <h3>API性能统计</h3>
      <div class="metrics">
        <div class="metric">
          <label>总调用次数:</label>
          <span>{{ stats.totalCalls }}</span>
        </div>
        <div class="metric">
          <label>缓存命中率:</label>
          <span>{{ stats.cacheHitRate }}%</span>
        </div>
        <div class="metric">
          <label>平均响应时间:</label>
          <span>{{ stats.averageResponseTime }}ms</span>
        </div>
        <div class="metric">
          <label>实时调用频率:</label>
          <span>{{ realtimeMetrics.callsPerMinute }}/分钟</span>
        </div>
      </div>
      
      <div class="actions">
        <button @click="refreshStats" class="btn-primary">
          刷新统计
        </button>
        <button @click="clearCache" class="btn-secondary">
          清除缓存
        </button>
        <button @click="exportReport" class="btn-secondary">
          导出报告
        </button>
      </div>
    </div>

    <div class="preload-panel">
      <h3>数据预加载状态</h3>
      <div class="preload-status">
        <div class="status-item">
          <label>队列大小:</label>
          <span>{{ preloadStatus.queueSize }}</span>
        </div>
        <div class="status-item">
          <label>处理状态:</label>
          <span :class="{ 'processing': preloadStatus.isProcessing }">
            {{ preloadStatus.isProcessing ? '处理中' : '空闲' }}
          </span>
        </div>
        <div class="status-item" v-if="preloadStatus.nextItem">
          <label>下一项:</label>
          <span class="next-item">{{ preloadStatus.nextItem }}</span>
        </div>
      </div>
      
      <div class="preload-actions">
        <button @click="triggerPreload('user:login')" class="btn-secondary">
          模拟用户登录预加载
        </button>
        <button @click="triggerPreload('route:articles')" class="btn-secondary">
          模拟文章列表预加载
        </button>
        <button @click="clearPreloadQueue" class="btn-secondary">
          清空预加载队列
        </button>
      </div>
    </div>

    <div class="cache-info">
      <h3>缓存信息</h3>
      <div class="cache-stats">
        <div class="cache-item">
          <label>内存缓存:</label>
          <span>{{ cacheStats.memorySize }} 项</span>
        </div>
        <div class="cache-item">
          <label>localStorage:</label>
          <span>{{ cacheStats.localStorageSize }} 项</span>
        </div>
        <div class="cache-item">
          <label>sessionStorage:</label>
          <span>{{ cacheStats.sessionStorageSize }} 项</span>
        </div>
      </div>
    </div>

    <div class="optimization-tips" v-if="optimizationTips.length > 0">
      <h3>优化建议</h3>
      <ul class="tips-list">
        <li v-for="tip in optimizationTips" :key="tip.id" :class="tip.level">
          <strong>{{ tip.title }}:</strong>
          {{ tip.description }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { apiMonitor } from '@/utils/api-monitor'
import { dataPreloader } from '@/utils/data-preloader'
import { cacheManager } from '@/utils/cache-manager'

const stats = ref({
  totalCalls: 0,
  cacheHitRate: 0,
  averageResponseTime: 0,
  totalDataTransferred: 0
})

const realtimeMetrics = ref({
  callsPerMinute: 0,
  callsPerSecond: 0,
  recentCacheHitRate: 0,
  recentAverageResponseTime: 0
})

const preloadStatus = ref({
  queueSize: 0,
  isProcessing: false,
  nextItem: ''
})

const cacheStats = ref({
  memorySize: 0,
  localStorageSize: 0,
  sessionStorageSize: 0
})

const optimizationTips = ref<Array<{
  id: string
  title: string
  description: string
  level: 'info' | 'warning' | 'error'
}>>([])

let updateInterval: ReturnType<typeof setInterval>

// 刷新统计数据
const refreshStats = () => {
  const apiStats = apiMonitor.generateStats()
  stats.value = {
    totalCalls: apiStats.totalCalls,
    cacheHitRate: apiStats.cacheHitRate,
    averageResponseTime: apiStats.averageResponseTime,
    totalDataTransferred: Math.round(apiStats.totalDataTransferred / 1024)
  }

  realtimeMetrics.value = apiMonitor.getRealtimeMetrics()
  preloadStatus.value = dataPreloader.getQueueStatus()
  cacheStats.value = cacheManager.getStats()

  // 生成优化建议
  generateOptimizationTips()
}

// 生成优化建议
const generateOptimizationTips = () => {
  const tips = []

  if (stats.value.cacheHitRate < 30) {
    tips.push({
      id: 'low-cache-rate',
      title: '缓存命中率较低',
      description: '考虑增加缓存时间或优化缓存策略',
      level: 'warning' as const
    })
  }

  if (stats.value.averageResponseTime > 2000) {
    tips.push({
      id: 'slow-response',
      title: '响应时间较慢',
      description: '考虑优化后端接口或实现渐进式加载',
      level: 'error' as const
    })
  }

  if (realtimeMetrics.value.callsPerMinute > 100) {
    tips.push({
      id: 'high-frequency',
      title: 'API调用频率较高',
      description: '考虑实现请求防抖或使用WebSocket',
      level: 'warning' as const
    })
  }

  if (stats.value.totalDataTransferred > 10240) { // 10MB
    tips.push({
      id: 'large-data',
      title: '数据传输量较大',
      description: '考虑实现数据压缩或分页加载',
      level: 'info' as const
    })
  }

  optimizationTips.value = tips
}

// 清除缓存
const clearCache = async () => {
  cacheManager.clear('memory')
  cacheManager.clear('localStorage')
  cacheManager.clear('sessionStorage')
  
  console.log('所有缓存已清除')
  refreshStats()
}

// 导出报告
const exportReport = () => {
  const reportData = apiMonitor.exportData()
  const blob = new Blob([reportData], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  
  const a = document.createElement('a')
  a.href = url
  a.download = `api-performance-report-${new Date().toISOString().split('T')[0]}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  
  URL.revokeObjectURL(url)
}

// 触发预加载
const triggerPreload = (event: string) => {
  dataPreloader.trigger(event)
  setTimeout(refreshStats, 100) // 延迟刷新以显示队列变化
}

// 清空预加载队列
const clearPreloadQueue = () => {
  dataPreloader.clearQueue()
  refreshStats()
}

onMounted(() => {
  refreshStats()
  
  // 定期更新数据
  updateInterval = setInterval(refreshStats, 5000)
})

onUnmounted(() => {
  if (updateInterval) {
    clearInterval(updateInterval)
  }
})
</script>

<style scoped>
.api-optimized-component {
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
  max-width: 1200px;
  margin: 0 auto;
}

.stats-panel,
.preload-panel,
.cache-info,
.optimization-tips {
  background: white;
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stats-panel h3,
.preload-panel h3,
.cache-info h3,
.optimization-tips h3 {
  margin-top: 0;
  color: #333;
  border-bottom: 2px solid #007bff;
  padding-bottom: 10px;
}

.metrics,
.preload-status,
.cache-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.metric,
.status-item,
.cache-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;
}

.metric label,
.status-item label,
.cache-item label {
  font-weight: 600;
  color: #555;
}

.metric span,
.status-item span,
.cache-item span {
  font-weight: 500;
  color: #007bff;
}

.processing {
  color: #28a745 !important;
  font-weight: 600;
}

.next-item {
  font-size: 0.9em;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.actions,
.preload-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn-primary,
.btn-secondary {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tips-list li {
  padding: 12px;
  margin-bottom: 10px;
  border-radius: 4px;
  border-left: 4px solid;
}

.tips-list li.info {
  background: #d1ecf1;
  border-color: #17a2b8;
  color: #0c5460;
}

.tips-list li.warning {
  background: #fff3cd;
  border-color: #ffc107;
  color: #856404;
}

.tips-list li.error {
  background: #f8d7da;
  border-color: #dc3545;
  color: #721c24;
}

@media (max-width: 768px) {
  .metrics,
  .preload-status,
  .cache-stats {
    grid-template-columns: 1fr;
  }
  
  .actions,
  .preload-actions {
    flex-direction: column;
  }
  
  .btn-primary,
  .btn-secondary {
    width: 100%;
  }
}
</style>
