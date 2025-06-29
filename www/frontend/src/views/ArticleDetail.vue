<template>
  <div class="article-detail-page">
    <div class="container">
      <!-- 返回导航 -->
      <div class="navigation">
        <button @click="goBack" class="back-btn">
          <span class="back-icon">←</span>
          返回首页
        </button>
        <div class="breadcrumb">
          <span>首页</span>
          <span class="separator">/</span>
          <span>{{ article?.category }}</span>
          <span class="separator">/</span>
          <span class="current">文章详情</span>
        </div>
      </div>

      <!-- 文章内容 -->
      <ArticleContent 
        v-if="article" 
        :article="article" 
        class="article-section"
      />
      
      <!-- 加载状态 -->
      <div v-else-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在加载文章内容...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-state">
        <div class="error-icon">❌</div>
        <h3>加载失败</h3>
        <p>{{ error }}</p>
        <button @click="loadArticle" class="retry-btn">重试</button>
      </div>

      <!-- 相关文章推荐 -->
      <RelatedArticles
        v-if="article"
        :current-article-id="article.id"
        :current-category="article.category"
        :current-tags="article.tags"
        :max-results="6"
        :show-refresh-button="true"
        @article-click="onRelatedArticleClick"
        class="related-section"
      />

      <!-- 评论区 -->
      <ArticleComments 
        v-if="article" 
        :article-id="article.id"
        @show-login="showLoginModal"
        class="comments-section"
      />
    </div>

    <!-- 右侧浮动按钮 -->
    <FloatingActionButtons
      v-if="article"
      :article-id="article.id"
      @view="handleView"
      @edit="handleEdit"
      @history="handleHistory"
      @favorite="handleFavorite"
      @more="handleMore"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ArticleContent from '../components/article/ArticleContent.vue'
import ArticleComments from '../components/article/ArticleComments.vue'
import RelatedArticles from '../components/article/RelatedArticles.vue'
import FloatingActionButtons from '../components/article/FloatingActionButtons.vue'

interface Article {
  id: string
  title: string
  content: string
  summary?: string
  author: string
  authorTitle: string
  category: string
  rating: number
  publishDate: string
  readTime: number
  views: number
  likes?: number
  tags?: string[]
}

const route = useRoute()
const router = useRouter()

const article = ref<Article | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

onMounted(() => {
  loadArticle()
})

// 监听路由变化，当文章ID改变时重新加载
watch(() => route.params.id, () => {
  if (route.params.id) {
    loadArticle()
  }
})

