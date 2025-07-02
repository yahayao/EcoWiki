import DOMPurify from 'dompurify'

// Wiki语法解析器
export class WikiParser {
  private static instance: WikiParser
  // 存储提取的分类
  private extractedCategories: string[] = []
  
  private constructor() {}
  
  static getInstance(): WikiParser {
    if (!WikiParser.instance) {
      WikiParser.instance = new WikiParser()
    }
    return WikiParser.instance
  }

  /**
   * 解析Wiki语法为HTML
   * @param wikiText Wiki语法文本
   * @returns 解析后的HTML字符串
   */
  parseToHtml(wikiText: string): string {
    if (!wikiText) return ''
    
    let html = wikiText
    
    // 1. 处理分类语法（在其他处理之前，避免干扰）
    html = this.parseCategories(html)
    
    // 2. 处理标题
    html = this.parseHeadings(html)
    
    // 3. 处理文本格式化
    html = this.parseTextFormatting(html)
    
    // 4. 处理列表
    html = this.parseLists(html)
    
    // 5. 处理链接
    html = this.parseLinks(html)
    
    // 6. 处理表格
    html = this.parseTables(html)
    
    // 7. 处理模板
    html = this.parseTemplates(html)
    
    // 8. 处理代码块
    html = this.parseCodeBlocks(html)
    
    // 9. 处理换行
    html = this.parseLineBreaks(html)
    
    // 10. 清理并返回安全的HTML
    return DOMPurify.sanitize(html, {
      ALLOWED_TAGS: [
        'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
        'p', 'br', 'div', 'span',
        'strong', 'b', 'em', 'i', 'u', 's',
        'ul', 'ol', 'li',
        'a', 'img',
        'table', 'thead', 'tbody', 'tr', 'th', 'td',
        'blockquote', 'pre', 'code',
        'sub', 'sup',
        'hr'
      ],
      ALLOWED_ATTR: [
        'href', 'title', 'alt', 'src',
        'class', 'id', 'style',
        'colspan', 'rowspan',
        'target', 'rel'
      ]
    })
  }

