import { useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import {
  Building2,
  Eye,
  EyeOff,
  LoaderCircle,
  LockKeyhole,
  Mail,
} from 'lucide-react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { useAuth } from './useAuth'
import { getApiErrorMessage } from '../../utils/apiError'

const loginSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, 'E-posta adresi zorunludur.')
    .email('Geçerli bir e-posta adresi girin.'),
  password: z.string().min(1, 'Şifre zorunludur.'),
})

type LoginFormValues = z.infer<typeof loginSchema>

export function LoginPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [showPassword, setShowPassword] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  })

  async function onSubmit(values: LoginFormValues): Promise<void> {
    setSubmitError(null)

    try {
      await login(values)
      navigate('/home', { replace: true })
    } catch (error: unknown) {
      setSubmitError(
        getApiErrorMessage(
          error,
          'Giriş yapılamadı. Lütfen bilgilerinizi kontrol edip tekrar deneyin.',
        ),
      )
    }
  }

  return (
    <main className="grid min-h-screen bg-slate-50 lg:grid-cols-[minmax(0,1fr)_minmax(480px,0.72fr)]">
      <section className="hidden border-r border-blue-900/15 bg-blue-950 px-12 py-14 text-white lg:flex lg:flex-col lg:justify-between">
        <div className="flex items-center gap-3">
          <div className="flex size-11 items-center justify-center border border-white/20 bg-white/10">
            <Building2 aria-hidden="true" size={24} />
          </div>
          <div>
            <p className="font-semibold">Yurt Belge Yönetim Sistemi</p>
            <p className="text-sm text-blue-100">İstanbul Büyükşehir Belediyesi</p>
          </div>
        </div>

        <div className="max-w-xl pb-10">
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-blue-200">
            Kurumsal Yönetim Platformu
          </p>
          <h1 className="mt-5 text-4xl font-semibold leading-tight tracking-tight xl:text-5xl">
            Yurt belge süreçlerini güvenli ve düzenli biçimde yönetin.
          </h1>
          <p className="mt-6 max-w-lg text-base leading-7 text-blue-100">
            Öğrenci belgelerinin yüklenmesi, incelenmesi ve kurumsal
            değerlendirme süreçleri için merkezi sistem.
          </p>
        </div>

        <p className="text-sm text-blue-200">
          Yetkili kullanıcı erişimi
        </p>
      </section>

      <section className="flex items-center justify-center px-5 py-10 sm:px-10">
        <div className="w-full max-w-md border border-slate-200 bg-white p-7 shadow-sm sm:p-9">
          <div className="mb-8 lg:hidden">
            <div className="mb-5 flex size-11 items-center justify-center bg-blue-50 text-blue-800">
              <Building2 aria-hidden="true" size={24} />
            </div>
            <p className="text-sm font-semibold text-blue-800">
              İstanbul Büyükşehir Belediyesi
            </p>
          </div>

          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.14em] text-blue-700">
              Güvenli Giriş
            </p>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight text-slate-950">
              Hesabınıza giriş yapın
            </h2>
            <p className="mt-3 text-sm leading-6 text-slate-600">
              Yurt Belge Yönetim Sistemi'ne kurumsal hesap bilgilerinizle
              erişin.
            </p>
          </div>

          <form className="mt-8 space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
            {submitError && (
              <div
                role="alert"
                className="border border-red-200 bg-red-50 px-4 py-3 text-sm leading-6 text-red-800"
              >
                {submitError}
              </div>
            )}

            <div>
              <label
                htmlFor="email"
                className="mb-2 block text-sm font-semibold text-slate-800"
              >
                E-posta
              </label>
              <div className="relative">
                <Mail
                  aria-hidden="true"
                  className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                  size={18}
                />
                <input
                  id="email"
                  type="email"
                  autoComplete="email"
                  aria-invalid={Boolean(errors.email)}
                  aria-describedby={errors.email ? 'email-error' : undefined}
                  className="h-12 w-full border border-slate-300 bg-white pl-11 pr-3 text-sm text-slate-950 outline-none transition focus:border-blue-700 focus:ring-2 focus:ring-blue-700/15"
                  placeholder="ornek@ibb.gov.tr"
                  {...register('email')}
                />
              </div>
              {errors.email && (
                <p id="email-error" className="mt-2 text-sm text-red-700">
                  {errors.email.message}
                </p>
              )}
            </div>

            <div>
              <label
                htmlFor="password"
                className="mb-2 block text-sm font-semibold text-slate-800"
              >
                Şifre
              </label>
              <div className="relative">
                <LockKeyhole
                  aria-hidden="true"
                  className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                  size={18}
                />
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  aria-invalid={Boolean(errors.password)}
                  aria-describedby={errors.password ? 'password-error' : undefined}
                  className="h-12 w-full border border-slate-300 bg-white pl-11 pr-12 text-sm text-slate-950 outline-none transition focus:border-blue-700 focus:ring-2 focus:ring-blue-700/15"
                  placeholder="Şifrenizi girin"
                  {...register('password')}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((visible) => !visible)}
                  className="absolute right-2 top-1/2 flex size-9 -translate-y-1/2 items-center justify-center text-slate-500 hover:text-slate-800 focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-blue-700"
                  aria-label={showPassword ? 'Şifreyi gizle' : 'Şifreyi göster'}
                >
                  {showPassword ? (
                    <EyeOff aria-hidden="true" size={18} />
                  ) : (
                    <Eye aria-hidden="true" size={18} />
                  )}
                </button>
              </div>
              {errors.password && (
                <p id="password-error" className="mt-2 text-sm text-red-700">
                  {errors.password.message}
                </p>
              )}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="flex h-12 w-full items-center justify-center gap-2 bg-blue-700 px-4 text-sm font-semibold text-white transition-colors hover:bg-blue-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 disabled:cursor-not-allowed disabled:bg-blue-400"
            >
              {isSubmitting && (
                <LoaderCircle aria-hidden="true" className="animate-spin" size={18} />
              )}
              {isSubmitting ? 'Giriş yapılıyor...' : 'Giriş Yap'}
            </button>
          </form>
        </div>
      </section>
    </main>
  )
}
