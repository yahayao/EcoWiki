<template>
  <div class="tag-selector">
    <label class="block text-sm font-medium text-gray-700 mb-2">
      文章标签
    </label>
    
    <!-- 标签输入框 -->
    <div class="relative mb-3">
      <input
        v-model="newTagInput"
        @keydown.enter.prevent="addTag"
        @keydown.comma.prevent="addTag"
        @input="onInputChange"
        type="text"
        placeholder="输入标签名称，按回车或逗号添加"
        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
      />
      
      <!-- 标签建议下拉列表 -->
      <div 
        v-if="showSuggestions && suggestions.length > 0"
        class="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg max-h-32 overflow-y-auto"
      >
        <div
          v-for="suggestion in suggestions"
          :key="suggestion.tagId"
          @click="selectSuggestion(suggestion)"
          class="px-3 py-2 hover:bg-blue-50 cursor-pointer text-sm"
        >
          <span class="font-medium">{{ suggestion.tagName }}</span>
          <span v-if="suggestion.description" class="text-gray-500 ml-2">
            - {{ suggestion.description }}
          </span>
          <span class="text-gray-400 text-xs ml-auto">
            ({{ suggestion.articleCount }} 篇文章)
          </span>
        </div>
      </div>
    </div>
    
    <!-- 已选择的标签 -->
    <div class="flex flex-wrap gap-2 mb-3">
      <span
        v-for="(tag, index) in selectedTags"
        :key="index"
        class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
      >
        {{ tag }}
        <button
          @click="removeTag(index)"
          type="button"
          class="ml-1 inline-flex items-center justify-center w-4 h-4 rounded-full text-blue-400 hover:bg-blue-200 hover:text-blue-600 focus:outline-none"
        >
          <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"></path>
          </svg>
        </button>
      </span>
    </div>
    
    <!-- 热门标签快捷选择 -->
    <div v-if="popularTags.length > 0" class="mb-3">
      <h4 class="text-sm font-medium text-gray-700 mb-2">热门标签</h4>
      <div class="flex flex-wrap gap-2">
        <button
          v-for="tag in popularTags"
          :key="tag.tagId"
          @click="addPopularTag(tag.tagName)"
          type="button"
          class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-500"
          :disabled="selectedTags.includes(tag.tagName)"
          :class="{ 'opacity-50 cursor-not-allowed': selectedTags.includes(tag.tagName) }"
        >
          {{ tag.tagName }}
          <span class="ml-1 text-gray-500">({{ tag.articleCount }})</span>
        </button>
      </div>
    </div>
    
    <!-- 帮助提示 -->
    <p class="text-xs text-gray-500">
      提示：输入标签名称后按回车或逗号添加，可以选择热门标签快速添加
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { TagApi, type TagDto } from '@/api/tag'

// Props
interface Props {
  modelValue: string
  placeholder?: string
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '输入标签名称'
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

// Reactive data
const newTagInput = ref('')
const selectedTags = ref<string[]>([])
const suggestions = ref<TagDto[]>([])
const popularTags = ref<TagDto[]>([])
const showSuggestions = ref(false)

// Computed
const tagsString = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value)
})

// Methods
const addTag = () => {
  const tag = newTagInput.value.trim()
  if (tag && !selectedTags.value.includes(tag)) {
    selectedTags.value.push(tag)
    updateTagsString()
    newTagInput.value = ''
    showSuggestions.value = false
  }
}

const removeTag = (index: number) => {
  selectedTags.value.splice(index, 1)
  updateTagsString()
}

const addPopularTag = (tagName: string) => {
  if (!selectedTags.value.includes(tagName)) {
    selectedTags.value.push(tagName)
    updateTagsString()
  }
}

const selectSuggestion = (suggestion: TagDto) => {
  if (!selectedTags.value.includes(suggestion.tagName)) {
    selectedTags.value.push(suggestion.tagName)
    updateTagsString()
    newTagInput.value = ''
    showSuggestions.value = false
  }
}

const updateTagsString = () => {
  tagsString.value = selectedTags.value.join(',')
}

const onInputChange = async () => {
  const input = newTagInput.value.trim()
  
  if (input.length >= 2) {
    try {
      const response = await TagApi.searchTags(input, 0, 10)
      if (response.success) {
        suggestions.value = response.data.content
        showSuggestions.value = true
      }
    } catch (error) {
      console.error('搜索标签失败:', error)
      suggestions.value = []
    }
  } else {
    suggestions.value = []
    showSuggestions.value = false
  }
}

const loadPopularTags = async () => {
  try {
    const response = await TagApi.getPopularTags(10)
    if (response.success) {
      popularTags.value = response.data
    }
  } catch (error) {
    console.error('获取热门标签失败:', error)
  }
}

// 点击外部隐藏建议列表
const handleClickOutside = (event: Event) => {
  const target = event.target as HTMLElement
  if (!target.closest('.tag-selector')) {
    showSuggestions.value = false
  }
}

// Watch
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    selectedTags.value = TagApi.parseTagsString(newValue)
  } else {
    selectedTags.value = []
  }
}, { immediate: true })

// Lifecycle
onMounted(() => {
  loadPopularTags()
  document.addEventListener('click', handleClickOutside)
})

// Cleanup
const cleanup = () => {
  document.removeEventListener('click', handleClickOutside)
}

// Expose cleanup for parent component
defineExpose({ cleanup })
</script>

<style scoped>
/* 自定义样式 */
.tag-selector {
  position: relative;
}

/* 确保下拉列表在其他元素之上 */
.tag-selector .absolute {
  z-index: 50;
}
</style>
