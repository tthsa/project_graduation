import request from './request'

// 提交记录类型
export interface Submission {
  id: number
  homeworkId: number
  studentId: number
  submitTime: string
  status: number // 0=待评测, 1=评测中, 2=完成, 3=失败
  files?: SubmissionFile[]
}

// 提交文件类型
export interface SubmissionFile {
  id: number
  submissionId: number
  fileName: string
  fileContent: string
  fileOrder: number
  createdAt: string
}

// 评测结果类型
export interface EvaluationResult {
  id: number
  submissionId: number
  testScore: number
  llmScore: number
  llmReview: string
  executionTime: number
  createdAt: string
}

// 获取某作业的所有学生提交情况
export function getSubmissionsByHomework(homeworkId: number): Promise<Submission[]> {
  return request.get(`/report/homework/${homeworkId}`)
}

// 获取某学生的所有提交记录
export function getSubmissionsByStudent(studentId: number): Promise<Submission[]> {
  return request.get(`/report/student/${studentId}`)
}

// 获取提交详情
export function getSubmissionDetail(submissionId: number): Promise<Submission> {
  return request.get(`/report/detail/${submissionId}`)
}

// 获取评测结果
export function getEvaluationResult(submissionId: number): Promise<EvaluationResult> {
  return request.get(`/student/submission/result/${submissionId}`)
}

// 获取提交文件列表
export function getSubmissionFiles(submissionId: number): Promise<SubmissionFile[]> {
  return request.get(`/student/submission/files/${submissionId}`)
}
