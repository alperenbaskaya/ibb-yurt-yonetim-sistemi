import {
  BadgeCheck,
  CircleCheckBig,
  CircleX,
  FileCheck2,
  FileUp,
  RefreshCw,
  TriangleAlert,
  type LucideIcon,
} from 'lucide-react'
import type { NotificationType } from '../../types/notification'

export type NotificationTone = 'success' | 'error' | 'warning' | 'info'

interface NotificationPresentation {
  label: string
  icon: LucideIcon
  tone: NotificationTone
}

export const notificationPresentation: Record<
  NotificationType,
  NotificationPresentation
> = {
  ADMISSION_APPROVED: {
    label: 'Kabul onaylandı',
    icon: BadgeCheck,
    tone: 'success',
  },
  ADMISSION_REJECTED: {
    label: 'Kabul reddedildi',
    icon: CircleX,
    tone: 'error',
  },
  DOCUMENT_UPLOADED: {
    label: 'Belge yüklendi',
    icon: FileUp,
    tone: 'info',
  },
  DOCUMENT_REUPLOADED: {
    label: 'Belge yeniden yüklendi',
    icon: RefreshCw,
    tone: 'info',
  },
  DOCUMENT_APPROVED: {
    label: 'Belge onaylandı',
    icon: FileCheck2,
    tone: 'success',
  },
  DOCUMENT_REJECTED: {
    label: 'Belge reddedildi',
    icon: CircleX,
    tone: 'error',
  },
  DOCUMENT_REVISION_REQUIRED: {
    label: 'Belge revizyonu gerekli',
    icon: TriangleAlert,
    tone: 'warning',
  },
  DOCUMENT_PROCESS_COMPLETED: {
    label: 'Belge süreci tamamlandı',
    icon: CircleCheckBig,
    tone: 'success',
  },
}

export const notificationToneClasses: Record<
  NotificationTone,
  { icon: string; label: string }
> = {
  success: {
    icon: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
    label: 'bg-emerald-50 text-emerald-700',
  },
  error: {
    icon: 'bg-red-50 text-red-700 ring-red-200',
    label: 'bg-red-50 text-red-700',
  },
  warning: {
    icon: 'bg-amber-50 text-amber-700 ring-amber-200',
    label: 'bg-amber-50 text-amber-800',
  },
  info: {
    icon: 'bg-blue-50 text-blue-700 ring-blue-200',
    label: 'bg-blue-50 text-blue-700',
  },
}
