import { DatabaseZap, LayoutDashboard } from 'lucide-react'
import { Link } from 'react-router-dom'
import { PageHeader } from '../../components/common/PageHeader'

export function ReviewerStudentsGapPage() {
  return (
    <section>
      <PageHeader title="Öğrenciler" description="Yurdunuza bağlı öğrencilerin belge süreçlerini görüntüleme alanı." />
      <div className="mt-6 border border-amber-200 bg-white p-6 shadow-sm sm:p-8">
        <span className="flex size-11 items-center justify-center bg-amber-50 text-amber-700"><DatabaseZap aria-hidden="true" size={22} /></span>
        <h2 className="mt-5 text-lg font-semibold text-slate-950">Reviewer öğrenci listesi backend desteği bekliyor</h2>
        <p className="mt-2 max-w-3xl text-sm leading-6 text-slate-600">
          Mevcut backend’de öğrenci listeleme endpoint’leri yalnızca ADMIN rolüne açıktır. Değerlendiricinin kendi yurdundaki tüm öğrencileri listeleyen bir endpoint bulunmadığı için bu sayfada eksik veya örnek veri gösterilmiyor.
        </p>
        <p className="mt-3 max-w-3xl text-sm leading-6 text-slate-600">
          Kontrol panelinde backend tarafından sağlanan yurt geneli sayılar ve öncelikli işlem gereken ilk 10 öğrenci görülebilir.
        </p>
        <Link to="/reviewer/dashboard" className="mt-5 inline-flex min-h-10 items-center justify-center gap-2 bg-blue-800 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-900 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700"><LayoutDashboard aria-hidden="true" size={17} /> Kontrol paneline dön</Link>
      </div>
    </section>
  )
}
