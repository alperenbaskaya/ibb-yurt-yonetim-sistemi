import { useQuery } from '@tanstack/react-query'
import { getRecentReviewerActivity } from '../../api/reviewerActivityApi'

export const reviewerActivityKeys = {
  root: (userId: number) => ['reviewer-activity', userId] as const,
  recent: (userId: number) => [...reviewerActivityKeys.root(userId), 'recent'] as const,
}

export function useRecentReviewerActivity(userId: number, enabled: boolean) {
  return useQuery({
    queryKey: reviewerActivityKeys.recent(userId),
    queryFn: getRecentReviewerActivity,
    enabled,
  })
}
