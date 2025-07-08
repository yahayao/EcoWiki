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
          <div class="lives">生命: {{ lives }}</div>
          <div class="difficulty">难度: {{ Math.floor((currentTime - gameStartTime) / 1000 / 10) + 1 }}</div>
          <div class="ammo" :class="{ 'low-ammo': currentAmmo <= 5 && !powerUpActive }">
            弹夹: 
            <span v-if="powerUpActive" class="infinite-ammo">∞/∞</span>
            <span v-else>{{ currentAmmo }}/{{ maxAmmo }}</span>
            <span v-if="isReloading && !powerUpActive" class="reloading">(换弹中)</span>
          </div>
          <button @click="togglePause" class="pause-btn">
            {{ isPaused ? '继续' : '暂停' }}
          </button>
          <button @click="closeGame" class="close-btn">×</button>
        </div>
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
      <div v-if="!gameStarted || isPaused || gameOver" class="game-status-overlay">
        <div class="status-content">
          <div v-if="!gameStarted" class="start-screen">
            <h3>太空射击</h3>
            <p>使用WASD移动，鼠标左键射击</p>
            <p style="font-size: 0.9rem; color: #ffa500;">💡 只有击中红色中心点才会减少生命</p>
            <p style="font-size: 0.8rem; color: #00ffff;">🎁 拾取蓝色增益球获得自动散弹射击</p>
            <button @click="startGame" class="start-btn">开始游戏</button>
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
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'

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
const isPaused = ref(false)
const gameOver = ref(false)
const score = ref(0)
const lives = ref(3)

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
 * 游戏难度和时间管理
 */
const gameStartTime = ref(0)
const currentTime = ref(0)

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
  movePattern?: {
    type: 'sine' | 'zigzag'
    amplitude: number
    frequency: number
    phase: number
    originalX: number
  }
}

interface SpreadBullet extends Bullet {
  parentBullet?: boolean
  spreadTime?: number
  hasSpread?: boolean
  spreadDistance?: number
}

interface PowerUp extends GameObject {
  active: boolean
  type: 'spreadShot'
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
 * 输入控制
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
  lives.value = 3
  lastEnemySpawn = 0
  lastPowerUpSpawn = 0
  gameStartTime.value = Date.now()
  currentTime.value = Date.now()
  
  // 重置弹夹和增益状态
  currentAmmo.value = maxAmmo
  isReloading.value = false
  powerUpActive.value = false
  
  // 清空玩家轨迹
  playerTrail = []
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
  startGame()
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
    case 'Space':
      keys.value.space = true
      event.preventDefault()
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
    case 'Space':
      keys.value.space = false
      break
  }
}

/**
 * 鼠标事件处理
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
  
  // 边界检查
  if (player.x < 0) {
    player.x = 0
    player.vx *= -0.2 // 减少反弹效果
  }
  if (player.x > gameWidth - player.width) {
    player.x = gameWidth - player.width
    player.vx *= -0.2
  }
  if (player.y < 0) {
    player.y = 0
    player.vy *= -0.2
  }
  if (player.y > gameHeight - player.height) {
    player.y = gameHeight - player.height
    player.vy *= -0.2
  }
  
  // 检查是否需要自动换弹（增益状态下不换弹）
  if (currentAmmo.value <= 0 && !isReloading.value && !powerUpActive.value) {
    startReload()
  }
  
  // 检查换弹是否完成
  if (isReloading.value && Date.now() - reloadStartTime.value >= reloadDuration) {
    finishReload()
  }
  
  // 增益状态下的自动射击
  if (powerUpActive.value && Date.now() < powerUpEndTime.value) {
    if (Date.now() - player.lastShot > 150) { // 自动射击间隔150ms，让散弹效果更明显
      shootWithPowerUp()
    }
  }
  
  // 手动射击 - 使用鼠标左键（增益状态下禁止手动射击）
  if (mousePressed.value && !isReloading.value && !powerUpActive.value && Date.now() - player.lastShot > 200) {
    // 只有非增益状态下才能手动射击
    shoot()
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
 * 射击函数（普通）
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
 * 生成增益道具
 */
