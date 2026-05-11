import request from './request'

export interface TeacherStatsOverview {
  courseCount: number
  homeworkCount: number
  pendingCount: number
  completedCount: number
}

// 获取教师首页统计
export function getTeacherStatsOverview(): Promise<TeacherStatsOverview> {
  return request.get('/teacher/stats/overview')
}
