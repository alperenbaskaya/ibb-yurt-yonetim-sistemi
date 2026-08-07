import { LoaderCircle, UserCog } from 'lucide-react'
import { toast } from 'sonner'
import { PageHeader } from '../../components/common/PageHeader'
import type { CreateReviewerRequest, UserResponse } from '../../types/dormitoryAdmin'
import { getApiErrorMessage } from '../../utils/apiError'
import { useAuth } from '../auth/useAuth'
import { AdminPageState } from './AdminPageState'
import { AdminStatusBadge } from './AdminStatusBadge'
import { CreateReviewerForm } from './CreateReviewerForm'
import { useCreateReviewer, useDormitoryReviewers, useUpdateReviewer } from './dormitoryAdminQueries'

export function DormitoryReviewersPage() {
  const { user } = useAuth()
  const userId = user?.userId ?? 0
  const dormitoryId = user?.dormitoryId ?? null
  const query = useDormitoryReviewers(userId)
  const createMutation = useCreateReviewer(userId)
  const updateMutation = useUpdateReviewer(userId)
  const create = async (request: CreateReviewerRequest) => { await createMutation.mutateAsync(request); toast.success('Değerlendirici oluşturuldu.') }
  const toggleActive = (reviewer: UserResponse) => {
    if (reviewer.dormitoryId === null) return
    const verb = reviewer.active ? 'pasif hale getirmek' : 'aktif hale getirmek'
    if (!window.confirm(`${reviewer.firstName} ${reviewer.lastName} kullanıcısını ${verb} istediğinizden emin misiniz?`)) return
    updateMutation.mutate({ reviewerId: reviewer.id, request: { firstName: reviewer.firstName, lastName: reviewer.lastName, email: reviewer.email, role: 'REVIEWER', adminScope: null, dormitoryId: reviewer.dormitoryId, active: !reviewer.active } }, { onSuccess: () => toast.success('Değerlendirici durumu güncellendi.'), onError: (error: unknown) => toast.error(getApiErrorMessage(error, 'Değerlendirici güncellenemedi.')) })
  }

  return <section><PageHeader title="Değerlendiriciler" description="Yurdunuza bağlı değerlendirici hesaplarını görüntüleyin, oluşturun ve aktiflik durumlarını yönetin." />
    {dormitoryId === null ? <AdminPageState state="error" title="Yurt ataması bulunamadı" message="Değerlendirici yönetimi için yönetici hesabına bir yurt atanmış olmalıdır." /> : <div className="mt-6 grid gap-6 xl:grid-cols-[0.85fr_1.15fr]"><CreateReviewerForm dormitoryId={dormitoryId} onCreate={create} /><section aria-labelledby="reviewer-list-title"><h2 id="reviewer-list-title" className="text-lg font-semibold text-slate-950">Yurt değerlendiricileri</h2>{query.isLoading && <AdminPageState state="loading" title="Değerlendiriciler yükleniyor" message="Yurdunuza bağlı hesaplar alınıyor..." />}{query.isError && <AdminPageState state="error" title="Değerlendiriciler yüklenemedi" message={getApiErrorMessage(query.error, 'Değerlendirici listesi alınamadı.')} onRetry={() => void query.refetch()} />}{query.isSuccess && query.data.length === 0 && <AdminPageState state="empty" title="Değerlendirici yok" message="Yurdunuza bağlı değerlendirici hesabı bulunmuyor." />}{query.isSuccess && query.data.length > 0 && <div className="mt-4 space-y-3">{query.data.map((reviewer) => { const pending = updateMutation.isPending && updateMutation.variables?.reviewerId === reviewer.id; return <article key={reviewer.id} className="border border-slate-200 bg-white p-4 shadow-sm"><div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between"><div className="flex min-w-0 items-start gap-3"><span className="flex size-10 shrink-0 items-center justify-center bg-blue-50 text-blue-700"><UserCog aria-hidden="true" size={20} /></span><div><h3 className="font-semibold text-slate-950">{reviewer.firstName} {reviewer.lastName}</h3><p className="mt-1 break-all text-sm text-slate-600">{reviewer.email}</p></div></div><AdminStatusBadge label={reviewer.active ? 'Aktif' : 'Pasif'} tone={reviewer.active ? 'success' : 'neutral'} /></div><button type="button" onClick={() => toggleActive(reviewer)} disabled={pending} className={`mt-4 inline-flex min-h-9 items-center gap-2 border px-3 py-2 text-sm font-semibold focus-visible:outline-2 focus-visible:outline-offset-2 disabled:opacity-60 ${reviewer.active ? 'border-red-300 text-red-700 hover:bg-red-50 focus-visible:outline-red-700' : 'border-emerald-300 text-emerald-700 hover:bg-emerald-50 focus-visible:outline-emerald-700'}`}>{pending && <LoaderCircle className="animate-spin" aria-hidden="true" size={16} />}{reviewer.active ? 'Pasif hale getir' : 'Aktif hale getir'}</button></article>})}</div>}</section></div>}
  </section>
}
