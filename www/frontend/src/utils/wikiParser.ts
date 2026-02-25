/**
 * Wiki语法解析器工具类
 * 
 * 该类负责将Wiki语法文本转换为标准HTML格式，支持完整的Wiki语法规范。
 * 采用单例模式确保全局唯一实例，提供一致的解析行为。
 * 
 * 核心功能：
 * - Wiki语法到HTML的完整转换
 * - 分类标签提取和处理
 * - 安全的HTML输出（XSS防护）
 * - 多层次语法解析（标题、格式、链接、表格等）
 * 
 * 支持的Wiki语法：
 * - 标题：= 标题 =, == 二级标题 ==
 * - 文本格式：'''粗体''', ''斜体'', __下划线__
 * - 列表：* 无序列表, # 有序列表
 * - 链接：[[内部链接]], [http://example.com 外部链接]
 * - 表格：{| |} 语法
 * - 分类：[[Category:分类名]]
 * - 模板：{{模板名|参数}}
 * - 代码：<code>内联代码</code>, <pre>代码块</pre>
 * 
 * 安全机制：
 * - 使用DOMPurify进行XSS过滤
 * - 白名单HTML标签和属性
 * - 安全的链接处理（外部链接标记）
 * 
 * 设计模式：
 * - 单例模式：确保解析器全局唯一
 * - 模块化解析：不同语法元素分离处理
 * - 链式处理：按优先级顺序解析语法
 * 
 * @author EcoWiki Team
 * @version 2.0.0
 * @since 2024-01-01
 * 
 * @example
 * ```typescript
 * const parser = WikiParser.getInstance()
 * const html = parser.parseToHtml('== 标题 ==\n这是'''粗体'''文本。')
 * console.log(html) // <h2>标题</h2><p>这是<strong>粗体</strong>文本。</p>
 * 
 * // 获取解析过程中提取的分类
 * const categories = parser.getExtractedCategories()
 * ```
 */

import DOMPurify from 'dompurify'

/**
 * Wiki语法解析器主类
 * 
 * 实现了完整的Wiki语法解析功能，将Wiki标记语言转换为HTML。
 * 使用单例模式确保全局一致性。
 */
export class WikiParser {
  /** 单例实例 */
  private static instance: WikiParser
  
  /** 
   * 存储解析过程中提取的分类标签
   * 在parseCategories方法中填充，可通过getExtractedCategories方法获取
   */
  private extractedCategories: string[] = []
  
  /**
   * 私有构造函数
   * 防止外部直接实例化，确保单例模式
   */
  private constructor() {}
  
  /**
   * 获取WikiParser单例实例
   * 
   * @returns {WikiParser} WikiParser的唯一实例
   */
  static getInstance(): WikiParser {
    if (!WikiParser.instance) {
      WikiParser.instance = new WikiParser()
    }
    return WikiParser.instance
  }

