<template>
  <div class="wiki-editor-container">
    <!-- 编辑器工具栏 -->
    <div class="editor-toolbar">
      <div class="toolbar-group">
        <button @click="insertText('\'\'\'', '\'\'\'', '粗体文字')" title="粗体 (Ctrl+B)" class="toolbar-btn">
          <strong>B</strong>
        </button>
        <button @click="insertText('\'\'', '\'\'', '斜体文字')" title="斜体 (Ctrl+I)" class="toolbar-btn">
          <em>I</em>
        </button>
        <button @click="insertText('~~~~~', '~~~~~', '下划线文字')" title="下划线" class="toolbar-btn">
          <u>U</u>
        </button>
        <button @click="insertText('----', '----', '删除线文字')" title="删除线" class="toolbar-btn">
          <s>S</s>
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
          <span class="icon">•</span>
        </button>
        <button @click="insertText('# ', '', '列表项')" title="有序列表" class="toolbar-btn">
          <span class="icon">1.</span>
        </button>
        <button @click="insertText(': ', '', '缩进文字')" title="缩进" class="toolbar-btn">
          <span class="icon">⇥</span>
        </button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button @click="insertText('[[', ']]', '链接文字')" title="内部链接" class="toolbar-btn">
          <span class="icon">🔗</span>
        </button>
        <button @click="insertText('[', ']', 'http://example.com 显示文字')" title="外部链接" class="toolbar-btn">
          <span class="icon">🌐</span>
        </button>
        <button @click="insertText('{{', '}}', '模板名称')" title="模板" class="toolbar-btn">
          <span class="icon">📄</span>
        </button>
        <button @click="insertText('[[分类:', ']]', '环保')" title="分类标签" class="toolbar-btn">
          <span class="icon">🏷</span>
        </button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button @click="insertTable" title="插入表格" class="toolbar-btn">
          <span class="icon">📊</span>
        </button>
        <button @click="insertText('<nowiki>', '</nowiki>', '原始文字')" title="原始文字" class="toolbar-btn">
          <span class="icon">&lt;&gt;</span>
        </button>
        <button @click="insertHorizontalRule" title="水平线" class="toolbar-btn">
          <span class="icon">—</span>
        </button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button @click="togglePreview" title="预览" class="toolbar-btn" :class="{ active: showPreview }">
          <span class="icon">👁</span>
        </button>
        <button @click="toggleHelpPanel" title="帮助" class="toolbar-btn" :class="{ active: showHelpPanel }">
          <span class="icon">❓</span>
        </button>
      </div>
    </div>

    <!-- 编辑器主体 -->
    <div class="editor-body" :class="{ 'split-view': showPreview }">
      <!-- 编辑区域 -->
      <div class="editor-pane" v-show="!showPreview || showEditor">
        <textarea
          ref="editorTextarea"
          v-model="content"
          class="wiki-editor"
          :placeholder="placeholder"
          @keydown="handleKeydown"
          @input="handleInput"
          @selectionchange="updateSelection"
          @scroll="syncScroll"
        ></textarea>
        
        <!-- 行号 -->
        <div v-if="showLineNumbers" class="line-numbers">
          <span v-for="n in lineCount" :key="n" class="line-number">{{ n }}</span>
        </div>
      </div>

      <!-- 预览区域 -->
      <div class="preview-pane" v-show="showPreview" ref="previewPane">
        <div class="preview-content" v-html="previewHtml"></div>
      </div>
    </div>

    <!-- 帮助面板 -->
    <div class="wiki-help-panel" v-show="showHelpPanel">
      <div class="help-header">
        <h4>Wiki语法帮助</h4>
        <button @click="toggleHelpPanel" class="close-help">✕</button>
      </div>
      <div class="help-content">
        <div class="help-section">
          <h5>文本格式</h5>
          <div class="help-items">
            <div class="help-item">
              <code>'''粗体'''</code> → <strong>粗体</strong>
            </div>
            <div class="help-item">
              <code>''斜体''</code> → <em>斜体</em>
            </div>
            <div class="help-item">
              <code>~~~~~下划线~~~~~</code> → <u>下划线</u>
            </div>
            <div class="help-item">
              <code>----删除线----</code> → <s>删除线</s>
            </div>
          </div>
        </div>

        <div class="help-section">
          <h5>标题</h5>
          <div class="help-items">
            <div class="help-item">
              <code>== 二级标题 ==</code>
            </div>
            <div class="help-item">
              <code>=== 三级标题 ===</code>
            </div>
            <div class="help-item">
              <code>==== 四级标题 ====</code>
            </div>
          </div>
        </div>

        <div class="help-section">
          <h5>链接</h5>
          <div class="help-items">
            <div class="help-item">
              <code>[[页面名称]]</code> - 内部链接
            </div>
            <div class="help-item">
              <code>[[页面名称|显示文本]]</code> - 带显示文本的内部链接
            </div>
            <div class="help-item">
              <code>[http://example.com]</code> - 外部链接
            </div>
            <div class="help-item">
              <code>[http://example.com 显示文本]</code> - 带显示文本的外部链接
            </div>
          </div>
        </div>

        <div class="help-section">
          <h5>列表</h5>
          <div class="help-items">
            <div class="help-item">
              <code>* 无序列表项</code>
            </div>
            <div class="help-item">
              <code>** 二级无序列表项</code>
            </div>
            <div class="help-item">
              <code># 有序列表项</code>
            </div>
            <div class="help-item">
              <code>## 二级有序列表项</code>
            </div>
          </div>
        </div>

        <div class="help-section">
          <h5>表格</h5>
          <div class="help-items">
            <div class="help-item">
              <pre><code>{|
! 标题1 !! 标题2
|-
| 单元格1 || 单元格2
|}</code></pre>
            </div>
          </div>
        </div>

        <div class="help-section">
          <h5>模板</h5>
          <div class="help-items">
            <div class="help-item">
              <code v-html="'{{info|信息内容}}'"></code> - 信息框
            </div>
            <div class="help-item">
              <code v-html="'{{warning|警告内容}}'"></code> - 警告框
            </div>
            <div class="help-item">
              <code v-html="'{{note|注意内容}}'"></code> - 注意框
            </div>
          </div>
        </div>

        <div class="help-section">
          <h5>分类和标签</h5>
          <div class="help-items">
            <div class="help-item">
              <code>[[分类:环保]]</code> - 添加分类标签
            </div>
            <div class="help-item">
              <code>[[Category:技术]]</code> - 英文分类语法
            </div>
            <div class="help-item">
              <span class="help-note">分类会自动转换为文章标签</span>
            </div>
          </div>
        </div>

        <div class="help-section">
          <h5>快捷键</h5>
          <div class="help-items">
            <div class="help-item">
              <code>Ctrl+B</code> → 粗体
            </div>
            <div class="help-item">
              <code>Ctrl+I</code> → 斜体
            </div>
            <div class="help-item">
              <code>Ctrl+S</code> → 保存
            </div>
            <div class="help-item">
              <code>Tab</code> → 缩进
            </div>
            <div class="help-item">
              <code>Shift+Tab</code> → 取消缩进
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 状态栏 -->
    <div class="editor-status-bar">
      <div class="status-left">
        <span class="status-item">行: {{ currentLine }}</span>
        <span class="status-item">列: {{ currentColumn }}</span>
        <span class="status-item">字符: {{ content.length }}</span>
      </div>
      <div class="status-right">
        <span class="status-item">Wiki语法</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { wikiParser } from '../utils/wikiParser'

interface Props {
  modelValue: string
  placeholder?: string
  readonly?: boolean
  showLineNumbers?: boolean
  autoPreview?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '在此输入Wiki语法...',
  readonly: false,
  showLineNumbers: true,
  autoPreview: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'save': [content: string]
  'preview': [html: string]
}>()

// 响应式数据
const content = ref(props.modelValue)
const showPreview = ref(props.autoPreview)
const showHelpPanel = ref(false)
const showEditor = ref(true)
const currentLine = ref(1)
const currentColumn = ref(1)

// 模板引用
const editorTextarea = ref<HTMLTextAreaElement>()
const previewPane = ref<HTMLElement>()

// 计算属性
const lineCount = computed(() => {
  return content.value.split('\n').length
})

const previewHtml = computed(() => {
  return wikiParser.parseToHtml(content.value)
})

// 监听内容变化
watch(content, (newValue) => {
  emit('update:modelValue', newValue)
  if (props.autoPreview) {
    emit('preview', previewHtml.value)
  }
})

watch(() => props.modelValue, (newValue) => {
  if (newValue !== content.value) {
    content.value = newValue
  }
})

// 方法
const insertText = (before: string, after: string = '', defaultText: string = '') => {
  const textarea = editorTextarea.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = content.value.substring(start, end)
  const textToInsert = selectedText || defaultText
  
  const newText = before + textToInsert + after
  
  content.value = content.value.substring(0, start) + newText + content.value.substring(end)
  
  nextTick(() => {
    textarea.focus()
    const newStart = start + before.length
    const newEnd = newStart + textToInsert.length
    textarea.setSelectionRange(newStart, newEnd)
  })
}

const insertHeading = (level: number) => {
  const prefix = '='.repeat(level) + ' '
  const suffix = ' ' + '='.repeat(level)
  insertText(prefix, suffix, '标题文字')
}

const insertTable = () => {
  const tableTemplate = `{|
! 标题1 !! 标题2 !! 标题3
|-
| 单元格1 || 单元格2 || 单元格3
|-
| 单元格4 || 单元格5 || 单元格6
|}`
  insertText(tableTemplate)
}

const insertHorizontalRule = () => {
  insertText('\n----\n')
}

const togglePreview = () => {
  showPreview.value = !showPreview.value
  if (showPreview.value) {
    emit('preview', previewHtml.value)
  }
}

const toggleHelpPanel = () => {
  showHelpPanel.value = !showHelpPanel.value
}

const handleKeydown = (event: KeyboardEvent) => {
  // 快捷键处理
  if (event.ctrlKey || event.metaKey) {
    switch (event.key) {
      case 'b':
      case 'B':
        event.preventDefault()
        insertText('\'\'\'', '\'\'\'', '粗体文字')
        break
      case 'i':
      case 'I':
        event.preventDefault()
        insertText('\'\'', '\'\'', '斜体文字')
        break
      case 's':
      case 'S':
        event.preventDefault()
        emit('save', content.value)
        break
    }
  } else if (event.key === 'Tab') {
    event.preventDefault()
    const textarea = editorTextarea.value
    if (!textarea) return

    const start = textarea.selectionStart
    const end = textarea.selectionEnd

    if (event.shiftKey) {
      // Shift+Tab: 取消缩进
      const lineStart = content.value.lastIndexOf('\n', start - 1) + 1
      const lineEnd = content.value.indexOf('\n', end)
      const lineEndPos = lineEnd === -1 ? content.value.length : lineEnd

      const lines = content.value.substring(lineStart, lineEndPos).split('\n')
      const unindentedLines = lines.map(line => {
        if (line.startsWith('  ')) return line.substring(2)
        if (line.startsWith('\t')) return line.substring(1)
        return line
      })

      const newText = unindentedLines.join('\n')
      content.value = content.value.substring(0, lineStart) + newText + content.value.substring(lineEndPos)
    } else {
      // Tab: 缩进
      insertText('  ')
    }
  }
}

const handleInput = () => {
  updateSelection()
}

const updateSelection = () => {
  const textarea = editorTextarea.value
  if (!textarea) return

  const start = textarea.selectionStart
  const textBeforeCursor = content.value.substring(0, start)
  const lines = textBeforeCursor.split('\n')
  
  currentLine.value = lines.length
  currentColumn.value = lines[lines.length - 1].length + 1
}

const syncScroll = () => {
  if (!showPreview.value || !previewPane.value || !editorTextarea.value) return

  const textarea = editorTextarea.value
  const preview = previewPane.value
  
  const scrollRatio = textarea.scrollTop / (textarea.scrollHeight - textarea.clientHeight)
  preview.scrollTop = scrollRatio * (preview.scrollHeight - preview.clientHeight)
}

// 生命周期
onMounted(() => {
  updateSelection()
})

onUnmounted(() => {
  // 清理工作
})
</script>

<style scoped>
.wiki-editor-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  background: #ffffff;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #f6f8fa;
  border-bottom: 1px solid #d0d7de;
  border-radius: 6px 6px 0 0;
  gap: 4px;
}

.toolbar-group {
  display: flex;
  gap: 2px;
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: #d0d7de;
  margin: 0 8px;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  color: #656d76;
  font-size: 14px;
  transition: all 0.2s;
}

.toolbar-btn:hover {
  background: #f3f4f6;
  color: #24292f;
}

.toolbar-btn.active {
  background: #0969da;
  color: white;
}

.toolbar-btn .icon {
  font-size: 16px;
}

.editor-body {
  display: flex;
  flex: 1;
  min-height: 0;
}

.editor-body.split-view {
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.editor-pane {
  position: relative;
  display: flex;
  flex: 1;
}

.wiki-editor {
  flex: 1;
  padding: 16px;
  border: none;
  outline: none;
  resize: none;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', 'Source Code Pro', monospace;
  font-size: 14px;
  line-height: 1.6;
  background: transparent;
}

.line-numbers {
  position: absolute;
  left: 0;
  top: 0;
  padding: 16px 8px;
  background: #f6f8fa;
  border-right: 1px solid #d0d7de;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', 'Source Code Pro', monospace;
  font-size: 12px;
  line-height: 1.6;
  color: #656d76;
  user-select: none;
  pointer-events: none;
}

.line-number {
  display: block;
  text-align: right;
  min-width: 24px;
}

.editor-pane .wiki-editor {
  padding-left: 56px;
}

.preview-pane {
  flex: 1;
  overflow-y: auto;
  border-left: 1px solid #d0d7de;
  background: #ffffff;
}

.preview-content {
  padding: 16px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans', Helvetica, Arial, sans-serif;
  font-size: 16px;
  line-height: 1.6;
  color: #24292f;
}

.wiki-help-panel {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #d0d7de;
  border-top: none;
  border-radius: 0 0 6px 6px;
  box-shadow: 0 8px 24px rgba(140, 149, 159, 0.2);
  z-index: 10;
  max-height: 400px;
  overflow-y: auto;
}

.help-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f6f8fa;
  border-bottom: 1px solid #d0d7de;
}

.help-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #24292f;
}

