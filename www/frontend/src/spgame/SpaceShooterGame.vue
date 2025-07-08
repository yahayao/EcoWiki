<!--
  太空射击小游戏组件
  
  这是一个经典的太空射击游戏，玩家控制飞船击败敌人并获得分数。
  游戏具有现代化的视觉效果和流畅的操作体验。
  
  游戏功能：
  - 玩家飞船移动和射击
  - 敌人飞船自动生成和移动
  - 碰撞检测和爆炸效果
  - 分数统计和游戏状态管理
  - 游戏开始/暂停/结束功能
  
  控制方式：
  - WASD或方向键：移动飞船
  - 鼠标左键：发射子弹
  - ESC键：暂停/恢复游戏
  
  游戏特色：
  - 精美的飞机造型设计
  - 精确的中心点碰撞检测机制
  - 只有击中红色中心点才会减少生命
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-07-08
-->
<template>
  <div class="game-container" v-show="isVisible">
    <!-- 游戏遮罩层 -->
    <div class="game-overlay" @click="closeGame"></div>
    
    <!-- 游戏主窗口 -->
    <div class="game-window">
      <!-- 游戏头部 -->
      <div class="game-header">
        <h2>太空射击游戏</h2>
        <div class="game-controls">
          <div class="score">分数: {{ score }}</div>
          <div class="shield" v-if="hasShield" style="color: #00ffff;">🛡️ 护盾</div>
          <div class="game-time" :class="{ 'time-warning': gameTimeSeconds >= 75 && gameTimeSeconds < 90 }">
            时间: {{ Math.floor(gameTimeSeconds) }}s
            <span v-if="gameTimeSeconds >= 75 && gameTimeSeconds < 90" class="intensity-warning">
              ⚠️ {{ 90 - Math.floor(gameTimeSeconds) }}s后进入高强度模式!
            </span>
            <span v-else-if="gameTimeSeconds >= 90" class="intensity-active">
              🔥 高强度模式！
            </span>
          </div>
          <div class="difficulty">
            {{ selectedDifficulty === 'easy' ? '简单模式' : '困难模式' }}
          </div>
          <div v-if="selectedDifficulty === 'hard'" class="ammo" :class="{ 'low-ammo': currentAmmo <= 5 && !powerUpActive }">
            弹夹: 
            <span v-if="powerUpActive" class="infinite-ammo">∞/∞</span>
            <span v-else>{{ currentAmmo }}/{{ maxAmmo }}</span>
            <span v-if="isReloading && !powerUpActive" class="reloading">(换弹中)</span>
          </div>
          <div v-else class="ammo-easy">
            🌟 无限弹药
          </div>
          <button @click="togglePause" class="pause-btn">
            {{ isPaused ? '继续' : '暂停' }}
          </button>
          <button @click="closeGame" class="close-btn">×</button>
        </div>
      </div>
      
      <!-- 血量条 -->
      <div class="health-bar-container" :class="{ 'shake': isShaking }">
        <div class="health-bar">
          <div 
            v-for="(segment, index) in maxLives" 
            :key="index"
            class="health-segment"
            :class="{ 
              'active': index < lives, 
              'damaged': index >= lives,
              'critical': lives <= 1 && index < lives
            }"
          >
          </div>
        </div>
        <div class="health-text">生命值: {{ lives }}/{{ maxLives }}</div>
      </div>
      
      <!-- 游戏画布 -->
      <canvas 
        ref="gameCanvas" 
        :width="gameWidth" 
        :height="gameHeight"
        @keydown="handleKeyDown"
        @keyup="handleKeyUp"
        @mousedown="handleMouseDown"
        @mouseup="handleMouseUp"
        tabindex="0"
        class="game-canvas"
      ></canvas>
      
      <!-- 游戏状态覆盖层 -->
      <div v-if="!gameStarted || isPaused || gameOver || !difficultySelected" class="game-status-overlay">
        <div class="status-content">
          <div v-if="!difficultySelected" class="difficulty-screen">
            <h3>选择游戏难度</h3>
            
            <div class="difficulty-options">
              <div class="difficulty-card" @click="selectDifficulty('easy')">
                <h4 style="color: #00ff00;">🌟 简单模式</h4>
                <div class="difficulty-features">
                  <p>✅ 自动发射子弹</p>
                  <p>✅ 无限弹药</p>
                  <p>✅ 较慢的敌人速度</p>
                  <p>✅ 适合休闲玩家</p>
                </div>
                <button class="difficulty-btn easy-btn">选择简单</button>
              </div>
              
              <div class="difficulty-card" @click="selectDifficulty('hard')">
                <h4 style="color: #ff4444;">🔥 困难模式</h4>
                <div class="difficulty-features">
                  <p>⚡ 手动鼠标点击射击</p>
                  <p>⚡ 有限弹夹，需要换弹</p>
                  <p>⚡ 更快的敌人速度</p>
                  <p>⚡ 挑战硬核玩家</p>
                </div>
                <button class="difficulty-btn hard-btn">选择困难</button>
              </div>
            </div>
          </div>
          
          <div v-else-if="!gameStarted" class="start-screen">
            <h3>太空射击</h3>
            <p v-if="selectedDifficulty === 'easy'">简单模式：使用WASD移动飞船，自动射击</p>
            <p v-else>困难模式：使用WASD移动，鼠标左键射击</p>
            <p style="font-size: 0.9rem; color: #ffa500;">💡 只有击中红色中心点才会减少生命</p>
            <p style="font-size: 0.8rem; color: #00ffff;">🎁 拾取蓝色增益球获得自动散弹射击</p>
            <p style="font-size: 0.8rem; color: #ffff00;">🛡️ 拾取金色护盾抵挡一次伤害</p>
            <div class="start-actions">
              <button @click="startGame" class="start-btn">开始游戏</button>
              <button @click="backToDifficulty" class="back-btn">重选难度</button>
            </div>
          </div>
          
          <div v-else-if="isPaused && !gameOver" class="pause-screen">
            <h3>游戏暂停</h3>
            <button @click="togglePause" class="resume-btn">继续游戏</button>
          </div>
          
          <div v-else-if="gameOver" class="game-over-screen">
            <h3>游戏结束</h3>
            <p>最终分数: {{ score }}</p>
            <button @click="restartGame" class="restart-btn">重新开始</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue'

/**
 * 组件属性定义
 */
interface Props {
  visible: boolean
}

const props = defineProps<Props>()

/**
 * 组件事件定义
 */
const emit = defineEmits(['close'])

/**
 * 游戏状态管理
 */
const isVisible = ref(props.visible)
const gameCanvas = ref<HTMLCanvasElement>()
const gameStarted = ref(false)
const difficultySelected = ref(false) // 难度是否已选择
const selectedDifficulty = ref<'easy' | 'hard'>('easy') // 选择的难度
const isPaused = ref(false)
const gameOver = ref(false)
const playerExploding = ref(false) // 玩家飞机爆炸状态
const explosionStartTime = ref(0) // 爆炸开始时间
const score = ref(0)
const lives = ref(3)
const maxLives = 3 // 最大生命值
const isShaking = ref(false) // 受伤震感状态

/**
 * 弹夹系统
 */
const currentAmmo = ref(30)
const maxAmmo = 30
const isReloading = ref(false)
const reloadStartTime = ref(0)
const reloadDuration = 2000 // 换弹时间2秒

/**
 * 增益系统
 */
const powerUpActive = ref(false)
const powerUpEndTime = ref(0)
const powerUpDuration = 8000 // 增益持续8秒

/**
 * 护盾系统
 */
const hasShield = ref(false)
const shieldWaves: { x: number, y: number, radius: number, opacity: number, startTime: number }[] = []

/**
 * 游戏难度和时间管理
 */
const gameStartTime = ref(0)
const currentTime = ref(0)

/**
 * 计算游戏时间（秒）
 */
const gameTimeSeconds = computed(() => {
  if (!gameStarted.value || gameStartTime.value === 0) return 0
  return (currentTime.value - gameStartTime.value) / 1000
})

/**
 * 游戏尺寸配置
 */
const gameWidth = 800
const gameHeight = 600

/**
 * 游戏对象类型定义
 */
interface GameObject {
  x: number
  y: number
  width: number
  height: number
  vx: number
  vy: number
  color: string
}

interface Player extends GameObject {
  shooting: boolean
  lastShot: number
}

interface Bullet extends GameObject {
  active: boolean
}

interface Enemy extends GameObject {
  active: boolean
  lastShot: number
  type: 'normal' | 'fast' | 'spread' // 敌人类型：普通、高速、扩散弹
}

interface SpreadBullet extends Bullet {
  parentBullet?: boolean
  spreadTime?: number
  hasSpread?: boolean
  spreadDistance?: number
}

interface PowerUp extends GameObject {
  active: boolean
  type: 'spreadShot' | 'shield'
  floatOffset: number
  curveSpeed: number
}

/**
 * 游戏对象
 */
let ctx: CanvasRenderingContext2D
let animationId: number
let player: Player
let bullets: Bullet[] = []
let enemies: Enemy[] = []
let enemyBullets: Bullet[] = []
let powerUps: PowerUp[] = []
let lastEnemySpawn = 0
let lastPowerUpSpawn = 0
let playerTrail: { x: number, y: number, time: number }[] = [] // 玩家轨迹

/**
 *
 */
const keys = ref({
  w: false,
  a: false,
  s: false,
  d: false,
  space: false
})

const mousePressed = ref(false)

/**
 * 游戏初始化
 */
function initGame() {
  if (!gameCanvas.value) return
  
  ctx = gameCanvas.value.getContext('2d')!
  
  // 初始化玩家
  player = {
    x: gameWidth / 2 - 20,
    y: gameHeight - 60,
    width: 40,
    height: 40,
    vx: 0,
    vy: 0,
    color: '#00ff00',
    shooting: false,
    lastShot: 0
  }
  
  // 重置游戏状态
  bullets = []
  enemies = []
  enemyBullets = []
  powerUps = []
  score.value = 0
  lives.value = maxLives
  isShaking.value = false
  playerExploding.value = false
  explosionStartTime.value = 0
  lastEnemySpawn = 0
  lastPowerUpSpawn = 0
  gameStartTime.value = Date.now()
  currentTime.value = Date.now()
  
  // 重置弹夹和增益状态
  currentAmmo.value = maxAmmo
  isReloading.value = false
  powerUpActive.value = false
  hasShield.value = false
  shieldWaves.length = 0
  powerUpPickupEffects.length = 0 // 重置道具拾取特效
  killEffects.length = 0 // 重置击杀特效
  
  // 清空玩家轨迹
  playerTrail = []
}

/**
 * 选择游戏难度
 */
function selectDifficulty(difficulty: 'easy' | 'hard') {
  selectedDifficulty.value = difficulty
  difficultySelected.value = true
}

/**
 * 返回难度选择
 */
function backToDifficulty() {
  difficultySelected.value = false
  gameStarted.value = false
}

/**
 * 开始游戏
 */
function startGame() {
  initGame()
  gameStarted.value = true
  isPaused.value = false
  gameOver.value = false
  gameLoop()
  
  // 聚焦画布以接收键盘事件
  nextTick(() => {
    gameCanvas.value?.focus()
  })
}

/**
 * 重新开始游戏
 */
function restartGame() {
  gameOver.value = false
  difficultySelected.value = false // 重新选择难度
  gameStarted.value = false
}

/**
 * 暂停/继续游戏
 */
function togglePause() {
  isPaused.value = !isPaused.value
  if (!isPaused.value && gameStarted.value && !gameOver.value) {
    gameLoop()
  }
}

/**
 * 关闭游戏
 */
function closeGame() {
  isVisible.value = false
  gameStarted.value = false
  difficultySelected.value = false // 重置难度选择
  isPaused.value = false
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  emit('close')
}

/**
 * 键盘事件处理
 */
function handleKeyDown(event: KeyboardEvent) {
  switch(event.code) {
    case 'KeyW':
    case 'ArrowUp':
      keys.value.w = true
      break
    case 'KeyA':
    case 'ArrowLeft':
      keys.value.a = true
      break
    case 'KeyS':
    case 'ArrowDown':
      keys.value.s = true
      break
    case 'KeyD':
    case 'ArrowRight':
      keys.value.d = true
      break
    case 'Escape':
      togglePause()
      break
  }
}

function handleKeyUp(event: KeyboardEvent) {
  switch(event.code) {
    case 'KeyW':
    case 'ArrowUp':
      keys.value.w = false
      break
    case 'KeyA':
    case 'ArrowLeft':
      keys.value.a = false
      break
    case 'KeyS':
    case 'ArrowDown':
      keys.value.s = false
      break
    case 'KeyD':
    case 'ArrowRight':
      keys.value.d = false
      break
  }
}

/**
 * 鼠标事件处理（困难模式用于射击）
 */
function handleMouseDown(event: MouseEvent) {
  if (event.button === 0) { // 左键
    mousePressed.value = true
    event.preventDefault()
  }
}

function handleMouseUp(event: MouseEvent) {
  if (event.button === 0) { // 左键
    mousePressed.value = false
    event.preventDefault()
  }
}

/**
 * 更新玩家位置
 */
