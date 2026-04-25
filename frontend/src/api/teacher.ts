import request from './request'

// 教师信息类型
export interface Teacher {
  id: number
  teacherNo: string
  name: string
  email: string
  phone: string
  status: number
}

// 添加教师参数
export interface AddTeacherParams {
  teacherNo: string
  name: string
  email: string
  phone: string
  password: string
}

// 更新教师参数
export interface UpdateTeacherParams {
  id: number
  teacherNo: string
  name: string
  email: string
  phone: string
  status: number
}

// 获取教师列表
export function getTeacherList(): Promise<Teacher[]> {
  return request.get('/teacher/list')
}

// 获取教师详情
export function getTeacherDetail(id: number): Promise<Teacher> {
  return request.get(`/teacher/${id}`)
}

// 添加教师
export function addTeacher(data: AddTeacherParams): Promise<Teacher> {
  return request.post('/teacher/create', data)
}

// 更新教师信息
export function updateTeacher(data: UpdateTeacherParams): Promise<Teacher> {
  return request.put(`/teacher/${data.id}`, data)
}

// 删除教师
export function deleteTeacher(id: number): Promise<void> {
  return request.delete(`/teacher/${id}`)
}
