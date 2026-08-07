import { zodResolver } from '@hookform/resolvers/zod'
import { LoaderCircle, UserPlus } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import type { CreateReviewerRequest } from '../../types/dormitoryAdmin'
import { getApiErrorMessage } from '../../utils/apiError'

const schema = z.object({
  firstName: z.string().trim().min(1, 'Ad zorunludur.').max(100, 'Ad en fazla 100 karakter olabilir.'),
  lastName: z.string().trim().min(1, 'Soyad zorunludur.').max(100, 'Soyad en fazla 100 karakter olabilir.'),
  email: z.string().trim().min(1, 'E-posta zorunludur.').email('Geçerli bir e-posta adresi girin.').max(150, 'E-posta en fazla 150 karakter olabilir.'),
  password: z.string().min(8, 'Şifre en az 8 karakter olmalıdır.').max(72, 'Şifre en fazla 72 karakter olabilir.'),
  active: z.boolean(),
})
type Values = z.infer<typeof schema>

export function CreateReviewerForm({ dormitoryId, onCreate }: { dormitoryId: number; onCreate: (request: CreateReviewerRequest) => Promise<void> }) {
  const { register, handleSubmit, reset, setError, formState: { errors, isSubmitting } } = useForm<Values>({ resolver: zodResolver(schema), defaultValues: { firstName: '', lastName: '', email: '', password: '', active: true } })
  const submit = async (values: Values) => {
    try {
      await onCreate({ ...values, firstName: values.firstName.trim(), lastName: values.lastName.trim(), email: values.email.trim(), role: 'REVIEWER', adminScope: null, dormitoryId })
      reset()
    } catch (error: unknown) {
      setError('root', { message: getApiErrorMessage(error, 'Değerlendirici oluşturulamadı.') })
    }
  }
  return <form onSubmit={handleSubmit(submit)} noValidate className="border border-slate-200 bg-white p-5 shadow-sm sm:p-6"><h2 className="text-lg font-semibold text-slate-950">Yeni değerlendirici</h2><p className="mt-1 text-sm text-slate-600">Kullanıcı yalnızca bağlı olduğunuz yurda atanır.</p><div className="mt-5 grid gap-4 sm:grid-cols-2">{([['firstName','Ad'],['lastName','Soyad'],['email','E-posta'],['password','Geçici şifre']] as const).map(([name,label]) => <div key={name} className={name === 'email' || name === 'password' ? 'sm:col-span-2' : ''}><label htmlFor={`reviewer-${name}`} className="block text-sm font-semibold text-slate-800">{label}</label><input id={`reviewer-${name}`} type={name === 'password' ? 'password' : name === 'email' ? 'email' : 'text'} {...register(name)} disabled={isSubmitting} aria-invalid={errors[name] ? 'true' : 'false'} aria-describedby={errors[name] ? `reviewer-${name}-error` : undefined} className="mt-2 min-h-11 w-full border border-slate-300 px-3 py-2 text-sm focus:border-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-100 disabled:opacity-60" />{errors[name] && <p id={`reviewer-${name}-error`} className="mt-2 text-sm text-red-700" role="alert">{errors[name]?.message}</p>}</div>)}</div><label className="mt-4 inline-flex items-center gap-2 text-sm font-medium text-slate-700"><input type="checkbox" {...register('active')} disabled={isSubmitting} className="size-4 accent-blue-800" /> Kullanıcı aktif oluşturulsun</label>{errors.root && <p className="mt-4 border border-red-200 bg-red-50 p-3 text-sm text-red-800" role="alert">{errors.root.message}</p>}<button type="submit" disabled={isSubmitting} className="mt-5 inline-flex min-h-10 items-center gap-2 bg-blue-800 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-900 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 disabled:opacity-60">{isSubmitting ? <LoaderCircle className="animate-spin" aria-hidden="true" size={17} /> : <UserPlus aria-hidden="true" size={17} />} {isSubmitting ? 'Oluşturuluyor...' : 'Değerlendirici oluştur'}</button></form>
}
