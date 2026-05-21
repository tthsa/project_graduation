import axios from 'axios'
import { ElMessage } from 'element-plus'

// 401 未授权处理函数（由 main.ts 注入，避免循环依赖）
let unauthorizedHandler = () => {
  localStorage.removeItem('token')
  window.location.replace('/login')
}

export function setUnauthorizedHandler(handler: () => void) {
  unauthorizedHandler = handler
}

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 兼容两种存储：独立 token 键（旧）或 Pinia persistedstate 的 user 键（新）
    let token = localStorage.getItem('token')
    if (!token) {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        try {
          const user = JSON.parse(userStr)
          token = user.token || null
        } catch {
          token = null
        }
      }
    }
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data

    if (res.code === 200) {
      return res.data
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('未登录或登录已过期')
          unauthorizedHandler()
          break
        case 403:
          ElMessage.error('没有权限')
          break
        case 404:
          ElMessage.error('请求资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  },
)

export default request