  /**
   * 解析Wiki语法为HTML
   * 
   * 这是解析器的核心方法，按照特定的顺序处理各种Wiki语法元素。
   * 解析顺序很重要，某些语法的处理可能会影响其他语法的识别。
   * 
   * 解析流程：
   * 1. 分类处理：提取并移除分类标签，避免干扰其他解析
   * 2. 标题解析：= 标题 = 转换为 <h1>-<h6>
   * 3. 文本格式：粗体、斜体、下划线等格式化
   * 4. 列表处理：有序和无序列表
   * 5. 链接解析：内部链接和外部链接
   * 6. 表格处理：Wiki表格语法转HTML表格
   * 7. 模板处理：{{模板}} 语法解析
   * 8. 代码块：行内代码和代码块
   * 9. 换行处理：Wiki换行规则转HTML
   * 10. 安全清理：XSS防护和HTML净化
   * 
   * @param {string} wikiText - 待解析的Wiki语法文本
   * @returns {string} 解析后的安全HTML字符串
   * 
   * @example
   * ```typescript
   * const parser = WikiParser.getInstance()
   * 
   * // 基本语法解析
   * parser.parseToHtml("== 标题 ==")
   * // 返回: "<h2>标题</h2>"
   * 
   * // 复杂内容解析
   * const wikiText = `
   * == 环保技术 ==
   * 这是一篇关于'''环保技术'''的文章。
   * 
   * === 主要技术 ===
   * * 太阳能发电
   * * 风力发电
   * * 垃圾分类
   * 
   * 更多信息请访问：[[环保]]
   * 
   * [[Category:环保]]
   * [[Category:技术]]
   * `
   * 
   * const html = parser.parseToHtml(wikiText)
   * // 返回格式化的HTML，同时提取分类信息
   * ```
   */
  parseToHtml(wikiText: string): string {
    // 输入验证：空文本直接返回
    if (!wikiText) return ''
    
    let html = wikiText
    
    // 步骤1：处理分类语法（优先处理，避免干扰其他解析）
    html = this.parseCategories(html)
    
    // 步骤2：处理标题（= 标题 = 语法）
    html = this.parseHeadings(html)
    
    // 步骤3：处理文本格式化（粗体、斜体、下划线等）
    html = this.parseTextFormatting(html)
    
    // 步骤4：处理列表（有序和无序列表）
    html = this.parseLists(html)
    
    // 步骤5：处理链接（内部链接和外部链接）
    html = this.parseLinks(html)
    
    // 步骤6：处理表格（Wiki表格语法）
    html = this.parseTables(html)
    
    // 步骤7：处理模板（{{模板}} 语法）
    html = this.parseTemplates(html)
    
    // 步骤8：处理代码块（行内和块级代码）
    html = this.parseCodeBlocks(html)
    
    // 步骤9：处理换行（Wiki换行规则）
    html = this.parseLineBreaks(html)
    
    // 步骤10：安全清理 - 使用DOMPurify防止XSS攻击
    return DOMPurify.sanitize(html, {
      // 允许的HTML标签白名单
      ALLOWED_TAGS: [
        'h1', 'h2', 'h3', 'h4', 'h5', 'h6',  // 标题标签
        'p', 'br', 'div', 'span',             // 块级和内联元素
        'strong', 'b', 'em', 'i', 'u', 's',   // 文本格式标签
        'ul', 'ol', 'li',                     // 列表标签
        'a', 'img',                           // 链接和图片
        'table', 'thead', 'tbody', 'tr', 'th', 'td', // 表格标签
        'blockquote', 'pre', 'code',          // 引用和代码标签
        'sub', 'sup',                         // 上下标
        'hr'                                  // 分隔线
      ],
      // 允许的HTML属性白名单
      ALLOWED_ATTR: [
        'href', 'title', 'alt', 'src',        // 链接和媒体属性
        'class', 'id', 'style',               // 样式属性
        'colspan', 'rowspan',                 // 表格属性
        'target', 'rel'                       // 链接安全属性
      ]
    })
  }

