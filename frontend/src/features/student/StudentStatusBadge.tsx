import type { ReactNode } from 'react'
import type { SemanticTone } from './studentPresentation'
import { toneClasses } from './studentPresentation'

interface StudentStatusBadgeProps {
  label: string
  tone: SemanticTone
  icon?: ReactNode
}

export function StudentStatusBadge({ label, tone, icon }: StudentStatusBadgeProps) {
  return (
    <span className={`inline-flex items-center gap-1.5 border px-2.5 py-1 text-xs font-semibold ${toneClasses[tone]}`}>
      {icon}
      {label}
    </span>
  )
}
