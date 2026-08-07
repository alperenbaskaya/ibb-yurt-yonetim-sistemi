import { zodResolver } from '@hookform/resolvers/zod'
import { Building2, Pencil } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { z } from 'zod'
import { PageHeader } from '../../components/common/PageHeader'
import type { DormitoryResponse } from '../../types/globalAdmin'
import { getApiErrorMessage } from '../../utils/apiError'
import { useAuth } from '../auth/useAuth'
import { AdminPageState } from '../dormitory-admin/AdminPageState'
import { AdminStatusBadge } from '../dormitory-admin/AdminStatusBadge'
import { useDormitories, useDormitoryMutations } from './globalAdminQueries'

const schema = z.object({
  name: z.string().trim().min(1, 'Yurt adı zorunludur.').max(150),
  address: z.string().max(500).optional(),
  capacity: z.number().int().positive('Kapasite sıfırdan büyük olmalıdır.'),
  active: z.boolean(),
})
type Values = z.infer<typeof schema>

export function GlobalDormitoriesPage() {
  const { user } = useAuth()
  const userId = user?.userId ?? 0
  const query = useDormitories(userId)
  const mutations = useDormitoryMutations(userId)
  const [editing, setEditing] = useState<DormitoryResponse | null>(null)
  const form = useForm<Values>({ resolver: zodResolver(schema), defaultValues: { name: '', address: '', capacity: 1, active: true } })

  useEffect(() => {
    form.reset(editing ? { name: editing.name, address: editing.address ?? '', capacity: editing.capacity, active: editing.active } : { name: '', address: '', capacity: 1, active: true })
  }, [editing, form.reset])

  const submit = async (values: Values) => {
    try {
      const base = { name: values.name.trim(), address: values.address?.trim() || null, capacity: values.capacity }
      if (editing) await mutations.update.mutateAsync({ id: editing.id, request: { ...base, active: values.active } })
      else await mutations.create.mutateAsync(base)
      toast.success(editing ? 'Yurt güncellendi.' : 'Yurt oluşturuldu.')
      setEditing(null)
      form.reset()
    } catch (error: unknown) {
      form.setError('root', { message: getApiErrorMessage(error, 'Yurt kaydedilemedi.') })
    }
  }

  return <section><PageHeader title="Yurtlar" description="Sistemdeki yurtları oluşturun, güncelleyin ve aktiflik durumlarını yönetin." /><div className="mt-6 grid gap-6 xl:grid-cols-[.8fr_1.2fr]"><form onSubmit={form.handleSubmit(submit)} className="h-fit border bg-white p-5"><h2 className="font-semibold">{editing ? 'Yurdu düzenle' : 'Yeni yurt'}</h2><label className="mt-4 block text-sm font-semibold">Yurt adı</label><input {...form.register('name')} className="mt-1 min-h-10 w-full border px-3"/><label className="mt-4 block text-sm font-semibold">Adres</label><input {...form.register('address')} className="mt-1 min-h-10 w-full border px-3"/><label className="mt-4 block text-sm font-semibold">Kapasite</label><input type="number" {...form.register('capacity',{valueAsNumber:true})} className="mt-1 min-h-10 w-full border px-3"/>{form.formState.errors.capacity&&<p className="text-sm text-red-700">{form.formState.errors.capacity.message}</p>}{editing&&<label className="mt-4 flex gap-2 text-sm"><input type="checkbox" {...form.register('active')}/> Aktif</label>}{form.formState.errors.root&&<p className="mt-3 text-sm text-red-700">{form.formState.errors.root.message}</p>}<div className="mt-4 flex gap-2"><button disabled={form.formState.isSubmitting} className="bg-blue-800 px-4 py-2 text-sm text-white">Kaydet</button>{editing&&<button type="button" onClick={()=>setEditing(null)} className="border px-3">İptal</button>}</div></form><div>{query.isLoading&&<AdminPageState state="loading" title="Yurtlar yükleniyor" message="Yurtlar alınıyor..."/>}{query.isError&&<AdminPageState state="error" title="Yurtlar alınamadı" message={getApiErrorMessage(query.error,'Yurtlar alınamadı.')} onRetry={()=>void query.refetch()}/>} {query.isSuccess&&query.data.length===0&&<AdminPageState state="empty" title="Yurt yok" message="Sistemde yurt bulunmuyor."/>}{query.isSuccess&&<div className="space-y-3">{query.data.map(item=><article key={item.id} className="border bg-white p-4"><div className="flex justify-between gap-3"><div className="flex gap-3"><Building2 className="text-blue-700"/><div><h3 className="font-semibold">{item.name}</h3><p className="text-sm text-slate-600">{item.address||'Adres belirtilmemiş'} · Kapasite {item.capacity}</p></div></div><AdminStatusBadge label={item.active?'Aktif':'Pasif'} tone={item.active?'success':'neutral'}/></div><button onClick={()=>setEditing(item)} className="mt-3 inline-flex gap-1 border px-3 py-2 text-sm"><Pencil size={16}/>Düzenle</button></article>)}</div>}</div></div><p className="mt-5 text-sm text-slate-500">Backend yurt silme endpoint’i sunmaz.</p></section>
}
