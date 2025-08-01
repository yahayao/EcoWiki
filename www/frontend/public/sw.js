/**
 * Service Worker for EcoWiki
 * 提供离线缓存和性能优化
 */

const CACHE_NAME = 'ecowiki-cache-v1'
const STATIC_CACHE_NAME = 'ecowiki-static-v1'
const DYNAMIC_CACHE_NAME = 'ecowiki-dynamic-v1'

// 需要缓存的静态资源
const STATIC_ASSETS = [
  '/',
  '/index.html',
  '/manifest.json',
  '/static/css/main.css',
  '/static/js/main.js',
  '/static/images/logo.png'
]

// 需要缓存的 API 路径模式
const CACHE_API_PATTERNS = [
  /^\/api\/articles/,
  /^\/api\/categories/,
  /^\/api\/tags/
]

// 需要网络优先的 API 路径
const NETWORK_FIRST_PATTERNS = [
  /^\/api\/auth/,
  /^\/api\/users\/me/,
  /^\/api\/drafts/
]

// 安装事件 - 缓存静态资源
self.addEventListener('install', (event) => {
  console.log('Service Worker installing...')
  
  event.waitUntil(
    caches.open(STATIC_CACHE_NAME)
      .then((cache) => {
        console.log('Caching static assets')
        return cache.addAll(STATIC_ASSETS)
      })
      .then(() => {
        return self.skipWaiting()
      })
  )
})

// 激活事件 - 清理旧缓存
self.addEventListener('activate', (event) => {
  console.log('Service Worker activating...')
  
  event.waitUntil(
    caches.keys()
      .then((cacheNames) => {
        return Promise.all(
          cacheNames
            .filter((cacheName) => {
              return cacheName !== STATIC_CACHE_NAME && 
                     cacheName !== DYNAMIC_CACHE_NAME
            })
            .map((cacheName) => {
              console.log('Deleting old cache:', cacheName)
              return caches.delete(cacheName)
            })
        )
      })
      .then(() => {
        return self.clients.claim()
      })
  )
})

// 获取事件 - 缓存策略
self.addEventListener('fetch', (event) => {
  const { request } = event
  const url = new URL(request.url)

  // 只处理 GET 请求
  if (request.method !== 'GET') {
    return
  }

  // 静态资源：缓存优先
  if (STATIC_ASSETS.some(asset => url.pathname.endsWith(asset))) {
    event.respondWith(cacheFirst(request, STATIC_CACHE_NAME))
    return
  }

  // API 请求处理
  if (url.pathname.startsWith('/api/')) {
    // 需要网络优先的 API
    if (NETWORK_FIRST_PATTERNS.some(pattern => pattern.test(url.pathname))) {
      event.respondWith(networkFirst(request, DYNAMIC_CACHE_NAME))
      return
    }
    
    // 可缓存的 API
    if (CACHE_API_PATTERNS.some(pattern => pattern.test(url.pathname))) {
      event.respondWith(staleWhileRevalidate(request, DYNAMIC_CACHE_NAME))
      return
    }
    
    // 其他 API 使用网络优先
    event.respondWith(networkFirst(request, DYNAMIC_CACHE_NAME))
    return
  }

  // 导航请求：网络优先，回退到缓存
  if (request.mode === 'navigate') {
    event.respondWith(networkFirst(request, DYNAMIC_CACHE_NAME))
    return
  }

  // 其他资源：过期时重新验证
  event.respondWith(staleWhileRevalidate(request, DYNAMIC_CACHE_NAME))
})

/**
 * 缓存优先策略
 */
async function cacheFirst(request, cacheName) {
  try {
    const cache = await caches.open(cacheName)
    const cachedResponse = await cache.match(request)
    
    if (cachedResponse) {
      return cachedResponse
    }
    
    const networkResponse = await fetch(request)
    
    if (networkResponse.ok) {
      cache.put(request, networkResponse.clone())
    }
    
    return networkResponse
  } catch (error) {
    console.error('Cache first strategy failed:', error)
    return new Response('网络错误', { 
      status: 408,
      headers: { 'Content-Type': 'text/plain; charset=utf-8' }
    })
  }
}

/**
 * 网络优先策略
 */
async function networkFirst(request, cacheName) {
  try {
    const networkResponse = await fetch(request)
    
    if (networkResponse.ok) {
      const cache = await caches.open(cacheName)
      cache.put(request, networkResponse.clone())
    }
    
    return networkResponse
  } catch (error) {
    console.log('Network failed, trying cache:', error)
    
    const cache = await caches.open(cacheName)
    const cachedResponse = await cache.match(request)
    
    if (cachedResponse) {
      return cachedResponse
    }
    
    return new Response('离线状态', {
      status: 503,
      headers: { 'Content-Type': 'text/plain; charset=utf-8' }
    })
  }
}

/**
 * 过期时重新验证策略
 */
async function staleWhileRevalidate(request, cacheName) {
  const cache = await caches.open(cacheName)
  const cachedResponse = await cache.match(request)
  
  // 异步更新缓存
  const networkResponsePromise = fetch(request)
    .then((networkResponse) => {
      if (networkResponse.ok) {
        cache.put(request, networkResponse.clone())
      }
      return networkResponse
    })
    .catch((error) => {
      console.log('Background fetch failed:', error)
      return null
    })

  // 如果有缓存，立即返回；否则等待网络响应
  if (cachedResponse) {
    return cachedResponse
  }
  
  return networkResponsePromise || new Response('数据不可用', {
    status: 503,
    headers: { 'Content-Type': 'text/plain; charset=utf-8' }
  })
}

/**
 * 处理缓存清理
 */
self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'CLEAR_CACHE') {
    event.waitUntil(
      caches.keys().then((cacheNames) => {
        return Promise.all(
          cacheNames.map((cacheName) => {
            if (cacheName.startsWith('ecowiki-dynamic')) {
              return caches.delete(cacheName)
            }
          })
        )
      })
    )
    
    event.ports[0].postMessage({ success: true })
  }
})

/**
 * 定期清理过期缓存
 */
function cleanupExpiredCache() {
  caches.open(DYNAMIC_CACHE_NAME)
    .then((cache) => {
      return cache.keys()
    })
    .then((requests) => {
      // 这里可以实现更复杂的过期逻辑
      console.log('Cache cleanup completed')
    })
}

// 每小时清理一次缓存
setInterval(cleanupExpiredCache, 60 * 60 * 1000)
