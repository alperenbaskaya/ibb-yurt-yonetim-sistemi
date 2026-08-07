import { LogOut, TriangleAlert } from 'lucide-react'
import { useAuth } from '../features/auth/useAuth'

export function UserConfigurationErrorPage() {
  const { logout } = useAuth()

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-50 px-6 py-12">
      <section className="w-full max-w-xl border border-amber-200 bg-white p-8 shadow-sm sm:p-10">
        <div className="flex size-12 items-center justify-center bg-amber-50 text-amber-700">
          <TriangleAlert aria-hidden="true" size={25} />
        </div>
        <h1 className="mt-6 text-2xl font-semibold tracking-tight text-slate-950">
          Kullanıcı yetki yapılandırması geçersiz
        </h1>
        <p className="mt-3 text-sm leading-6 text-slate-600">
          Hesabınızın rol ve yetki kapsamı sistemde desteklenen bir
          yapılandırmayla eşleşmiyor. Lütfen sistem yöneticinizle iletişime
          geçin.
        </p>
        <button
          type="button"
          onClick={logout}
          className="mt-7 inline-flex items-center gap-2 border border-slate-300 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700"
        >
          <LogOut aria-hidden="true" size={17} />
          Çıkış Yap
        </button>
      </section>
    </main>
  )
}
