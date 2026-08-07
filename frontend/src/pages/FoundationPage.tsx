import { Building2, CheckCircle2 } from 'lucide-react'

export function FoundationPage() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-50 px-6 py-12">
      <section className="w-full max-w-2xl border border-slate-200 bg-white p-8 shadow-sm sm:p-12">
        <div className="mb-8 flex size-12 items-center justify-center bg-blue-50 text-blue-700">
          <Building2 aria-hidden="true" size={26} strokeWidth={1.8} />
        </div>

        <p className="mb-3 text-sm font-semibold uppercase tracking-[0.16em] text-blue-700">
          İBB Yurtları
        </p>
        <h1 className="max-w-xl text-3xl font-semibold tracking-tight text-slate-950 sm:text-4xl">
          Yurt Belge Yönetim ve Değerlendirme Sistemi
        </h1>
        <p className="mt-5 max-w-xl text-base leading-7 text-slate-600">
          React uygulama temeli hazır. Kimlik doğrulama ve rol bazlı
          ekranlar sonraki geliştirme adımlarında eklenecek.
        </p>

        <div className="mt-8 flex items-center gap-3 border-t border-slate-200 pt-6 text-sm font-medium text-emerald-700">
          <CheckCircle2 aria-hidden="true" size={19} />
          Frontend altyapısı başarıyla çalışıyor
        </div>
      </section>
    </main>
  )
}
