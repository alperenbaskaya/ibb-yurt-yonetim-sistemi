import type { CurrentUserResponse } from '../types/auth'
import type { UserCategory } from '../types/userCategory'

export const userCategoryLabels: Record<UserCategory, string> = {
  STUDENT: 'Öğrenci',
  REVIEWER: 'Değerlendirici',
  DORMITORY_ADMIN: 'Yurt Yöneticisi',
  GLOBAL_ADMIN: 'Genel Yönetici',
}

export function getUserCategory(
  user: CurrentUserResponse,
): UserCategory | null {
  if (user.role === 'STUDENT' && user.adminScope === null) {
    return 'STUDENT'
  }

  if (user.role === 'REVIEWER' && user.adminScope === null) {
    return 'REVIEWER'
  }

  if (user.role === 'ADMIN' && user.adminScope === 'DORMITORY') {
    return 'DORMITORY_ADMIN'
  }

  if (user.role === 'ADMIN' && user.adminScope === 'GLOBAL') {
    return 'GLOBAL_ADMIN'
  }

  return null
}
