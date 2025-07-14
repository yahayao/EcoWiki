/**
 * 编辑器操作 Composable
 * 
 * 提供文章编辑器的常用操作功能，包括文本插入、格式化、快捷键处理等。
 * 可以在多个组件中复用这些编辑功能。
 */

import { ref, type Ref } from 'vue'

export interface EditorOperations {
  insertText: (before: string, after?: string, placeholder?: string) => void
  insertHeading: (level: number) => void
  insertTable: () => void
  handleKeydown: (event: KeyboardEvent) => void
}

/**
 * 使用编辑器操作功能
 * @param contentRef 文章内容的响应式引用
 * @param getTextarea 获取文本区域元素的函数
 * @param onSave 保存回调函数
 * @returns 编辑器操作方法
 */
export function useEditorOperations(
  contentRef: Ref<string>,
  getTextarea: () => HTMLTextAreaElement | null,
  onSave?: () => void
): EditorOperations {
  
  /**
   * 在编辑器中插入文本
   * @param before 前缀文本
   * @param after 后缀文本
   * @param placeholder 占位符文本（当没有选中文本时使用）
   */
  const insertText = (before: string, after: string = '', placeholder: string = '') => {
    const textarea = getTextarea()
    if (!textarea) return

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
    contentRef.value = newValue

    // 触发Vue的响应式更新
    textarea.value = newValue

    // 设置光标位置
    setTimeout(() => {
      textarea.focus()
      const newPosition = start + before.length + (selectedText || placeholder).length
      textarea.setSelectionRange(newPosition, newPosition)
    }, 0)
  }

  /**
   * 插入标题
   * @param level 标题级别 (2-6)
   */
  const insertHeading = (level: number) => {
    const prefix = '='.repeat(level) + ' '
    const suffix = ' ' + '='.repeat(level)
    insertText(prefix, suffix, '标题文字')
  }

  /**
   * 插入表格模板
   */
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

  /**
   * 处理键盘事件
   * @param event 键盘事件
   */
  const handleKeydown = (event: KeyboardEvent) => {
    // 处理快捷键
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
          if (onSave) {
            onSave()
          }
          break
      }
    }

    // Tab键插入缩进
    if (event.key === 'Tab') {
      event.preventDefault()
      insertText('    ')
    }
  }

  return {
    insertText,
    insertHeading,
    insertTable,
    handleKeydown
  }
}

/**
 * Wiki语法快捷插入功能
 */
export const WikiSyntax = {
  /**
   * 粗体文本
   */
  bold: () => ({ before: '\'\'\'', after: '\'\'\'', placeholder: '粗体文字' }),
  
  /**
   * 斜体文本
   */
  italic: () => ({ before: '\'\'', after: '\'\'', placeholder: '斜体文字' }),
  
  /**
   * 内部链接
   */
  internalLink: () => ({ before: '[[', after: ']]', placeholder: '链接文字' }),
  
  /**
   * 外部链接
   */
  externalLink: () => ({ before: '[', after: ' ]', placeholder: 'http://example.com 显示文字' }),
  
  /**
   * 引用
   */
  reference: () => ({ before: '<ref>', after: '</ref>', placeholder: '引用内容' }),
  
  /**
   * 无序列表
   */
  unorderedList: () => ({ before: '* ', after: '', placeholder: '列表项' }),
  
  /**
   * 有序列表
   */
  orderedList: () => ({ before: '# ', after: '', placeholder: '列表项' }),
  
  /**
   * 模板
   */
  template: () => ({ before: '{{', after: '}}', placeholder: '模板名称' }),
  
  /**
   * 分类
   */
  category: () => ({ before: '[[分类:', after: ']]', placeholder: '环保' }),
  
  /**
   * 原始文本
   */
  nowiki: () => ({ before: '<nowiki>', after: '</nowiki>', placeholder: '原始文字' })
}