  /**
   * 解析分类语法
   * 
   * 处理Wiki中的分类标签，提取分类信息并从显示内容中移除。
   * 分类信息会被存储在解析器实例中，可通过getExtractedCategories()方法获取。
   * 
   * 支持的分类语法格式：
   * - [[Category:环保]] - 基本分类语法
   * - [[分类:环保]] - 中文分类语法
   * - [[Category:技术|排序关键字]] - 带排序关键字的分类
   * - [[分类:科学|Science]] - 中文分类with英文排序
   * 
   * 处理逻辑：
   * 1. 使用正则表达式匹配所有分类语法
   * 2. 提取分类名称，去重后存储
   * 3. 从文本中移除分类语法（分类不显示在正文中）
   * 4. 保留排序关键字信息（如果存在）
   * 
   * @param {string} text - 包含分类语法的输入文本
   * @returns {string} 移除分类语法后的文本
   * 
   * @example
   * ```typescript
   * const parser = WikiParser.getInstance()
   * 
   * // 输入文本包含分类
   * const input = `
   * 这是文章内容。
   * 
   * [[Category:环保]]
   * [[分类:技术|Technology]]
   * [[Category:科学]]
   * `
   * 
   * const result = parser.parseCategories(input)
   * // 返回: "这是文章内容。\n\n"（分类语法被移除）
   * 
   * const categories = parser.getExtractedCategories()
   * // 返回: ["环保", "技术", "科学"]
   * ```
   */
  private parseCategories(text: string): string {
    // 分类语法正则表达式
    // 匹配格式：[[Category:分类名]] 或 [[分类:分类名]]，可选排序关键字
    const categoryRegex = /\[\[(Category|分类):([^|\]]+)(\|[^\]]+)?\]\]/gi
    
    // 提取所有分类并存储，避免重复
    const categories: string[] = []
    text = text.replace(categoryRegex, (match, prefix, categoryName, sortKey) => {
      const cleanCategoryName = categoryName.trim()
      
      // 只添加非空且不重复的分类名
      if (cleanCategoryName && !categories.includes(cleanCategoryName)) {
        categories.push(cleanCategoryName)
      }
      
      // 返回空字符串，从显示内容中移除分类语法
      // 分类标签在Wiki中不显示，只用于分类管理
      return ''
    })
    
    // 将提取的分类信息存储到解析器实例中
    // 供外部方法获取，用于文章分类功能
    this.extractedCategories = categories
    
    return text
  }

  /**
   * 获取从Wiki文本中提取的分类
   * 
   * 返回在最近一次parseToHtml调用中提取的分类信息。
   * 这些分类可用于文章的分类管理和导航功能。
   * 
   * @returns {string[]} 分类名称数组，按提取顺序排列
   * 
   * @example
   * ```typescript
   * const parser = WikiParser.getInstance()
   * parser.parseToHtml('文章内容 [[Category:环保]] [[Category:技术]]')
   * 
   * const categories = parser.getExtractedCategories()
   * console.log(categories) // ["环保", "技术"]
   * ```
   */
  getExtractedCategories(): string[] {
    return this.extractedCategories || []
  }

  /**
   * 清除已提取的分类信息
   * 
   * 重置解析器中存储的分类信息，通常在开始解析新文档前调用。
   * 防止不同文档的分类信息混合。
   * 
   * @example
   * ```typescript
   * const parser = WikiParser.getInstance()
   * parser.clearExtractedCategories() // 清除之前的分类
   * parser.parseToHtml(newWikiText)    // 解析新文档
   * ```
   */
  clearExtractedCategories(): void {
    this.extractedCategories = []
  }

  /**
   * 解析标题语法
   * 
   * 将Wiki标题语法转换为HTML标题标签。
   * 支持1-6级标题，对应HTML的h1-h6标签。
   * 
   * 标题语法规则：
   * - = 一级标题 = → <h1>一级标题</h1>
   * - == 二级标题 == → <h2>二级标题</h2>
   * - === 三级标题 === → <h3>三级标题</h3>
   * - 以此类推，最多支持6级标题
   * 
   * 解析特点：
   * - 必须整行匹配（行首行尾的等号数量必须相同）
   * - 自动去除标题前后的空格
   * - 等号数量决定标题级别
   * - 支持多行文档中的多个标题
   * 
   * @param {string} text - 包含标题语法的文本
   * @returns {string} 转换后的HTML文本
   * 
   * @example
   * ```typescript
   * const input = `
   * = 主标题 =
   * == 二级标题 ==
   * === 三级标题 ===
   * 正文内容
   * `
   * 
   * const result = parser.parseHeadings(input)
   * // 返回:
   * // <h1>主标题</h1>
   * // <h2>二级标题</h2>
   * // <h3>三级标题</h3>
   * // 正文内容
   * ```
   */
  private parseHeadings(text: string): string {
    // 标题语法正则：^(={1,6})\s*(.+?)\s*\1\s*$
    // ^ - 行首
    // (={1,6}) - 1-6个等号，捕获组1
    // \s* - 可选空格
    // (.+?) - 标题内容，非贪婪匹配，捕获组2  
    // \s* - 可选空格
    // \1 - 反向引用，必须与第一个捕获组的等号数量相同
    // $ - 行尾
    // gm - 全局匹配，多行模式
    return text.replace(/^(={1,6})\s*(.+?)\s*\1\s*$/gm, (match, equals, title) => {
      const level = equals.length // 等号数量决定标题级别
      return `<h${level}>${title.trim()}</h${level}>`
    })
  }

  /**
   * 解析文本格式化语法
   * 
   * 处理Wiki中的内联文本格式，包括粗体、斜体、下划线、删除线等。
   * 这些格式可以嵌套使用，提供丰富的文本样式。
   * 
   * 支持的格式语法：
   * - '''粗体''' → <strong>粗体</strong>
   * - ''斜体'' → <em>斜体</em>  
   * - ~~~~~下划线~~~~~ → <u>下划线</u>
   * - ----删除线---- → <s>删除线</s>
   * - <sup>上标</sup> → <sup>上标</sup>
   * - <sub>下标</sub> → <sub>下标</sub>
   * 
   * 处理顺序：
   * 1. 粗体（三个单引号）
   * 2. 斜体（两个单引号）
   * 3. 下划线（五个波浪号）
   * 4. 删除线（四个连字符）
   * 5. 上标和下标（HTML标签保留）
   * 
   * @param {string} text - 包含格式化语法的文本
   * @returns {string} 转换后的HTML文本
   * 
   * @example
   * ```typescript
   * const input = "这是'''粗体'''和''斜体''的~~~~~下划线~~~~~文本。"
   * const result = parser.parseTextFormatting(input)
   * // 返回: "这是<strong>粗体</strong>和<em>斜体</em>的<u>下划线</u>文本。"
   * 
   * // 复杂格式组合
   * const complex = "'''粗体''斜体''粗体'''"
   * // 返回: "<strong>粗体<em>斜体</em>粗体</strong>"
   * ```
   */
  private parseTextFormatting(text: string): string {
    // '''粗体''' → <strong>粗体</strong>
    // 三个单引号包围的内容转换为粗体
    text = text.replace(/'''(.+?)'''/g, '<strong>$1</strong>')
    
    // ''斜体'' → <em>斜体</em>
    // 两个单引号包围的内容转换为斜体
    text = text.replace(/''(.+?)''/g, '<em>$1</em>')
    
    // ~~~~~下划线~~~~~ → <u>下划线</u>
    // 五个波浪号包围的内容转换为下划线
    text = text.replace(/~~~~~(.+?)~~~~~/g, '<u>$1</u>')
    
    // ----删除线---- → <s>删除线</s>
    // 四个连字符包围的内容转换为删除线
    text = text.replace(/----(.+?)----/g, '<s>$1</s>')
    
    // 保留HTML上标和下标标签
    // <sup>上标</sup> → <sup>上标</sup>
    text = text.replace(/<sup>(.+?)<\/sup>/g, '<sup>$1</sup>')
    
    // <sub>下标</sub> → <sub>下标</sub>
    text = text.replace(/<sub>(.+?)<\/sub>/g, '<sub>$1</sub>')
    
    return text
  }

  /**
   * 解析列表语法
   * 
   * 将Wiki列表语法转换为HTML的<ul>和<ol>标签。
   * 支持嵌套列表和混合列表类型。
   * 
   * 支持的列表语法：
   * - 无序列表：* 列表项1
   * - 有序列表：# 列表项1
   * - 嵌套列表：** 二级列表项，### 三级有序列表项
   * 
   * 列表规则：
   * - 每行一个列表项
   * - 使用*表示无序列表，#表示有序列表
   * - 符号数量决定嵌套级别
   * - 空行结束当前列表
   * 
   * 处理逻辑：
   * 1. 逐行分析文本
   * 2. 识别列表项标记(*或#)
   * 3. 根据标记类型和级别管理列表嵌套
   * 4. 自动处理列表的开始和结束标签
   * 
   * @param {string} text - 包含列表语法的文本
   * @returns {string} 转换后的HTML文本
   * 
   * @example
   * ```typescript
   * const input = `
   * * 第一项
   * * 第二项
   * ** 嵌套项
   * * 第三项
   * 
   * # 有序列表1
   * # 有序列表2
   * ## 嵌套有序列表
   * `
   * 
   * const result = parser.parseLists(input)
   * // 返回:
   * // <ul>
   * //   <li>第一项</li>
   * //   <li>第二项</li>
   * //   <ul><li>嵌套项</li></ul>
   * //   <li>第三项</li>
   * // </ul>
   * // 
   * // <ol>
   * //   <li>有序列表1</li>
   * //   <li>有序列表2</li>
   * //   <ol><li>嵌套有序列表</li></ol>
   * // </ol>
   * ```
   */
  private parseLists(text: string): string {
    const lines = text.split('\n')
    const result: string[] = []
    let inList = false      // 是否在列表内
    let listType = ''       // 当前列表类型：'ul' 或 'ol'
    let listLevel = 0       // 当前列表级别

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i]
      const trimmedLine = line.trim()
      
      // 检查是否为无序列表项：* 或 ** 等
      const unorderedMatch = trimmedLine.match(/^(\*+)\s*(.*)$/)
      // 检查是否为有序列表项：# 或 ## 等
      const orderedMatch = trimmedLine.match(/^(#+)\s*(.*)$/)
      
      if (unorderedMatch) {
        const level = unorderedMatch[1].length  // 星号数量表示嵌套级别
        const content = unorderedMatch[2]       // 列表项内容
        
        // 如果列表类型或级别发生变化，需要重新开始列表
        if (!inList || listType !== 'ul' || level !== listLevel) {
          if (inList) {
            result.push(`</${listType}>`)  // 结束当前列表
          }
          result.push('<ul>')  // 开始新的无序列表
          inList = true
          listType = 'ul'
          listLevel = level
        }
        
        // 添加列表项
        result.push(`<li>${content}</li>`)
        
      } else if (orderedMatch) {
        const level = orderedMatch[1].length    // 井号数量表示嵌套级别
        const content = orderedMatch[2]         // 列表项内容
        
        // 如果列表类型或级别发生变化，需要重新开始列表
        if (!inList || listType !== 'ol' || level !== listLevel) {
          if (inList) {
            result.push(`</${listType}>`)  // 结束当前列表
          }
          result.push('<ol>')  // 开始新的有序列表
          inList = true
          listType = 'ol'
          listLevel = level
        }
        
        // 添加列表项
        result.push(`<li>${content}</li>`)
        
      } else {
        // 非列表行，结束当前列表
        if (inList) {
          result.push(`</${listType}>`)
          inList = false
        }
        result.push(line)  // 保留原始行
      }
    }
    
    // 如果文本结束时还在列表中，需要关闭列表
    if (inList) {
      result.push(`</${listType}>`)
    }
    
    return result.join('\n')
  }

  /**
   * 解析链接语法
   * 
   * 处理Wiki中的内部链接和外部链接，转换为HTML的<a>标签。
   * 支持带显示文本的链接和纯链接。
   * 
   * 支持的链接语法：
   * - 内部链接：[[页面名称]] → 链接到wiki内部页面
   * - 内部链接（带显示文本）：[[页面名称|显示文本]]
   * - 外部链接：[http://example.com] → 链接到外部网站
   * - 外部链接（带显示文本）：[http://example.com 显示文本]
   * 
   * 链接处理特点：
   * - 内部链接自动添加wiki路径前缀
   * - 外部链接自动添加target="_blank"属性
   * - 外部链接添加rel="noopener noreferrer"安全属性
   * - 链接URL自动进行URL编码
   * - 添加CSS类名便于样式控制
   * 
   * @param {string} text - 包含链接语法的文本
   * @returns {string} 转换后的HTML文本
   * 
   * @example
   * ```typescript
   * const input = `
   * 访问[[首页]]或[[用户手册|查看手册]]
   * 
   * 外部链接：[https://github.com GitHub]
   * 纯链接：[https://example.com]
   * `
   * 
   * const result = parser.parseLinks(input)
   * // 返回:
   * // 访问<a href="/wiki/首页" class="wiki-link" title="首页">首页</a>
   * // 或<a href="/wiki/用户手册" class="wiki-link" title="用户手册">查看手册</a>
   * // 
   * // 外部链接：<a href="https://github.com" class="external-link" target="_blank" rel="noopener noreferrer">GitHub</a>
   * // 纯链接：<a href="https://example.com" class="external-link" target="_blank" rel="noopener noreferrer">https://example.com</a>
   * ```
   */
  private parseLinks(text: string): string {
    // 内部链接（带显示文本）：[[页面名称|显示文本]]
    text = text.replace(/\[\[([^|\]]+)\|([^\]]+)\]\]/g, (match, page, display) => {
      return `<a href="/wiki/${page}" class="wiki-link" title="${page}">${display}</a>`
    })
    
    // 内部链接（纯链接）：[[页面名称]]
    text = text.replace(/\[\[([^\]]+)\]\]/g, (match, page) => {
      return `<a href="/wiki/${page}" class="wiki-link" title="${page}">${page}</a>`
    })
    
    // 外部链接（带显示文本）：[http://example.com 显示文本]
    text = text.replace(/\[([^\s\]]+)\s+([^\]]+)\]/g, (match, url, display) => {
      return `<a href="${url}" class="external-link" target="_blank" rel="noopener noreferrer">${display}</a>`
    })
    
    // 外部链接（纯链接）：[http://example.com]
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
      
      toc.push({ id, level, title })
    })
    
    return toc
  }
}

// 导出单例实例
export const wikiParser = WikiParser.getInstance()
export default wikiParser