function updatePlayer() {
  const maxSpeed = 1.5 // 最大速度
  const acceleration = 0.25 // 加速度 (降低让加速更缓慢)
  const friction = 1.2 // 摩擦力/阻力系数 (进一步提高让减速更缓慢)
  
  // 基于按键输入计算目标速度
  let targetVx = 0
  let targetVy = 0
  
  if (keys.value.a) targetVx = -maxSpeed
  if (keys.value.d) targetVx = maxSpeed
  if (keys.value.w) targetVy = -maxSpeed
  if (keys.value.s) targetVy = maxSpeed
  
  // 应用加速度向目标速度靠近
  player.vx += (targetVx - player.vx) * acceleration
  player.vy += (targetVy - player.vy) * acceleration
  
  // 应用摩擦力
  player.vx *= friction
  player.vy *= friction
  
  // 更新位置
  player.x += player.vx
  player.y += player.vy
  
  // 记录轨迹点（仅当有明显移动时）
  const totalSpeed = Math.sqrt(player.vx * player.vx + player.vy * player.vy)
  if (totalSpeed > 0.5) {
    playerTrail.push({
      x: player.x + player.width / 2,
      y: player.y + player.height / 2,
      time: Date.now()
    })
    
    // 限制轨迹长度和清理旧轨迹
    const maxTrailLength = 15
    const maxTrailAge = 300 // 300毫秒
    const currentTime = Date.now()
    
    playerTrail = playerTrail.filter(point => 
      currentTime - point.time < maxTrailAge
    ).slice(-maxTrailLength)
  }
  
  // 边界检查 - 去掉反弹效果，直接停在边界
  if (player.x < 0) {
    player.x = 0
    player.vx = 0 // 直接停止，不反弹
  }
  if (player.x > gameWidth - player.width) {
    player.x = gameWidth - player.width
    player.vx = 0 // 直接停止，不反弹
  }
  if (player.y < 0) {
    player.y = 0
    player.vy = 0 // 直接停止，不反弹
  }
  if (player.y > gameHeight - player.height) {
    player.y = gameHeight - player.height
    player.vy = 0 // 直接停止，不反弹
  }
  
  // 检查是否需要自动换弹（仅困难模式有弹夹系统）
  if (selectedDifficulty.value === 'hard' && currentAmmo.value <= 0 && !isReloading.value && !powerUpActive.value) {
    startReload()
  }
  
  // 检查换弹是否完成（仅困难模式）
  if (selectedDifficulty.value === 'hard' && isReloading.value && Date.now() - reloadStartTime.value >= reloadDuration) {
    finishReload()
  }
  
  // 增益状态下的自动散弹射击（两种难度都有）
  if (powerUpActive.value && Date.now() < powerUpEndTime.value) {
    if (Date.now() - player.lastShot > 150) { // 自动射击间隔150ms，让散弹效果更明显
      shootWithPowerUp()
    }
  } else {
    // 根据难度模式处理射击
    if (selectedDifficulty.value === 'easy') {
      // 简单模式：自动射击，无限弹药
      if (Date.now() - player.lastShot > 200) { // 简单模式射击更快
        shootEasyMode()
      }
    } else {
      // 困难模式：手动射击，有弹夹限制
      if (mousePressed.value && !isReloading.value && Date.now() - player.lastShot > 200) {
        shoot()
      }
    }
  }
  
  // 检查增益是否过期
  if (powerUpActive.value && Date.now() >= powerUpEndTime.value) {
    powerUpActive.value = false
  }
}

/**
 * 开始换弹
 */
function startReload() {
  isReloading.value = true
  reloadStartTime.value = Date.now()
}

/**
 * 完成换弹
 */
function finishReload() {
  isReloading.value = false
  currentAmmo.value = maxAmmo
}

/**
 * 射击函数（困难模式 - 有弹夹限制）
 */
function shoot() {
  if (currentAmmo.value <= 0 || isReloading.value) return
  
  // 普通射击
  bullets.push({
    x: player.x + player.width / 2 - 2,
    y: player.y,
    width: 4,
    height: 10,
    vx: 0,
    vy: -8,
    color: '#ffff00',
    active: true
  })
  
  currentAmmo.value--
  player.lastShot = Date.now()
}

/**
 * 射击函数（简单模式 - 无限弹药）
 */
function shootEasyMode() {
  // 简单模式：无弹夹限制，自动射击
  bullets.push({
    x: player.x + player.width / 2 - 2,
    y: player.y,
    width: 4,
    height: 10,
    vx: 0,
    vy: -8,
    color: '#00ff00', // 绿色子弹表示简单模式
    active: true
  })
  
  player.lastShot = Date.now()
  // 注意：简单模式不消耗弹药，不减少 currentAmmo
}

/**
 * 增益状态下的射击函数（散弹，无限子弹）
 */
function shootWithPowerUp() {
  // 增益状态下不消耗弹夹，不受换弹限制
  
  // 散弹射击
  const angles = [-0.4, -0.2, 0, 0.2, 0.4]
  angles.forEach(angle => {
    bullets.push({
      x: player.x + player.width / 2 - 2,
      y: player.y,
      width: 4,
      height: 10,
      vx: angle * 6, // 增加水平速度，让扩散更明显
      vy: -8,
      color: '#ffaa00',
      active: true
    })
  })
  
  player.lastShot = Date.now()
}

/**
 * 击毁敌机后生成道具
 */
function createPowerUpFromEnemy(enemyX: number, enemyY: number, enemyType: 'normal' | 'fast' | 'spread') {
  // 根据敌机类型调整掉落概率
  let dropChance = 0.15 // 默认15%掉落概率
  
  if (enemyType === 'fast') {
    dropChance = 0.3 // 高速敌机25%掉落概率
  } else if (enemyType === 'spread') {
    dropChance = 0.25 // 扩散弹敌机30%掉落概率
  }
  
  if (Math.random() < dropChance) {
    // 随机选择道具类型：60%散弹，40%护盾
    const powerUpType = Math.random() < 0.6 ? 'spreadShot' : 'shield'
    
    powerUps.push({
      x: enemyX + Math.random() * 20 - 10, // 在敌机位置附近随机生成
      y: enemyY,
      width: 30,
      height: 30,
      vx: (Math.random() - 0.5) * 2, // 随机初始水平速度
      vy: 1.5, // 较慢的下落速度
      color: powerUpType === 'spreadShot' ? '#00ffff' : '#ffff00',
      active: true,
      type: powerUpType,
      floatOffset: Math.random() * Math.PI * 2,
      curveSpeed: 0.02 + Math.random() * 0.02
    })
  }
}

/**
 * 生成增益道具（已弃用，改为击毁敌机掉落）
 */
function spawnPowerUp() {
  // 这个函数不再使用，道具现在通过击毁敌机掉落
}
function spawnEnemy() {
  currentTime.value = Date.now()
  const gameTimeSeconds = (currentTime.value - gameStartTime.value) / 1000
  
  // 根据难度调整基础速度和难度递增
  const baseDifficultyMultiplier = selectedDifficulty.value === 'easy' ? 0.7 : 1.2 // 简单模式更慢，困难模式更快
  let difficultyMultiplier = baseDifficultyMultiplier + gameTimeSeconds / 30 // 每30秒增加难度
  
  // 在1分30秒（90秒）后大幅加速
  if (gameTimeSeconds > 90) {
    const extraTime = gameTimeSeconds - 90
    difficultyMultiplier = baseDifficultyMultiplier + 3 + extraTime / 15 // 90秒后基础倍率+3，并且每15秒再增加
  }
  
  const baseSpeed = selectedDifficulty.value === 'easy' ? 1.5 : 2.5 // 简单模式基础速度更慢
  const enemySpeed = baseSpeed * Math.min(difficultyMultiplier, selectedDifficulty.value === 'easy' ? 6 : 8) // 提高最大速度上限
  
  // 根据难度调整敌人生成频率，90秒后大幅增加敌人数量
  let baseSpawnInterval = selectedDifficulty.value === 'easy' ? 1200 : 800 // 简单模式生成更慢
  let minSpawnInterval = selectedDifficulty.value === 'easy' ? 300 : 150
  
  // 90秒后敌人生成频率大幅提升
  if (gameTimeSeconds > 90) {
    baseSpawnInterval = selectedDifficulty.value === 'easy' ? 600 : 400 // 基础生成间隔减半
    minSpawnInterval = selectedDifficulty.value === 'easy' ? 120 : 80 // 最小间隔也显著降低
  }
  
  const spawnInterval = Math.max(minSpawnInterval, baseSpawnInterval / difficultyMultiplier)
  
  if (currentTime.value - lastEnemySpawn > spawnInterval) {
    // 敌人尺寸更大
    const enemySize = 45 // 比之前的30更大
    
    // 根据游戏时间决定敌人类型概率
    const random = Math.random()
    let enemyType: 'normal' | 'fast' | 'spread' = 'normal'
    let enemyColor = '#ff0000'
    let enemyVy = enemySpeed
    
    const advancedEnemyStartTime = selectedDifficulty.value === 'easy' ? 30 : 20 // 简单模式延迟出现高级敌人
    if (gameTimeSeconds > advancedEnemyStartTime) {
      let fastChance = selectedDifficulty.value === 'easy' ? 0.15 : 0.25 // 简单模式减少高速敌人
      let spreadChance = selectedDifficulty.value === 'easy' ? 0.25 : 0.4 // 简单模式减少扩散弹敌人
      
      // 90秒后大幅增加高级敌人出现概率
      if (gameTimeSeconds > 90) {
        fastChance = selectedDifficulty.value === 'easy' ? 0.35 : 0.45 // 显著增加高速敌人
        spreadChance = selectedDifficulty.value === 'easy' ? 0.65 : 0.75 // 显著增加扩散弹敌人
      }
      
      if (random < fastChance) { // 高速型
        enemyType = 'fast'
        enemyColor = '#ff8800' // 橙色
        enemyVy = enemySpeed * (selectedDifficulty.value === 'easy' ? 2 : 2.5) // 简单模式高速敌人相对较慢
      } else if (random < spreadChance) { // 扩散弹型
        enemyType = 'spread'
        enemyColor = '#8800ff' // 紫色
        enemyVy = enemySpeed * 0.8 // 稍慢但会射击
      }
    }
    
    enemies.push({
      x: Math.random() * (gameWidth - enemySize),
      y: -enemySize,
      width: enemySize,
      height: enemySize,
      vx: 0,
      vy: enemyVy,
      color: enemyColor,
      active: true,
      lastShot: 0,
      type: enemyType
    })
    lastEnemySpawn = currentTime.value
  }
}

/**
 * 更新游戏对象
 */
