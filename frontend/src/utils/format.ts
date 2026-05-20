/**
 * 格式化日期时间：将 ISO 格式或带 T 的字符串转为可读格式
 * @param time 时间字符串或 Date
 * @returns 格式化后的字符串，如 2026-05-12 10:30:00
 */
export function formatTime(time: string | Date | null | undefined): string {
  if (!time) return '-'
  const s = typeof time === 'string' ? time : time.toISOString()
  return s.replace('T', ' ').replace(/\.\d+$/, '')
}
