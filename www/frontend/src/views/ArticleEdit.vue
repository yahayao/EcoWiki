<template>
  <div class="article-edit-page">
    <div class="container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在加载文章数据...</p>
      </div>

      <!-- 编辑界面 -->
      <template v-else>
        <!-- 页面标题栏 -->
        <div class="edit-header">
                <div class="title-section">
                    <h1>编辑 "{{ articleTitle }}"</h1>
                    <p class="subtitle">
                        您正在编辑此页面的当前版本。 请在"摘要"框中描述您的更改摘要，并记录您对各条目的编辑摘要，
                        以帮助其他编辑者和未来的自己了解您的更改。
                    </p>
                </div>
                <div class="action-buttons">
                    <button @click="goBack" class="back-btn">
                        <span class="back-icon">←</span>
                        返回文章
                    </button>
                </div>
            </div>

            <!-- 编辑提示 -->
            <div class="edit-notice">
                <div class="notice-icon">💡</div>
                <div class="notice-content">
                    <p><strong>关于页面编辑：</strong> 我们很高兴您愿意为本项目做出贡献，您也是为这一次性接受新条目的贡献，我们仅能接受作者授权条目或原创。</p>
                    <p><strong>关于页面内容：</strong> 此页面作为发布到互联网的条目，所以其在流程语义信息，如有雷同工作的实体条目条目，访问者应该是对同类条目至关重要的平台。</p>
                </div>
            </div>

            <!-- 编辑工具栏 -->
            <div class="edit-toolbar">
                <div class="toolbar-group">
                    <button @click="insertText('\'\'\'', '\'\'\'', '粗体文字')" title="粗体" class="toolbar-btn">
                        <strong>B</strong>
                    </button>
                    <button @click="insertText('\'\'', '\'\'', '斜体文字')" title="斜体" class="toolbar-btn">
                        <em>I</em>
                    </button>
                    <button @click="insertText('== ', ' ==', '标题文字')" title="标题" class="toolbar-btn">
                        H2
                    </button>
                    <button @click="insertText('[[', ']]', '链接文字')" title="内部链接" class="toolbar-btn">
                        🔗
                    </button>
                    <button @click="insertText('{{', '}}', '模板名称')" title="模板" class="toolbar-btn">
                        📄
                    </button>
                </div>
                <div class="toolbar-group">
                    <button @click="insertText('* ', '', '列表项')" title="无序列表" class="toolbar-btn">
                        •
                    </button>
                    <button @click="insertText('# ', '', '列表项')" title="有序列表" class="toolbar-btn">
                        1.
                    </button>
                    <button @click="insertText(':', '', '缩进文字')" title="缩进" class="toolbar-btn">
                        ⇥
                    </button>
                    <button @click="insertText('<nowiki>', '</nowiki>', '原始文字')" title="原始文字" class="toolbar-btn">
                        &lt;&gt;
                    </button>
                </div>
            </div>

            <!-- 编辑表单 -->
            <div class="edit-form">
                <!-- 编辑区域 -->
                <div class="edit-section">
                    <div class="editor-container">
                        <textarea ref="editorTextarea" v-model="editorContent" class="wiki-editor"
                            placeholder="在此输入wiki语法..." @keydown="handleKeydown"
                            @selectionchange="updateSelection"></textarea>

                        <!-- Wiki语法帮助面板 -->
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
                                    </div>
                                </div>

                                <div class="help-section">
                                    <h5>链接</h5>
                                    <div class="help-items">
                                        <div class="help-item">
                                            <code>[[页面名称]]</code>
                                        </div>
                                        <div class="help-item">
                                            <code>[[页面名称|显示文本]]</code>
                                        </div>
                                    </div>
                                </div>

                                <div class="help-section">
                                    <h5>列表</h5>
                                    <div class="help-items">
                                        <div class="help-item">
                                            <code>* 无序列表</code>
                                        </div>
                                        <div class="help-item">
                                            <code># 有序列表</code>
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
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- 工具栏扩展 -->
                    <div class="editor-tools">
                        <button @click="toggleHelpPanel" class="tool-btn" :class="{ active: showHelpPanel }">
                            📖 语法帮助
                        </button>
                        <button @click="insertTemplate" class="tool-btn">
                            📄 插入模板
                        </button>
                        <button @click="insertTable" class="tool-btn">
                            📊 插入表格
                        </button>
                    </div>
                </div>

                <!-- 编辑摘要 -->
                <div class="summary-section">
                    <label for="edit-summary" class="summary-label">摘要（描述你的更改）：</label>
                    <input id="edit-summary" v-model="editSummary" type="text" class="summary-input"
                        placeholder="例如：修正错别字、添加新内容、更新信息等..." maxlength="200" />
                    <div class="summary-help">
                        <span class="char-count">{{ editSummary.length }}/200</span>
                        <span class="summary-tip">请简要说明您的更改内容</span>
                    </div>
                </div>

                <!-- 保存选项 -->
                <div class="save-options">
                    <div class="save-buttons">
                        <button @click="saveChanges" class="save-btn primary" :disabled="saving">
                            <span v-if="saving" class="loading-spinner"></span>
                            {{ saving ? '保存中...' : '保存更改' }}
                        </button>
                        <button @click="showPreview" class="preview-btn">显示预览</button>
                        <button @click="showDiff" class="diff-btn">显示更改</button>
                        <button @click="goBack" class="cancel-btn">取消</button>
                    </div>

                    <div class="save-info">
                        <label class="checkbox-label">
                            <input type="checkbox" v-model="isMinorEdit" />
                            <span class="checkmark"></span>
                            这是一个小编辑
                        </label>
                        <label class="checkbox-label">
                            <input type="checkbox" v-model="watchPage" />
                            <span class="checkmark"></span>
                            监视此页面
                        </label>
                    </div>
                </div>
            </div>

            <!-- 预览区域 -->
            <div v-if="showPreviewMode" class="preview-section">
                <div class="preview-header">
                    <h3>预览</h3>
                    <button @click="hidePreview" class="close-preview">✕</button>
                </div>
                <div class="preview-content" v-html="previewHtml"></div>
            </div>

            <!-- 更改对比区域 -->
            <div v-if="showDiffMode" class="diff-section">
                <div class="diff-header">
                    <h3>更改对比</h3>
                    <button @click="hideDiff" class="close-diff">✕</button>
                </div>
                <div class="diff-content">
                    <div class="diff-old">
                        <h4>原始版本</h4>
                        <pre>{{ originalContent }}</pre>
                    </div>
                    <div class="diff-new">
                        <h4>您的版本</h4>
                        <pre>{{ editorContent }}</pre>
                    </div>
                </div>
            </div>
        </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'

