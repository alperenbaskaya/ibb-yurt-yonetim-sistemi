import type { ReviewerTone } from './reviewerPresentation'
import { reviewerToneClasses } from './reviewerPresentation'

interface ReviewerStatusBadgeProps {
  label: string
  tone: ReviewerTone
}

export function ReviewerStatusBadge({ label, tone }: ReviewerStatusBadgeProps) {
  return (
    <span className={`inline-flex items-center border px-2.5 py-1 text-xs font-semibold ${reviewerToneClasses[tone]}`}>
      {label}
    </span>
  )
}
