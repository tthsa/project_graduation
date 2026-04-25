import request from './request'

// 提交参数
export interface SubmitParams {
  homeworkId: number
  studentId: number
  code: string
}

// 提交作业
export function submitHomework(data: SubmitParams): Promise<void> {
  return request.post('/student/submission/submit', data)
}
