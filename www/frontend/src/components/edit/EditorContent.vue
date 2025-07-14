<!--
  编辑器内容区域组件
  
  纯文本编辑器，不包含预览功能。预览功能移至独立区域。
-->
<template>
  <div class="editor-content">
    <!-- 文本编辑器 -->
    <div class="editor-pane">
      <textarea
        ref="editorTextarea"
        :value="modelValue"
        @input="updateValue"
        class="wiki-editor"
        placeholder="请输入文章内容..."
        @keydown="$emit('keydown', $event)"
        rows="20"
      ></textarea>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  modelValue: string
}

// 定义 props
defineProps<Props>()

// 获取 emit 函数
const emit = defineEmits<{
  'update:modelValue': [value: string]
  keydown: [event: KeyboardEvent]
}>()

// 组件引用
const editorTextarea = ref<HTMLTextAreaElement | null>(null)

// 更新值的方法
const updateValue = (event: Event) => {
  const target = event.target as HTMLTextAreaElement
  emit('update:modelValue', target.value)
}

// 暴露编辑器引用
defineExpose({
  editorTextarea
})
</script>

<style scoped>
.editor-content {
  display: flex;
  border: 1px solid #e5e7eb;
  border-radius: 0 0 12px 12px;
  min-height: 500px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.editor-pane {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.wiki-editor {
  width: 100%;
  min-height: 500px;
  padding: 1rem;
  font-family: 'Courier New', monospace;
  font-size: 0.875rem;
  line-height: 1.5;
  border: none;
  outline: none;
  resize: vertical;
  background-color: white;
}

.wiki-editor:focus {
  outline: none;
}
</style>