function updateGameObjects() {
  currentTime.value = Date.now()
  const gameTimeSeconds = (currentTime.value - gameStartTime.value) / 1000
  
  // 随时间递增的敌人子弹速度
  const difficultyMultiplier = 1 + gameTimeSeconds / 30
  const baseBulletSpeed = 4
  const enemyBulletSpeed = baseBulletSpeed * Math.min(difficultyMultiplier, 3) // 最大3倍速度
  
  // 更新玩家子弹
  bullets.forEach(bullet => {
    bullet.x += bullet.vx // 添加水平移动更新
    bullet.y += bullet.vy
    if (bullet.y < 0 || bullet.x < 0 || bullet.x > gameWidth) bullet.active = false
  })
  bullets = bullets.filter(bullet => bullet.active)
  
  // 更新敌人
  enemies.forEach(enemy => {
    // 基础垂直移动
    enemy.y += enemy.vy
    
    if (enemy.y > gameHeight) enemy.active = false
    
    // 根据敌人类型决定射击行为
    if (enemy.type === 'normal' || enemy.type === 'spread') {
      const shootChance = enemy.type === 'spread' ? 0.015 : 0.01 // 扩散弹型射击更频繁
      const shootInterval = enemy.type === 'spread' ? 800 : 1000
      
      if (Math.random() < shootChance && Date.now() - enemy.lastShot > shootInterval) {
        if (enemy.type === 'spread') {
          // 扩散弹型：发射会扩散的子弹
          const spreadBullet: SpreadBullet = {
            x: enemy.x + enemy.width / 2 - 2,
            y: enemy.y + enemy.height,
            width: 4,
            height: 8,
            vx: 0,
            vy: enemyBulletSpeed,
            color: '#cc00ff',
            active: true,
            parentBullet: true,
            spreadTime: Date.now(),
            hasSpread: false,
            spreadDistance: 0
          }
          enemyBullets.push(spreadBullet)
        } else {
          // 普通型：发射普通子弹
          enemyBullets.push({
            x: enemy.x + enemy.width / 2 - 2,
            y: enemy.y + enemy.height,
            width: 4,
            height: 8,
            vx: 0,
            vy: enemyBulletSpeed,
            color: '#ff8800',
            active: true
          })
        }
        enemy.lastShot = Date.now()
      }
    }
    // 高速型敌人不射击，只是飞得很快
  })
  enemies = enemies.filter(enemy => enemy.active)
  
  // 更新敌人子弹
  enemyBullets.forEach(bullet => {
    const spreadBullet = bullet as SpreadBullet
    
    // 处理扩散弹逻辑
    if (spreadBullet.parentBullet && !spreadBullet.hasSpread) {
      spreadBullet.spreadDistance = (spreadBullet.spreadDistance || 0) + Math.abs(spreadBullet.vy)
      
      // 当子弹飞行一定距离后扩散
      if (spreadBullet.spreadDistance > 100) {
        spreadBullet.hasSpread = true
        
        // 生成5个方向的扩散子弹
        const angles = [-0.8, -0.4, 0, 0.4, 0.8]
        angles.forEach(angle => {
          enemyBullets.push({
            x: spreadBullet.x,
            y: spreadBullet.y,
            width: 3,
            height: 6,
            vx: angle * 1.5,
            vy: enemyBulletSpeed * 0.4,
            color: '#ff00cc',
            active: true
          })
        })
        
        // 移除原始子弹
        spreadBullet.active = false
      }
    }
    
    bullet.x += bullet.vx
    bullet.y += bullet.vy
    if (bullet.y > gameHeight || bullet.x < 0 || bullet.x > gameWidth) bullet.active = false
  })
  enemyBullets = enemyBullets.filter(bullet => bullet.active)
  
  // 更新增益道具
  powerUps.forEach(powerUp => {
    // 计算与玩家的距离
    const playerCenterX = player.x + player.width / 2
    const playerCenterY = player.y + player.height / 2
    const powerUpCenterX = powerUp.x + powerUp.width / 2
    const powerUpCenterY = powerUp.y + powerUp.height / 2
    
    const distance = Math.sqrt(
      Math.pow(powerUpCenterX - playerCenterX, 2) + 
      Math.pow(powerUpCenterY - playerCenterY, 2)
    )
    
    // 强力自动吸附和直接拾取系统
    const strongAttractRange = 80  // 强力吸附范围
    const autoPickupRange = 45     // 自动拾取范围
    
    if (distance <= autoPickupRange) {
      // 在拾取范围内直接触发拾取效果
      powerUp.active = false
      
      // 创建拾取特效
      createPowerUpPickupEffect(powerUpCenterX, powerUpCenterY, powerUp.type)
      
      if (powerUp.type === 'spreadShot') {
        // 激活散弹增益效果
        powerUpActive.value = true
        powerUpEndTime.value = Date.now() + powerUpDuration
      } else if (powerUp.type === 'shield') {
        // 激活护盾效果
        hasShield.value = true
      }
    } else if (distance <= strongAttractRange && distance > 0) {
      // 强力吸附模式：快速向玩家移动
      const dx = playerCenterX - powerUpCenterX
      const dy = playerCenterY - powerUpCenterY
      
      // 更强的吸附力，距离越近速度越快
      const attractionFactor = (strongAttractRange - distance) / strongAttractRange
      const attractSpeed = 8 + attractionFactor * 12  // 基础速度8，最高20
      
      // 直线吸附到玩家位置
      powerUp.vx = (dx / distance) * attractSpeed
      powerUp.vy = (dy / distance) * attractSpeed
      
      // 添加轻微的振动效果增强视觉
      const vibration = Math.sin(Date.now() * 0.05) * 2 * attractionFactor
      powerUp.vx += vibration
      powerUp.vy += vibration * 0.5
    } else {
      // 正常的曲线飘落运动
      powerUp.floatOffset += powerUp.curveSpeed
      powerUp.vx = Math.sin(powerUp.floatOffset) * 1.5 // 减小水平移动幅度
      if (!powerUp.vy) powerUp.vy = 2 // 确保有基础下落速度
    }
    
    // 更新位置
    powerUp.x += powerUp.vx
    powerUp.y += powerUp.vy
    
    // 防止道具飘出左右屏幕
    if (powerUp.x < 0) {
      powerUp.x = 0
      powerUp.vx = Math.abs(powerUp.vx) // 反弹向右
    }
    if (powerUp.x + powerUp.width > gameWidth) {
      powerUp.x = gameWidth - powerUp.width
      powerUp.vx = -Math.abs(powerUp.vx) // 反弹向左
    }
    
    if (powerUp.y > gameHeight) powerUp.active = false
  })
  powerUps = powerUps.filter(powerUp => powerUp.active)
}

/**
 * 碰撞检测
 */
function checkCollisions() {
  // 玩家子弹击中敌人
  bullets.forEach((bullet, bulletIndex) => {
    enemies.forEach((enemy, enemyIndex) => {
      if (bullet.active && enemy.active &&
          bullet.x < enemy.x + enemy.width &&
          bullet.x + bullet.width > enemy.x &&
          bullet.y < enemy.y + enemy.height &&
          bullet.y + bullet.height > enemy.y) {
        bullet.active = false
        enemy.active = false
        score.value += 10
        
        // 创建击杀特效
        createKillEffect(enemy.x + enemy.width / 2, enemy.y + enemy.height / 2, enemy.type)
        
        // 击毁敌机后有概率掉落道具
        createPowerUpFromEnemy(enemy.x, enemy.y, enemy.type)
      }
    })
  })
  
  // 敌人子弹击中玩家中心点（更精确的碰撞检测）
  const playerCenterX = player.x + player.width / 2
  const playerCenterY = player.y + player.height / 2
  const centerHitRadius = 8 // 中心点碰撞半径
  
  enemyBullets.forEach(bullet => {
    if (bullet.active) {
      const bulletCenterX = bullet.x + bullet.width / 2
      const bulletCenterY = bullet.y + bullet.height / 2
      
      // 计算子弹中心与玩家中心点的距离
      const distance = Math.sqrt(
        Math.pow(bulletCenterX - playerCenterX, 2) + 
        Math.pow(bulletCenterY - playerCenterY, 2)
      )
      
      // 只有击中中心点才会触发伤害或护盾
      if (distance <= centerHitRadius) {
        bullet.active = false
        
        if (hasShield.value) {
          // 护盾抵挡伤害，产生冲击波特效
          createShieldWave(playerCenterX, playerCenterY)
          hasShield.value = false
        } else {
          // 没有护盾，正常扣血
          lives.value--
          triggerShakeEffect() // 触发震感效果
          if (lives.value <= 0) {
            triggerPlayerExplosion() // 触发玩家爆炸
          }
        }
      }
    }
  })
  
  // 敌人撞击玩家中心点
  enemies.forEach(enemy => {
    if (enemy.active) {
      const enemyCenterX = enemy.x + enemy.width / 2
      const enemyCenterY = enemy.y + enemy.height / 2
      
      // 计算敌人中心与玩家中心点的距离
      const distance = Math.sqrt(
        Math.pow(enemyCenterX - playerCenterX, 2) + 
        Math.pow(enemyCenterY - playerCenterY, 2)
      )
      
      // 只有撞击中心点才会触发伤害或护盾
      if (distance <= centerHitRadius + 10) {
        enemy.active = false
        
        if (hasShield.value) {
          // 护盾抵挡伤害，产生冲击波特效
          createShieldWave(playerCenterX, playerCenterY)
          hasShield.value = false
        } else {
          // 没有护盾，正常扣血
          lives.value--
          triggerShakeEffect() // 触发震感效果
          if (lives.value <= 0) {
            triggerPlayerExplosion() // 触发玩家爆炸
          }
        }
      }
    }
  })
  
  // 玩家拾取增益道具的逻辑已移至updateGameObjects中的道具更新部分
}

/**
 * 渲染游戏
 */
function render() {
  // 清空画布
  ctx.fillStyle = '#000011'
  ctx.fillRect(0, 0, gameWidth, gameHeight)
  
  // 绘制星星背景
  for (let i = 0; i < 50; i++) {
    ctx.fillStyle = '#ffffff'
    ctx.fillRect(
      (i * 37 + Date.now() * 0.1) % gameWidth,
      (i * 43 + Date.now() * 0.05) % gameHeight,
      1, 1
    )
  }
  
  // 绘制玩家轨迹
  drawPlayerTrail()
  
  // 绘制玩家飞机或爆炸效果
  if (playerExploding.value) {
    drawPlayerExplosion(player.x, player.y, player.width, player.height)
  } else {
    drawPlayerShip(player.x, player.y, player.width, player.height)
  }
  
  // 绘制玩家子弹
  bullets.forEach(bullet => {
    ctx.fillStyle = bullet.color
    ctx.fillRect(bullet.x, bullet.y, bullet.width, bullet.height)
  })
  
  // 绘制敌人飞机
  enemies.forEach(enemy => {
    drawEnemyShip(enemy.x, enemy.y, enemy.width, enemy.height, enemy.type)
  })
  
  // 绘制敌人子弹
  enemyBullets.forEach(bullet => {
    ctx.fillStyle = bullet.color
    ctx.fillRect(bullet.x, bullet.y, bullet.width, bullet.height)
  })
  
  // 绘制增益道具
  powerUps.forEach(powerUp => {
    drawPowerUp(powerUp.x, powerUp.y, powerUp.width, powerUp.height, powerUp.type)
  })
  
  // 绘制护盾冲击波
  drawShieldWaves()
  
  // 绘制击杀特效
  drawKillEffects()
  
  // 绘制道具拾取特效
  drawPowerUpPickupEffects()
  
  // 绘制UI元素
  drawAmmoDisplay()
  drawReloadAnimation()
  drawPowerUpIndicator()
  drawGameTimer() // 添加游戏计时器
  drawSpeedIndicator() // 添加速度指示器
}

/**
 * 击杀特效数组
 */
let killEffects: {
  x: number,
  y: number,
  particles: Array<{
    x: number,
    y: number,
    vx: number,
    vy: number,
    life: number,
    maxLife: number,
    size: number,
    color: string,
    rotation: number,
    rotationSpeed: number
  }>,
  startTime: number,
  enemyType: 'normal' | 'fast' | 'spread'
}[] = []

/**
 * 创建击杀特效
 */
function createKillEffect(x: number, y: number, enemyType: 'normal' | 'fast' | 'spread') {
  const particleCount = 25
  const particles = []
  
  // 根据敌机类型设置特效颜色
  let colors = ['#ff4444', '#ff8800', '#ffaa00'] // 普通敌机：红橙色
  if (enemyType === 'fast') {
    colors = ['#ff8800', '#ffaa00', '#ffcc44'] // 高速敌机：橙黄色
  } else if (enemyType === 'spread') {
    colors = ['#8800ff', '#aa44ff', '#cc66ff'] // 扩散弹敌机：紫色
  }
  
  // 生成爆炸粒子
  for (let i = 0; i < particleCount; i++) {
    const angle = (i / particleCount) * Math.PI * 2 + Math.random() * 0.5
    const speed = 2 + Math.random() * 8
    const life = 600 + Math.random() * 800
    
    particles.push({
      x: x,
      y: y,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      life: life,
      maxLife: life,
      size: 1 + Math.random() * 4,
      color: colors[Math.floor(Math.random() * colors.length)],
      rotation: Math.random() * Math.PI * 2,
      rotationSpeed: (Math.random() - 0.5) * 0.2
    })
  }
  
  // 添加金属碎片效果
  for (let i = 0; i < 8; i++) {
    const angle = Math.random() * Math.PI * 2
    const speed = 1 + Math.random() * 3
    const life = 1000 + Math.random() * 500
    
    particles.push({
      x: x,
      y: y,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      life: life,
      maxLife: life,
      size: 2 + Math.random() * 3,
      color: '#cccccc',
      rotation: Math.random() * Math.PI * 2,
      rotationSpeed: (Math.random() - 0.5) * 0.3
    })
  }
  
  killEffects.push({
    x: x,
    y: y,
    particles: particles,
    startTime: Date.now(),
    enemyType: enemyType
  })
}

/**
 * 更新击杀特效
 */
function updateKillEffects() {
  killEffects.forEach(effect => {
    effect.particles.forEach(particle => {
      // 更新粒子位置
      particle.x += particle.vx
      particle.y += particle.vy
      
      // 添加重力和阻力
      particle.vy += 0.15 // 重力
      particle.vx *= 0.98 // 阻力
      particle.vy *= 0.98
      
      // 更新旋转
      particle.rotation += particle.rotationSpeed
      
      // 减少生命值
      particle.life -= 16
    })
    
    // 移除死亡粒子
    effect.particles = effect.particles.filter(particle => particle.life > 0)
  })
  
  // 移除没有粒子的特效
  killEffects = killEffects.filter(effect => effect.particles.length > 0)
}

/**
 * 绘制击杀特效
 */
function drawKillEffects() {
  ctx.save()
  
  killEffects.forEach(effect => {
    effect.particles.forEach(particle => {
      const alpha = particle.life / particle.maxLife
      const size = particle.size * (0.5 + alpha * 0.5)
      
      ctx.save()
      ctx.translate(particle.x, particle.y)
      ctx.rotate(particle.rotation)
      
      // 创建粒子渐变
      const gradient = ctx.createRadialGradient(0, 0, 0, 0, 0, size * 2)
      gradient.addColorStop(0, `${particle.color}${Math.floor(alpha * 255).toString(16).padStart(2, '0')}`)
      gradient.addColorStop(1, `${particle.color}00`)
      
      ctx.fillStyle = gradient
      
      if (particle.color === '#cccccc') {
        // 金属碎片 - 矩形
        ctx.fillRect(-size/2, -size/2, size, size)
      } else {
        // 火焰粒子 - 圆形
        ctx.beginPath()
        ctx.arc(0, 0, size, 0, Math.PI * 2)
        ctx.fill()
      }
      
      ctx.restore()
    })
    
    // 绘制爆炸中心闪光
    const elapsed = Date.now() - effect.startTime
    if (elapsed < 200) {
      const flashAlpha = 1 - (elapsed / 200)
      const flashSize = 15 + (elapsed / 200) * 25
      
      const flashGradient = ctx.createRadialGradient(
        effect.x, effect.y, 0,
        effect.x, effect.y, flashSize
      )
      
      flashGradient.addColorStop(0, `rgba(255, 255, 255, ${flashAlpha})`)
      flashGradient.addColorStop(0.5, `rgba(255, 200, 100, ${flashAlpha * 0.6})`)
      flashGradient.addColorStop(1, 'rgba(255, 100, 50, 0)')
      
      ctx.fillStyle = flashGradient
      ctx.beginPath()
      ctx.arc(effect.x, effect.y, flashSize, 0, Math.PI * 2)
      ctx.fill()
    }
  })
  
  ctx.restore()
}

