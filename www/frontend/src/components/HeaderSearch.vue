<template>
  <!-- 搜索栏 -->
  <div class="search-container">
    <div class="search-wrapper">
      <input 
        type="text" 
        class="search-input" 
        placeholder="搜索知识内容..." 
        v-model="searchTerm"
        @keyup.enter="handleSearch"
      />
      <button class="search-button" @click="handleSearch">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M21 21L16.514 16.506L21 21ZM19 10.5C19 15.194 15.194 19 10.5 19C5.806 19 2 15.194 2 10.5C2 5.806 5.806 2 10.5 2C15.194 2 19 5.806 19 10.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 定义事件
const emit = defineEmits<{
  search: [term: string]
}>()

// 搜索词
const searchTerm = ref('')

// 处理搜索
const handleSearch = () => {
  if (searchTerm.value.trim()) {
    emit('search', searchTerm.value.trim())
  }
}
</script>

<style scoped>
/* 搜索栏 */
.search-container {
  flex: 1;
  max-width: 500px;
  margin: 0 20px;
}

.search-wrapper {
  position: relative;
  width: 100%;
}

.search-input {
  width: 100%;
  padding: 12px 50px 12px 20px;
  border: none;
  border-radius: 25px;
  font-size: 1rem;
  outline: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.search-input:focus {
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.15);
  background: white;
}

.search-input::placeholder {
  color: #718096;
}

.search-button {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  color: #667eea;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-button:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-50%) scale(1.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-container {
    max-width: 100%;
    margin: 0;
  }
}
</style>
