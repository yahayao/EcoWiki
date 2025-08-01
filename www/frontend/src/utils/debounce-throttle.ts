/**
 * 防抖节流工具
 * 提供高性能的防抖和节流功能
 */

/**
 * 防抖函数
 * 在指定时间内只执行最后一次调用
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number,
  immediate: boolean = false
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null
  let result: ReturnType<T>

  const debounced = function(this: any, ...args: Parameters<T>) {
    const context = this
    const later = () => {
      timeout = null
      if (!immediate) {
        result = func.apply(context, args)
      }
    }

    const callNow = immediate && !timeout
    
    if (timeout) {
      clearTimeout(timeout)
    }
    
    timeout = setTimeout(later, wait)
    
    if (callNow) {
      result = func.apply(context, args)
    }
    
    return result
  }

  // 取消防抖
  debounced.cancel = () => {
    if (timeout) {
      clearTimeout(timeout)
      timeout = null
    }
  }

  // 立即执行
  debounced.flush = function(this: any, ...args: Parameters<T>) {
    if (timeout) {
      clearTimeout(timeout)
      timeout = null
    }
    return func.apply(this, args)
  }

  return debounced
}

/**
 * 节流函数
 * 在指定时间间隔内只执行一次
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  wait: number,
  options: {
    leading?: boolean
    trailing?: boolean
  } = {}
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null
  let previous = 0
  let result: ReturnType<T>
  
  const { leading = true, trailing = true } = options

  const throttled = function(this: any, ...args: Parameters<T>) {
    const context = this
    const now = Date.now()
    
    if (!previous && !leading) {
      previous = now
    }
    
    const remaining = wait - (now - previous)
    
    if (remaining <= 0 || remaining > wait) {
      if (timeout) {
        clearTimeout(timeout)
        timeout = null
      }
      previous = now
      result = func.apply(context, args)
    } else if (!timeout && trailing) {
      timeout = setTimeout(() => {
        previous = leading ? Date.now() : 0
        timeout = null
        result = func.apply(context, args)
      }, remaining)
    }
    
    return result
  }

  // 取消节流
  throttled.cancel = () => {
    if (timeout) {
      clearTimeout(timeout)
      timeout = null
    }
    previous = 0
  }

  // 立即执行
  throttled.flush = function(this: any, ...args: Parameters<T>) {
    if (timeout) {
      clearTimeout(timeout)
      timeout = null
    }
    previous = 0
    return func.apply(this, args)
  }

  return throttled
}

/**
 * 请求动画帧节流
 * 使用 requestAnimationFrame 进行节流
 */
export function rafThrottle<T extends (...args: any[]) => any>(
  func: T
): (...args: Parameters<T>) => void {
  let rafId: number | null = null
  let lastArgs: Parameters<T>

  const throttled = function(this: any, ...args: Parameters<T>) {
    const context = this
    lastArgs = args

    if (rafId === null) {
      rafId = requestAnimationFrame(() => {
        rafId = null
        func.apply(context, lastArgs)
      })
    }
  }

  throttled.cancel = () => {
    if (rafId !== null) {
      cancelAnimationFrame(rafId)
      rafId = null
    }
  }

  return throttled
}

/**
 * 空闲时间回调
 * 在浏览器空闲时执行回调
 */
export function requestIdleCallback(
  callback: IdleRequestCallback,
  options?: IdleRequestOptions
): number {
  if ('requestIdleCallback' in window) {
    return window.requestIdleCallback(callback, options)
  } else {
    // 兼容性回退
    return setTimeout(() => {
      const start = Date.now()
      callback({
        didTimeout: false,
        timeRemaining() {
          return Math.max(0, 50 - (Date.now() - start))
        }
      })
    }, 1) as any
  }
}

export function cancelIdleCallback(id: number): void {
  if ('cancelIdleCallback' in window) {
    window.cancelIdleCallback(id)
  } else {
    clearTimeout(id)
  }
}

/**
 * 批量处理函数
 * 将多个调用批量处理
 */
export function batchProcess<T, R>(
  processor: (items: T[]) => R[],
  batchSize: number = 10,
  delay: number = 16 // 一帧的时间
) {
  let queue: T[] = []
  let timeout: ReturnType<typeof setTimeout> | null = null
  let resolvers: Array<(value: R) => void> = []

  return function(item: T): Promise<R> {
    return new Promise<R>((resolve) => {
      queue.push(item)
      resolvers.push(resolve)

      if (timeout) {
        clearTimeout(timeout)
      }

      timeout = setTimeout(() => {
        const currentQueue = queue.slice()
        const currentResolvers = resolvers.slice()
        
        queue = []
        resolvers = []
        timeout = null

        try {
          const results = processor(currentQueue)
          currentResolvers.forEach((resolve, index) => {
            resolve(results[index])
          })
        } catch (error) {
          currentResolvers.forEach((resolve) => {
            resolve(error as R)
          })
        }
      }, delay)
    })
  }
}

/**
 * 限制并发数量的函数
 */
export function limitConcurrency<T extends (...args: any[]) => Promise<any>>(
  func: T,
  limit: number
): (...args: Parameters<T>) => Promise<ReturnType<T>> {
  let running = 0
  const queue: Array<{
    args: Parameters<T>
    resolve: (value: ReturnType<T>) => void
    reject: (reason: any) => void
    context: any
  }> = []

  const processQueue = async () => {
    if (running >= limit || queue.length === 0) {
      return
    }

    running++
    const { args, resolve, reject, context } = queue.shift()!

    try {
      const result = await func.apply(context, args)
      resolve(result)
    } catch (error) {
      reject(error)
    } finally {
      running--
      processQueue()
    }
  }

  return function(this: any, ...args: Parameters<T>): Promise<ReturnType<T>> {
    return new Promise((resolve, reject) => {
      queue.push({
        args,
        resolve,
        reject,
        context: this
      })
      processQueue()
    })
  }
}

export default {
  debounce,
  throttle,
  rafThrottle,
  requestIdleCallback,
  cancelIdleCallback,
  batchProcess,
  limitConcurrency
}
