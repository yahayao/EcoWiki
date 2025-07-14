<!-- UserContribute.vue -->
<template>
  <div class="contribute-container">
    <h2 class="section-title">贡献统计</h2>
    
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24">
            <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z" />
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">42</div>
          <div class="stat-label">创建页面</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24">
            <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z" />
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">156</div>
          <div class="stat-label">编辑次数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24">
            <path d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4M11,17V16H9V14H13V13H10A1,1 0 0,1 9,12V9A1,1 0 0,1 10,8H11V7H13V8H15V10H11V11H14A1,1 0 0,1 15,12V15A1,1 0 0,1 14,16H13V17H11Z" />
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">3,245</div>
          <div class="stat-label">贡献积分</div>
        </div>
      </div>
    </div>
    
    <div class="contribute-history">
      <h3>最近贡献</h3>
      <div class="history-list">
        <div class="history-item">
          <div class="history-meta">
            <span class="history-type created">创建</span>
            <span class="history-title">可持续能源发展指南</span>
            <span class="history-date">2025-07-12</span>
          </div>
        </div>
        <div class="history-item">
          <div class="history-meta">
            <span class="history-type edited">编辑</span>
            <span class="history-title">气候变化的影响</span>
            <span class="history-date">2025-07-10</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.contribute-container {
  padding: 32px;
}

.section-title {
  margin: 0 0 24px 0;
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  background: #e8f5e9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
  fill: #43a047;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #2c3e50;
}

.stat-label {
  font-size: 14px;
  color: #6c757d;
}

.contribute-history {
  background: white;
  border-radius: 8px;
  padding: 24px;
}

.contribute-history h3 {
  margin: 0 0 16px 0;
  color: #2c3e50;
}

.history-item {
  padding: 12px 0;
  border-bottom: 1px solid #e9ecef;
}

.history-item:last-child {
  border-bottom: none;
}

.history-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.history-type {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.history-type.created {
  background: #d4edda;
  color: #155724;
}

.history-type.edited {
  background: #d1ecf1;
  color: #0c5460;
}

.history-title {
  font-weight: 500;
  color: #495057;
}

.history-date {
  color: #6c757d;
  font-size: 14px;
  margin-left: auto;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>