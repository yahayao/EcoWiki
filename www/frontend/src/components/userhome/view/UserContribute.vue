<!-- UserContribute.vue -->
<template>
  <div class="contribute-container">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M3,22V8H7V22H3M10,22V2H14V22H10M17,22V14H21V22H17Z" />
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">贡献统计</h1>
          <p class="page-subtitle">您的EcoWiki贡献记录和成就</p>
        </div>
      </div>
      <div class="header-actions">
        <div class="total-score">
          <div class="score-number">{{ totalScore }}</div>
          <div class="score-label">总积分</div>
        </div>
      </div>
    </div>

    <!-- 贡献概览 -->
    <div class="stats-overview">
      <div class="stat-card primary">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z" />
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.createdPages }}</div>
          <div class="stat-label">创建页面</div>
          <div class="stat-change positive">+{{ stats.createdThisMonth }} 本月</div>
        </div>
        <div class="stat-trend">
          <svg viewBox="0 0 24 24" class="trend-icon">
            <path d="M16,6L18.29,8.29L13.41,13.17L9.41,9.17L2,16.59L3.41,18L9.41,12L13.41,16L19.71,9.71L22,12V6H16Z"/>
          </svg>
        </div>
      </div>
      
      <div class="stat-card secondary">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z" />
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.editCount }}</div>
          <div class="stat-label">编辑次数</div>
          <div class="stat-change positive">+{{ stats.editsThisMonth }} 本月</div>
        </div>
        <div class="stat-trend">
          <svg viewBox="0 0 24 24" class="trend-icon">
            <path d="M16,6L18.29,8.29L13.41,13.17L9.41,9.17L2,16.59L3.41,18L9.41,12L13.41,16L19.71,9.71L22,12V6H16Z"/>
          </svg>
        </div>
      </div>
      
      <div class="stat-card accent">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4M11,17V16H9V14H13V13H10A1,1 0 0,1 9,12V9A1,1 0 0,1 10,8H11V7H13V8H15V10H11V11H14A1,1 0 0,1 15,12V15A1,1 0 0,1 14,16H13V17H11Z" />
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.points }}</div>
          <div class="stat-label">贡献积分</div>
          <div class="stat-change positive">+{{ stats.pointsThisMonth }} 本月</div>
        </div>
        <div class="stat-trend">
          <svg viewBox="0 0 24 24" class="trend-icon">
            <path d="M16,6L18.29,8.29L13.41,13.17L9.41,9.17L2,16.59L3.41,18L9.41,12L13.41,16L19.71,9.71L22,12V6H16Z"/>
          </svg>
        </div>
      </div>
    </div>

    <!-- 成就徽章 -->
    <div class="achievements-section">
      <div class="section-header">
        <h2 class="section-title">
          <svg viewBox="0 0 24 24" class="section-icon">
            <path d="M5,16L3,5H1V3H4L6,14L7,18H20V16H8L9,13H20L22,6H11L10.27,3H19V1H8.73C8.39,1 8.1,1.2 8,1.54L7.27,4H2V6H6.73L8.77,15H19V13H10.5L11.27,10H21L19.5,8H12.5L13.27,5H17V3H14.5L15.27,1H9.73C9.39,1 9.1,1.2 9,1.54L8.27,4H3V6H7.27L9.77,17H20V15H11.5L12.27,12H21.5L20,10H13.5L14.27,7H18V5H15.5L16.27,2H10.73C10.39,2 10.1,2.2 10,2.54L9.27,5H4V7H8.27L10.27,16H5Z"/>
          </svg>
          成就徽章
        </h2>
        <span class="badge-count">{{ achievements.filter(a => a.unlocked).length }}/{{ achievements.length }}</span>
      </div>
      
      <div class="achievements-grid">
        <div 
          v-for="achievement in achievements" 
          :key="achievement.id"
          class="achievement-card"
          :class="{ unlocked: achievement.unlocked }"
        >
          <div class="achievement-icon">
            <svg viewBox="0 0 24 24" class="icon">
              <path :d="achievement.icon"/>
            </svg>
          </div>
          <div class="achievement-content">
            <h4 class="achievement-name">{{ achievement.name }}</h4>
            <p class="achievement-description">{{ achievement.description }}</p>
            <div v-if="achievement.unlocked" class="achievement-date">
              {{ achievement.unlockedDate }}
            </div>
            <div v-else class="achievement-progress">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: achievement.progress + '%' }"></div>
              </div>
              <span class="progress-text">{{ achievement.progress }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 贡献历史 -->
    <div class="history-section">
      <div class="section-header">
        <h2 class="section-title">
          <svg viewBox="0 0 24 24" class="section-icon">
            <path d="M12,20A7,7 0 0,1 5,13A7,7 0 0,1 12,6A7,7 0 0,1 19,13A7,7 0 0,1 12,20M19.03,7.39L20.45,5.97C20,5.46 19.55,5 19.04,4.56L17.62,6C16.07,4.74 14.12,4 12,4A9,9 0 0,0 3,13A9,9 0 0,0 12,22C17,22 21,17.97 21,13C21,10.88 20.26,8.93 19.03,7.39M11,14H13V8H11M15,1H9V3H15V1Z"/>
          </svg>
          最近贡献
        </h2>
        <div class="history-filter">
          <select v-model="historyFilter" class="filter-select">
            <option value="all">全部</option>
            <option value="created">创建</option>
            <option value="edited">编辑</option>
            <option value="reviewed">审核</option>
          </select>
        </div>
      </div>
      
      <div class="history-timeline">
        <div 
          v-for="item in filteredHistory" 
          :key="item.id"
          class="timeline-item"
        >
          <div class="timeline-marker" :class="item.type">
            <svg viewBox="0 0 24 24" class="marker-icon">
              <path :d="getTypeIcon(item.type)"/>
            </svg>
          </div>
          <div class="timeline-content">
            <div class="timeline-header">
              <h4 class="timeline-title">{{ item.title }}</h4>
              <span class="timeline-type" :class="item.type">{{ getTypeLabel(item.type) }}</span>
            </div>
            <p class="timeline-description">{{ item.description }}</p>
            <div class="timeline-meta">
              <span class="timeline-date">{{ item.date }}</span>
              <span class="timeline-points">+{{ item.points }} 积分</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { userApi } from '@/api/user'
import toast from '@/utils/toast'

// 统计数据
const stats = ref({
  createdPages: 0,
  createdThisMonth: 0,
  editCount: 0,
  editsThisMonth: 0,
  points: 0,
  pointsThisMonth: 0
})

const achievements = ref<any[]>([])
const contributionCalendar = ref<any[]>([])

const totalScore = computed(() => stats.value.points)

// 历史记录
const historyFilter = ref('all')

const history = ref([
  {
    id: 1,
    type: 'created',
    title: '可持续能源发展指南',
    description: '创建了关于可持续能源技术发展趋势的详细指南',
    date: '2025-07-12 14:30',
    points: 50
  },
  {
    id: 2,
    type: 'edited',
    title: '气候变化的影响',
    description: '更新了气候变化对生态系统影响的最新数据',
    date: '2025-07-10 09:15',
    points: 20
  },
  {
    id: 3,
    type: 'reviewed',
    title: '海洋保护措施',
    description: '审核并改进了海洋保护措施的相关内容',
    date: '2025-07-08 16:45',
    points: 30
  },
  {
    id: 4,
    type: 'created',
    title: '生物多样性保护',
    description: '新建了生物多样性保护的完整知识体系',
    date: '2025-07-05 11:20',
    points: 60
  }
])

// 过滤后的历史记录
const filteredHistory = computed(() => {
  if (historyFilter.value === 'all') {
    return history.value
  }
  return history.value.filter(item => item.type === historyFilter.value)
})

// 获取类型图标
const getTypeIcon = (type: string) => {
  const icons = {
    created: 'M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z',
    edited: 'M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z',
    reviewed: 'M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z'
  }
  return icons[type as keyof typeof icons] || icons.created
}

// 获取类型标签
const getTypeLabel = (type: string) => {
  const labels = {
    created: '创建',
    edited: '编辑',
    reviewed: '审核'
  }
  return labels[type as keyof typeof labels] || '未知'
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '无'
  try {
    return new Date(dateString).toLocaleDateString('zh-CN')
  } catch {
    return '无效日期'
  }
}

// 加载用户贡献数据
const loadContributions = async () => {
  try {
    const contributions = await userApi.getUserContributions()
    
    stats.value = {
      createdPages: contributions.createdPages,
      createdThisMonth: contributions.createdThisMonth,
      editCount: contributions.editCount,
      editsThisMonth: contributions.editsThisMonth,
      points: contributions.points,
      pointsThisMonth: contributions.pointsThisMonth
    }
    
    // 处理成就数据
    if (contributions.achievements && contributions.achievements.length > 0) {
      achievements.value = contributions.achievements
    } else {
      // 使用默认成就数据
      achievements.value = [
        {
          id: 1,
          name: '首次创建',
          description: '创建您的第一个页面',
          icon: 'M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z',
          unlocked: contributions.createdPages > 0,
          unlockedDate: contributions.createdPages > 0 ? '2025-06-15' : '',
          progress: contributions.createdPages > 0 ? 100 : 0
        },
        {
          id: 2,
          name: '编辑达人',
          description: '完成100次编辑',
          icon: 'M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z',
          unlocked: contributions.editCount >= 100,
          unlockedDate: contributions.editCount >= 100 ? '2025-07-01' : '',
          progress: Math.min(100, (contributions.editCount / 100) * 100)
        },
        {
          id: 3,
          name: '知识分享者',
          description: '创建10个高质量页面',
          icon: 'M12,2A2,2 0 0,1 14,4C14,4.74 13.6,5.39 13,5.73V7H14A7,7 0 0,1 21,14H22A1,1 0 0,1 23,15V18A1,1 0 0,1 22,19H21V20A2,2 0 0,1 19,22H5A2,2 0 0,1 3,20V19H2A1,1 0 0,1 1,18V15A1,1 0 0,1 2,14H3A7,7 0 0,1 10,7H11V5.73C10.4,5.39 10,4.74 10,4A2,2 0 0,1 12,2M7.5,13A2.5,2.5 0 0,0 5,15.5A2.5,2.5 0 0,0 7.5,18A2.5,2.5 0 0,0 10,15.5A2.5,2.5 0 0,0 7.5,13M16.5,13A2.5,2.5 0 0,0 14,15.5A2.5,2.5 0 0,0 16.5,18A2.5,2.5 0 0,0 19,15.5A2.5,2.5 0 0,0 16.5,13Z',
          unlocked: contributions.createdPages >= 10,
          unlockedDate: contributions.createdPages >= 10 ? '2025-07-05' : '',
          progress: Math.min(100, (contributions.createdPages / 10) * 100)
        },
        {
          id: 4,
          name: '社区贡献者',
          description: '获得1000积分',
          icon: 'M16,17H5V7H16L19.55,12L16,17M5.5,6C5.5,4.89 6.39,4 7.5,4H16.5L20.5,9L16.5,14H7.5C6.39,14 5.5,13.11 5.5,12V6M5.5,18V16H7.5V18H5.5Z',
          unlocked: contributions.points >= 1000,
          unlockedDate: contributions.points >= 1000 ? '2025-07-10' : '',
          progress: Math.min(100, (contributions.points / 1000) * 100)
        }
      ]
    }
    
    // 处理贡献日历数据
    contributionCalendar.value = contributions.contributionCalendar || []
    
  } catch (error: any) {
    console.error('加载贡献数据失败:', error)
    toast.error('加载贡献数据失败', '错误')
    
    // 使用默认数据
    stats.value = {
      createdPages: 42,
      createdThisMonth: 5,
      editCount: 156,
      editsThisMonth: 23,
      points: 3245,
      pointsThisMonth: 180
    }
    
    achievements.value = [
      {
        id: 1,
        name: '首次创建',
        description: '创建您的第一个页面',
        icon: 'M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z',
        unlocked: true,
        unlockedDate: '2025-06-15',
        progress: 100
      },
      {
        id: 2,
        name: '编辑达人',
        description: '完成100次编辑',
        icon: 'M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z',
        unlocked: true,
        unlockedDate: '2025-07-01',
        progress: 100
      },
      {
        id: 3,
        name: '知识分享者',
        description: '创建10个高质量页面',
        icon: 'M12,2A2,2 0 0,1 14,4C14,4.74 13.6,5.39 13,5.73V7H14A7,7 0 0,1 21,14H22A1,1 0 0,1 23,15V18A1,1 0 0,1 22,19H21V20A2,2 0 0,1 19,22H5A2,2 0 0,1 3,20V19H2A1,1 0 0,1 1,18V15A1,1 0 0,1 2,14H3A7,7 0 0,1 10,7H11V5.73C10.4,5.39 10,4.74 10,4A2,2 0 0,1 12,2M7.5,13A2.5,2.5 0 0,0 5,15.5A2.5,2.5 0 0,0 7.5,18A2.5,2.5 0 0,0 10,15.5A2.5,2.5 0 0,0 7.5,13M16.5,13A2.5,2.5 0 0,0 14,15.5A2.5,2.5 0 0,0 16.5,18A2.5,2.5 0 0,0 19,15.5A2.5,2.5 0 0,0 16.5,13Z',
        unlocked: false,
        progress: 80
      },
      {
        id: 4,
        name: '社区贡献者',
        description: '获得1000积分',
        icon: 'M16,17H5V7H16L19.55,12L16,17M5.5,6C5.5,4.89 6.39,4 7.5,4H16.5L20.5,9L16.5,14H7.5C6.39,14 5.5,13.11 5.5,12V6M5.5,18V16H7.5V18H5.5Z',
        unlocked: true,
        unlockedDate: '2025-07-10',
        progress: 100
      }
    ]
  }
}

// 初始化
onMounted(() => {
  loadContributions()
})
</script>

<style scoped>
/* 主容器 */
.contribute-container {
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

.total-score {
  text-align: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #ffd89b 0%, #19547b 100%);
  border-radius: 16px;
  color: white;
}

.score-number {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 4px;
}

.score-label {
  font-size: 14px;
  font-weight: 500;
  opacity: 0.9;
}

/* 统计概览 */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.stat-card.primary {
  border-left: 4px solid #667eea;
}

.stat-card.secondary {
  border-left: 4px solid #48bb78;
}

.stat-card.accent {
  border-left: 4px solid #ed8936;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-card.primary .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.secondary .stat-icon {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
}

.stat-card.accent .stat-icon {
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
}

.stat-icon .icon {
  width: 28px;
  height: 28px;
  fill: white;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #718096;
  font-weight: 500;
  margin-bottom: 8px;
}

.stat-change {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 8px;
  display: inline-block;
}

.stat-change.positive {
  background: #f0fff4;
  color: #22543d;
}

.stat-trend {
  position: absolute;
  top: 16px;
  right: 16px;
  opacity: 0.6;
}

.trend-icon {
  width: 20px;
  height: 20px;
  fill: #48bb78;
}

/* 成就徽章部分 */
.achievements-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 600;
  color: #1a202c;
  margin: 0;
}

.section-icon {
  width: 20px;
  height: 20px;
  fill: #667eea;
}

.badge-count {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.achievement-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  gap: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  opacity: 0.6;
}

.achievement-card.unlocked {
  opacity: 1;
  border-color: #48bb78;
  background: linear-gradient(135deg, #f0fff4 0%, #ffffff 100%);
}

.achievement-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.achievement-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: #f7fafc;
  border: 2px solid #e2e8f0;
}

.achievement-card.unlocked .achievement-icon {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  border-color: #48bb78;
}

.achievement-icon .icon {
  width: 24px;
  height: 24px;
  fill: #718096;
}

.achievement-card.unlocked .achievement-icon .icon {
  fill: white;
}

.achievement-content {
  flex: 1;
}

.achievement-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a202c;
}