const route = useRoute()
const router = useRouter()

const articleTitle = ref('')
const editorContent = ref('')
const originalContent = ref('')
const editSummary = ref('')
const isMinorEdit = ref(false)
const watchPage = ref(true)
const saving = ref(false)

const showPreviewMode = ref(false)
const showDiffMode = ref(false)
const showHelpPanel = ref(false)
const previewHtml = ref('')
const loading = ref(true)

const editorTextarea = ref<HTMLTextAreaElement>()

onMounted(() => {
    loadArticleForEdit()
    // 添加浏览器 beforeunload 事件监听器
    window.addEventListener('beforeunload', handleBeforeUnload)
})

onUnmounted(() => {
    // 组件卸载时移除事件监听器
    window.removeEventListener('beforeunload', handleBeforeUnload)
})

// Vue Router 导航守卫 - 捕获所有路由离开事件（包括浏览器后退按钮）
onBeforeRouteLeave((to, from, next) => {
    // 检查是否有未保存的更改
    if (editorContent.value !== originalContent.value) {
        const answer = confirm('是否离开网站？\n您所做的更改可能未保存。')
        if (answer) {
            next() // 允许导航
        } else {
            next(false) // 取消导航
        }
    } else {
        next() // 没有更改，直接允许导航
    }
})