/**
 * 道具拾取特效数组
 */
let powerUpPickupEffects: { 
  x: number, 
  y: number, 
  particles: Array<{
    x: number, 
    y: number, 
    vx: number, 
    vy: number, 
    life: number, 
    maxLife: number,
    size: number,
    color: string
  }>, 
  startTime: number,
  type: 'spreadShot' | 'shield'
}[] = []

/**
 * 创建道具拾取特效
 */
function createPowerUpPickupEffect(x: number, y: number, type: 'spreadShot' | 'shield') {
  const particleCount = 20
  const particles = []
  
  const baseColor = type === 'shield' ? '#ffff00' : '#00ffff'
  
  // 生成粒子
  for (let i = 0; i < particleCount; i++) {
    const angle = (i / particleCount) * Math.PI * 2
    const speed = 3 + Math.random() * 4
    const life = 800 + Math.random() * 400
    
    particles.push({
      x: x,
      y: y,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      life: life,
      maxLife: life,
      size: 2 + Math.random() * 3,
      color: baseColor
    })
  }
  
  powerUpPickupEffects.push({
    x: x,
    y: y,
    particles: particles,
    startTime: Date.now(),
    type: type
  })
}

/**
 * 更新道具拾取特效
 */
function updatePowerUpPickupEffects() {
  const currentTime = Date.now()
  
  powerUpPickupEffects.forEach(effect => {
    effect.particles.forEach(particle => {
      // 更新粒子位置
      particle.x += particle.vx
      particle.y += particle.vy
      
      // 添加重力效果
      particle.vy += 0.1
      
      // 减少生命值
      particle.life -= 16 // 约60FPS下每帧减少16ms
      
      // 添加阻力
      particle.vx *= 0.98
      particle.vy *= 0.98
    })
    
    // 移除死亡粒子
    effect.particles = effect.particles.filter(particle => particle.life > 0)
  })
  
  // 移除没有粒子的特效
  powerUpPickupEffects = powerUpPickupEffects.filter(effect => effect.particles.length > 0)
}

/**
 * 绘制道具拾取特效
 */
function drawPowerUpPickupEffects() {
  ctx.save()
  
  powerUpPickupEffects.forEach(effect => {
    effect.particles.forEach(particle => {
      const alpha = particle.life / particle.maxLife
      const size = particle.size * alpha
      
      // 创建粒子渐变
      const gradient = ctx.createRadialGradient(
        particle.x, particle.y, 0,
        particle.x, particle.y, size * 2
      )
      gradient.addColorStop(0, `${particle.color}${Math.floor(alpha * 255).toString(16).padStart(2, '0')}`)
      gradient.addColorStop(1, `${particle.color}00`)
      
      ctx.fillStyle = gradient
      ctx.beginPath()
      ctx.arc(particle.x, particle.y, size, 0, Math.PI * 2)
      ctx.fill()
    })
    
    // 绘制中心闪光效果
    const elapsed = Date.now() - effect.startTime
    if (elapsed < 300) {
      const flashAlpha = 1 - (elapsed / 300)
      const flashSize = 20 + (elapsed / 300) * 30
      
      const flashGradient = ctx.createRadialGradient(
        effect.x, effect.y, 0,
        effect.x, effect.y, flashSize
      )
      
      const flashColor = effect.type === 'shield' ? '255,255,0' : '0,255,255'
      flashGradient.addColorStop(0, `rgba(${flashColor}, ${flashAlpha})`)
      flashGradient.addColorStop(0.5, `rgba(255,255,255, ${flashAlpha * 0.8})`)
      flashGradient.addColorStop(1, `rgba(${flashColor}, 0)`)
      
      ctx.fillStyle = flashGradient
      ctx.beginPath()
      ctx.arc(effect.x, effect.y, flashSize, 0, Math.PI * 2)
      ctx.fill()
    }
  })
  
  ctx.restore()
}

/**
 * 创建护盾冲击波特效
 */
function createShieldWave(x: number, y: number) {
  shieldWaves.push({
    x: x,
    y: y,
    radius: 0,
    opacity: 1,
    startTime: Date.now()
  })
}

/**
 * 触发受伤震感效果
 */
function triggerShakeEffect() {
  isShaking.value = true
  
  // 使用定时器在500ms后停止震感
  setTimeout(() => {
    isShaking.value = false
  }, 500)
}

/**
 * 触发玩家飞机爆炸效果
 */
function triggerPlayerExplosion() {
  playerExploding.value = true
  explosionStartTime.value = Date.now()
  
  // 2秒后结束爆炸动画并显示游戏结束
  setTimeout(() => {
    playerExploding.value = false
    gameOver.value = true
  }, 2000)
}

/**
 * 更新护盾冲击波
 */
function updateShieldWaves() {
  const currentTime = Date.now()
  
  // 更新冲击波效果
  shieldWaves.forEach(wave => {
    const elapsed = currentTime - wave.startTime
    const duration = 800 // 冲击波持续800ms
    
    if (elapsed < duration) {
      // 半径增长
      wave.radius = (elapsed / duration) * 150 // 最大半径150像素
      // 透明度递减
      wave.opacity = 1 - (elapsed / duration)
    }
  })
  
  // 移除已完成的冲击波
  shieldWaves.splice(0, shieldWaves.length, ...shieldWaves.filter(wave => 
    Date.now() - wave.startTime < 800
  ))
}

/**
 * 绘制护盾冲击波
 */
function drawShieldWaves() {
  shieldWaves.forEach(wave => {
    if (wave.opacity > 0) {
      ctx.save()
      
      // 外圈冲击波
      ctx.strokeStyle = `rgba(0, 255, 255, ${wave.opacity})`
      ctx.lineWidth = 4
      ctx.setLineDash([10, 5])
      ctx.beginPath()
      ctx.arc(wave.x, wave.y, wave.radius, 0, Math.PI * 2)
      ctx.stroke()
      
      // 内圈光晕
      const innerGradient = ctx.createRadialGradient(
        wave.x, wave.y, 0,
        wave.x, wave.y, wave.radius * 0.8
      )
      innerGradient.addColorStop(0, `rgba(255, 255, 255, ${wave.opacity * 0.3})`)
      innerGradient.addColorStop(0.5, `rgba(0, 255, 255, ${wave.opacity * 0.2})`)
      innerGradient.addColorStop(1, 'rgba(0, 255, 255, 0)')
      
      ctx.fillStyle = innerGradient
      ctx.beginPath()
      ctx.arc(wave.x, wave.y, wave.radius * 0.8, 0, Math.PI * 2)
      ctx.fill()
      
      ctx.restore()
    }
  })
}

/**
 * 绘制玩家飞船 - 高级战斗机设计
 */
