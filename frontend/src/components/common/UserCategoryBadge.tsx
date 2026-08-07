import type { UserCategory } from '../../types/userCategory'
import { userCategoryLabels } from '../../utils/userCategory'

interface UserCategoryBadgeProps {
  category: UserCategory
}

export function UserCategoryBadge({ category }: UserCategoryBadgeProps) {
  return (
    <span className="inline-flex border border-blue-200 bg-blue-50 px-2.5 py-1 text-xs font-semibold text-blue-800">
      {userCategoryLabels[category]}
    </span>
  )
}
