import { CheckCircle2, CircleOff } from 'lucide-react'

interface StatusBadgeProps {
  active: boolean
}

export function StatusBadge({ active }: StatusBadgeProps) {
  const Icon = active ? CheckCircle2 : CircleOff

  return (
    <span
      className={
        active
          ? 'inline-flex items-center gap-1.5 border border-emerald-200 bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700'
          : 'inline-flex items-center gap-1.5 border border-slate-200 bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-600'
      }
    >
      <Icon aria-hidden="true" size={14} />
      {active ? 'Aktif' : 'Pasif'}
    </span>
  )
}
