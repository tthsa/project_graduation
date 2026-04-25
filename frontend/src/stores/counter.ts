import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo as getUserInfoApi } from '@/api/user'
import type { LoginParams } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<{
    userId: number
    username: string
    name: string
    userType: string
  } | null>(null)

  // 登录
  const login = async (params: LoginParams) => {
    const result = await loginApi(params)

    // 保存 token
    token.value = result.token
    localStorage.setItem('token', result.token)

    // 保存用户信息
    userInfo.value = {
      userId: result.userId,
      username: result.username,
      name: result.name,
      userType: result.userType,
    }

    return result
  }

  // 获取用户信息
  const getUserInfo = async () => {
    const result = await getUserInfoApi()
    userInfo.value = {
      userId: result.userId,
      username: result.username,
      name: result.name,
      userType: result.userType,
    }
    return result
  }

  // 退出登录
  const logout = async () => {
    await logoutApi()

    // 清除状态
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  // 是否已登录
  const isLoggedIn = () => {
    return !!token.value
  }

  return {
    token,
    userInfo,
    login,
    logout,
    getUserInfo,
    isLoggedIn,
  }
})
