import { useEffect, useState } from 'react'
import { PageHeader } from '../../components/common/PageHeader'
import { Pagination } from '../../components/common/Pagination'
import { HistoryPersonSearch } from '../../components/common/HistoryPersonSearch'
import { useDebouncedValue } from '../../hooks/useDebouncedValue'
import { getApiErrorMessage } from '../../utils/apiError'
import { useAuth } from '../auth/useAuth'
import { ReviewerPageState } from './ReviewerPageState'
import { ReviewRecordCard } from './ReviewRecordCard'
import { useMyDocumentReviewHistory } from './reviewerQueries'

export function MyReviewsPage() {
  const { user } = useAuth()
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const debouncedSearch = useDebouncedValue(search)
  const reviewsQuery = useMyDocumentReviewHistory(user?.userId ?? 0, page, debouncedSearch)

  useEffect(() => {
    if (reviewsQuery.data && page >= reviewsQuery.data.totalPages && reviewsQuery.data.totalPages > 0) {
      setPage(reviewsQuery.data.totalPages - 1)
    }
  }, [page, reviewsQuery.data])

  const changeSearch = (value: string) => { setSearch(value); setPage(0) }

  return (
    <section>
      <PageHeader title="Değerlendirmelerim" description="Kendi hesabınızla gerçekleştirdiğiniz belge değerlendirmelerini en yeniden eskiye görüntüleyin." />
      <HistoryPersonSearch value={search} onChange={changeSearch} />
      {reviewsQuery.isLoading && <ReviewerPageState state="loading" title="Değerlendirmeler yükleniyor" message="Değerlendirme geçmişiniz alınıyor..." />}
      {reviewsQuery.isError && <ReviewerPageState state="error" title="Değerlendirmeler yüklenemedi" message={getApiErrorMessage(reviewsQuery.error, 'Değerlendirme geçmişiniz alınamadı.')} onRetry={() => void reviewsQuery.refetch()} />}
      {reviewsQuery.isSuccess && reviewsQuery.data.items.length === 0 && <ReviewerPageState state="empty" title={search ? 'Aramanızla eşleşen geçmiş kaydı bulunamadı' : 'Henüz değerlendirme yok'} message={search ? 'Arama metnini değiştirerek tekrar deneyin.' : 'Bu hesapla kaydedilmiş bir belge değerlendirmesi bulunmuyor.'} />}
      {reviewsQuery.isSuccess && reviewsQuery.data.items.length > 0 && (
        <div className="mt-6">
          <div className="grid gap-3 lg:grid-cols-2">
            {reviewsQuery.data.items.map((review) => <ReviewRecordCard key={review.id} review={review} />)}
          </div>
          <Pagination currentPage={reviewsQuery.data.page} totalPages={reviewsQuery.data.totalPages} totalElements={reviewsQuery.data.totalElements} pageSize={reviewsQuery.data.size} onPageChange={setPage} itemLabel="değerlendirme" className="mt-5" />
        </div>
      )}
    </section>
  )
}