function drawPlayerShip(x: number, y: number, width: number, height: number) {
  ctx.save()
  
  // 整体阴影效果
  ctx.fillStyle = 'rgba(0, 60, 120, 0.4)'
  ctx.beginPath()
  ctx.moveTo(x + width / 2 + 3, y + 3) // 机头阴影
  ctx.lineTo(x + width * 0.15 + 3, y + height * 0.4 + 3) // 左机身阴影
  ctx.lineTo(x - width * 0.2 + 3, y + height * 0.6 + 3) // 左机翼阴影
  ctx.lineTo(x + width * 0.2 + 3, y + height * 0.8 + 3)
  ctx.lineTo(x + width * 0.25 + 3, y + height + 3) // 左尾部阴影
  ctx.lineTo(x + width * 0.75 + 3, y + height + 3) // 右尾部阴影
  ctx.lineTo(x + width * 0.8 + 3, y + height * 0.8 + 3)
  ctx.lineTo(x + width * 1.2 + 3, y + height * 0.6 + 3) // 右机翼阴影
  ctx.lineTo(x + width * 0.85 + 3, y + height * 0.4 + 3) // 右机身阴影
  ctx.closePath()
  ctx.fill()
  
  // 主机身 - F22战斗机风格
  const bodyGradient = ctx.createLinearGradient(x, y, x, y + height)
  bodyGradient.addColorStop(0, '#1a4d80')
  bodyGradient.addColorStop(0.3, '#2e6ba8')
  bodyGradient.addColorStop(0.7, '#1f5588')
  bodyGradient.addColorStop(1, '#0f3356')
  ctx.fillStyle = bodyGradient
  
  ctx.beginPath()
  ctx.moveTo(x + width / 2, y) // 尖锐机头
  ctx.lineTo(x + width * 0.15, y + height * 0.4) // 左机身收缩
  ctx.lineTo(x + width * 0.25, y + height) // 左尾部
  ctx.lineTo(x + width * 0.75, y + height) // 右尾部
  ctx.lineTo(x + width * 0.85, y + height * 0.4) // 右机身收缩
  ctx.closePath()
  ctx.fill()
  
  // 机身高光线条
  ctx.strokeStyle = '#4a8bc2'
  ctx.lineWidth = 2
  ctx.beginPath()
  ctx.moveTo(x + width / 2, y + 5)
  ctx.lineTo(x + width * 0.3, y + height * 0.8)
  ctx.stroke()
  
  ctx.beginPath()
  ctx.moveTo(x + width / 2, y + 5)
  ctx.lineTo(x + width * 0.7, y + height * 0.8)
  ctx.stroke()
  
  // 左主机翼 - 三角翼设计
  const leftWingGradient = ctx.createLinearGradient(x - width * 0.2, y + height * 0.6, x + width * 0.3, y + height * 0.6)
  leftWingGradient.addColorStop(0, '#2d5a9e')
  leftWingGradient.addColorStop(0.5, '#4470b8')
  leftWingGradient.addColorStop(1, '#1a4d80')
  ctx.fillStyle = leftWingGradient
  
  ctx.beginPath()
  ctx.moveTo(x + width * 0.2, y + height * 0.5) // 机翼根部前
  ctx.lineTo(x - width * 0.2, y + height * 0.6) // 机翼尖端
  ctx.lineTo(x + width * 0.05, y + height * 0.8) // 机翼根部后
  ctx.lineTo(x + width * 0.3, y + height * 0.7) // 机身连接点
  ctx.closePath()
  ctx.fill()
  
  // 右主机翼
  const rightWingGradient = ctx.createLinearGradient(x + width * 0.7, y + height * 0.6, x + width * 1.2, y + height * 0.6)
  rightWingGradient.addColorStop(0, '#1a4d80')
  rightWingGradient.addColorStop(0.5, '#4470b8')
  rightWingGradient.addColorStop(1, '#2d5a9e')
  ctx.fillStyle = rightWingGradient
  
  ctx.beginPath()
  ctx.moveTo(x + width * 0.8, y + height * 0.5) // 机翼根部前
  ctx.lineTo(x + width * 1.2, y + height * 0.6) // 机翼尖端
  ctx.lineTo(x + width * 0.95, y + height * 0.8) // 机翼根部后
  ctx.lineTo(x + width * 0.7, y + height * 0.7) // 机身连接点
  ctx.closePath()
  ctx.fill()
  
  // 机翼装饰条纹
  ctx.strokeStyle = '#ffffff'
  ctx.lineWidth = 1
  ctx.setLineDash([3, 2])
  ctx.beginPath()
  ctx.moveTo(x + width * 0.1, y + height * 0.65)
  ctx.lineTo(x + width * 0.15, y + height * 0.75)
  ctx.stroke()
  
  ctx.beginPath()
  ctx.moveTo(x + width * 0.9, y + height * 0.65)
  ctx.lineTo(x + width * 0.85, y + height * 0.75)
  ctx.stroke()
  ctx.setLineDash([])
  
  // 垂直尾翼
  ctx.fillStyle = '#1a4d80'
  ctx.fillRect(x + width * 0.47, y + height * 0.2, width * 0.06, height * 0.3)
  
  // 尾翼顶部
  ctx.beginPath()
  ctx.moveTo(x + width * 0.47, y + height * 0.2)
  ctx.lineTo(x + width / 2, y + height * 0.15)
  ctx.lineTo(x + width * 0.53, y + height * 0.2)
  ctx.closePath()
  ctx.fill()
  
  // 驾驶舱 - 多层玻璃效果
  const cockpitGradient = ctx.createRadialGradient(
    x + width / 2, y + height * 0.3, 0,
    x + width / 2, y + height * 0.3, 8
  )
  cockpitGradient.addColorStop(0, '#87ceeb')
  cockpitGradient.addColorStop(0.6, '#4682b4')
  cockpitGradient.addColorStop(1, '#1e3a5f')
  ctx.fillStyle = cockpitGradient
  
  ctx.beginPath()
  ctx.ellipse(x + width / 2, y + height * 0.3, 8, 6, 0, 0, Math.PI * 2)
  ctx.fill()
  
  // 驾驶舱反光
  ctx.fillStyle = 'rgba(255, 255, 255, 0.7)'
  ctx.beginPath()
  ctx.ellipse(x + width / 2 - 2, y + height * 0.3 - 2, 4, 3, 0, 0, Math.PI * 2)
  ctx.fill()
  
  // 进气口设计
  ctx.fillStyle = '#0f2744'
  ctx.beginPath()
  ctx.ellipse(x + width * 0.35, y + height * 0.45, 4, 3, 0, 0, Math.PI * 2)
  ctx.fill()
  
  ctx.beginPath()
  ctx.ellipse(x + width * 0.65, y + height * 0.45, 4, 3, 0, 0, Math.PI * 2)
  ctx.fill()
  
  // 发动机喷口 - 矢量推进器
  const engineGradient = ctx.createRadialGradient(
    x + width * 0.3, y + height * 0.95, 0,
    x + width * 0.3, y + height * 0.95, 6
  )
  engineGradient.addColorStop(0, '#ff4500')
  engineGradient.addColorStop(0.5, '#1e3a5f')
  engineGradient.addColorStop(1, '#0f1f33')
  ctx.fillStyle = engineGradient
  
  ctx.beginPath()
  ctx.ellipse(x + width * 0.3, y + height * 0.95, 5, 8, 0, 0, Math.PI * 2)
  ctx.fill()
  
  ctx.beginPath()
  ctx.ellipse(x + width * 0.7, y + height * 0.95, 5, 8, 0, 0, Math.PI * 2)
  ctx.fill()
  
  // 发动机内环
  ctx.fillStyle = '#ff6600'
  ctx.beginPath()
  ctx.ellipse(x + width * 0.3, y + height * 0.95, 3, 5, 0, 0, Math.PI * 2)
  ctx.fill()
  
  ctx.beginPath()
  ctx.ellipse(x + width * 0.7, y + height * 0.95, 3, 5, 0, 0, Math.PI * 2)
  ctx.fill()
  
  // 推进器火焰效果 - 基于飞船速度
  const totalSpeed = Math.sqrt(player.vx * player.vx + player.vy * player.vy)
  if (totalSpeed > 0.5) { // 只有当速度足够大时才显示火焰
    const speedFactor = Math.min(totalSpeed / 6, 1) // 标准化速度因子
    const flameIntensity = (0.4 + Math.random() * 0.4) * speedFactor
    const flameLength = (6 + Math.random() * 8) * speedFactor
    
    // 根据移动方向调整火焰效果
    const flameOffsetX = -player.vx * 0.8 // 反方向偏移
    const flameOffsetY = -player.vy * 0.3 // 轻微的垂直偏移
    
    // 左推进器火焰
    const leftFlameGradient = ctx.createLinearGradient(
      x + width * 0.3, y + height * 0.95,
      x + width * 0.3 + flameOffsetX, y + height + flameLength + flameOffsetY
    )
    leftFlameGradient.addColorStop(0, `rgba(255, 255, 255, ${flameIntensity})`)
    leftFlameGradient.addColorStop(0.3, `rgba(0, 150, 255, ${flameIntensity})`)
    leftFlameGradient.addColorStop(0.7, `rgba(255, 100, 0, ${flameIntensity * 0.8})`)
    leftFlameGradient.addColorStop(1, 'rgba(255, 0, 0, 0)')
    ctx.fillStyle = leftFlameGradient
    
    ctx.beginPath()
    ctx.ellipse(
      x + width * 0.3 + flameOffsetX * 0.5, 
      y + height + flameLength / 2 + flameOffsetY * 0.5, 
      2 + speedFactor, 
      flameLength / 2, 
      0, 0, Math.PI * 2
    )
    ctx.fill()
    
    // 右推进器火焰
    const rightFlameGradient = ctx.createLinearGradient(
      x + width * 0.7, y + height * 0.95,
      x + width * 0.7 + flameOffsetX, y + height + flameLength + flameOffsetY
    )
    rightFlameGradient.addColorStop(0, `rgba(255, 255, 255, ${flameIntensity})`)
    rightFlameGradient.addColorStop(0.3, `rgba(0, 150, 255, ${flameIntensity})`)
    rightFlameGradient.addColorStop(0.7, `rgba(255, 100, 0, ${flameIntensity * 0.8})`)
    rightFlameGradient.addColorStop(1, 'rgba(255, 0, 0, 0)')
    ctx.fillStyle = rightFlameGradient
    
    ctx.beginPath()
    ctx.ellipse(
      x + width * 0.7 + flameOffsetX * 0.5, 
      y + height + flameLength / 2 + flameOffsetY * 0.5, 
      2 + speedFactor, 
      flameLength / 2, 
      0, 0, Math.PI * 2
    )
    ctx.fill()
  }
  
  // 武器挂载点
  ctx.fillStyle = '#2a4d6e'
  ctx.fillRect(x + width * 0.15, y + height * 0.6, 3, 6)
  ctx.fillRect(x + width * 0.82, y + height * 0.6, 3, 6)
  
  // 中心碰撞检测点（隐形雷达效果）
  const blinkSpeed = 400
  const isVisible = Math.floor(Date.now() / blinkSpeed) % 2 === 0
  
  if (isVisible) {
    // 雷达扫描圈
    const radarGradient = ctx.createRadialGradient(
      x + width / 2, y + height / 2, 0,
      x + width / 2, y + height / 2, 12
    )
    radarGradient.addColorStop(0, 'rgba(0, 255, 100, 0.8)')
    radarGradient.addColorStop(0.7, 'rgba(0, 255, 100, 0.3)')
    radarGradient.addColorStop(1, 'rgba(0, 255, 100, 0)')
    ctx.fillStyle = radarGradient
    ctx.beginPath()
    ctx.arc(x + width / 2, y + height / 2, 12, 0, Math.PI * 2)
    ctx.fill()
    
    // 中心点
    ctx.fillStyle = '#00ff64'
    ctx.beginPath()
    ctx.arc(x + width / 2, y + height / 2, 3, 0, Math.PI * 2)
    ctx.fill()
    
    ctx.fillStyle = '#ffffff'
    ctx.beginPath()
    ctx.arc(x + width / 2, y + height / 2, 1, 0, Math.PI * 2)
    ctx.fill()
  }
  
  // 护盾指示器
  if (hasShield.value) {
    const time = Date.now() * 0.01
    const shieldRadius = width / 2 + 15 + Math.sin(time) * 3
    
    // 护盾光环
    const shieldGradient = ctx.createRadialGradient(
      x + width / 2, y + height / 2, shieldRadius - 5,
      x + width / 2, y + height / 2, shieldRadius + 5
    )
    shieldGradient.addColorStop(0, 'rgba(0, 255, 255, 0)')
    shieldGradient.addColorStop(0.5, 'rgba(0, 255, 255, 0.6)')
    shieldGradient.addColorStop(1, 'rgba(0, 255, 255, 0)')
    
    ctx.strokeStyle = shieldGradient
    ctx.lineWidth = 3
    ctx.setLineDash([8, 4])
    ctx.lineDashOffset = time * 2
    ctx.beginPath()
    ctx.arc(x + width / 2, y + height / 2, shieldRadius, 0, Math.PI * 2)
    ctx.stroke()
    ctx.setLineDash([]) // 重置虚线
  }
  
  ctx.restore()
}

/**
 * 绘制玩家飞机爆炸效果
 */
function drawPlayerExplosion(x: number, y: number, width: number, height: number) {
  const currentTime = Date.now()
  const elapsed = currentTime - explosionStartTime.value
  const duration = 2000 // 爆炸持续2秒
  const progress = Math.min(elapsed / duration, 1)
  
  ctx.save()
  
  // 计算爆炸中心
  const centerX = x + width / 2
  const centerY = y + height / 2
  
  // 多层爆炸效果
  for (let i = 0; i < 8; i++) {
    const angle = (i / 8) * Math.PI * 2 + elapsed * 0.01
    const distance = progress * (50 + i * 15)
    const particleX = centerX + Math.cos(angle) * distance
    const particleY = centerY + Math.sin(angle) * distance
    const size = (1 - progress) * (10 + i * 3)
    
    // 火焰颜色渐变
    const colors = ['#ffff00', '#ff8800', '#ff4400', '#ff0000', '#880000']
    const colorIndex = Math.floor(progress * colors.length)
    ctx.fillStyle = colors[Math.min(colorIndex, colors.length - 1)]
    
    // 绘制爆炸粒子
    ctx.globalAlpha = (1 - progress) * 0.8
    ctx.beginPath()
    ctx.arc(particleX, particleY, size, 0, Math.PI * 2)
    ctx.fill()
  }
  
  // 中心火球
  const fireballRadius = (1 - progress * 0.3) * 25
  const fireballGradient = ctx.createRadialGradient(
    centerX, centerY, 0,
    centerX, centerY, fireballRadius
  )
  fireballGradient.addColorStop(0, `rgba(255, 255, 255, ${(1 - progress) * 0.9})`)
  fireballGradient.addColorStop(0.3, `rgba(255, 200, 0, ${(1 - progress) * 0.8})`)
  fireballGradient.addColorStop(0.6, `rgba(255, 100, 0, ${(1 - progress) * 0.6})`)
  fireballGradient.addColorStop(1, `rgba(200, 0, 0, ${(1 - progress) * 0.3})`)
  
  ctx.globalAlpha = 1
  ctx.fillStyle = fireballGradient
  ctx.beginPath()
  ctx.arc(centerX, centerY, fireballRadius, 0, Math.PI * 2)
  ctx.fill()
  
  // 冲击波环
  if (progress < 0.8) {
    const shockwaveRadius = progress * 80
    ctx.strokeStyle = `rgba(255, 255, 255, ${(0.8 - progress) * 0.6})`
    ctx.lineWidth = 3
    ctx.beginPath()
    ctx.arc(centerX, centerY, shockwaveRadius, 0, Math.PI * 2)
    ctx.stroke()
  }
  
  // 飞机碎片效果
  for (let i = 0; i < 12; i++) {
    const angle = (i / 12) * Math.PI * 2 + elapsed * 0.005
    const distance = progress * (30 + i * 8)
    const pieceX = centerX + Math.cos(angle) * distance
    const pieceY = centerY + Math.sin(angle) * distance
    const rotation = elapsed * 0.02 + i
    
    ctx.save()
    ctx.translate(pieceX, pieceY)
    ctx.rotate(rotation)
    ctx.fillStyle = `rgba(120, 120, 120, ${(1 - progress) * 0.7})`
    ctx.fillRect(-3, -1, 6, 2) // 小碎片
    ctx.restore()
  }
  
  ctx.restore()
}

/**
 * 绘制敌人飞船 - 三种不同设计的战斗机
 */
