export type NotificationType =
  | 'ADMISSION_APPROVED'
  | 'ADMISSION_REJECTED'
  | 'DOCUMENT_UPLOADED'
  | 'DOCUMENT_REUPLOADED'
  | 'DOCUMENT_APPROVED'
  | 'DOCUMENT_REJECTED'
  | 'DOCUMENT_REVISION_REQUIRED'
  | 'DOCUMENT_PROCESS_COMPLETED'

export type NotificationReferenceType =
  | 'ADMISSION'
  | 'STUDENT_DOCUMENT'
  | 'STUDENT'
  | 'DORMITORY'

export interface NotificationResponse {
  id: number
  type: NotificationType
  title: string
  message: string
  read: boolean
  referenceType: NotificationReferenceType | null
  referenceId: number | null
  createdAt: string
}

export interface NotificationUnreadCountResponse {
  unreadCount: number
}
