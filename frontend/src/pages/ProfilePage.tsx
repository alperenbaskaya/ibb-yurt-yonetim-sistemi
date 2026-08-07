import { Building2, Info, Mail, ShieldCheck, UserRound } from 'lucide-react'
import { PageHeader } from '../components/common/PageHeader'
import { StatusBadge } from '../components/common/StatusBadge'
import { UserCategoryBadge } from '../components/common/UserCategoryBadge'
import { useAuth } from '../features/auth/useAuth'
import { getUserCategory, userCategoryLabels } from '../utils/userCategory'

export function ProfilePage() {
  const { user } = useAuth()

  if (!user) {
    return null
  }

  const category = getUserCategory(user)

  if (!category) {
    return null
  }

  return (
    <div>
      <PageHeader
        title="Profil"
        description="Sistemde kayıtlı hesap ve yetki bilgilerinizi görüntüleyin."
      />

      <section className="mt-7 border border-slate-200 bg-white shadow-sm">
        <div className="flex flex-col gap-5 border-b border-slate-200 p-6 sm:flex-row sm:items-center sm:p-8">
          <div className="flex size-14 shrink-0 items-center justify-center bg-blue-100 text-blue-800">
            <UserRound aria-hidden="true" size={27} />
          </div>
          <div className="min-w-0 flex-1">
            <h2 className="text-xl font-semibold text-slate-950">
              {user.firstName} {user.lastName}
            </h2>
            <p className="mt-1 truncate text-sm text-slate-600">{user.email}</p>
          </div>
          <div className="flex flex-wrap gap-2">
            <UserCategoryBadge category={category} />
            <StatusBadge active={user.active} />
          </div>
        </div>

        <dl className="grid gap-px bg-slate-200 sm:grid-cols-2">
          <ProfileField icon={UserRound} label="Ad" value={user.firstName} />
          <ProfileField icon={UserRound} label="Soyad" value={user.lastName} />
          <ProfileField icon={Mail} label="E-posta" value={user.email} />
          <ProfileField
            icon={ShieldCheck}
            label="Rol"
            value={userCategoryLabels[category]}
          />
          {user.adminScope && (
            <ProfileField
              icon={ShieldCheck}
              label="Yönetim kapsamı"
              value={user.adminScope === 'GLOBAL' ? 'Global' : 'Yurt'}
            />
          )}
          {user.dormitoryName && (
            <ProfileField
              icon={Building2}
              label="Bağlı yurt"
              value={user.dormitoryName}
            />
          )}
        </dl>
      </section>

      <aside className="mt-6 flex gap-3 border border-blue-200 bg-blue-50 px-5 py-4 text-sm leading-6 text-blue-950">
        <Info aria-hidden="true" className="mt-0.5 shrink-0" size={18} />
        <p>
          Profil bilgileri bu ekranda yalnızca görüntülenebilir. Mevcut
          backend sürümünde kullanıcının kendi profilini güncellemesine yönelik
          bir servis bulunmamaktadır.
        </p>
      </aside>
    </div>
  )
}

interface ProfileFieldProps {
  icon: typeof UserRound
  label: string
  value: string
}

function ProfileField({ icon: Icon, label, value }: ProfileFieldProps) {
  return (
    <div className="bg-white p-6">
      <dt className="flex items-center gap-2 text-sm font-medium text-slate-500">
        <Icon aria-hidden="true" size={16} />
        {label}
      </dt>
      <dd className="mt-2 text-sm font-semibold text-slate-900">{value}</dd>
    </div>
  )
}
