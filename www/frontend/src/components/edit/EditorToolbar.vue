<!--
  编辑器工具栏组件
  
  提供Wiki编辑器的工具栏功能，包括格式化按钮、标题、列表、链接等。
  支持文本插入、预览切换等操作。
-->
<template>
  <div class="editor-toolbar">
    <div class="toolbar-section">
      <div class="toolbar-group">
        <button @click="insertText('\'\'\'', '\'\'\'', '粗体文字')" title="粗体 (Ctrl+B)" class="toolbar-btn">
          <strong>B</strong>
        </button>
        <button @click="insertText('\'\'', '\'\'', '斜体文字')" title="斜体 (Ctrl+I)" class="toolbar-btn">
          <em>I</em>
        </button>
        <button @click="insertText('[[', ']]', '链接文字')" title="内部链接" class="toolbar-btn">
          链接
        </button>
        <button @click="insertText('[', ' ]', 'http://example.com 显示文字')" title="外部链接" class="toolbar-btn">
          外链
        </button>
        <button @click="insertText('<ref>', '</ref>', '引用内容')" title="引用" class="toolbar-btn">
          引用
        </button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button @click="insertHeading(2)" title="二级标题" class="toolbar-btn">H2</button>
        <button @click="insertHeading(3)" title="三级标题" class="toolbar-btn">H3</button>
        <button @click="insertHeading(4)" title="四级标题" class="toolbar-btn">H4</button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button @click="insertText('* ', '', '列表项')" title="无序列表" class="toolbar-btn">
          • 列表
        </button>
        <button @click="insertText('# ', '', '列表项')" title="有序列表" class="toolbar-btn">
          1. 列表
        </button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button @click="insertTable" title="插入表格" class="toolbar-btn">
          表格
        </button>
        <button @click="insertText('{{', '}}', '模板名称')" title="模板" class="toolbar-btn">
          模板
        </button>
        <button @click="insertText('[[分类:', ']]', '环保')" title="添加分类标签" class="toolbar-btn">
           分类
        </button>
        <button @click="insertText('<nowiki>', '</nowiki>', '原始文字')" title="原始文字" class="toolbar-btn">
          原始
        </button>
      </div>
    </div>
    
    <div class="toolbar-right">
      <button @click="$emit('togglePreview')" class="toolbar-btn preview-btn" :class="{ active: showPreview }">
        {{ showPreview ? '隐藏预览' : '显示预览' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  showPreview: boolean
}

// 定义 props
defineProps<Props>()

// 获取 emit 函数
const emit = defineEmits<{
  togglePreview: []
  insertText: [prefix: string, suffix: string, placeholder: string]
  insertHeading: [level: number]
  insertTable: []
}>()

// 方法
const insertText = (prefix: string, suffix: string, placeholder: string) => {
  emit('insertText', prefix, suffix, placeholder)
}

const insertHeading = (level: number) => {
  emit('insertHeading', level)
}

const insertTable = () => {
  emit('insertTable')
}
</script>

<style scoped>
.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border: 1px solid #e5e7eb;
  border-bottom: none;
  border-radius: 12px 12px 0 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.toolbar-section {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.toolbar-divider {
  width: 1px;
  height: 1.5rem;
  background-color: #d1d5db;
  margin: 0 0.5rem;
}

.toolbar-btn {
  padding: 0.5rem 0.875rem;
  font-size: 0.875rem;
  color: #374151;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.toolbar-btn:hover {
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  border-color: #9ca3af;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.toolbar-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.preview-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  padding: 0.625rem 1.25rem;
  border-radius: 20px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}

.preview-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}

.preview-btn.active {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
  box-shadow: 0 2px 12px rgba(5, 150, 105, 0.4);
}

.toolbar-right {
  display: flex;
  align-items: center;
}
</style>
