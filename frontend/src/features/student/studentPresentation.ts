import type {
  AdmissionStatus,
  DocumentReviewDecision,
  StudentDocumentActionReason,
  StudentDocumentStatus,
  UploadPeriodStatus,
} from '../../types/student'

export type SemanticTone = 'success' | 'info' | 'warning' | 'error' | 'neutral'

export const documentStatusPresentation: Record<
  StudentDocumentStatus,
  { label: string; tone: SemanticTone }
> = {
  UPLOADED: { label: 'Değerlendirme Bekliyor', tone: 'info' },
  APPROVED: { label: 'Onaylandı', tone: 'success' },
  REJECTED: { label: 'Reddedildi', tone: 'error' },
  REVISION_REQUIRED: { label: 'Düzeltme Gerekli', tone: 'warning' },
}

export const admissionStatusPresentation: Record<
  AdmissionStatus,
  { label: string; tone: SemanticTone }
> = {
  PENDING: { label: 'Değerlendirme Bekliyor', tone: 'warning' },
  APPROVED: { label: 'Kabul Onaylandı', tone: 'success' },
  REJECTED: { label: 'Kabul Reddedildi', tone: 'error' },
}

export const uploadPeriodPresentation: Record<
  UploadPeriodStatus,
  { label: string; tone: SemanticTone }
> = {
  NOT_STARTED: { label: 'Henüz Başlamadı', tone: 'warning' },
  OPEN: { label: 'Yüklemeye Açık', tone: 'success' },
  CLOSED: { label: 'Sona Erdi', tone: 'error' },
}

export const reviewDecisionPresentation: Record<
  DocumentReviewDecision,
  { label: string; tone: SemanticTone }
> = {
  APPROVED: { label: 'Onaylandı', tone: 'success' },
  REJECTED: { label: 'Reddedildi', tone: 'error' },
  REVISION_REQUIRED: { label: 'Düzeltme Gerekli', tone: 'warning' },
}

export const actionReasonPresentation: Record<
  StudentDocumentActionReason,
  { label: string; tone: SemanticTone }
> = {
  MISSING: { label: 'Belge Eksik', tone: 'warning' },
  REVISION_REQUIRED: { label: 'Düzeltme Gerekli', tone: 'warning' },
  REJECTED: { label: 'Belge Reddedildi', tone: 'error' },
}

export const toneClasses: Record<SemanticTone, string> = {
  success: 'border-emerald-200 bg-emerald-50 text-emerald-700',
  info: 'border-blue-200 bg-blue-50 text-blue-700',
  warning: 'border-amber-200 bg-amber-50 text-amber-800',
  error: 'border-red-200 bg-red-50 text-red-700',
  neutral: 'border-slate-200 bg-slate-100 text-slate-700',
}
