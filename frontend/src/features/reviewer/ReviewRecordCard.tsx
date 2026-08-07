import { MessageSquareText } from 'lucide-react'
import type { DocumentReviewResponse } from '../../types/reviewer'
import { formatDateTime } from '../../utils/formatDateTime'
import { ReviewerStatusBadge } from './ReviewerStatusBadge'
import { reviewerDecisionPresentation } from './reviewerPresentation'

interface ReviewRecordCardProps {
  review: DocumentReviewResponse
  showReviewer?: boolean
}

export function ReviewRecordCard({ review, showReviewer = false }: ReviewRecordCardProps) {
  const decision = reviewerDecisionPresentation[review.decision]
  return (
    <article className="border border-slate-200 bg-white p-4 shadow-sm">
      <div className="flex items-start gap-3">
        <MessageSquareText aria-hidden="true" className="mt-0.5 shrink-0 text-blue-700" size={19} />
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-start justify-between gap-2">
            <div>
              <h3 className="font-semibold text-slate-900">{review.studentFirstName} {review.studentLastName}</h3>
              <p className="mt-1 text-sm text-slate-600">{review.documentTypeName} · {review.originalFileName}</p>
            </div>
            <ReviewerStatusBadge label={decision.label} tone={decision.tone} />
          </div>
          {review.comment && <p className="mt-3 whitespace-pre-wrap border-l-2 border-slate-300 pl-3 text-sm leading-6 text-slate-700">{review.comment}</p>}
          <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-xs font-medium text-slate-500">
            <time dateTime={review.reviewedAt}>{formatDateTime(review.reviewedAt)}</time>
            {showReviewer && <span>Değerlendiren: {review.reviewerFirstName} {review.reviewerLastName}</span>}
          </div>
        </div>
      </div>
    </article>
  )
}