.close-help {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #656d76;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.close-help:hover {
  background: #f3f4f6;
  color: #24292f;
}

.help-content {
  padding: 16px;
}

.help-section {
  margin-bottom: 24px;
}

.help-section:last-child {
  margin-bottom: 0;
}

.help-section h5 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #24292f;
}

.help-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.help-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.help-item code {
  background: #f6f8fa;
  border: 1px solid #d0d7de;
  border-radius: 3px;
  padding: 2px 6px;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', 'Source Code Pro', monospace;
  font-size: 12px;
}

.help-item pre {
  margin: 0;
  background: #f6f8fa;
  border: 1px solid #d0d7de;
  border-radius: 3px;
  padding: 8px;
  font-size: 12px;
  white-space: pre-wrap;
}

.editor-status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f6f8fa;
  border-top: 1px solid #d0d7de;
  border-radius: 0 0 6px 6px;
  font-size: 12px;
  color: #656d76;
}

.status-left,
.status-right {
  display: flex;
  gap: 16px;
}

.status-item {
  white-space: nowrap;
}

/* 预览内容样式 */
.preview-content :deep(h1),
.preview-content :deep(h2),
.preview-content :deep(h3),
.preview-content :deep(h4),
.preview-content :deep(h5),
.preview-content :deep(h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}

