import request from './request'

// 学生信息类型
export interface Student {
  id: number
  studentNo: string
  name: string
  email: string
  classId: number
  status: number
}

// 添加学生参数
export interface AddStudentParams {
  studentNo: string
  name: string
  email: string
  classId: number
  password: string
  status: number
  firstLogin: number
}

// 更新学生参数
export interface UpdateStudentParams {
  id: number
  name: string
  email: string
  classId: number
  status: number
}

// 获取学生列表
export function getStudentList(): Promise<Student[]> {
  return request.get('/student/list')
}

// 根据班级ID获取学生列表
export function getStudentListByClass(classId: number): Promise<Student[]> {
  return request.get(`/student/class/${classId}`)
}

// 获取学生详情
export function getStudentDetail(id: number): Promise<Student> {
  return request.get(`/student/${id}`)
}

// 添加学生
export function addStudent(data: AddStudentParams): Promise<Student> {
  return request.post('/student/create', data)
}

// 更新学生信息
export function updateStudent(data: UpdateStudentParams): Promise<Student> {
  return request.put(`/student/${data.id}`, data)
}

// 删除学生
export function deleteStudent(id: number): Promise<void> {
  return request.delete(`/student/${id}`)
}
