import { PageHeader } from '../../components/common/PageHeader'
import { getApiErrorMessage } from '../../utils/apiError'
import { useAuth } from '../auth/useAuth'
import { ReviewerPageState } from './ReviewerPageState'
import { ReviewRecordCard } from './ReviewRecordCard'
import { useMyDocumentReviews } from './reviewerQueries'

export function MyReviewsPage() {
  const { user } = useAuth()
  const reviewsQuery = useMyDocumentReviews(user?.userId ?? 0)

  return (
    <section>
      <PageHeader title="Değerlendirmelerim" description="Kendi hesabınızla gerçekleştirdiğiniz belge değerlendirmelerini en yeniden eskiye görüntüleyin." />
      {reviewsQuery.isLoading && <ReviewerPageState state="loading" title="Değerlendirmeler yükleniyor" message="Değerlendirme geçmişiniz alınıyor..." />}
      {reviewsQuery.isError && <ReviewerPageState state="error" title="Değerlendirmeler yüklenemedi" message={getApiErrorMessage(reviewsQuery.error, 'Değerlendirme geçmişiniz alınamadı.')} onRetry={() => void reviewsQuery.refetch()} />}
      {reviewsQuery.isSuccess && reviewsQuery.data.length === 0 && <ReviewerPageState state="empty" title="Henüz değerlendirme yok" message="Bu hesapla kaydedilmiş bir belge değerlendirmesi bulunmuyor." />}
      {reviewsQuery.isSuccess && reviewsQuery.data.length > 0 && (
        <div className="mt-6 grid gap-3 lg:grid-cols-2">
          {reviewsQuery.data.map((review) => <ReviewRecordCard key={review.id} review={review} />)}
        </div>
      )}
    </section>
  )
}
