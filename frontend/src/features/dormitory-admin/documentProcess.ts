import type { StudentDocumentResponse, StudentDocumentStatus } from '../../types/student'
import type { TermDocumentRequirementResponse } from '../../types/globalAdmin'
import type { AdminTone } from './adminPresentation'

export type DocumentProcessState = 'COMPLETED' | 'REVISION_REQUIRED' | 'REJECTED' | 'WAITING'

export interface DocumentProcessSummary {
  approved: number
  total: number
  missing: number
  revisionRequired: number
  rejected: number
  waitingReview: number
  state: DocumentProcessState
}

export const documentProcessPresentation: Record<DocumentProcessState, { label: string; tone: AdminTone }> = {
  COMPLETED: { label: 'Tamamlandı', tone: 'success' },
  REVISION_REQUIRED: { label: 'Düzeltme Gerekli', tone: 'warning' },
  REJECTED: { label: 'Reddedilen Belge Var', tone: 'error' },
  WAITING: { label: 'Bekliyor', tone: 'info' },
}

export function summarizeDocumentProcess(
  requirements: TermDocumentRequirementResponse[],
  documents: StudentDocumentResponse[],
): DocumentProcessSummary {
  const required = requirements.filter((requirement) => requirement.required)
  const documentsByType = new Map(documents.map((document) => [document.documentTypeId, document]))
  const statuses = required.map((requirement) => documentsByType.get(requirement.documentTypeId)?.status ?? null)
  const count = (status: StudentDocumentStatus) => statuses.filter((item) => item === status).length
  const approved = count('APPROVED')
  const missing = statuses.filter((status) => status === null).length
  const revisionRequired = count('REVISION_REQUIRED')
  const rejected = count('REJECTED')
  const waitingReview = count('UPLOADED')

  let state: DocumentProcessState = 'WAITING'
  if (rejected > 0) state = 'REJECTED'
  else if (revisionRequired > 0) state = 'REVISION_REQUIRED'
  else if (required.length > 0 && approved === required.length) state = 'COMPLETED'

  return { approved, total: required.length, missing, revisionRequired, rejected, waitingReview, state }
}
