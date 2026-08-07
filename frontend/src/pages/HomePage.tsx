import { Building2, CheckCircle2, LogOut, UserRound } from 'lucide-react'
import type { AdminScope, Role } from '../types/auth'
import { useAuth } from '../features/auth/useAuth'

const roleLabels: Record<Role, string> = {
  ADMIN: 'Yönetici',
  REVIEWER: 'Belge Değerlendiricisi',
  STUDENT: 'Öğrenci',
}

const scopeLabels: Record<AdminScope, string> = {
  GLOBAL: 'Global Yönetim',
  DORMITORY: 'Yurt Yönetimi',
}

export function HomePage() {
  const { user, logout } = useAuth()

  if (!user) {
    return null
  }

  return (
    <main className="min-h-screen bg-slate-50 px-5 py-8 sm:px-8 lg:px-12 lg:py-12">
      <div className="mx-auto max-w-5xl">
        <header className="flex flex-col gap-5 border-b border-slate-200 pb-7 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-4">
            <div className="flex size-12 items-center justify-center bg-blue-800 text-white">
              <Building2 aria-hidden="true" size={25} />
            </div>
            <div>
              <p className="text-sm font-semibold text-blue-800">
                İBB Yurtları
              </p>
              <h1 className="text-xl font-semibold text-slate-950">
                Yurt Belge Yönetim Sistemi
              </h1>
            </div>
          </div>

          <button
            type="button"
            onClick={logout}
            className="inline-flex h-10 items-center justify-center gap-2 self-start border border-slate-300 bg-white px-4 text-sm font-semibold text-slate-700 hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 sm:self-auto"
          >
            <LogOut aria-hidden="true" size={17} />
            Çıkış Yap
          </button>
        </header>

        <section className="mt-8 border border-slate-200 bg-white shadow-sm">
          <div className="border-b border-slate-200 p-6 sm:p-8">
            <div className="flex items-start gap-4">
              <div className="flex size-11 shrink-0 items-center justify-center bg-slate-100 text-slate-700">
                <UserRound aria-hidden="true" size={22} />
              </div>
              <div>
                <p className="text-sm font-medium text-slate-500">Hoş geldiniz</p>
                <h2 className="mt-1 text-2xl font-semibold tracking-tight text-slate-950">
                  {user.firstName} {user.lastName}
                </h2>
                <p className="mt-2 text-sm text-slate-600">{user.email}</p>
              </div>
            </div>
          </div>

          <dl className="grid gap-px bg-slate-200 sm:grid-cols-2 lg:grid-cols-3">
            <IdentityItem label="Kullanıcı rolü" value={roleLabels[user.role]} />
            {user.adminScope && (
              <IdentityItem
                label="Yönetim kapsamı"
                value={scopeLabels[user.adminScope]}
              />
            )}
            {user.dormitoryName && (
              <IdentityItem label="Bağlı yurt" value={user.dormitoryName} />
            )}
            <div className="bg-white p-6">
              <dt className="text-sm font-medium text-slate-500">Hesap durumu</dt>
              <dd className="mt-2 flex items-center gap-2 text-sm font-semibold text-emerald-700">
                <CheckCircle2 aria-hidden="true" size={18} />
                {user.active ? 'Aktif' : 'Pasif'}
              </dd>
            </div>
          </dl>
        </section>

        <aside className="mt-6 border-l-4 border-blue-700 bg-blue-50 px-5 py-4 text-sm leading-6 text-blue-950">
          Rolünüze özel navigasyon ve yönetim ekranları bir sonraki uygulama
          yerleşimi aşamasında eklenecektir.
        </aside>
      </div>
    </main>
  )
}

interface IdentityItemProps {
  label: string
  value: string
}

function IdentityItem({ label, value }: IdentityItemProps) {
  return (
    <div className="bg-white p-6">
      <dt className="text-sm font-medium text-slate-500">{label}</dt>
      <dd className="mt-2 text-sm font-semibold text-slate-900">{value}</dd>
    </div>
  )
}