.preview-content :deep(h1) {
  font-size: 2em;
  border-bottom: 1px solid #d0d7de;
  padding-bottom: 10px;
}

.preview-content :deep(h2) {
  font-size: 1.5em;
  border-bottom: 1px solid #d0d7de;
  padding-bottom: 8px;
}

.preview-content :deep(h3) {
  font-size: 1.25em;
}

.preview-content :deep(p) {
  margin-bottom: 16px;
}

.preview-content :deep(ul),
.preview-content :deep(ol) {
  margin-bottom: 16px;
  padding-left: 32px;
}

.preview-content :deep(li) {
  margin-bottom: 4px;
}

.preview-content :deep(a) {
  color: #0969da;
  text-decoration: none;
}

.preview-content :deep(a:hover) {
  text-decoration: underline;
}

.preview-content :deep(.wiki-link) {
  color: #0969da;
}

.preview-content :deep(.external-link) {
  color: #0969da;
}

.preview-content :deep(.external-link):after {
  content: " ↗";
  font-size: 0.8em;
}

.preview-content :deep(table) {
  border-collapse: collapse;
  margin-bottom: 16px;
  width: 100%;
}

.preview-content :deep(th),
.preview-content :deep(td) {
  border: 1px solid #d0d7de;
  padding: 8px 12px;
  text-align: left;
}

.preview-content :deep(th) {
  background: #f6f8fa;
  font-weight: 600;
}

.preview-content :deep(.wiki-template) {
  margin: 16px 0;
  padding: 12px;
  border-radius: 6px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.preview-content :deep(.info-box) {
  background: #dbeafe;
  border: 1px solid #3b82f6;
}

.preview-content :deep(.warning-box) {
  background: #fef3c7;
  border: 1px solid #f59e0b;
}

.preview-content :deep(.note-box) {
  background: #f0f9ff;
  border: 1px solid #0ea5e9;
}

.preview-content :deep(.template-icon) {
  font-size: 18px;
  flex-shrink: 0;
}

.preview-content :deep(.template-content) {
  flex: 1;
}

.preview-content :deep(pre) {
  background: #f6f8fa;
  border-radius: 6px;
  padding: 16px;
  overflow-x: auto;
  margin-bottom: 16px;
}

.preview-content :deep(code) {
  background: #f6f8fa;
  border-radius: 3px;
  padding: 2px 4px;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', 'Source Code Pro', monospace;
  font-size: 85%;
}

.preview-content :deep(pre code) {
  background: transparent;
  padding: 0;
}
</style>