// 处理浏览器 beforeunload 事件（刷新页面、关闭标签页等）
const handleBeforeUnload = (event: BeforeUnloadEvent) => {
    // 检查是否有未保存的更改
    if (editorContent.value !== originalContent.value) {
        // 设置返回值以触发浏览器的确认对话框
        event.preventDefault()
        // 对于现代浏览器，返回值会被忽略，但设置它仍然是个好习惯
        event.returnValue = '您所做的更改可能未保存。'
        return '您所做的更改可能未保存。'
    }
}

const loadArticleForEdit = async () => {
    try {
        loading.value = true
        const articleId = route.params.id as string
        
        if (!articleId) {
            throw new Error('文章ID不存在')
        }

        // 模拟加载文章数据
        await new Promise(resolve => setTimeout(resolve, 500))

        // 这里应该从API获取真实的文章数据
        // const response = await fetch(`/api/articles/${articleId}`)
        // const articleData = await response.json()
        
        // 模拟根据ID获取不同的文章数据
        const mockArticles: Record<string, { title: string; content: string }> = {
            '1': {
                title: '人工智能发展史与未来展望',
                content: `== 引言 ==
人工智能（Artificial Intelligence，AI）作为21世纪最具革命性的技术之一，正在深刻改变着我们的生活方式、工作模式和社会结构。

== 人工智能的历史发展 ==
从1950年代艾伦·图灵提出著名的"图灵测试"开始，到如今ChatGPT等大型语言模型的横空出世，人工智能经历了数十年的发展历程。

=== 早期探索阶段（1950-1980年代） ===
1950年，英国数学家艾伦·图灵发表了具有里程碑意义的论文《计算机器与智能》。

== 现代AI的技术特征 ==
* '''大型语言模型'''：GPT系列、BERT等
* '''多模态AI'''：DALL-E、Midjourney等
* '''深度学习'''：神经网络的突破性进展`
            },
            '2': {
                title: 'U(龙与雀斑公主)',
                content: `== 概述 ==
《U(龙与雀斑公主)》是一部由[[细田守]]执导的日本动画电影。

== 剧情简介 ==
故事讲述了一个关于[[虚拟世界]]和[[现实世界]]的冒险故事。

== 角色介绍 ==
* '''主人公'''：铃
* '''配角'''：其他角色

== 制作信息 ==
* '''导演'''：细田守
* '''制作公司'''：Studio Chizu`
            }
        }
        
        // 获取对应ID的文章数据，如果不存在则使用默认数据
        const articleData = mockArticles[articleId] || {
            title: `文章 ${articleId}`,
            content: `== 标题 ==
这是文章 ${articleId} 的内容。

请在此处编辑内容...`
        }

        articleTitle.value = articleData.title
        originalContent.value = articleData.content
        editorContent.value = originalContent.value

    } catch (error) {
        console.error('Failed to load article for editing:', error)
        // 设置默认值，避免页面出错
        articleTitle.value = '未知文章'
        originalContent.value = '== 标题 ==\n\n请在此处编辑内容...'
        editorContent.value = originalContent.value
    } finally {
        loading.value = false
    }
}

const insertText = (before: string, after: string = '', placeholder: string = '') => {
    if (!editorTextarea.value) return

    const textarea = editorTextarea.value
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const selectedText = textarea.value.substring(start, end)

    let insertText = before
    if (selectedText) {
        insertText += selectedText
    } else if (placeholder) {
        insertText += placeholder
    }
    insertText += after

    const newValue = textarea.value.substring(0, start) + insertText + textarea.value.substring(end)
    editorContent.value = newValue

    nextTick(() => {
        textarea.focus()
        const newPosition = start + before.length + (selectedText || placeholder).length
        textarea.setSelectionRange(newPosition, newPosition)
    })
}

