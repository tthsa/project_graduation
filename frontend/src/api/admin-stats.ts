import request from './request'

export interface AdminStatsOverview {
  teacherCount: number
  studentCount: number
  courseCount: number
  homeworkCount: number
}

// 获取管理员首页统计
export function getAdminStatsOverview(): Promise<AdminStatsOverview> {
  return request.get('/admin/stats/overview')
}