function drawEnemyShip(x: number, y: number, width: number, height: number, type: 'normal' | 'fast' | 'spread') {
  ctx.save()
  
  if (type === 'normal') {
    // 普通型 - 标准拦截机设计（红色）
    
    // 阴影效果
    ctx.fillStyle = 'rgba(120, 20, 20, 0.4)'
    ctx.beginPath()
    ctx.moveTo(x + width / 2 + 2, y + height + 2) // 机头阴影
    ctx.lineTo(x + width * 0.1 + 2, y + 2) // 左机身阴影
    ctx.lineTo(x + width * 0.9 + 2, y + 2) // 右机身阴影
    ctx.closePath()
    ctx.fill()
    
    // 主机身
    const bodyGradient = ctx.createLinearGradient(x, y, x, y + height)
    bodyGradient.addColorStop(0, '#cc2020')
    bodyGradient.addColorStop(0.5, '#ff4040')
    bodyGradient.addColorStop(1, '#aa1010')
    ctx.fillStyle = bodyGradient
    
    ctx.beginPath()
    ctx.moveTo(x + width / 2, y + height) // 尖锐机头
    ctx.lineTo(x + width * 0.15, y + height * 0.3) // 左机身
    ctx.lineTo(x + width * 0.1, y) // 左翼根
    ctx.lineTo(x + width * 0.9, y) // 右翼根
    ctx.lineTo(x + width * 0.85, y + height * 0.3) // 右机身
    ctx.closePath()
    ctx.fill()
    
    // 机翼设计
    ctx.fillStyle = '#990000'
    // 左机翼
    ctx.beginPath()
    ctx.moveTo(x + width * 0.1, y)
    ctx.lineTo(x - width * 0.15, y + height * 0.2)
    ctx.lineTo(x + width * 0.05, y + height * 0.4)
    ctx.lineTo(x + width * 0.25, y + height * 0.3)
    ctx.closePath()
    ctx.fill()
    
    // 右机翼
    ctx.beginPath()
    ctx.moveTo(x + width * 0.9, y)
    ctx.lineTo(x + width * 1.15, y + height * 0.2)
    ctx.lineTo(x + width * 0.95, y + height * 0.4)
    ctx.lineTo(x + width * 0.75, y + height * 0.3)
    ctx.closePath()
    ctx.fill()
    
    // 武器挂载点
    ctx.fillStyle = '#660000'
    ctx.fillRect(x + width * 0.2, y + height * 0.15, 3, 8)
    ctx.fillRect(x + width * 0.77, y + height * 0.15, 3, 8)
    
    // 驾驶舱
    ctx.fillStyle = '#ff6666'
    ctx.beginPath()
    ctx.arc(x + width / 2, y + height * 0.6, 4, 0, Math.PI * 2)
    ctx.fill()
    
    ctx.fillStyle = 'rgba(255, 255, 255, 0.6)'
    ctx.beginPath()
    ctx.arc(x + width / 2 - 1, y + height * 0.6 - 1, 2, 0, Math.PI * 2)
    ctx.fill()
    
  } else if (type === 'fast') {
    // 高速型 - 流线型战斗机设计（橙色）
    
    // 阴影效果
    ctx.fillStyle = 'rgba(140, 80, 20, 0.4)'
    ctx.beginPath()
    ctx.moveTo(x + width / 2 + 2, y + height + 2)
    ctx.lineTo(x + width * 0.2 + 2, y + height * 0.4 + 2)
    ctx.lineTo(x + width * 0.05 + 2, y + 2)
    ctx.lineTo(x + width * 0.95 + 2, y + 2)
    ctx.lineTo(x + width * 0.8 + 2, y + height * 0.4 + 2)
    ctx.closePath()
    ctx.fill()
    
    // 主机身 - 更加流线型
    const speedGradient = ctx.createLinearGradient(x, y, x, y + height)
    speedGradient.addColorStop(0, '#ff8800')
    speedGradient.addColorStop(0.3, '#ffaa44')
    speedGradient.addColorStop(0.7, '#ff6600')
    speedGradient.addColorStop(1, '#cc4400')
    ctx.fillStyle = speedGradient
    
    ctx.beginPath()
    ctx.moveTo(x + width / 2, y + height) // 尖锐机头
    ctx.lineTo(x + width * 0.2, y + height * 0.4) // 左机身收缩
    ctx.lineTo(x + width * 0.05, y) // 左后部
    ctx.lineTo(x + width * 0.95, y) // 右后部
    ctx.lineTo(x + width * 0.8, y + height * 0.4) // 右机身收缩
    ctx.closePath()
    ctx.fill()
    
    // 小型前掠翼
    ctx.fillStyle = '#dd5500'
    // 左翼
    ctx.beginPath()
    ctx.moveTo(x + width * 0.25, y + height * 0.25)
    ctx.lineTo(x - width * 0.05, y + height * 0.1)
    ctx.lineTo(x + width * 0.15, y + height * 0.15)
    ctx.closePath()
    ctx.fill()
    
    // 右翼
    ctx.beginPath()
    ctx.moveTo(x + width * 0.75, y + height * 0.25)
    ctx.lineTo(x + width * 1.05, y + height * 0.1)
    ctx.lineTo(x + width * 0.85, y + height * 0.15)
    ctx.closePath()
    ctx.fill()
    
    // 速度线条特效
    ctx.strokeStyle = 'rgba(255, 200, 100, 0.7)'
    ctx.lineWidth = 2
    for (let i = 0; i < 4; i++) {
      const offset = i * width * 0.15
      ctx.beginPath()
      ctx.moveTo(x + width * 0.3 + offset, y - 8 - i * 4)
      ctx.lineTo(x + width * 0.35 + offset, y + height * 0.2 - i * 2)
      ctx.stroke()
    }
    
    // 推进器
    ctx.fillStyle = '#ff4400'
    ctx.beginPath()
    ctx.ellipse(x + width * 0.35, y, 3, 6, 0, 0, Math.PI * 2)
    ctx.fill()
    ctx.beginPath()
    ctx.ellipse(x + width * 0.65, y, 3, 6, 0, 0, Math.PI * 2)
    ctx.fill()
    
    // 推进器内核
    ctx.fillStyle = '#ffaa00'
    ctx.beginPath()
    ctx.ellipse(x + width * 0.35, y, 1, 3, 0, 0, Math.PI * 2)
    ctx.fill()
    ctx.beginPath()
    ctx.ellipse(x + width * 0.65, y, 1, 3, 0, 0, Math.PI * 2)
    ctx.fill()
    
    // 小型驾驶舱
    ctx.fillStyle = '#ffbb66'
    ctx.beginPath()
    ctx.arc(x + width / 2, y + height * 0.5, 3, 0, Math.PI * 2)
    ctx.fill()
    
  } else if (type === 'spread') {
    // 扩散弹型 - 重型轰炸机设计（紫色）
    
    // 阴影效果
    ctx.fillStyle = 'rgba(80, 20, 120, 0.4)'
    ctx.beginPath()
    ctx.moveTo(x + width / 2 + 2, y + height + 2)
    ctx.lineTo(x + width * 0.05 + 2, y + height * 0.2 + 2)
    ctx.lineTo(x + width * 0.15 + 2, y + 2)
    ctx.lineTo(x + width * 0.85 + 2, y + 2)
    ctx.lineTo(x + width * 0.95 + 2, y + height * 0.2 + 2)
    ctx.closePath()
    ctx.fill()
    
    // 主机身 - 更厚重的设计
    const heavyGradient = ctx.createLinearGradient(x, y, x, y + height)
    heavyGradient.addColorStop(0, '#6644cc')
    heavyGradient.addColorStop(0.3, '#8866ff')
    heavyGradient.addColorStop(0.7, '#5533aa')
    heavyGradient.addColorStop(1, '#441188')
    ctx.fillStyle = heavyGradient
    
    ctx.beginPath()
    ctx.moveTo(x + width / 2, y + height) // 机头
    ctx.lineTo(x + width * 0.05, y + height * 0.2) // 左机身
    ctx.lineTo(x + width * 0.15, y) // 左后部
    ctx.lineTo(x + width * 0.85, y) // 右后部
    ctx.lineTo(x + width * 0.95, y + height * 0.2) // 右机身
    ctx.closePath()
    ctx.fill()
    
    // 大型机翼
    ctx.fillStyle = '#553399'
    // 左大翼
    ctx.beginPath()
    ctx.moveTo(x + width * 0.2, y + height * 0.4)
    ctx.lineTo(x - width * 0.2, y + height * 0.15)
    ctx.lineTo(x - width * 0.1, y + height * 0.05)
    ctx.lineTo(x + width * 0.1, y + height * 0.25)
    ctx.lineTo(x + width * 0.3, y + height * 0.5)
    ctx.closePath()
    ctx.fill()
    
    // 右大翼
    ctx.beginPath()
    ctx.moveTo(x + width * 0.8, y + height * 0.4)
    ctx.lineTo(x + width * 1.2, y + height * 0.15)
    ctx.lineTo(x + width * 1.1, y + height * 0.05)
    ctx.lineTo(x + width * 0.9, y + height * 0.25)
    ctx.lineTo(x + width * 0.7, y + height * 0.5)
    ctx.closePath()
    ctx.fill()
    
    // 能量核心脉动效果
    const pulseTime = Date.now() * 0.005
    const pulseIntensity = 0.4 + Math.sin(pulseTime) * 0.3
    const coreGradient = ctx.createRadialGradient(
      x + width / 2, y + height * 0.3, 0,
      x + width / 2, y + height * 0.3, 12
    )
    coreGradient.addColorStop(0, `rgba(200, 100, 255, ${pulseIntensity})`)
    coreGradient.addColorStop(0.6, `rgba(150, 50, 255, ${pulseIntensity * 0.6})`)
    coreGradient.addColorStop(1, 'rgba(100, 0, 200, 0)')
    ctx.fillStyle = coreGradient
    ctx.beginPath()
    ctx.arc(x + width / 2, y + height * 0.3, 12, 0, Math.PI * 2)
    ctx.fill()
    
    // 能量核心
    ctx.fillStyle = '#aa66ff'
    ctx.beginPath()
    ctx.arc(x + width / 2, y + height * 0.3, 6, 0, Math.PI * 2)
    ctx.fill()
    
    ctx.fillStyle = '#cc88ff'
    ctx.beginPath()
    ctx.arc(x + width / 2, y + height * 0.3, 3, 0, Math.PI * 2)
    ctx.fill()
    
    // 多管武器系统
    ctx.fillStyle = '#442288'
    for (let i = 0; i < 3; i++) {
      const weaponX = x + width * (0.25 + i * 0.25)
      ctx.fillRect(weaponX - 1, y + height * 0.6, 2, 12)
    }
    
    // 装甲板纹理
    ctx.strokeStyle = '#7755bb'
    ctx.lineWidth = 1
    ctx.setLineDash([2, 2])
    ctx.beginPath()
    ctx.moveTo(x + width * 0.2, y + height * 0.1)
    ctx.lineTo(x + width * 0.8, y + height * 0.1)
    ctx.stroke()
    
    ctx.beginPath()
    ctx.moveTo(x + width * 0.25, y + height * 0.7)
    ctx.lineTo(x + width * 0.75, y + height * 0.7)
    ctx.stroke()
    ctx.setLineDash([])
    
    // 大型驾驶舱
    ctx.fillStyle = '#9966dd'
    ctx.beginPath()
    ctx.ellipse(x + width / 2, y + height * 0.5, 6, 4, 0, 0, Math.PI * 2)
    ctx.fill()
    
    ctx.fillStyle = 'rgba(255, 255, 255, 0.5)'
    ctx.beginPath()
    ctx.ellipse(x + width / 2 - 2, y + height * 0.5 - 1, 3, 2, 0, 0, Math.PI * 2)
    ctx.fill()
  }
  
  ctx.restore()
}

/**
 * 绘制增益道具 - 科幻风格，包含粒子特效、能量场和动态光效
 */