const loadArticle = async () => {
  try {
    loading.value = true
    error.value = null
    
    const articleId = route.params.id as string
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据，实际应该从API获取
    article.value = {
      id: articleId,
      title: '人工智能发展史与未来展望：从图灵测试到ChatGPT的技术革命',
      summary: '本文深入探讨了人工智能从早期概念到现代大型语言模型的发展历程，分析了关键技术突破和里程碑事件，并对AI未来发展趋势进行了前瞻性思考。',
      content: `
        <h2>引言</h2>
        <p>人工智能（Artificial Intelligence，AI）作为21世纪最具革命性的技术之一，正在深刻改变着我们的生活方式、工作模式和社会结构。从1950年代艾伦·图灵提出著名的"图灵测试"开始，到如今ChatGPT等大型语言模型的横空出世，人工智能经历了数十年的发展历程。</p>
        
        <h2>人工智能的历史发展</h2>
        
        <h3>早期探索阶段（1950-1980年代）</h3>
        <p>1950年，英国数学家艾伦·图灵发表了具有里程碑意义的论文《计算机器与智能》，首次提出了"机器能否思考"这一根本性问题，并设计了著名的图灵测试。这标志着人工智能学科的正式诞生。</p>
        
        <blockquote>
        "我认为在世纪末，词汇的使用和受过教育的观点将会改变得如此之多，以至于人们能够谈论机器思考而不会遭到反驳。" —— 艾伦·图灵
        </blockquote>
        
        <p>1956年，达特茅斯会议正式确立了"人工智能"这一术语，标志着AI作为一个独立学科的确立。早期的AI研究主要集中在符号主义方法上，试图通过逻辑推理和知识表示来模拟人类智能。</p>
        
        <h3>专家系统时代（1980-1990年代）</h3>
        <p>1980年代，专家系统成为AI应用的主流。这些系统通过将特定领域专家的知识编码成规则，能够在特定领域内进行推理和决策。著名的专家系统包括：</p>
        
        <ul>
        <li><strong>MYCIN</strong>：用于医疗诊断的专家系统</li>
        <li><strong>DENDRAL</strong>：用于化学分析的专家系统</li>
        <li><strong>XCON</strong>：数字设备公司的计算机配置系统</li>
        </ul>
        
        <h3>机器学习的崛起（1990年代至今）</h3>
        <p>随着计算能力的提升和大数据的出现，机器学习逐渐成为AI发展的主导力量。从传统的监督学习、无监督学习，到深度学习的突破，机器学习不断刷新着人们对AI能力的认知。</p>
        
        <h2>深度学习革命</h2>
        <p>2006年，杰弗里·辛顿（Geoffrey Hinton）提出了深度信念网络，开启了深度学习的新时代。深度学习通过多层神经网络模拟人脑神经元的工作方式，在图像识别、自然语言处理、语音识别等领域取得了突破性进展。</p>
        
        <h3>关键里程碑事件</h3>
        <ul>
        <li><strong>2012年</strong>：AlexNet在ImageNet竞赛中取得突破性成果</li>
        <li><strong>2016年</strong>：AlphaGo击败世界围棋冠军李世石</li>
        <li><strong>2017年</strong>：Transformer架构的提出，为后续的语言模型奠定基础</li>
        <li><strong>2018年</strong>：BERT模型的发布，革命性地提升了自然语言理解能力</li>
        <li><strong>2022年</strong>：ChatGPT的发布，标志着大型语言模型进入公众视野</li>
        </ul>
        
        <h2>现代AI的技术特征</h2>
        
        <h3>大型语言模型（LLMs）</h3>
        <p>以GPT系列、BERT、T5等为代表的大型语言模型，通过在海量文本数据上进行预训练，获得了强大的语言理解和生成能力。这些模型展现出了惊人的泛化能力，能够在零样本或少样本的情况下完成各种任务。</p>
        
        <h3>多模态AI</h3>
        <p>现代AI系统不再局限于单一模态，而是能够同时处理文本、图像、音频等多种类型的数据。DALL-E、Midjourney等文生图模型，以及GPT-4V等多模态模型，展示了AI在跨模态理解和生成方面的强大能力。</p>
        
        <h2>AI对社会的影响</h2>
        
        <h3>积极影响</h3>
        <ul>
        <li><strong>提高生产效率</strong>：自动化和智能化提升了各行业的工作效率</li>
        <li><strong>医疗健康</strong>：AI辅助诊断、药物发现加速了医疗技术进步</li>
        <li><strong>教育普及</strong>：个性化学习系统提供了更好的教育体验</li>
        <li><strong>科学研究</strong>：AI加速了科学发现和技术创新</li>
        </ul>
        
        <h3>挑战与风险</h3>
        <ul>
        <li><strong>就业冲击</strong>：自动化可能导致部分工作岗位的消失</li>
        <li><strong>隐私安全</strong>：数据收集和算法决策带来的隐私风险</li>
        <li><strong>算法偏见</strong>：训练数据的偏见可能导致不公平的结果</li>
        <li><strong>技术滥用</strong>：深度伪造、自主武器等技术的恶意使用</li>
        </ul>
        
        <h2>未来展望</h2>
        
        <h3>通用人工智能（AGI）</h3>
        <p>通用人工智能是AI研究的终极目标，指的是在所有认知任务上都能达到或超越人类水平的AI系统。虽然我们还没有实现真正的AGI，但随着技术的不断进步，这一目标正在逐步接近现实。</p>
        
        <h3>技术发展趋势</h3>
        <ul>
        <li><strong>更大规模的模型</strong>：模型参数量和训练数据将继续增长</li>
        <li><strong>多模态融合</strong>：视觉、语言、音频等模态的深度融合</li>
        <li><strong>推理能力提升</strong>：从模式识别向逻辑推理的转变</li>
        <li><strong>效率优化</strong>：更高效的训练和推理算法</li>
        <li><strong>可解释性</strong>：让AI的决策过程更加透明和可理解</li>
        </ul>
        
        <h3>应用前景</h3>
        <p>未来的AI将在更多领域发挥重要作用：</p>
        <ul>
        <li><strong>智能助手</strong>：更智能、更个性化的数字助手</li>
        <li><strong>自动驾驶</strong>：完全自动化的交通系统</li>
        <li><strong>机器人技术</strong>：能够胜任复杂任务的通用机器人</li>
        <li><strong>创意产业</strong>：AI辅助的内容创作和艺术设计</li>
        <li><strong>科学研究</strong>：AI驱动的科学发现和技术创新</li>
        </ul>
        
        <h2>结论</h2>
        <p>人工智能的发展历程充满了突破和创新，从早期的符号主义到现在的深度学习，从专用系统到通用模型，AI正在以前所未有的速度改变着我们的世界。虽然面临着诸多挑战和风险，但AI技术的巨大潜力和广阔前景让我们有理由相信，人工智能将继续推动人类社会的进步和发展。</p>
        
        <p>作为新时代的见证者和参与者，我们需要以开放的心态拥抱AI技术的发展，同时也要理性地应对其带来的挑战。只有在技术创新和伦理规范并重的前提下，人工智能才能真正成为造福人类的强大工具。</p>
      `,
      author: '科技研究员',
      authorTitle: 'AI技术专家 · 清华大学计算机系教授',
      category: '学术研究',
      rating: 4.8,
      publishDate: '2025-01-15T10:00:00Z',
      readTime: 15,
      views: 2890,
      likes: 156,
      tags: ['人工智能', '机器学习', '深度学习', '技术发展', 'ChatGPT', '未来科技']
    }
    
  } catch (err) {
    error.value = '加载文章失败，请稍后重试'
    console.error('Failed to load article:', err)
  } finally {
    loading.value = false
  }
}