const handleKeydown = (event: KeyboardEvent) => {
    // 处理快捷键
    if (event.ctrlKey || event.metaKey) {
        switch (event.key) {
            case 'b':
                event.preventDefault()
                insertText('\'\'\'', '\'\'\'', '粗体文字')
                break
            case 'i':
                event.preventDefault()
                insertText('\'\'', '\'\'', '斜体文字')
                break
            case 's':
                event.preventDefault()
                saveChanges()
                break
        }
    }

    // Tab键插入缩进
    if (event.key === 'Tab') {
        event.preventDefault()
        insertText('    ')
    }
}

const updateSelection = () => {
    // 更新选择状态
}

const saveChanges = async () => {
    if (saving.value) return

    try {
        saving.value = true

        // 模拟保存API调用
        await new Promise(resolve => setTimeout(resolve, 1500))

        console.log('Saving changes:', {
            content: editorContent.value,
            summary: editSummary.value,
            minor: isMinorEdit.value,
            watch: watchPage.value
        })

        // 保存成功后跳转回文章页面
        router.push(`/article/${route.params.id}`)

    } catch (error) {
        console.error('Failed to save changes:', error)
        alert('保存失败，请重试')
    } finally {
        saving.value = false
    }
}

const showPreview = () => {
    // 将wiki语法转换为HTML（简单示例）
    previewHtml.value = convertWikiToHtml(editorContent.value)
    showPreviewMode.value = true
    showDiffMode.value = false
}

const showDiff = () => {
    showDiffMode.value = true
    showPreviewMode.value = false
}

const hidePreview = () => {
    showPreviewMode.value = false
}

const hideDiff = () => {
    showDiffMode.value = false
}

const goBack = () => {
    if (editorContent.value !== originalContent.value) {
        if (confirm('是否离开网站？\n您所做的更改可能未保存。')) {
            router.push(`/article/${route.params.id}`)
        }
    } else {
        router.push(`/article/${route.params.id}`)
    }
}

const toggleHelpPanel = () => {
    showHelpPanel.value = !showHelpPanel.value
}

const insertTemplate = () => {
    insertText('{{', '}}', '模板名称')
}

const insertTable = () => {
    const tableTemplate = `{| class="wikitable"
|-
! 标题1 !! 标题2 !! 标题3
|-
| 单元格1 || 单元格2 || 单元格3
|-
| 单元格4 || 单元格5 || 单元格6
|}`
    insertText(tableTemplate)
}

