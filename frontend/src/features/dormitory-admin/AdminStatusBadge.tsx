import type { AdminTone } from './adminPresentation'
import { adminToneClasses } from './adminPresentation'

export function AdminStatusBadge({ label, tone }: { label: string; tone: AdminTone }) {
  return <span className={`inline-flex items-center border px-2.5 py-1 text-xs font-semibold ${adminToneClasses[tone]}`}>{label}</span>
}
