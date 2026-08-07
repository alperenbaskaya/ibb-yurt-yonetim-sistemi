import { ArrowLeft, ShieldX } from 'lucide-react'
import { Link } from 'react-router-dom'

export function ForbiddenPage() {
  return (
    <section className="mx-auto flex max-w-2xl flex-col items-start border border-slate-200 bg-white p-8 shadow-sm sm:p-10">
      <div className="flex size-12 items-center justify-center bg-red-50 text-red-700">
        <ShieldX aria-hidden="true" size={25} />
      </div>
      <p className="mt-6 text-sm font-semibold uppercase tracking-[0.14em] text-red-700">
        Erişim Engellendi
      </p>
      <h1 className="mt-2 text-2xl font-semibold tracking-tight text-slate-950 sm:text-3xl">
        Bu sayfayı görüntüleme yetkiniz bulunmuyor
      </h1>
      <p className="mt-4 text-sm leading-6 text-slate-600">
        Hesabınıza tanımlı rol veya yetki kapsamı bu alana erişime izin
        vermiyor. Yetkiniz olduğunu düşünüyorsanız sistem yöneticinizle
        iletişime geçin.
      </p>
      <Link
        to="/home"
        className="mt-7 inline-flex items-center gap-2 bg-blue-700 px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700"
      >
        <ArrowLeft aria-hidden="true" size={17} />
        Ana sayfaya dön
      </Link>
    </section>
  )
}