function spawnPowerUp() {
  currentTime.value = Date.now()
  
  // 每20秒生成一个增益道具
  if (currentTime.value - lastPowerUpSpawn > 20000) {
    powerUps.push({
      x: Math.random() * (gameWidth - 30),
      y: -30,
      width: 30,
      height: 30,
      vx: 0,
      vy: 2,
      color: '#00ffff',
      active: true,
      type: 'spreadShot',
      floatOffset: Math.random() * Math.PI * 2,
      curveSpeed: 0.02 + Math.random() * 0.02
    })
    lastPowerUpSpawn = currentTime.value
  }
}
function spawnEnemy() {
  currentTime.value = Date.now()
  const gameTimeSeconds = (currentTime.value - gameStartTime.value) / 1000
  
  // 随时间递增的难度
  const difficultyMultiplier = 1 + gameTimeSeconds / 30 // 每30秒增加1倍难度
  const baseSpeed = 2
  const enemySpeed = baseSpeed * Math.min(difficultyMultiplier, 4) // 最大4倍速度
  
  // 敌人生成频率随时间增加（最小间隔200ms）
  const baseSpawnInterval = 1000
  const spawnInterval = Math.max(200, baseSpawnInterval / difficultyMultiplier)
  
  if (currentTime.value - lastEnemySpawn > spawnInterval) {
    // 敌人尺寸更大
    const enemySize = 45 // 比之前的30更大
    
    // 根据游戏时间决定敌人类型概率
    const random = Math.random()
    let enemyType: 'normal' | 'fast' | 'spread' = 'normal'
    let enemyColor = '#ff0000'
    let enemyVy = enemySpeed
    
    if (gameTimeSeconds > 20) { // 20秒后开始出现新类型敌人
      if (random < 0.2) { // 20%概率生成高速型
        enemyType = 'fast'
        enemyColor = '#ff8800' // 橙色
        enemyVy = enemySpeed * 2.5 // 高速飞行
      } else if (random < 0.4) { // 20%概率生成扩散弹型
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
    
    // 90秒后启用左右移动模式
    if (gameTimeSeconds > 90) {
      // 为每个敌人添加左右移动逻辑
      if (!enemy.movePattern) {
        // 初始化移动模式
        enemy.movePattern = {
          type: Math.random() < 0.5 ? 'sine' : 'zigzag',
          amplitude: 15 + Math.random() * 25, // 移动幅度 (减小幅度)
          frequency: 0.008 + Math.random() * 0.012, // 移动频率 (降低频率让移动更慢)
          phase: Math.random() * Math.PI * 2, // 初始相位
          originalX: enemy.x // 记录初始X位置
        }
      }
      
      // 根据移动模式计算水平位置
      const timeFactor = currentTime.value * enemy.movePattern.frequency + enemy.movePattern.phase
      
      if (enemy.movePattern.type === 'sine') {
        // 正弦波移动
        enemy.x = enemy.movePattern.originalX + Math.sin(timeFactor) * enemy.movePattern.amplitude
      } else {
        // 锯齿波移动
        const zigzag = ((timeFactor % (Math.PI * 2)) / (Math.PI * 2)) * 2 - 1
        enemy.x = enemy.movePattern.originalX + zigzag * enemy.movePattern.amplitude
      }
      
      // 边界限制
      if (enemy.x < 0) {
        enemy.x = 0
        enemy.movePattern.originalX = 0
      }
      if (enemy.x > gameWidth - enemy.width) {
        enemy.x = gameWidth - enemy.width
        enemy.movePattern.originalX = gameWidth - enemy.width
      }
    }
    
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
    // 曲线飘落运动
    powerUp.floatOffset += powerUp.curveSpeed
    powerUp.x += Math.sin(powerUp.floatOffset) * 2
    powerUp.y += powerUp.vy
    
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
      
      // 只有击中中心点才减少生命
      if (distance <= centerHitRadius) {
        bullet.active = false
        lives.value--
        if (lives.value <= 0) {
          gameOver.value = true
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
      
      // 只有撞击中心点才减少生命
      if (distance <= centerHitRadius + 10) {
        enemy.active = false
        lives.value--
        if (lives.value <= 0) {
          gameOver.value = true
        }
      }
    }
  })
  
  // 玩家拾取增益道具
  powerUps.forEach(powerUp => {
    if (powerUp.active) {
      const distance = Math.sqrt(
        Math.pow(powerUp.x + powerUp.width / 2 - playerCenterX, 2) + 
        Math.pow(powerUp.y + powerUp.height / 2 - playerCenterY, 2)
      )
      
      if (distance <= 20) {
        powerUp.active = false
        // 激活增益效果
        powerUpActive.value = true
        powerUpEndTime.value = Date.now() + powerUpDuration
      }
    }
  })
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
  
  // 绘制玩家飞机（三角形 + 机翼）
  drawPlayerShip(player.x, player.y, player.width, player.height)
  
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
    drawPowerUp(powerUp.x, powerUp.y, powerUp.width, powerUp.height)
  })
  
  // 绘制UI元素
  drawAmmoDisplay()
  drawReloadAnimation()
  drawPowerUpIndicator()
  drawGameTimer() // 添加游戏计时器
  drawSpeedIndicator() // 添加速度指示器
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
 * 绘制增益道具
 */
function drawPowerUp(x: number, y: number, width: number, height: number) {
  ctx.save()
  
  // 半透明球体效果
  const gradient = ctx.createRadialGradient(
    x + width / 2, y + height / 2, 0,
    x + width / 2, y + height / 2, width / 2
  )
  gradient.addColorStop(0, 'rgba(0, 255, 255, 0.8)')
  gradient.addColorStop(0.7, 'rgba(0, 200, 255, 0.4)')
  gradient.addColorStop(1, 'rgba(0, 150, 255, 0.1)')
  
  ctx.fillStyle = gradient
  ctx.beginPath()
  ctx.arc(x + width / 2, y + height / 2, width / 2, 0, Math.PI * 2)
  ctx.fill()
  
  // 内部发光效果
  const pulseIntensity = 0.5 + Math.sin(Date.now() * 0.01) * 0.3
  ctx.fillStyle = `rgba(255, 255, 255, ${pulseIntensity})`
  ctx.beginPath()
  ctx.arc(x + width / 2, y + height / 2, width / 4, 0, Math.PI * 2)
  ctx.fill()
  
  // 散弹图标
  ctx.fillStyle = '#ffffff'
  ctx.font = '12px Arial'
  ctx.textAlign = 'center'
  ctx.fillText('散', x + width / 2, y + height / 2 + 4)
  
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
  spawnPowerUp()
  updateGameObjects()
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

.score, .lives, .difficulty, .ammo {
  font-weight: bold;
  font-size: 0.9rem;
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
</style>
