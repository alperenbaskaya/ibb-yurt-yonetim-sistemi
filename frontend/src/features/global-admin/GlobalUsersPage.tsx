import { zodResolver } from '@hookform/resolvers/zod'
import { LoaderCircle, Search, UserRound, X } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { useForm, type UseFormReturn } from 'react-hook-form'
import { toast } from 'sonner'
import { z } from 'zod'
import { PageHeader } from '../../components/common/PageHeader'
import type { UserResponse } from '../../types/globalAdmin'
import { getApiErrorMessage } from '../../utils/apiError'
import { useAuth } from '../auth/useAuth'
import { AdminPageState } from '../dormitory-admin/AdminPageState'
import { AdminStatusBadge } from '../dormitory-admin/AdminStatusBadge'
import { useDormitories, useGlobalUsers, useUserMutations } from './globalAdminQueries'

const userSchema = z
  .object({
    firstName: z.string().trim().min(1).max(100),
    lastName: z.string().trim().min(1).max(100),
    email: z.string().trim().email().max(150),
    password: z.string(),
    role: z.enum(['ADMIN', 'REVIEWER', 'STUDENT']),
    adminScope: z.enum(['GLOBAL', 'DORMITORY']).nullable(),
    dormitoryId: z.number().nullable(),
    active: z.boolean(),
  })
  .superRefine((values, context) => {
    if (values.password && (values.password.length < 8 || values.password.length > 72)) {
      context.addIssue({
        code: 'custom',
        path: ['password'],
        message: 'Şifre 8–72 karakter olmalıdır.',
      })
    }
    if (values.role === 'ADMIN' && !values.adminScope) {
      context.addIssue({
        code: 'custom',
        path: ['adminScope'],
        message: 'Admin kapsamı zorunludur.',
      })
    }
    const needsDormitory =
      values.role === 'REVIEWER' ||
      (values.role === 'ADMIN' && values.adminScope === 'DORMITORY')
    if (needsDormitory && !values.dormitoryId) {
      context.addIssue({
        code: 'custom',
        path: ['dormitoryId'],
        message: 'Yurt seçimi zorunludur.',
      })
    }
  })

type UserFormValues = z.infer<typeof userSchema>

const emptyUser: UserFormValues = {
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  role: 'STUDENT',
  adminScope: null,
  dormitoryId: null,
  active: true,
}

const roleLabels = {
  ADMIN: 'Admin',
  REVIEWER: 'Değerlendirici',
  STUDENT: 'Öğrenci',
} as const

function valuesFromUser(user: UserResponse): UserFormValues {
  return {
    firstName: user.firstName,
    lastName: user.lastName,
    email: user.email,
    password: '',
    role: user.role,
    adminScope: user.adminScope,
    dormitoryId: user.dormitoryId,
    active: user.active,
  }
}

function normalizeUserRequest(values: UserFormValues) {
  const adminScope = values.role === 'ADMIN' ? values.adminScope : null
  let dormitoryId =
    values.role === 'REVIEWER' ||
    (values.role === 'ADMIN' && adminScope === 'DORMITORY')
      ? values.dormitoryId
      : null

  if (values.role === 'ADMIN' && adminScope === 'GLOBAL') {
    dormitoryId = null
  }

  return {
    firstName: values.firstName.trim(),
    lastName: values.lastName.trim(),
    email: values.email.trim(),
    role: values.role,
    adminScope,
    dormitoryId,
    active: values.active,
  }
}