function drawPowerUp(x: number, y: number, width: number, height: number, powerUpType: 'spreadShot' | 'shield' = 'spreadShot') {
  ctx.save()
  
  // 计算与玩家的距离用于吸附效果
  const playerCenterX = player.x + player.width / 2
  const playerCenterY = player.y + player.height / 2
  const powerUpCenterX = x + width / 2
  const powerUpCenterY = y + height / 2
  
  const distance = Math.sqrt(
    Math.pow(powerUpCenterX - playerCenterX, 2) + 
    Math.pow(powerUpCenterY - playerCenterY, 2)
  )
  
  const attractRange = 120
  const isInAttractRange = distance <= attractRange
  const time = Date.now() * 0.01
  
  // 根据道具类型设置颜色和特效参数
  let baseColor, secondaryColor, pulseSpeed, iconSymbol
  if (powerUpType === 'shield') {
    baseColor = { r: 255, g: 215, b: 0 }     // 金色
    secondaryColor = { r: 255, g: 165, b: 0 } // 橙金色
    pulseSpeed = 0.006
    iconSymbol = '🛡️'
  } else {
    baseColor = { r: 0, g: 255, b: 255 }     // 青色
    secondaryColor = { r: 0, g: 150, b: 255 } // 蓝青色
    pulseSpeed = 0.008
    iconSymbol = '⚡'
  }
  
  // 外部能量场 - 多层光圈
  for (let i = 3; i >= 1; i--) {
    const ringRadius = (width / 2) + (15 * i) + Math.sin(time * 2 + i) * 5
    const alpha = (0.15 - i * 0.03) * (isInAttractRange ? 2 : 1)
    
    const ringGradient = ctx.createRadialGradient(
      powerUpCenterX, powerUpCenterY, ringRadius * 0.8,
      powerUpCenterX, powerUpCenterY, ringRadius
    )
    ringGradient.addColorStop(0, `rgba(${baseColor.r}, ${baseColor.g}, ${baseColor.b}, 0)`)
    ringGradient.addColorStop(0.8, `rgba(${baseColor.r}, ${baseColor.g}, ${baseColor.b}, ${alpha})`)
    ringGradient.addColorStop(1, `rgba(${baseColor.r}, ${baseColor.g}, ${baseColor.b}, 0)`)
    
    ctx.fillStyle = ringGradient
    ctx.beginPath()
    ctx.arc(powerUpCenterX, powerUpCenterY, ringRadius, 0, Math.PI * 2)
    ctx.fill()
  }
  
  // 吸附效果 - 能量流束
  if (isInAttractRange) {
    const attractionFactor = (attractRange - distance) / attractRange
    const streamCount = 8
    
    for (let i = 0; i < streamCount; i++) {
      const angle = (i / streamCount) * Math.PI * 2 + time * 0.5
      const streamLength = 30 + attractionFactor * 20
      const startX = powerUpCenterX + Math.cos(angle) * (width / 2 + 5)
      const startY = powerUpCenterY + Math.sin(angle) * (height / 2 + 5)
      const endX = startX + Math.cos(angle) * streamLength
      const endY = startY + Math.sin(angle) * streamLength
      
      const streamGradient = ctx.createLinearGradient(startX, startY, endX, endY)
      streamGradient.addColorStop(0, `rgba(255, 255, 100, ${attractionFactor * 0.8})`)
      streamGradient.addColorStop(1, 'rgba(255, 255, 100, 0)')
      
      ctx.strokeStyle = streamGradient
      ctx.lineWidth = 2 + attractionFactor * 2
      ctx.beginPath()
      ctx.moveTo(startX, startY)
      ctx.lineTo(endX, endY)
      ctx.stroke()
    }
  }
  
  // 六边形外框 - 科幻风格
  const hexRadius = width / 2 + 8
  const hexPoints = []
  for (let i = 0; i < 6; i++) {
    const angle = (i / 6) * Math.PI * 2 + time * 0.3
    hexPoints.push({
      x: powerUpCenterX + Math.cos(angle) * hexRadius,
      y: powerUpCenterY + Math.sin(angle) * hexRadius
    })
  }
  
  ctx.strokeStyle = isInAttractRange ? '#ffff00' : `rgba(${baseColor.r}, ${baseColor.g}, ${baseColor.b}, 0.8)`
  ctx.lineWidth = 2
  ctx.setLineDash([5, 3])
  ctx.lineDashOffset = time * 2
  ctx.beginPath()
  ctx.moveTo(hexPoints[0].x, hexPoints[0].y)
  for (let i = 1; i < hexPoints.length; i++) {
    ctx.lineTo(hexPoints[i].x, hexPoints[i].y)
  }
  ctx.closePath()
  ctx.stroke()
  ctx.setLineDash([])
  
  // 主体核心 - 立体球体
  const coreGradient = ctx.createRadialGradient(
    powerUpCenterX - width * 0.15, powerUpCenterY - height * 0.15, 0,
    powerUpCenterX, powerUpCenterY, width / 2
  )
  coreGradient.addColorStop(0, `rgba(255, 255, 255, 0.9)`)
  coreGradient.addColorStop(0.3, `rgba(${baseColor.r}, ${baseColor.g}, ${baseColor.b}, 0.8)`)
  coreGradient.addColorStop(0.7, `rgba(${secondaryColor.r}, ${secondaryColor.g}, ${secondaryColor.b}, 0.6)`)
  coreGradient.addColorStop(1, `rgba(${Math.floor(baseColor.r * 0.3)}, ${Math.floor(baseColor.g * 0.3)}, ${Math.floor(baseColor.b * 0.3)}, 0.4)`)
  
  ctx.fillStyle = coreGradient
  ctx.beginPath()
  ctx.arc(powerUpCenterX, powerUpCenterY, width / 2, 0, Math.PI * 2)
  ctx.fill()
  
  // 内部旋转能量环
  const ringCount = 3
  for (let ring = 0; ring < ringCount; ring++) {
    const ringRadius = (width / 4) + ring * 3
    const rotationSpeed = (ring + 1) * 0.02
    const segmentCount = 6 + ring * 2
    
    for (let seg = 0; seg < segmentCount; seg++) {
      const angle = (seg / segmentCount) * Math.PI * 2 + time * rotationSpeed
      const segX = powerUpCenterX + Math.cos(angle) * ringRadius
      const segY = powerUpCenterY + Math.sin(angle) * ringRadius
      const segRadius = 2 - ring * 0.3
      
      const segmentAlpha = 0.6 - ring * 0.15
      ctx.fillStyle = `rgba(${baseColor.r}, ${baseColor.g}, ${baseColor.b}, ${segmentAlpha})`
      ctx.beginPath()
      ctx.arc(segX, segY, segRadius, 0, Math.PI * 2)
      ctx.fill()
    }
  }
  
  // 动态脉冲核心
  const pulseIntensity = 0.7 + Math.sin(time * pulseSpeed * 100) * 0.3
  const pulseRadius = width / 4 * pulseIntensity
  
  const pulseGradient = ctx.createRadialGradient(
    powerUpCenterX, powerUpCenterY, 0,
    powerUpCenterX, powerUpCenterY, pulseRadius
  )
  pulseGradient.addColorStop(0, `rgba(255, 255, 255, ${pulseIntensity})`)
  pulseGradient.addColorStop(0.5, `rgba(${baseColor.r}, ${baseColor.g}, ${baseColor.b}, ${pulseIntensity * 0.6})`)
  pulseGradient.addColorStop(1, 'rgba(255, 255, 255, 0)')
  
  ctx.fillStyle = pulseGradient
  ctx.beginPath()
  ctx.arc(powerUpCenterX, powerUpCenterY, pulseRadius, 0, Math.PI * 2)
  ctx.fill()
  
  // 粒子特效系统
  const particleCount = isInAttractRange ? 12 : 8
  for (let i = 0; i < particleCount; i++) {
    const particleAngle = (i / particleCount) * Math.PI * 2 + time * 0.8
    const particleDistance = (width / 2) + 15 + Math.sin(time * 3 + i) * 10
    const particleX = powerUpCenterX + Math.cos(particleAngle) * particleDistance
    const particleY = powerUpCenterY + Math.sin(particleAngle) * particleDistance
    const particleSize = 2 + Math.sin(time * 4 + i * 0.5) * 1
    
    const particleGradient = ctx.createRadialGradient(
      particleX, particleY, 0,
      particleX, particleY, particleSize * 2
    )
    particleGradient.addColorStop(0, `rgba(${baseColor.r}, ${baseColor.g}, ${baseColor.b}, 0.8)`)
    particleGradient.addColorStop(1, `rgba(${baseColor.r}, ${baseColor.g}, ${baseColor.b}, 0)`)
    
    ctx.fillStyle = particleGradient
    ctx.beginPath()
    ctx.arc(particleX, particleY, particleSize, 0, Math.PI * 2)
    ctx.fill()
  }
  
  // 科幻图标显示
  ctx.fillStyle = '#ffffff'
  ctx.shadowColor = `rgba(${baseColor.r}, ${baseColor.g}, ${baseColor.b}, 0.8)`
  ctx.shadowBlur = 8
  
  if (powerUpType === 'shield') {
    ctx.font = 'bold 18px Arial'
    ctx.textAlign = 'center'
    ctx.fillText(iconSymbol, powerUpCenterX, powerUpCenterY + 6)
  } else {
    ctx.font = 'bold 20px Arial'
    ctx.textAlign = 'center'
    ctx.strokeStyle = '#000000'
    ctx.lineWidth = 2
    ctx.strokeText(iconSymbol, powerUpCenterX, powerUpCenterY + 7)
    ctx.fillText(iconSymbol, powerUpCenterX, powerUpCenterY + 7)
  }
  
  ctx.shadowBlur = 0
  
  // 吸附状态指示器
  if (isInAttractRange) {
    const attractionFactor = (attractRange - distance) / attractRange
    
    // 动态吸附文字
    ctx.fillStyle = `rgba(255, 255, 0, ${0.8 + Math.sin(time * 8) * 0.2})`
    ctx.font = 'bold 14px Arial'
    ctx.textAlign = 'center'
    ctx.shadowColor = 'rgba(255, 255, 0, 0.5)'
    ctx.shadowBlur = 4
    ctx.fillText('⚡ 能量锁定 ⚡', powerUpCenterX, powerUpCenterY + height / 2 + 20)
    
    // 吸附进度条
    const barWidth = 60
    const barHeight = 6
    const barX = powerUpCenterX - barWidth / 2
    const barY = powerUpCenterY + height / 2 + 30
    
    ctx.fillStyle = 'rgba(0, 0, 0, 0.6)'
    ctx.fillRect(barX, barY, barWidth, barHeight)
    
    ctx.fillStyle = `rgba(255, 255, 0, ${attractionFactor})`
    ctx.fillRect(barX, barY, barWidth * attractionFactor, barHeight)
    
    ctx.strokeStyle = '#ffff00'
    ctx.lineWidth = 1
    ctx.strokeRect(barX, barY, barWidth, barHeight)
    
    ctx.shadowBlur = 0
  }
  
  ctx.restore()
}

/**
 * 绘制弹夹显示
 */
function drawAmmoDisplay() {
  ctx.save()
  
  // 弹夹背景
  const ammoX = gameWidth - 120
  const ammoY = gameHeight - 60
  
  ctx.fillStyle = 'rgba(0, 0, 0, 0.7)'
  ctx.fillRect(ammoX, ammoY, 100, 40)
  
  ctx.strokeStyle = '#ffffff'
  ctx.lineWidth = 2
  ctx.strokeRect(ammoX, ammoY, 100, 40)
  
  // 弹夹文字
  ctx.fillStyle = '#ffffff'
  ctx.font = '14px Arial'
  ctx.textAlign = 'left'
  ctx.fillText('弹夹:', ammoX + 5, ammoY + 18)
  
  // 子弹数量
  if (powerUpActive.value) {
    ctx.fillStyle = '#00ff00'
    ctx.font = 'bold 16px Arial'
    ctx.textAlign = 'right'
    ctx.fillText('∞/∞', ammoX + 95, ammoY + 35)
  } else {
    const ammoColor = currentAmmo.value <= 5 ? '#ff6666' : '#ffffff'
    ctx.fillStyle = ammoColor
    ctx.font = 'bold 16px Arial'
    ctx.textAlign = 'right'
    ctx.fillText(`${currentAmmo.value}/${maxAmmo}`, ammoX + 95, ammoY + 35)
  }
  
  ctx.restore()
}

/**
 * 绘制换弹动画
 */
function drawReloadAnimation() {
  if (!isReloading.value) return
  
  ctx.save()
  
  const progress = (Date.now() - reloadStartTime.value) / reloadDuration
  const centerX = gameWidth / 2
  const centerY = gameHeight / 2
  
  // 换弹进度环
  ctx.strokeStyle = '#ffaa00'
  ctx.lineWidth = 8
  ctx.beginPath()
  ctx.arc(centerX, centerY, 50, -Math.PI / 2, -Math.PI / 2 + progress * Math.PI * 2)
  ctx.stroke()
  
  // 换弹文字
  ctx.fillStyle = '#ffffff'
  ctx.font = 'bold 20px Arial'
  ctx.textAlign = 'center'
  ctx.fillText('换弹中...', centerX, centerY + 5)
  
  // 进度百分比
  ctx.font = '16px Arial'
  ctx.fillText(`${Math.floor(progress * 100)}%`, centerX, centerY + 25)
  
  ctx.restore()
}

/**
 * 绘制增益指示器
 */
function drawPowerUpIndicator() {
  if (!powerUpActive.value) return
  
  ctx.save()
  
  const timeLeft = powerUpEndTime.value - Date.now()
  const progress = timeLeft / powerUpDuration
  
  // 增益背景
  const indicatorX = 20
  const indicatorY = gameHeight - 80
  
  ctx.fillStyle = 'rgba(255, 165, 0, 0.8)'
  ctx.fillRect(indicatorX, indicatorY, 150, 30)
  
  ctx.strokeStyle = '#ffffff'
  ctx.lineWidth = 2
  ctx.strokeRect(indicatorX, indicatorY, 150, 30)
  
  // 增益文字
  ctx.fillStyle = '#ffffff'
  ctx.font = 'bold 14px Arial'
  ctx.textAlign = 'left'
  ctx.fillText('散弹增益 [自动射击]', indicatorX + 5, indicatorY + 20)
  
  // 时间条
  ctx.fillStyle = '#00ff00'
  ctx.fillRect(indicatorX + 5, indicatorY + 25, (150 - 10) * progress, 3)
  
  ctx.restore()
}

/**
 * 绘制游戏计时器和90秒警告
 */
function drawGameTimer() {
  ctx.save()
  
  const gameTimeSeconds = (currentTime.value - gameStartTime.value) / 1000
  const minutes = Math.floor(gameTimeSeconds / 60)
  const seconds = Math.floor(gameTimeSeconds % 60)
  
  // 游戏时间显示
  const timerX = gameWidth / 2 - 50
  const timerY = 30
  
  ctx.fillStyle = 'rgba(0, 0, 0, 0.6)'
  ctx.fillRect(timerX - 20, timerY - 20, 140, 35)
  
  ctx.strokeStyle = '#ffffff'
  ctx.lineWidth = 1
  ctx.strokeRect(timerX - 20, timerY - 20, 140, 35)
  
  ctx.fillStyle = '#ffffff'
  ctx.font = '16px Arial'
  ctx.textAlign = 'center'
  ctx.fillText(`时间: ${minutes}:${seconds.toString().padStart(2, '0')}`, timerX + 50, timerY)
  
  // 90秒警告显示
  if (gameTimeSeconds >= 85 && gameTimeSeconds < 95) {
    // 闪烁警告
    const blinkIntensity = Math.sin(Date.now() * 0.01) * 0.5 + 0.5
    const warningY = timerY + 50
    
    ctx.fillStyle = `rgba(255, 100, 100, ${0.8 * blinkIntensity})`
    ctx.fillRect(timerX - 40, warningY - 20, 180, 40)
    
    ctx.strokeStyle = '#ff0000'
    ctx.lineWidth = 2
    ctx.strokeRect(timerX - 40, warningY - 20, 180, 40)
    
    ctx.fillStyle = '#ffffff'
    ctx.font = 'bold 14px Arial'
    ctx.fillText('警告：敌机即将启动机动模式！', timerX + 50, warningY - 5)
    
    const timeToActivation = Math.max(0, 90 - gameTimeSeconds)
    ctx.fillText(`倒计时: ${timeToActivation.toFixed(1)}秒`, timerX + 50, warningY + 10)
  } else if (gameTimeSeconds >= 90) {
    // 已激活提示
    const statusY = timerY + 50
    
    ctx.fillStyle = 'rgba(255, 150, 0, 0.8)'
    ctx.fillRect(timerX - 30, statusY - 15, 160, 25)
    
    ctx.strokeStyle = '#ff8800'
    ctx.lineWidth = 2
    ctx.strokeRect(timerX - 30, statusY - 15, 160, 25)
    
    ctx.fillStyle = '#ffffff'
    ctx.font = 'bold 12px Arial'
    ctx.fillText('敌机机动模式已激活！', timerX + 50, statusY)
  }
  
  ctx.restore()
}

