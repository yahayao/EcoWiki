<template>
  <div class="game-wrapper" v-show="isVisible">
    <div class="game-overlay" @click="closeGame"></div>

    <div class="game-window">
      <div class="game-header">
        <span>生命: {{ lives }}</span>
        <span>分数: {{ score }}</span>
        <button @click="closeGame">×</button>
      </div>

      <canvas
        ref="canvas"
        :width="W"
        :height="H"
        tabindex="0"
        @keydown="handleKeyDown"
        @keyup="handleKeyUp"
      />
      <!-- 游戏状态遮罩 -->
      <div v-if="!gameStarted || gameOver" class="game-mask">
        <div class="mask-body">
          <h2>{{ gameOver ? '游戏结束' : '横版跑酷' }}</h2>
          <p v-if="gameOver">最终分数：{{ score }}</p>
          <button @click="startGame">{{ gameOver ? '重新开始' : '开始游戏' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

// -------------- 基本配置 --------------
const W = 800, H = 400
const GRAVITY = 0.6
const JUMP = -13
const SPEED = 4
const PLATFORM_H = 20
const GAP_MIN = 80, GAP_MAX = 180
const OBSTACLE_CHANCE = 0.3

// -------------- 响应式状态 --------------
const canvas = ref<HTMLCanvasElement>()
const ctx = ref<CanvasRenderingContext2D>()
const isVisible = ref(true)
const gameStarted = ref(false)
const gameOver = ref(false)
const lives = ref(3)
const score = ref(0)

// -------------- 游戏对象 --------------
interface Rect { x: number; y: number; w: number; h: number }
interface Platform extends Rect {}
interface Obstacle extends Rect {}

let player: Rect & { vy: number; onGround: boolean; canDouble: boolean }
let platforms: Platform[] = []
let obstacles: Obstacle[] = []
let offsetX = 0
let lastPlatformX = 0
let lastObstacleX = 0
let keys: Record<string, boolean> = {}

// -------------- 初始化 --------------
function initGame () {
  player = { x: W * 0.2, y: H / 2, w: 30, h: 30, vy: 0, onGround: false, canDouble: true }
  platforms = [{ x: 0, y: H - 60, w: 300, h: PLATFORM_H }]
  obstacles = []
  offsetX = 0
  lastPlatformX = 300
  lastObstacleX = 600
  lives.value = 3
  score.value = 0
}

// -------------- 生成平台 --------------
function addPlatform () {
  const prev = platforms[platforms.length - 1]
  const x = lastPlatformX + GAP_MIN + Math.random() * (GAP_MAX - GAP_MIN)
  const y = prev.y + (Math.random() - 0.5) * 120
  const w = 100 + Math.random() * 150
  platforms.push({ x, y: Math.min(Math.max(y, H * 0.6), H - PLATFORM_H), w, h: PLATFORM_H })
  lastPlatformX = x + w

  // 随机障碍物
  if (Math.random() < OBSTACLE_CHANCE && x - lastObstacleX > 200) {
    obstacles.push({ x: x + w / 2, y: y - 25, w: 20, h: 25 })
    lastObstacleX = x
  }
}

// -------------- 更新 --------------
function update () {
  if (gameOver.value) return

  // 玩家水平位置固定，世界向左移动
  offsetX += SPEED

  // 分数 = 存活时间
  score.value = Math.floor(offsetX / 10)

  // 生成新平台
  while (lastPlatformX < offsetX + W + 200) addPlatform()

  // 重力
  player.vy += GRAVITY
  player.y += player.vy
  player.onGround = false

  // 与平台碰撞
  for (const p of platforms) {
    if (
      player.x < p.x + p.w &&
      player.x + player.w > p.x &&
      player.y + player.h > p.y &&
      player.y + player.h < p.y + p.h + player.vy
    ) {
      player.y = p.y - player.h
      player.vy = 0
      player.onGround = true
      player.canDouble = true
    }
  }

  // 出界
  if (player.y > H) {
    lives.value--
    if (lives.value <= 0) {
      gameOver.value = true
      return
    }
    // 重置到安全位置
    player.y = H / 2
    player.vy = 0
  }

  // 障碍物碰撞
  for (const o of obstacles) {
    if (
      player.x < o.x + o.w &&
      player.x + player.w > o.x &&
      player.y < o.y + o.h &&
      player.y + player.h > o.y
    ) {
      obstacles = obstacles.filter(ob => ob !== o)
      lives.value--
      if (lives.value <= 0) {
        gameOver.value = true
        return
      }
    }
  }
}

// -------------- 绘制 --------------
function draw () {
  if (!ctx.value) return
  const c = ctx.value
  c.clearRect(0, 0, W, H)
  c.fillStyle = '#111'
  c.fillRect(0, 0, W, H)

  // 星空背景
  c.fillStyle = '#fff'
  for (let i = 0; i < 60; i++) {
    const x = (i * 137 + offsetX * 0.2) % W
    const y = (i * 97) % H
    c.fillRect(x, y, 1, 1)
  }

  // 平台和障碍物
  c.fillStyle = '#0af'
  platforms.forEach(p => c.fillRect(p.x - offsetX, p.y, p.w, p.h))
  c.fillStyle = '#f55'
  obstacles.forEach(o => c.fillRect(o.x - offsetX, o.y, o.w, o.h))

  // 玩家
  c.fillStyle = '#5f5'
  c.fillRect(player.x, player.y, player.w, player.h)
}

// -------------- 主循环 --------------
let id: number
function loop () {
  update()
  draw()
  id = requestAnimationFrame(loop)
}

// -------------- 事件 --------------
function handleKeyDown (e: KeyboardEvent) {
  keys[e.code] = true
  if (e.code === 'Space' || e.code === 'ArrowUp') {
    e.preventDefault()
    if (player.onGround) {
      player.vy = JUMP
      player.onGround = false
    } else if (player.canDouble) {
      player.vy = JUMP * 0.8
      player.canDouble = false
    }
  }
}
function handleKeyUp (e: KeyboardEvent) {
  keys[e.code] = false
}

// -------------- 对外 --------------
function startGame () {
  initGame()
  gameStarted.value = true
  gameOver.value = false
  loop()
  canvas.value?.focus()
}
function closeGame () {
  isVisible.value = false
  if (id) cancelAnimationFrame(id)
}

// -------------- 生命周期 --------------
onMounted(() => {
  ctx.value = canvas.value!.getContext('2d')!
})
onUnmounted(() => {
  if (id) cancelAnimationFrame(id)
})
</script>

<style scoped>
.game-wrapper {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.7);
  z-index: 1000;
}
.game-overlay {
  position: absolute;
  inset: 0;
}
.game-window {
  background: #111;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 0 20px #000;
}
.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 1rem;
  background: #222;
  color: #eee;
}
.game-header button {
  background: none;
  border: none;
  color: #eee;
  font-size: 1.5rem;
  cursor: pointer;
}
canvas {
  display: block;
}
.game-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #eee;
}
.mask-body {
  text-align: center;
}
.mask-body button {
  margin-top: 1rem;
  padding: 0.5rem 1.5rem;
  background: #0af;
  border: none;
  border-radius: 4px;
  color: #fff;
  cursor: pointer;
}
</style>