export function GlobalUsersPage() {
  const { user } = useAuth()
  const userId = user?.userId ?? 0
  const usersQuery = useGlobalUsers(userId)
  const dormitoriesQuery = useDormitories(userId)
  const mutations = useUserMutations(userId)
  const [searchText, setSearchText] = useState('')
  const [selectedUser, setSelectedUser] = useState<UserResponse | null>(null)

  const createForm = useForm<UserFormValues>({
    resolver: zodResolver(userSchema),
    defaultValues: emptyUser,
  })
  const editForm = useForm<UserFormValues>({
    resolver: zodResolver(userSchema),
    defaultValues: emptyUser,
  })

  useEffect(() => {
    if (selectedUser) {
      editForm.reset(valuesFromUser(selectedUser))
    }
  }, [editForm, selectedUser])

  const normalizedSearch = searchText.trim().toLocaleLowerCase('tr-TR')
  const searchResults = useMemo(() => {
    if (normalizedSearch.length < 2) {
      return []
    }

    return (usersQuery.data ?? [])
      .filter((candidate) => {
        const firstName = candidate.firstName.toLocaleLowerCase('tr-TR')
        const lastName = candidate.lastName.toLocaleLowerCase('tr-TR')
        const fullName = `${firstName} ${lastName}`
        const email = candidate.email.toLocaleLowerCase('tr-TR')
        return (
          firstName.includes(normalizedSearch) ||
          lastName.includes(normalizedSearch) ||
          fullName.includes(normalizedSearch) ||
          email.includes(normalizedSearch)
        )
      })
      .slice(0, 10)
  }, [normalizedSearch, usersQuery.data])

  async function createUser(values: UserFormValues) {
    if (!values.password) {
      createForm.setError('password', { message: 'Şifre zorunludur.' })
      return
    }

    try {
      const request = normalizeUserRequest(values)
      await mutations.create.mutateAsync({ ...request, password: values.password })
      toast.success('Kullanıcı oluşturuldu.')
      createForm.reset(emptyUser)
    } catch (error: unknown) {
      createForm.setError('root', {
        message: getApiErrorMessage(error, 'Kullanıcı kaydedilemedi.'),
      })
    }
  }

  async function updateUser(values: UserFormValues) {
    if (!selectedUser) {
      return
    }

    try {
      const updatedUser = await mutations.update.mutateAsync({
        id: selectedUser.id,
        request: normalizeUserRequest(values),
      })
      setSelectedUser(updatedUser)
      toast.success('Kullanıcı güncellendi.')
    } catch (error: unknown) {
      editForm.setError('root', {
        message: getApiErrorMessage(error, 'Kullanıcı kaydedilemedi.'),
      })
    }
  }

  function selectUser(nextUser: UserResponse) {
    setSelectedUser(nextUser)
    editForm.clearErrors()
  }

  return (
    <section>
      <PageHeader
        title="Kullanıcılar"
        description="Sistem kullanıcılarını rol, admin kapsamı, yurt ataması ve aktiflik bilgileriyle yönetin."
      />

      <div className="mt-6 grid items-start gap-6 xl:grid-cols-2">
        <form
          onSubmit={createForm.handleSubmit(createUser)}
          className="border border-slate-200 bg-white p-5 shadow-sm sm:p-6"
          noValidate
        >
          <h2 className="text-lg font-semibold text-slate-950">Yeni Kullanıcı</h2>
          <p className="mt-1 text-sm text-slate-600">Sisteme yeni bir kullanıcı hesabı ekleyin.</p>
          <UserFields
            form={createForm}
            idPrefix="create-user"
            dormitories={dormitoriesQuery.data ?? []}
            showPassword
          />
          <button
            type="submit"
            disabled={createForm.formState.isSubmitting}
            className="mt-5 inline-flex min-h-10 items-center justify-center gap-2 bg-blue-800 px-4 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60"
          >
            {createForm.formState.isSubmitting && (
              <LoaderCircle aria-hidden="true" className="animate-spin" size={16} />
            )}
            Kaydet
          </button>
        </form>

        <section className="border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
          <h2 className="text-lg font-semibold text-slate-950">Kullanıcı Düzenle</h2>
          <p className="mt-1 text-sm text-slate-600">
            Kullanıcıyı arayın, seçin ve mevcut bilgilerini güncelleyin.
          </p>

          <div className="relative mt-5">
            <label className="sr-only" htmlFor="user-search">Kullanıcı ara</label>
            <Search
              aria-hidden="true"
              className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
              size={18}
            />
            <input
              id="user-search"
              type="search"
              value={searchText}
              onChange={(event) => setSearchText(event.target.value)}
              className="min-h-11 w-full border border-slate-300 bg-white pl-10 pr-10 text-sm text-slate-950 outline-none focus:border-blue-700 focus:ring-2 focus:ring-blue-100"
              placeholder="Ad, soyad veya e-posta ile kullanıcı ara"
              autoComplete="off"
            />
            {searchText && (
              <button
                type="button"
                onClick={() => setSearchText('')}
                className="absolute right-1 top-1/2 flex size-9 -translate-y-1/2 items-center justify-center text-slate-500 hover:bg-slate-100 hover:text-slate-900 focus-visible:outline-2 focus-visible:outline-blue-700"
                aria-label="Aramayı temizle"
              >
                <X aria-hidden="true" size={17} />
              </button>
            )}
          </div>

          <div className="mt-3">
            {usersQuery.isLoading && (
              <AdminPageState
                state="loading"
                title="Kullanıcılar yükleniyor"
                message="Kullanıcı kayıtları alınıyor..."
              />
            )}
            {usersQuery.isError && (
              <AdminPageState
                state="error"
                title="Kullanıcılar alınamadı"
                message={getApiErrorMessage(usersQuery.error, 'Kullanıcılar alınamadı.')}
                onRetry={() => void usersQuery.refetch()}
              />
            )}
            {usersQuery.isSuccess && usersQuery.data.length === 0 && (
              <AdminPageState state="empty" title="Kullanıcı yok" message="Kullanıcı bulunmuyor." />
            )}
            {usersQuery.isSuccess && usersQuery.data.length > 0 && normalizedSearch.length < 2 && (
              <p className="border border-slate-200 bg-slate-50 px-4 py-5 text-sm text-slate-600">
                Düzenlemek istediğiniz kullanıcıyı arayın. En az 2 karakter girin.
              </p>
            )}
            {usersQuery.isSuccess && normalizedSearch.length >= 2 && searchResults.length === 0 && (
              <p className="border border-slate-200 bg-slate-50 px-4 py-5 text-sm text-slate-600">
                Aramanızla eşleşen kullanıcı bulunamadı.
              </p>
            )}
            {usersQuery.isSuccess && searchResults.length > 0 && (
              <ul className="max-h-80 space-y-2 overflow-y-auto pr-1" aria-label="Kullanıcı arama sonuçları">
                {searchResults.map((result) => {
                  const isSelected = selectedUser?.id === result.id
                  return (
                    <li key={result.id}>
                      <button
                        type="button"
                        onClick={() => selectUser(result)}
                        className={`flex w-full items-start gap-3 border p-3 text-left transition-colors focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-blue-700 ${
                          isSelected
                            ? 'border-blue-700 bg-blue-50'
                            : 'border-slate-200 bg-white hover:bg-slate-50'
                        }`}
                        aria-pressed={isSelected}
                      >
                        <span className="mt-0.5 flex size-9 shrink-0 items-center justify-center bg-blue-100 text-blue-800">
                          <UserRound aria-hidden="true" size={18} />
                        </span>
                        <span className="min-w-0 flex-1">
                          <span className="block font-semibold text-slate-950">
                            {result.firstName} {result.lastName}
                          </span>
                          <span className="block truncate text-sm text-slate-600">{result.email}</span>
                          <span className="mt-1 block text-xs text-slate-500">
                            {roleLabels[result.role]}
                            {result.adminScope ? ` · ${result.adminScope}` : ''}
                            {result.dormitoryName ? ` · ${result.dormitoryName}` : ''}
                          </span>
                        </span>
                        <AdminStatusBadge
                          label={result.active ? 'Aktif' : 'Pasif'}
                          tone={result.active ? 'success' : 'neutral'}
                        />
                      </button>
                    </li>
                  )
                })}
              </ul>
            )}
          </div>

          {selectedUser ? (
            <form
              onSubmit={editForm.handleSubmit(updateUser)}
              className="mt-6 border-t border-slate-200 pt-5"
              noValidate
            >
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h3 className="font-semibold text-slate-950">
                    {selectedUser.firstName} {selectedUser.lastName}
                  </h3>
                  <p className="text-sm text-slate-600">{selectedUser.email}</p>
                </div>
                <button
                  type="button"
                  onClick={() => setSelectedUser(null)}
                  className="border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
                >
                  Seçimi Kaldır
                </button>
              </div>

              <UserFields
                form={editForm}
                idPrefix="edit-user"
                dormitories={dormitoriesQuery.data ?? []}
              />
              <button
                type="submit"
                disabled={editForm.formState.isSubmitting}
                className="mt-5 inline-flex min-h-10 items-center justify-center gap-2 bg-blue-800 px-4 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60"
              >
                {editForm.formState.isSubmitting && (
                  <LoaderCircle aria-hidden="true" className="animate-spin" size={16} />
                )}
                Değişiklikleri Kaydet
              </button>
            </form>
          ) : (
            usersQuery.isSuccess && usersQuery.data.length > 0 && (
              <p className="mt-6 border-t border-slate-200 pt-5 text-sm text-slate-500">
                Düzenleme formunu açmak için arama sonuçlarından bir kullanıcı seçin.
              </p>
            )
          )}
        </section>
      </div>
    </section>
  )
}

