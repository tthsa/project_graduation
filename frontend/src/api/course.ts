import request from './request'

// 课程信息类型
export interface Course {
  id: number
  name: string
  teacherId: number
  classId: number | null
  createdAt?: string
}

// 添加课程参数
export interface AddCourseParams {
  name: string
  classId: number | null
}

// 更新课程参数
export interface UpdateCourseParams {
  id: number
  name: string
  classId: number | null
}

// 获取当前教师的课程列表
export function getCourseList(): Promise<Course[]> {
  return request.get('/teacher/course/list')
}

// 获取课程详情
export function getCourseDetail(id: number): Promise<Course> {
  return request.get(`/teacher/course/${id}`)
}

// 创建课程
export function addCourse(data: AddCourseParams): Promise<Course> {
  return request.post('/teacher/course/create', data)
}

// 更新课程
export function updateCourse(data: UpdateCourseParams): Promise<Course> {
  return request.put(`/teacher/course/${data.id}`, data)
}

// 删除课程
export function deleteCourse(id: number): Promise<void> {
  return request.delete(`/teacher/course/${id}`)
}