// 简单的wiki语法转HTML转换器
const convertWikiToHtml = (wikiText: string): string => {
    let html = wikiText

    // 标题转换
    html = html.replace(/^===== (.*?) =====/gm, '<h5>$1</h5>')
    html = html.replace(/^==== (.*?) ====/gm, '<h4>$1</h4>')
    html = html.replace(/^=== (.*?) ===/gm, '<h3>$1</h3>')
    html = html.replace(/^== (.*?) ==/gm, '<h2>$1</h2>')
    html = html.replace(/^= (.*?) =/gm, '<h1>$1</h1>')

    // 文本格式
    html = html.replace(/'''(.*?)'''/g, '<strong>$1</strong>')
    html = html.replace(/''(.*?)''/g, '<em>$1</em>')
    html = html.replace(/____(.+?)____/g, '<u>$1</u>')
    html = html.replace(/----(.+?)----/g, '<s>$1</s>')

    // 链接转换
    html = html.replace(/\[\[([^|\]]+)\|([^\]]+)\]\]/g, '<a href="#" title="$1">$2</a>')
    html = html.replace(/\[\[([^\]]+)\]\]/g, '<a href="#" title="$1">$1</a>')
    html = html.replace(/\[([^\s]+)\s+([^\]]+)\]/g, '<a href="$1" target="_blank">$2</a>')

    // 模板转换
    html = html.replace(/\{\{([^}]+)\}\}/g, '<span class="template" style="background: #f0f0f0; padding: 2px 4px; border-radius: 3px;">模板:$1</span>')

    // 列表转换
    const lines = html.split('\n')
    let inUL = false, inOL = false
    const processedLines: string[] = []

    for (let i = 0; i < lines.length; i++) {
        const line = lines[i].trim()

        if (line.startsWith('* ')) {
            if (!inUL) {
                processedLines.push('<ul>')
                inUL = true
            }
            if (inOL) {
                processedLines.push('</ol>')
                inOL = false
            }
            processedLines.push(`<li>${line.substring(2)}</li>`)
        } else if (line.startsWith('# ')) {
            if (!inOL) {
                processedLines.push('<ol>')
                inOL = true
            }
            if (inUL) {
                processedLines.push('</ul>')
                inUL = false
            }
            processedLines.push(`<li>${line.substring(2)}</li>`)
        } else if (line.startsWith(': ')) {
            // 缩进
            processedLines.push(`<div style="margin-left: 20px;">${line.substring(2)}</div>`)
        } else {
            if (inUL) {
                processedLines.push('</ul>')
                inUL = false
            }
            if (inOL) {
                processedLines.push('</ol>')
                inOL = false
            }

            if (line === '') {
                processedLines.push('<br>')
            } else if (!line.startsWith('<h') && !line.includes('<li>')) {
                processedLines.push(`<p>${line}</p>`)
            } else {
                processedLines.push(line)
            }
        }
    }

    // 关闭未关闭的列表
    if (inUL) processedLines.push('</ul>')
    if (inOL) processedLines.push('</ol>')

    html = processedLines.join('\n')

    // 表格转换（基础支持）
    html = html.replace(/\{\|\s*class="wikitable"/g, '<table class="wikitable" style="border-collapse: collapse; border: 1px solid #ccc;">')
    html = html.replace(/\|\}/g, '</table>')
    html = html.replace(/\|-/g, '</tr><tr>')
    html = html.replace(/!\s*(.+)/g, '<th style="border: 1px solid #ccc; padding: 5px; background: #f0f0f0;">$1</th>')
    html = html.replace(/\|\s*(.+)/g, '<td style="border: 1px solid #ccc; padding: 5px;">$1</td>')
    html = html.replace(/<table[^>]*><\/tr>/, '<table class="wikitable" style="border-collapse: collapse; border: 1px solid #ccc;"><tr>')

    // nowiki标签
    html = html.replace(/<nowiki>(.*?)<\/nowiki>/g, '<code style="background: #f4f4f4; padding: 2px 4px; border-radius: 3px;">$1</code>')

    return html
}
</script>

<style scoped>
.article-edit-page {
    min-height: 100vh;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    padding: 20px 0;
}

.container {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
}

.loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    background: white;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.loading-state .loading-spinner {
    width: 40px;
    height: 40px;
    border: 4px solid #f3f4f6;
    border-top: 4px solid #667eea;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 16px;
}

.loading-state p {
    color: #718096;
    font-size: 1rem;
}

.edit-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    padding: 24px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.title-section h1 {
    color: #1a202c;
    font-size: 1.8rem;
    font-weight: 600;
    margin-bottom: 8px;
}

.subtitle {
    color: #718096;
    font-size: 0.9rem;
    line-height: 1.5;
    max-width: 600px;
}

.back-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 16px;
    background: linear-gradient(135deg, #667eea, #764ba2);
    color: white;
    border: none;
    border-radius: 20px;
    font-size: 0.9rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
}

.back-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.back-icon {
    font-size: 1.2rem;
    font-weight: bold;
}