.achievement-description {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #718096;
  line-height: 1.4;
}

.achievement-date {
  font-size: 12px;
  color: #48bb78;
  font-weight: 500;
}

.achievement-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  color: #718096;
  font-weight: 500;
  min-width: 32px;
}

/* 贡献历史部分 */
.history-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.history-filter {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  cursor: pointer;
}

.history-timeline {
  margin-top: 24px;
}

.timeline-item {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  position: relative;
}

.timeline-item:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 20px;
  top: 48px;
  width: 2px;
  height: calc(100% - 20px);
  background: #e2e8f0;
}

.timeline-marker {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 3px solid white;
  box-shadow: 0 0 0 2px #e2e8f0;
}

.timeline-marker.created {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.timeline-marker.edited {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
}

.timeline-marker.reviewed {
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
}

.marker-icon {
  width: 18px;
  height: 18px;
  fill: white;
}

.timeline-content {
  flex: 1;
  background: #f7fafc;
  padding: 16px 20px;
  border-radius: 12px;
  border-left: 3px solid #e2e8f0;
}

.timeline-item .timeline-marker.created + .timeline-content {
  border-left-color: #667eea;
}

.timeline-item .timeline-marker.edited + .timeline-content {
  border-left-color: #48bb78;
}

.timeline-item .timeline-marker.reviewed + .timeline-content {
  border-left-color: #ed8936;
}

.timeline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.timeline-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a202c;
}

.timeline-type {
  padding: 4px 8px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.timeline-type.created {
  background: #e6f3ff;
  color: #2b6cb0;
}

.timeline-type.edited {
  background: #f0fff4;
  color: #22543d;
}

.timeline-type.reviewed {
  background: #fffbeb;
  color: #d69e2e;
}

.timeline-description {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #718096;
  line-height: 1.5;
}

.timeline-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}

.timeline-date {
  color: #a0aec0;
}

.timeline-points {
  color: #ed8936;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .contribute-container {
    padding: 16px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .stats-overview {
    grid-template-columns: 1fr;
  }
  
  .achievements-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .timeline-item {
    flex-direction: column;
    gap: 12px;
  }
  
  .timeline-item:not(:last-child)::after {
    display: none;
  }
}

@media (max-width: 480px) {
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
    gap: 12px;
  }
  
  .achievement-card {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .timeline-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>