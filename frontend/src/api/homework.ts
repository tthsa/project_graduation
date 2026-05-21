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

// 作业基础字段（创建和更新共用）
interface HomeworkBaseParams {
  courseId: number
  title: string
  description: string
  deadline: string
  status: number
  testWeight?: number | null
  llmWeight?: number | null
  gradeAThreshold?: number | null
  gradeBThreshold?: number | null
  gradeCThreshold?: number | null
  llmDimensions?: string | null
}

// 添加作业参数
export type AddHomeworkParams = HomeworkBaseParams

// 更新作业参数
export type UpdateHomeworkParams = HomeworkBaseParams & { id: number }

export interface Homework {
  id: number
  courseId: number
  title: string
  description: string
  deadline: string
  status: number
  createdAt: string
  updatedAt: string
  testWeight: number | null
  llmWeight: number | null
  gradeAThreshold: number | null
  gradeBThreshold: number | null
  gradeCThreshold: number | null
  llmDimensions: string | null
}

// 作业带状态类型
export interface HomeworkWithStatus {
  homework: Homework
  submitStatus: number | null
  submissionId: number | null
  submitTime: string | null
  score: number | null
  expired: boolean
}

// ==================== 教师接口 ====================

export function getHomeworkList(): Promise<Homework[]> {
  return request.get('/teacher/homework/list')
}

export function getHomeworkDetail(id: number): Promise<Homework> {
  return request.get(`/teacher/homework/${id}`)
}

export function createHomework(data: AddHomeworkParams): Promise<Homework> {
  return request.post('/teacher/homework/create', data)
}

export function updateHomework(data: UpdateHomeworkParams): Promise<Homework> {
  return request.put(`/teacher/homework/${data.id}`, data)
}

export function deleteHomework(id: number): Promise<void> {
  return request.delete(`/teacher/homework/${id}`)
}

// ==================== 学生接口 ====================

export function getHomeworkListForStudent(): Promise<HomeworkWithStatus[]> {
  return request.get('/student/homework/list')
}

export function getHomeworkDetailForStudent(id: number): Promise<Homework> {
  return request.get(`/student/homework/${id}`)
}

export function getPublicTestCases(homeworkId: number): Promise<TestCase[]> {
  return request.get(`/student/homework/${homeworkId}/testcases`)
}
