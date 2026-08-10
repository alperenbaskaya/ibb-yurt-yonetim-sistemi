import { useQuery } from '@tanstack/react-query'
import {
  getDormitoryAdminHistory,
  type DormitoryHistoryCategory,
} from '../../api/dormitoryAdminHistoryApi'

export const dormitoryAdminHistoryKeys = {
  root: (userId: number) => ['dormitory-admin-history', userId] as const,
  page: (userId: number, category: DormitoryHistoryCategory, page: number) =>
    [...dormitoryAdminHistoryKeys.root(userId), category, page] as const,
}

export function useDormitoryAdminHistory(
  userId: number,
  category: DormitoryHistoryCategory,
  page: number,
) {
  return useQuery({
    queryKey: dormitoryAdminHistoryKeys.page(userId, category, page),
    queryFn: () => getDormitoryAdminHistory(category, page),
  })
}
