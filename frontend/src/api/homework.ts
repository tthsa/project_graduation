import request from './request'

// 测试用例类型
export interface TestCase {
  id: number
  homeworkId: number
  name: string
  input: string
  expectedOutput: string
  isPublic: number
  sortOrder: number
  createdAt: string
}

// 作业信息类型
export interface Homework {
  id: number
  courseId: number
  title: string
  description: string
  deadline: string
  status: number
  createdAt: string
  updatedAt: string
}

// 添加作业参数
export interface AddHomeworkParams {
  courseId: number
  title: string
  description: string
  deadline: string
  status: number
}

// 更新作业参数
export interface UpdateHomeworkParams {
  id: number
  courseId: number
  title: string
  description: string
  deadline: string
  status: number
}

// ==================== 教师接口 ====================

// 获取作业列表（教师）
export function getHomeworkList(): Promise<Homework[]> {
  return request.get('/teacher/homework/list')
}

// 获取作业详情（教师）
export function getHomeworkDetail(id: number): Promise<Homework> {
  return request.get(`/teacher/homework/${id}`)
}

// 创建作业
export function createHomework(data: AddHomeworkParams): Promise<Homework> {
  const now = new Date().toISOString().replace('Z', '')
  const payload = {
    courseId: data.courseId,
    title: data.title,
    description: data.description,
    deadline: data.deadline ? data.deadline.replace(' ', 'T') : null,
    status: data.status,
    createdAt: now,
  }
  return request.post('/teacher/homework/create', payload)
}

// 更新作业
export function updateHomework(data: UpdateHomeworkParams): Promise<Homework> {
  const now = new Date().toISOString().replace('Z', '')
  const payload = {
    id: data.id,
    courseId: data.courseId,
    title: data.title,
    description: data.description,
    deadline: data.deadline ? data.deadline.replace(' ', 'T') : null,
    status: data.status,
    updatedAt: now,
  }
  return request.put(`/teacher/homework/${data.id}`, payload)
}

// 删除作业
export function deleteHomework(id: number): Promise<void> {
  return request.delete(`/teacher/homework/${id}`)
}

// ==================== 学生接口 ====================

// 作业带状态类型
export interface HomeworkWithStatus {
  homework: Homework
  submitStatus: number
  submissionId: number | null
  submitTime: string | null
  score: number | null
  expired: boolean
}

// 获取作业列表（学生，带提交状态）
export function getHomeworkListForStudent(studentId: number): Promise<HomeworkWithStatus[]> {
  return request.get('/student/homework/list', { params: { studentId } })
}

// 获取作业详情（学生）
export function getHomeworkDetailForStudent(id: number): Promise<Homework> {
  return request.get(`/student/homework/${id}`)
}

// 获取公开测试用例
export function getPublicTestCases(homeworkId: number): Promise<TestCase[]> {
  return request.get(`/student/homework/${homeworkId}/testcases`)
}