.edit-notice {
    display: flex;
    gap: 12px;
    padding: 16px;
    background: #fff3cd;
    border: 1px solid #ffeaa7;
    border-radius: 8px;
    margin-bottom: 24px;
}

.notice-icon {
    font-size: 1.2rem;
    flex-shrink: 0;
}

.notice-content p {
    margin: 4px 0;
    color: #856404;
    font-size: 0.9rem;
    line-height: 1.4;
}

.edit-toolbar {
    display: flex;
    gap: 16px;
    padding: 12px 16px;
    background: white;
    border: 1px solid #e2e8f0;
    border-radius: 8px 8px 0 0;
    flex-wrap: wrap;
}

.toolbar-group {
    display: flex;
    gap: 4px;
}

.toolbar-btn {
    padding: 6px 10px;
    background: #f8f9fa;
    border: 1px solid #dee2e6;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s ease;
    font-size: 0.9rem;
    min-width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.toolbar-btn:hover {
    background: #e9ecef;
    border-color: #adb5bd;
}

.toolbar-btn:active {
    background: #dee2e6;
}

.edit-form {
    background: white;
    border: 1px solid #e2e8f0;
    border-top: none;
    border-radius: 0 0 8px 8px;
    overflow: hidden;
}

.edit-section {
    position: relative;
}

.editor-container {
    position: relative;
    display: flex;
}

.wiki-editor {
    flex: 1;
    min-height: 400px;
    padding: 16px;
    border: none;
    resize: vertical;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    font-size: 0.9rem;
    line-height: 1.5;
    outline: none;
    background: #fafafa;
}

.wiki-editor:focus {
    background: white;
}

.wiki-help-panel {
    width: 300px;
    background: white;
    border-left: 1px solid #e2e8f0;
    max-height: 400px;
    overflow-y: auto;
    flex-shrink: 0;
}

.help-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: #f8f9fa;
    border-bottom: 1px solid #e2e8f0;
    position: sticky;
    top: 0;
    z-index: 1;
}

.help-header h4 {
    margin: 0;
    color: #374151;
    font-size: 0.9rem;
    font-weight: 600;
}

.close-help {
    background: none;
    border: none;
    font-size: 1.1rem;
    color: #6b7280;
    cursor: pointer;
    padding: 2px;
    border-radius: 3px;
    transition: all 0.2s ease;
}

.close-help:hover {
    background: #e5e7eb;
    color: #374151;
}

.help-content {
    padding: 12px;
}

.help-section {
    margin-bottom: 16px;
}