  /**
   * 解析分类语法
   * 支持的格式：
   * - [[Category:环保]] 或 [[分类:环保]]
   * - [[Category:技术|排序关键字]] (带排序关键字的分类)
   * 
   * @param text 输入文本
   * @returns 处理后的文本（移除分类语法）
   */
  private parseCategories(text: string): string {
    // 匹配分类语法：[[Category:xxx]] 或 [[分类:xxx]]
    const categoryRegex = /\[\[(Category|分类):([^|\]]+)(\|[^\]]+)?\]\]/gi
    
    // 提取所有分类并存储
    const categories: string[] = []
    text = text.replace(categoryRegex, (match, prefix, categoryName, sortKey) => {
      const cleanCategoryName = categoryName.trim()
      if (cleanCategoryName && !categories.includes(cleanCategoryName)) {
        categories.push(cleanCategoryName)
      }
      // 返回空字符串，从显示内容中移除分类语法
      return ''
    })
    
    // 将分类信息存储到解析器实例中，供外部获取
    this.extractedCategories = categories
    
    return text
  }

  /**
   * 获取从Wiki文本中提取的分类
   * @returns 分类数组
   */
  getExtractedCategories(): string[] {
    return this.extractedCategories || []
  }

  /**
   * 清除提取的分类
   */
  clearExtractedCategories(): void {
    this.extractedCategories = []
  }

  /**
   * 解析标题
   */
  private parseHeadings(text: string): string {
    // = 标题 = -> h1
    // == 标题 == -> h2
    // === 标题 === -> h3
    // 以此类推
    return text.replace(/^(={1,6})\s*(.+?)\s*\1\s*$/gm, (match, equals, title) => {
      const level = equals.length
      return `<h${level}>${title.trim()}</h${level}>`
    })
  }

  /**
   * 解析文本格式化
   */
  private parseTextFormatting(text: string): string {
    // '''粗体''' -> <strong>粗体</strong>
    text = text.replace(/'''(.+?)'''/g, '<strong>$1</strong>')
    
    // ''斜体'' -> <em>斜体</em>
    text = text.replace(/''(.+?)''/g, '<em>$1</em>')
    
    // ~~~~~ 五个波浪号 -> 下划线
    text = text.replace(/~~~~~(.+?)~~~~~/g, '<u>$1</u>')
    
    // ---- 四个连字符 -> 删除线
    text = text.replace(/----(.+?)----/g, '<s>$1</s>')
    
    // <sup>上标</sup>
    text = text.replace(/<sup>(.+?)<\/sup>/g, '<sup>$1</sup>')
    
    // <sub>下标</sub>
    text = text.replace(/<sub>(.+?)<\/sub>/g, '<sub>$1</sub>')
    
    return text
  }

  /**
   * 解析列表
   */
  private parseLists(text: string): string {
    const lines = text.split('\n')
    const result: string[] = []
    let inList = false
    let listType = ''
    let listLevel = 0

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i]
      const trimmedLine = line.trim()
      
      // 检查是否为列表项
      const unorderedMatch = trimmedLine.match(/^(\*+)\s*(.*)$/)
      const orderedMatch = trimmedLine.match(/^(#+)\s*(.*)$/)
      
      if (unorderedMatch) {
        const level = unorderedMatch[1].length
        const content = unorderedMatch[2]
        
        if (!inList || listType !== 'ul' || level !== listLevel) {
          if (inList) {
            result.push(`</${listType}>`)
          }
          result.push('<ul>')
          inList = true
          listType = 'ul'
          listLevel = level
        }
        
        result.push(`<li>${content}</li>`)
      } else if (orderedMatch) {
        const level = orderedMatch[1].length
        const content = orderedMatch[2]
        
        if (!inList || listType !== 'ol' || level !== listLevel) {
          if (inList) {
            result.push(`</${listType}>`)
          }
          result.push('<ol>')
          inList = true
          listType = 'ol'
          listLevel = level
        }
        
        result.push(`<li>${content}</li>`)
      } else {
        if (inList) {
          result.push(`</${listType}>`)
          inList = false
        }
        result.push(line)
      }
    }
    
    if (inList) {
      result.push(`</${listType}>`)
    }
    
    return result.join('\n')
  }

  /**
   * 解析链接
   */
  private parseLinks(text: string): string {
    // 内部链接 [[页面名称|显示文本]] 或 [[页面名称]]
    text = text.replace(/\[\[([^|\]]+)\|([^\]]+)\]\]/g, (match, page, display) => {
      return `<a href="/wiki/${encodeURIComponent(page)}" class="wiki-link" title="${page}">${display}</a>`
    })
    
    text = text.replace(/\[\[([^\]]+)\]\]/g, (match, page) => {
      return `<a href="/wiki/${encodeURIComponent(page)}" class="wiki-link" title="${page}">${page}</a>`
    })
    
    // 外部链接 [http://example.com 显示文本] 或 [http://example.com]
    text = text.replace(/\[([^\s\]]+)\s+([^\]]+)\]/g, (match, url, display) => {
      return `<a href="${url}" class="external-link" target="_blank" rel="noopener noreferrer">${display}</a>`
    })
    
    text = text.replace(/\[([^\s\]]+)\]/g, (match, url) => {
      return `<a href="${url}" class="external-link" target="_blank" rel="noopener noreferrer">${url}</a>`
    })
    
    return text
  }

  /**
   * 解析表格
   */
  private parseTables(text: string): string {
    // 简单的表格解析
    const lines = text.split('\n')
    const result: string[] = []
    let inTable = false
    let tableRows: string[] = []

    for (const line of lines) {
      const trimmedLine = line.trim()
      
      if (trimmedLine.startsWith('{|')) {
        // 表格开始
        inTable = true
        tableRows = []
        continue
      } else if (trimmedLine === '|}') {
        // 表格结束
        if (inTable && tableRows.length > 0) {
          result.push('<table class="wiki-table">')
          result.push('<tbody>')
          for (const row of tableRows) {
            result.push(row)
          }
          result.push('</tbody>')
          result.push('</table>')
        }
        inTable = false
        tableRows = []
        continue
      } else if (inTable && trimmedLine.startsWith('|-')) {
        // 表格行分隔符，忽略
        continue
      } else if (inTable && trimmedLine.startsWith('!')) {
        // 表格头
        const cells = trimmedLine.substring(1).split('!!')
        const rowHtml = '<tr>' + cells.map(cell => `<th>${cell.trim()}</th>`).join('') + '</tr>'
        tableRows.push(rowHtml)
      } else if (inTable && trimmedLine.startsWith('|')) {
        // 表格数据行
        const cells = trimmedLine.substring(1).split('||')
        const rowHtml = '<tr>' + cells.map(cell => `<td>${cell.trim()}</td>`).join('') + '</tr>'
        tableRows.push(rowHtml)
      } else if (!inTable) {
        result.push(line)
      }
    }
    
    return result.join('\n')
  }

  /**
   * 解析模板
   */
  private parseTemplates(text: string): string {
    // 简单的模板解析 {{模板名称|参数1|参数2}}
    return text.replace(/\{\{([^}|]+)(\|[^}]*)?\}\}/g, (match, templateName, params) => {
      const cleanTemplateName = templateName.trim()
      const templateParams = params ? params.substring(1).split('|') : []
      
      // 这里可以根据模板名称返回不同的HTML
      switch (cleanTemplateName.toLowerCase()) {
        case 'info':
          return `<div class="wiki-template info-box">
            <div class="template-icon">ℹ️</div>
            <div class="template-content">${templateParams.join(' ')}</div>
          </div>`
        case 'warning':
          return `<div class="wiki-template warning-box">
            <div class="template-icon">⚠️</div>
            <div class="template-content">${templateParams.join(' ')}</div>
          </div>`
        case 'note':
          return `<div class="wiki-template note-box">
            <div class="template-icon">📝</div>
            <div class="template-content">${templateParams.join(' ')}</div>
          </div>`
        default:
          return `<div class="wiki-template">模板: ${cleanTemplateName}</div>`
      }
    })
  }

  /**
   * 解析代码块
   */
  private parseCodeBlocks(text: string): string {
    // <nowiki>内容</nowiki> -> 原始文本
    text = text.replace(/<nowiki>([\s\S]*?)<\/nowiki>/g, (match, content) => {
      return `<pre><code>${this.escapeHtml(content)}</code></pre>`
    })
    
    // <pre>内容</pre> -> 预格式化文本
    text = text.replace(/<pre>([\s\S]*?)<\/pre>/g, (match, content) => {
      return `<pre>${this.escapeHtml(content)}</pre>`
    })
    
    return text
  }

  /**
   * 解析换行
   */
  private parseLineBreaks(text: string): string {
    // 空行转换为段落
    text = text.replace(/\n\s*\n/g, '</p><p>')
    
    // 单个换行保持为换行
    text = text.replace(/\n/g, '<br>')
    
    // 包装在段落中
    if (text.trim()) {
      text = '<p>' + text + '</p>'
    }
    
    // 清理空段落
    text = text.replace(/<p>\s*<\/p>/g, '')
    
    return text
  }

  /**
   * 转义HTML字符
   */
  private escapeHtml(text: string): string {
    const div = document.createElement('div')
    div.textContent = text
    return div.innerHTML
  }

  /**
   * 从HTML中提取纯文本摘要
   */
  extractSummary(html: string, maxLength = 200): string {
    const div = document.createElement('div')
    div.innerHTML = html
    const text = div.textContent || div.innerText || ''
    
    if (text.length <= maxLength) {
      return text
    }
    
    return text.substring(0, maxLength - 3) + '...'
  }

  /**
   * 生成目录
   */
  generateToc(html: string): { id: string; level: number; title: string }[] {
    const div = document.createElement('div')
    div.innerHTML = html
    const headings = div.querySelectorAll('h1, h2, h3, h4, h5, h6')
    
    const toc: { id: string; level: number; title: string }[] = []
    
    headings.forEach((heading, index) => {
      const level = parseInt(heading.tagName.substring(1))
      const title = heading.textContent || ''
      const id = `heading-${index}`
      
      heading.id = id
      toc.push({ id, level, title })
    })
    
    return toc
  }
}

// 导出单例实例
export const wikiParser = WikiParser.getInstance()
export default wikiParser
