import request from './request'

// 触发单条提交评测
export function triggerEvaluation(submissionId: number): Promise<null> {
  return request.post(`/teacher/evaluation/trigger/${submissionId}`)
}

// 批量触发某作业下所有待评测/失败的提交
export function triggerBatchEvaluation(
  homeworkId: number,
): Promise<{ triggered: number; skipped: number }> {
  return request.post('/teacher/evaluation/trigger-batch', { homeworkId })
}
