import { LoaderCircle, X } from 'lucide-react'

interface AdmissionApprovalDialogProps {
  title: string
  description: string
  names?: string[]
  confirmLabel: string
  pending: boolean
  errorMessage: string | null
  onCancel: () => void
  onConfirm: () => void
}

export function AdmissionApprovalDialog({ title, description, names, confirmLabel, pending, errorMessage, onCancel, onConfirm }: AdmissionApprovalDialogProps) {
  return <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/45 p-4">
    <section role="dialog" aria-modal="true" aria-labelledby="admission-dialog-title" className="w-full max-w-lg border border-slate-200 bg-white p-6 shadow-2xl">
      <div className="flex items-start justify-between gap-4">
        <div><h2 id="admission-dialog-title" className="text-lg font-semibold text-slate-950">{title}</h2><p className="mt-2 text-sm text-slate-600">{description}</p></div>
        <button type="button" aria-label="Pencereyi kapat" disabled={pending} onClick={onCancel} className="p-1 text-slate-500 hover:text-slate-800"><X size={20}/></button>
      </div>
      {names && <ul className="mt-4 max-h-64 list-disc space-y-1 overflow-y-auto border-y border-slate-200 py-3 pl-6 text-sm text-slate-800">{names.map((name, index)=><li key={`${index}-${name}`}>{name}</li>)}</ul>}
      {errorMessage && <p className="mt-4 border border-red-200 bg-red-50 p-3 text-sm text-red-700">{errorMessage}</p>}
      <div className="mt-6 flex justify-end gap-2"><button type="button" disabled={pending} onClick={onCancel} className="border border-slate-300 px-4 py-2 text-sm font-semibold">İptal</button><button type="button" disabled={pending} onClick={onConfirm} className="bg-emerald-700 px-4 py-2 text-sm font-semibold text-white disabled:opacity-60">{pending&&<LoaderCircle className="mr-1 inline animate-spin" size={16}/>} {confirmLabel}</button></div>
    </section>
  </div>
}