const loadRelatedArticles = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 相关文章推荐现在由 RelatedArticles 组件处理
    
  } catch (err) {
    console.error('Failed to load related articles:', err)
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const goBack = () => {
  router.push('/')
}

const navigateToArticle = (articleId: string) => {
  router.push(`/article/${articleId}`)
}

const onRelatedArticleClick = (articleId: string) => {
  // 相关文章点击处理
  console.log('Related article clicked:', articleId)
  // 路由跳转已在 RelatedArticles 组件内部处理
}

const showLoginModal = () => {
  // 触发登录模态框显示
  // 这里应该调用全局的登录状态管理
  console.log('Show login modal')
}

// 浮动按钮处理函数
const handleView = () => {
  // 重新加载文章或滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
  console.log('查看文章')
}

const handleEdit = () => {
  // 跳转到编辑页面
  if (article.value) {
    console.log('编辑文章:', article.value.id)
    router.push(`/edit/${article.value.id}`)
  }
}

const handleHistory = () => {
  // 查看文章历史版本
  if (article.value) {
    console.log('查看历史:', article.value.id)
    // router.push(`/history/${article.value.id}`)
  }
}

const handleFavorite = () => {
  // 收藏或取消收藏文章
  if (article.value) {
    console.log('收藏文章:', article.value.id)
    // 这里应该调用收藏API
  }
}

const handleMore = () => {
  // 显示更多操作菜单
  console.log('更多操作')
  // 可以显示一个下拉菜单或模态框
}
</script>

<style scoped>
.article-detail-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px 0;
}

.container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 16px 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
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

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #718096;
  font-size: 0.9rem;
}

.separator {
  color: #cbd5e0;
}

.current {
  color: #667eea;
  font-weight: 500;
}

.article-section {
  margin-bottom: 32px;
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

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-state p {
  color: #718096;
  font-size: 1rem;
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  text-align: center;
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.error-state h3 {
  color: #1a202c;
  margin-bottom: 8px;
  font-size: 1.4rem;
}

.error-state p {
  color: #718096;
  margin-bottom: 24px;
  font-size: 1rem;
}

.retry-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 25px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.comments-section {
  margin-top: 0;
}

.related-section {
  margin: 32px 0 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .container {
    max-width: 100%;
    padding: 0 16px;
  }
}

@media (max-width: 768px) {
  .container {
    padding: 0 12px;
  }
  
  .navigation {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 16px;
  }
  
  .breadcrumb {
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .article-detail-page {
    padding: 10px 0;
  }
  
  .navigation {
    margin-bottom: 20px;
  }
  
  .back-btn {
    padding: 8px 12px;
    font-size: 0.85rem;
  }
}
</style>
