import { useQuery } from '@tanstack/react-query'
import {
  getDormitoryAdminHistory,
  type DormitoryHistoryCategory,
} from '../../api/dormitoryAdminHistoryApi'

export const dormitoryAdminHistoryKeys = {
  root: (userId: number) => ['dormitory-admin-history', userId] as const,
  page: (userId: number, category: DormitoryHistoryCategory, page: number, query: string) =>
    [...dormitoryAdminHistoryKeys.root(userId), category, query, page] as const,
}

export function useDormitoryAdminHistory(
  userId: number,
  category: DormitoryHistoryCategory,
  page: number,
  query: string,
) {
  return useQuery({
    queryKey: dormitoryAdminHistoryKeys.page(userId, category, page, query),
    queryFn: () => getDormitoryAdminHistory(category, page, query),
  })
}
