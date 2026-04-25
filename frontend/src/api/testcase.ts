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

// 添加测试用例参数
export interface AddTestCaseParams {
  homeworkId: number
  name: string
  input: string
  expectedOutput: string
  isPublic: number
  sortOrder: number
}

// 更新测试用例参数
export interface UpdateTestCaseParams {
  id: number
  homeworkId: number
  name: string
  input: string
  expectedOutput: string
  isPublic: number
  sortOrder: number
}

// 获取测试用例列表
export function getTestCaseList(homeworkId: number): Promise<TestCase[]> {
  return request.get('/teacher/testcase/list', {
    params: { homeworkId },
  })
}

// 创建测试用例
export function createTestCase(data: AddTestCaseParams): Promise<TestCase> {
  return request.post('/teacher/testcase/create', data)
}

// 更新测试用例
export function updateTestCase(data: UpdateTestCaseParams): Promise<TestCase> {
  return request.put(`/teacher/testcase/${data.id}`, data)
}

// 删除测试用例
export function deleteTestCase(id: number): Promise<void> {
  return request.delete(`/teacher/testcase/${id}`)
}
