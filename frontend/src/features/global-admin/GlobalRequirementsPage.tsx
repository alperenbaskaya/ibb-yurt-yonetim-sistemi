import { zodResolver } from '@hookform/resolvers/zod'
import { Link2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { z } from 'zod'
import { PageHeader } from '../../components/common/PageHeader'
import { getApiErrorMessage } from '../../utils/apiError'
import { useAuth } from '../auth/useAuth'
import { AdminPageState } from '../dormitory-admin/AdminPageState'
import { AdminStatusBadge } from '../dormitory-admin/AdminStatusBadge'
import { useDocumentTypes, useDormitoryTerms, useRequirementMutations, useRequirements } from './globalAdminQueries'

const schema = z.object({ documentTypeId: z.number().positive('Belge türü seçin.'), required: z.boolean() })
type Values = z.infer<typeof schema>

export function GlobalRequirementsPage() {
  const { user } = useAuth()
  const userId = user?.userId ?? 0
  const terms = useDormitoryTerms(userId)
  const types = useDocumentTypes(userId)
  const [termId, setTermId] = useState<number | null>(null)
  useEffect(() => { if (termId === null && terms.data?.length) setTermId(terms.data.find((term) => term.active)?.id ?? terms.data[0].id) }, [termId, terms.data])
  const query = useRequirements(userId, termId)
  const mutations = useRequirementMutations(userId, termId ?? 0)
  const form = useForm<Values>({ resolver: zodResolver(schema), defaultValues: { documentTypeId: 0, required: true } })
  const submit = async (values: Values) => { if (termId === null) return; try { await mutations.create.mutateAsync({ dormitoryTermId: termId, ...values }); toast.success('Gereksinim oluşturuldu.'); form.reset() } catch (error: unknown) { form.setError('root', { message: getApiErrorMessage(error, 'Gereksinim oluşturulamadı.') }) } }
  const toggle = async (id: number, required: boolean) => { try { await mutations.update.mutateAsync({ id, required: !required }); toast.success('Zorunluluk güncellendi.') } catch (error: unknown) { toast.error(getApiErrorMessage(error, 'Gereksinim güncellenemedi.')) } }

  return <section><PageHeader title="Belge Gereksinimleri" description="Yurt dönemi ile belge türü arasındaki gereksinim ilişkisini yönetin."/><div className="mt-6"><label className="text-sm font-semibold">Yurt dönemi</label><select value={termId??''} onChange={event=>setTermId(event.target.value?Number(event.target.value):null)} className="ml-3 min-h-10 border px-3"><option value="">Seçin</option>{terms.data?.map(term=><option key={term.id} value={term.id}>{term.name}{term.active?' (Aktif)':''}</option>)}</select></div>{termId!==null&&<div className="mt-6 grid gap-6 xl:grid-cols-[.8fr_1.2fr]"><form onSubmit={form.handleSubmit(submit)} className="h-fit border bg-white p-5"><h2 className="font-semibold">Yeni gereksinim</h2><label className="mt-4 block text-sm font-semibold">Belge türü</label><select {...form.register('documentTypeId',{valueAsNumber:true})} className="mt-1 min-h-10 w-full border px-3"><option value="0">Seçin</option>{types.data?.filter(type=>type.active).map(type=><option key={type.id} value={type.id}>{type.name}</option>)}</select><label className="mt-4 flex gap-2 text-sm"><input type="checkbox" {...form.register('required')}/> Zorunlu belge</label>{form.formState.errors.documentTypeId&&<p className="text-sm text-red-700">{form.formState.errors.documentTypeId.message}</p>}{form.formState.errors.root&&<p className="text-sm text-red-700">{form.formState.errors.root.message}</p>}<button className="mt-4 bg-blue-800 px-4 py-2 text-sm text-white">Ekle</button></form><div>{query.isLoading&&<AdminPageState state="loading" title="Gereksinimler yükleniyor" message="Kayıtlar alınıyor..."/>}{query.isError&&<AdminPageState state="error" title="Gereksinimler alınamadı" message={getApiErrorMessage(query.error,'Gereksinimler alınamadı.')} onRetry={()=>void query.refetch()}/>} {query.isSuccess&&query.data.length===0&&<AdminPageState state="empty" title="Gereksinim yok" message="Seçilen dönem için gereksinim bulunmuyor."/>}{query.isSuccess&&<div className="space-y-3">{query.data.map(item=><article key={item.id} className="border bg-white p-4"><div className="flex justify-between gap-3"><div className="flex gap-3"><Link2 className="text-blue-700"/><div><h3 className="font-semibold">{item.documentTypeName}</h3><p className="text-sm text-slate-600">{item.dormitoryTermName} · {item.documentTypeDescription||'Açıklama yok'}</p></div></div><div className="flex flex-col gap-2"><AdminStatusBadge label={item.required?'Zorunlu':'İsteğe bağlı'} tone={item.required?'warning':'neutral'}/><AdminStatusBadge label={item.documentTypeActive?'Tür aktif':'Tür pasif'} tone={item.documentTypeActive?'success':'neutral'}/></div></div><button onClick={()=>void toggle(item.id,item.required)} className="mt-3 border px-3 py-2 text-sm">Zorunluluğu değiştir</button></article>)}</div>}</div></div>}<p className="mt-5 text-sm text-slate-500">Backend requirement silme endpoint’i sunmaz.</p></section>
}
