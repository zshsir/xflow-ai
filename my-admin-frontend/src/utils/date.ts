import { format, parseISO, isBefore, isEqual } from 'date-fns'
import { zhCN } from 'date-fns/locale'

export function formatDate(date: string | Date, pattern = 'yyyy-MM-dd'): string {
  const d = typeof date === 'string' ? parseISO(date) : date
  return format(d, pattern, { locale: zhCN })
}

export function formatMonth(date: string | Date): string {
  return formatDate(date, 'yyyy-MM')
}

export function getToday(): string {
  return formatDate(new Date())
}

export function isToday(dateStr: string): boolean {
  return dateStr === getToday()
}

/** 判断计划日期是否已过期（早于今天） */
export function isOverdue(plannedDate: string | null | undefined): boolean {
  if (!plannedDate) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const planned = parseISO(plannedDate)
  return isBefore(planned, today)
}

/** 判断计划日期是否是今天 */
export function isDueToday(plannedDate: string | null | undefined): boolean {
  if (!plannedDate) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const planned = parseISO(plannedDate)
  return isEqual(planned, today)
}