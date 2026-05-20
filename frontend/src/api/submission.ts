import request from './request'
import type { Submission } from './report'
export type { Submission } from './report'

// 提交作业（文件上传）
export function submitHomework(data: {
  homeworkId: number
  files: File[]
}): Promise<{ submissionId: number }> {
  const formData = new FormData()
  formData.append('homeworkId', data.homeworkId.toString())
  data.files.forEach((file) => {
    formData.append('files', file)
  })
  return request.post('/student/submission/submit', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// 当前学生的所有提交记录
export function getMySubmissions(): Promise<Submission[]> {
  return request.get('/student/submission/list')
}

// 当前学生的某条提交详情(仅本人可见)
export function getMySubmissionDetail(submissionId: number): Promise<Submission> {
  return request.get(`/student/submission/detail/${submissionId}`)
}