.help-section h5 {
    margin: 0 0 8px 0;
    color: #374151;
    font-size: 0.8rem;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.help-items {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.help-item {
    display: flex;
    align-items: center;
    padding: 4px 8px;
    background: #f8f9fa;
    border-radius: 4px;
    font-size: 0.8rem;
    line-height: 1.4;
}

.help-item code {
    background: #e1e5e9;
    color: #495057;
    padding: 2px 4px;
    border-radius: 3px;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    font-size: 0.75rem;
    margin-right: 8px;
    white-space: nowrap;
}

.editor-tools {
    display: flex;
    gap: 8px;
    padding: 12px 16px;
    background: #f8f9fa;
    border-top: 1px solid #e2e8f0;
    flex-wrap: wrap;
}

.tool-btn {
    padding: 6px 12px;
    background: white;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-size: 0.8rem;
    color: #374151;
    cursor: pointer;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    gap: 4px;
}

.tool-btn:hover {
    background: #f3f4f6;
    border-color: #9ca3af;
}

.tool-btn.active {
    background: #667eea;
    color: white;
    border-color: #667eea;
}

.summary-section {
    padding: 16px;
    border-top: 1px solid #e2e8f0;
    background: #f8f9fa;
}

.summary-label {
    display: block;
    margin-bottom: 8px;
    color: #374151;
    font-weight: 500;
    font-size: 0.9rem;
}

.summary-input {
    width: 100%;
    padding: 8px 12px;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-size: 0.9rem;
    outline: none;
    transition: border-color 0.2s ease;
}

.summary-input:focus {
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.summary-help {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 4px;
    font-size: 0.8rem;
    color: #6b7280;
}

.char-count {
    font-weight: 500;
}

.save-options {
    padding: 16px;
    border-top: 1px solid #e2e8f0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
}

.save-buttons {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
}

.save-btn {
    padding: 10px 20px;
    background: linear-gradient(135deg, #10b981, #059669);
    color: white;
    border: none;
    border-radius: 6px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 8px;
}

.save-btn:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.save-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.preview-btn,
.diff-btn {
    padding: 10px 16px;
    background: white;
    color: #374151;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

.preview-btn:hover,
.diff-btn:hover {
    background: #f9fafb;
    border-color: #9ca3af;
}

.cancel-btn {
    padding: 10px 16px;
    background: white;
    color: #ef4444;
    border: 1px solid #ef4444;
    border-radius: 6px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

.cancel-btn:hover {
    background: #ef4444;
    color: white;
}

.save-info {
    display: flex;
    gap: 24px;
    align-items: center;
    flex-wrap: wrap;
}

.checkbox-label {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    font-size: 0.9rem;
    color: #374151;
}

.checkbox-label input[type="checkbox"] {
    width: 16px;
    height: 16px;
    cursor: pointer;
}

.loading-spinner {
    width: 16px;
    height: 16px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-top: 2px solid white;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

.preview-section,
.diff-section {
    margin-top: 24px;
    background: white;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    overflow: hidden;
}

.preview-header,
.diff-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: #f8f9fa;
    border-bottom: 1px solid #e2e8f0;
}

.preview-header h3,
.diff-header h3 {
    margin: 0;
    color: #374151;
    font-size: 1.1rem;
}

.close-preview,
.close-diff {
    background: none;
    border: none;
    font-size: 1.2rem;
    color: #6b7280;
    cursor: pointer;
    padding: 4px;
    border-radius: 4px;
    transition: all 0.2s ease;
}

.close-preview:hover,
.close-diff:hover {
    background: #e5e7eb;
    color: #374151;
}

.preview-content {
    padding: 16px;
    line-height: 1.6;
}

.diff-content {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1px;
    background: #e2e8f0;
}

.diff-old,
.diff-new {
    background: white;
    padding: 16px;
}

.diff-old h4,
.diff-new h4 {
    margin: 0 0 12px 0;
    color: #374151;
    font-size: 0.9rem;
    font-weight: 600;
}

.diff-old pre,
.diff-new pre {
    margin: 0;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    font-size: 0.8rem;
    line-height: 1.4;
    white-space: pre-wrap;
    word-wrap: break-word;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .container {
        padding: 0 12px;
    }

    .edit-header {
        flex-direction: column;
        gap: 16px;
        padding: 16px;
    }

    .edit-toolbar {
        padding: 8px 12px;
    }

    .toolbar-group {
        gap: 2px;
    }

    .toolbar-btn {
        min-width: 28px;
        height: 28px;
        font-size: 0.8rem;
    }

    .editor-container {
        flex-direction: column;
    }

    .wiki-help-panel {
        width: 100%;
        max-height: 200px;
        border-left: none;
        border-top: 1px solid #e2e8f0;
    }

    .wiki-editor {
        min-height: 300px;
        padding: 12px;
        font-size: 0.8rem;
    }

    .editor-tools {
        padding: 8px 12px;
    }

    .tool-btn {
        font-size: 0.75rem;
        padding: 4px 8px;
    }

    .save-options {
        flex-direction: column;
        align-items: stretch;
    }

    .save-buttons {
        order: 2;
    }

    .save-info {
        order: 1;
        justify-content: center;
    }

    .diff-content {
        grid-template-columns: 1fr;
    }
}
</style>