/**
 * 绘制速度指示器
 */
function drawSpeedIndicator() {
  ctx.save()
  
  const totalSpeed = Math.sqrt(player.vx * player.vx + player.vy * player.vy)
  const maxIndicatorSpeed = 7 // 对应最大速度
  const speedPercent = Math.min(totalSpeed / maxIndicatorSpeed, 1)
  
  // 速度计位置
  const indicatorX = 20
  const indicatorY = 60
  const indicatorSize = 80
  
  // 背景圆环
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.3)'
  ctx.lineWidth = 3
  ctx.beginPath()
  ctx.arc(indicatorX + indicatorSize / 2, indicatorY + indicatorSize / 2, indicatorSize / 2 - 5, 0, Math.PI * 2)
  ctx.stroke()
  
  // 速度弧线
  const startAngle = -Math.PI / 2 // 从顶部开始
  const endAngle = startAngle + (Math.PI * 2 * speedPercent)
  
  // 根据速度设置颜色
  let speedColor = '#00ff00' // 绿色 - 低速
  if (speedPercent > 0.5) speedColor = '#ffff00' // 黄色 - 中速
  if (speedPercent > 0.8) speedColor = '#ff6600' // 橙色 - 高速
  
  ctx.strokeStyle = speedColor
  ctx.lineWidth = 4
  ctx.beginPath()
  ctx.arc(indicatorX + indicatorSize / 2, indicatorY + indicatorSize / 2, indicatorSize / 2 - 5, startAngle, endAngle)
  ctx.stroke()
  
  // 方向指针
  if (totalSpeed > 0.1) {
    const angle = Math.atan2(player.vy, player.vx)
    const centerX = indicatorX + indicatorSize / 2
    const centerY = indicatorY + indicatorSize / 2
    const arrowLength = indicatorSize / 2 - 10
    
    ctx.strokeStyle = '#ffffff'
    ctx.lineWidth = 2
    ctx.beginPath()
    ctx.moveTo(centerX, centerY)
    ctx.lineTo(
      centerX + Math.cos(angle) * arrowLength,
      centerY + Math.sin(angle) * arrowLength
    )
    ctx.stroke()
    
    // 箭头头部
    const arrowHeadLength = 8
    const arrowHeadAngle = 0.5
    const arrowTipX = centerX + Math.cos(angle) * arrowLength
    const arrowTipY = centerY + Math.sin(angle) * arrowLength
    
    ctx.beginPath()
    ctx.moveTo(arrowTipX, arrowTipY)
    ctx.lineTo(
      arrowTipX - Math.cos(angle - arrowHeadAngle) * arrowHeadLength,
      arrowTipY - Math.sin(angle - arrowHeadAngle) * arrowHeadLength
    )
    ctx.moveTo(arrowTipX, arrowTipY)
    ctx.lineTo(
      arrowTipX - Math.cos(angle + arrowHeadAngle) * arrowHeadLength,
      arrowTipY - Math.sin(angle + arrowHeadAngle) * arrowHeadLength
    )
    ctx.stroke()
  }
  
  // 文字标签
  ctx.fillStyle = '#ffffff'
  ctx.font = '12px Arial'
  ctx.textAlign = 'center'
  ctx.fillText('速度', indicatorX + indicatorSize / 2, indicatorY + indicatorSize + 15)
  
  // 数值显示
  ctx.font = '10px Arial'
  ctx.fillText(`${(totalSpeed * 10).toFixed(1)}`, indicatorX + indicatorSize / 2, indicatorY + indicatorSize + 30)
  
  ctx.restore()
}

/**
 * 绘制玩家飞船轨迹
 */
function drawPlayerTrail() {
  if (playerTrail.length < 2) return
  
  ctx.save()
  
  const currentTime = Date.now()
  
  // 绘制轨迹线条
  for (let i = 1; i < playerTrail.length; i++) {
    const point = playerTrail[i]
    const prevPoint = playerTrail[i - 1]
    const age = currentTime - point.time
    const alpha = Math.max(0, 1 - age / 300) // 随时间淡化
    
    // 计算线条粗细（越新越粗）
    const thickness = 1 + alpha * 2
    
    ctx.strokeStyle = `rgba(100, 200, 255, ${alpha * 0.8})`
    ctx.lineWidth = thickness
    ctx.lineCap = 'round'
    
    ctx.beginPath()
    ctx.moveTo(prevPoint.x, prevPoint.y)
    ctx.lineTo(point.x, point.y)
    ctx.stroke()
  }
  
  // 绘制轨迹粒子效果
  playerTrail.forEach((point, index) => {
    const age = currentTime - point.time
    const alpha = Math.max(0, 1 - age / 300)
    const size = 1 + alpha * 2
    
    if (index % 2 === 0) { // 每隔一个点绘制粒子
      ctx.fillStyle = `rgba(150, 220, 255, ${alpha * 0.6})`
      ctx.beginPath()
      ctx.arc(point.x, point.y, size, 0, Math.PI * 2)
      ctx.fill()
    }
  })
  
  ctx.restore()
}

/**
 * 游戏主循环
 */
function gameLoop() {
  if (isPaused.value || gameOver.value) return
  
  // 更新当前时间
  currentTime.value = Date.now()
  
  updatePlayer()
  spawnEnemy()
  // spawnPowerUp() // 移除定时道具生成，改为击毁敌机掉落
  updateGameObjects()
  updateShieldWaves()
  updatePowerUpPickupEffects() // 更新道具拾取特效
  updateKillEffects() // 更新击杀特效
  checkCollisions()
  render()
  
  animationId = requestAnimationFrame(gameLoop)
}

/**
 * 监听props变化
 */
watch(() => props.visible, (newVal: boolean) => {
  isVisible.value = newVal
  if (newVal) {
    nextTick(() => {
      gameCanvas.value?.focus()
    })
  }
})

/**
 * 组件生命周期
 */
onMounted(() => {
  // 添加全局键盘事件监听
  document.addEventListener('keydown', handleKeyDown)
  document.addEventListener('keyup', handleKeyUp)
  // 添加全局鼠标事件监听
  document.addEventListener('mousedown', handleMouseDown)
  document.addEventListener('mouseup', handleMouseUp)
})

onUnmounted(() => {
  // 清理事件监听和动画
  document.removeEventListener('keydown', handleKeyDown)
  document.removeEventListener('keyup', handleKeyUp)
  document.removeEventListener('mousedown', handleMouseDown)
  document.removeEventListener('mouseup', handleMouseUp)
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
})
</script>

<style scoped>
.game-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.game-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(5px);
}

.game-window {
  position: relative;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border-radius: 15px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  border: 2px solid #4a5568;
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: linear-gradient(90deg, #2d3748, #4a5568);
  border-bottom: 1px solid #718096;
}

.game-header h2 {
  color: #e2e8f0;
  margin: 0;
  font-size: 1.2rem;
}

.game-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
  color: #cbd5e0;
}

.score, .lives, .difficulty, .ammo, .game-time {
  font-weight: bold;
  font-size: 0.9rem;
}

.game-time {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.2rem;
}

.game-time.time-warning {
  color: #ffa500;
}

.intensity-warning {
  color: #ff6b6b;
  font-size: 0.8rem;
  animation: blink 1s infinite;
}

.intensity-active {
  color: #ff4444;
  font-size: 0.8rem;
  font-weight: bold;
  animation: pulse-red 0.8s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

@keyframes pulse-red {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.05); }
}

/* 血量条样式 */
.health-bar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 1.5rem;
  background: rgba(0, 0, 0, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 0;
  margin: 0 1rem;
  transition: transform 0.1s ease;
  position: relative;
  clip-path: polygon(10px 0%, 100% 0%, calc(100% - 10px) 100%, 0% 100%);
}

.health-bar-container.shake {
  animation: shake 0.5s ease-in-out;
}

.health-bar {
  display: flex;
  gap: 3px;
  padding: 6px;
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  clip-path: polygon(8px 0%, 100% 0%, calc(100% - 8px) 100%, 0% 100%);
}

.health-segment {
  width: 45px;
  height: 14px;
  transition: all 0.3s ease;
  position: relative;
  clip-path: polygon(4px 0%, 100% 0%, calc(100% - 4px) 100%, 0% 100%);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.health-segment.active {
  background: linear-gradient(45deg, #00ff88, #00ffaa, #00cc66);
  box-shadow: 
    0 0 12px rgba(0, 255, 136, 0.6),
    inset 0 0 8px rgba(255, 255, 255, 0.2);
  border: 1px solid #00ffaa;
}

.health-segment.active.critical {
  background: linear-gradient(45deg, #ff4444, #ff6666, #cc0000);
  box-shadow: 
    0 0 16px rgba(255, 68, 68, 0.9),
    inset 0 0 8px rgba(255, 255, 255, 0.3);
  border: 1px solid #ff6666;
  animation: pulse-critical 0.8s infinite;
}

.health-segment.damaged {
  background: linear-gradient(45deg, rgba(100, 100, 100, 0.2), rgba(60, 60, 60, 0.4));
  box-shadow: inset 0 0 4px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(100, 100, 100, 0.3);
}

.health-text {
  color: #e2e8f0;
  font-size: 0.9rem;
  font-weight: bold;
  letter-spacing: 1px;
}

@keyframes shake {
  0%, 100% { transform: translateX(0) rotate(0deg); }
  10% { transform: translateX(-8px) rotate(-2deg); }
  20% { transform: translateX(8px) rotate(2deg); }
  30% { transform: translateX(-6px) rotate(-1deg); }
  40% { transform: translateX(6px) rotate(1deg); }
  50% { transform: translateX(-4px) rotate(-0.5deg); }
  60% { transform: translateX(4px) rotate(0.5deg); }
  70% { transform: translateX(-2px) rotate(-0.2deg); }
  80% { transform: translateX(2px) rotate(0.2deg); }
  90% { transform: translateX(-1px) rotate(-0.1deg); }
}

@keyframes pulse-critical {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(1.1); }
}

.ammo {
  transition: color 0.3s;
}

.ammo.low-ammo {
  color: #ff6b6b;
  animation: pulse 1s infinite;
}

.reloading {
  color: #ffa500;
  font-style: italic;
}

.infinite-ammo {
  color: #00ff00;
  font-weight: bold;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.pause-btn, .close-btn {
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.pause-btn:hover, .close-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.close-btn {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  padding: 0.5rem 0.8rem;
  font-size: 1.2rem;
  font-weight: bold;
}

.game-canvas {
  display: block;
  border: none;
  outline: none;
  background: #000011;
}

.game-status-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.status-content {
  text-align: center;
  padding: 2rem;
  background: rgba(26, 26, 46, 0.9);
  border-radius: 10px;
  border: 1px solid #4a5568;
}

.status-content h3 {
  margin-bottom: 1rem;
  color: #e2e8f0;
  font-size: 1.5rem;
}

.status-content p {
  margin-bottom: 1.5rem;
  color: #cbd5e0;
}

.start-btn, .resume-btn, .restart-btn {
  padding: 1rem 2rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1.1rem;
  font-weight: bold;
  transition: all 0.3s;
}

.start-btn:hover, .resume-btn:hover, .restart-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

/* 难度选择样式 */
.difficulty-screen {
  max-width: 800px;
  width: 100%;
}

.difficulty-options {
  display: flex;
  gap: 2rem;
  justify-content: center;
  margin-top: 2rem;
}

.difficulty-card {
  background: linear-gradient(135deg, rgba(26, 32, 44, 0.9), rgba(45, 55, 72, 0.9));
  border: 2px solid transparent;
  border-radius: 15px;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 280px;
  position: relative;
  overflow: hidden;
}

.difficulty-card:hover {
  transform: translateY(-5px);
  border-color: #667eea;
  box-shadow: 0 15px 35px rgba(102, 126, 234, 0.3);
}

.difficulty-card:before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.5s;
}

.difficulty-card:hover:before {
  left: 100%;
}

.difficulty-card h4 {
  margin-bottom: 1.5rem;
  font-size: 1.3rem;
  text-align: center;
}

.difficulty-features {
  margin-bottom: 1.5rem;
}

.difficulty-features p {
  margin: 0.5rem 0;
  text-align: left;
  font-size: 0.9rem;
  color: #a0aec0;
}

.difficulty-btn {
  width: 100%;
  padding: 0.8rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.easy-btn {
  background: linear-gradient(135deg, #48bb78, #38a169);
  color: white;
}

.easy-btn:hover {
  background: linear-gradient(135deg, #38a169, #2f855a);
  transform: translateY(-1px);
}

.hard-btn {
  background: linear-gradient(135deg, #f56565, #e53e3e);
  color: white;
}

.hard-btn:hover {
  background: linear-gradient(135deg, #e53e3e, #c53030);
  transform: translateY(-1px);
}

.start-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-top: 1rem;
}

.back-btn {
  padding: 1rem 2rem;
  background: linear-gradient(135deg, #718096, #4a5568);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: bold;
  transition: all 0.3s;
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(113, 128, 150, 0.4);
}

.ammo-easy {
  color: #48bb78;
  font-weight: bold;
}
</style>