interface UserFieldsProps {
  form: UseFormReturn<UserFormValues>
  idPrefix: string
  dormitories: Array<{ id: number; name: string; active: boolean }>
  showPassword?: boolean
}

function UserFields({ form, idPrefix, dormitories, showPassword = false }: UserFieldsProps) {
  const role = form.watch('role')
  const adminScope = form.watch('adminScope')
  const needsDormitory =
    role === 'REVIEWER' || (role === 'ADMIN' && adminScope === 'DORMITORY')
  const fieldClassName =
    'mt-1 min-h-10 w-full border border-slate-300 bg-white px-3 text-sm text-slate-950 outline-none focus:border-blue-700 focus:ring-2 focus:ring-blue-100'

  return (
    <>
      {(
        [
          ['firstName', 'Ad'],
          ['lastName', 'Soyad'],
          ['email', 'E-posta'],
        ] as const
      ).map(([name, label]) => (
        <div className="mt-4" key={name}>
          <label className="text-sm font-semibold text-slate-800" htmlFor={`${idPrefix}-${name}`}>
            {label}
          </label>
          <input id={`${idPrefix}-${name}`} {...form.register(name)} className={fieldClassName} />
          {form.formState.errors[name] && (
            <p className="mt-1 text-sm text-red-700">{form.formState.errors[name]?.message}</p>
          )}
        </div>
      ))}

      {showPassword && (
        <div className="mt-4">
          <label className="text-sm font-semibold text-slate-800" htmlFor={`${idPrefix}-password`}>
            Şifre
          </label>
          <input
            id={`${idPrefix}-password`}
            type="password"
            autoComplete="new-password"
            {...form.register('password')}
            className={fieldClassName}
          />
          <p className="mt-1 text-xs text-slate-500">8–72 karakter</p>
          {form.formState.errors.password && (
            <p className="mt-1 text-sm text-red-700">{form.formState.errors.password.message}</p>
          )}
        </div>
      )}

      <div className="mt-4">
        <label className="text-sm font-semibold text-slate-800" htmlFor={`${idPrefix}-role`}>
          Rol
        </label>
        <select id={`${idPrefix}-role`} {...form.register('role')} className={fieldClassName}>
          <option value="STUDENT">Öğrenci</option>
          <option value="REVIEWER">Değerlendirici</option>
          <option value="ADMIN">Admin</option>
        </select>
      </div>

      {role === 'ADMIN' && (
        <div className="mt-4">
          <label className="text-sm font-semibold text-slate-800" htmlFor={`${idPrefix}-scope`}>
            Admin kapsamı
          </label>
          <select
            id={`${idPrefix}-scope`}
            {...form.register('adminScope', { setValueAs: (value) => value || null })}
            className={fieldClassName}
          >
            <option value="">Seçin</option>
            <option value="GLOBAL">GLOBAL</option>
            <option value="DORMITORY">DORMITORY</option>
          </select>
          {form.formState.errors.adminScope && (
            <p className="mt-1 text-sm text-red-700">{form.formState.errors.adminScope.message}</p>
          )}
        </div>
      )}

      {needsDormitory && (
        <div className="mt-4">
          <label className="text-sm font-semibold text-slate-800" htmlFor={`${idPrefix}-dormitory`}>
            Yurt
          </label>
          <select
            id={`${idPrefix}-dormitory`}
            {...form.register('dormitoryId', {
              setValueAs: (value) => (value ? Number(value) : null),
            })}
            className={fieldClassName}
          >
            <option value="">Seçin</option>
            {dormitories
              .filter((dormitory) => dormitory.active)
              .map((dormitory) => (
                <option key={dormitory.id} value={dormitory.id}>
                  {dormitory.name}
                </option>
              ))}
          </select>
          {form.formState.errors.dormitoryId && (
            <p className="mt-1 text-sm text-red-700">{form.formState.errors.dormitoryId.message}</p>
          )}
        </div>
      )}

      <label className="mt-4 flex items-center gap-2 text-sm font-medium text-slate-800">
        <input type="checkbox" {...form.register('active')} />
        Aktif
      </label>

      {form.formState.errors.root && (
        <p className="mt-3 border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {form.formState.errors.root.message}
        </p>
      )}
    </>
  )
}
