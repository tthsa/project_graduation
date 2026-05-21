import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo as getUserInfoApi } from '@/api/user'
import type { LoginParams } from '@/api/user'

interface UserInfo {
  userId: number
  username: string
  name: string
  userType: string
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)

  // 登录
  const login = async (params: LoginParams) => {
    const result = await loginApi(params)

    token.value = result.token

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
    token.value = ''
    userInfo.value = null
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
}, {
  persist: {
    pick: ['token', 'userInfo'],
  },
})
