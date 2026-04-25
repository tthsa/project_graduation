import request from './request'

// 登录参数类型
export interface LoginParams {
  username: string
  password: string
  userType: string // admin, teacher, student
}

// 登录返回数据类型
export interface LoginResult {
  token: string
  tokenType: string
  expiresIn: number
  userId: number
  username: string
  userType: string
  name: string
}

// 登录接口
export function login(data: LoginParams): Promise<LoginResult> {
  return request.post('/auth/login', data)
}

// 获取用户信息
export function getUserInfo(): Promise<LoginResult> {
  return request.get('/auth/me')
}

// 退出登录
export function logout(): Promise<void> {
  return request.post('/auth/logout')
}
