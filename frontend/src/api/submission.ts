import request from './request'

// 提交作业（文件上传）
export function submitHomework(data: {
  homeworkId: number
  studentId: number
  files: File[]
}): Promise<void> {
  const formData = new FormData()
  formData.append('homeworkId', data.homeworkId.toString())
  formData.append('studentId', data.studentId.toString())
  data.files.forEach((file) => {
    formData.append('files', file)
  })
  return request.post('/student/submission/submit', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}
