import { zodResolver } from '@hookform/resolvers/zod'
import { LoaderCircle, Send } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import type { CreateDocumentReviewRequest } from '../../types/reviewer'
import { getApiErrorMessage } from '../../utils/apiError'

const reviewSchema = z
  .object({
    decision: z.enum(['APPROVED', 'REJECTED', 'REVISION_REQUIRED'], {
      message: 'Değerlendirme kararı zorunludur.',
    }),
    comment: z.string().max(1000, 'Açıklama en fazla 1000 karakter olabilir.'),
  })
  .superRefine((values, context) => {
    if (
      (values.decision === 'REJECTED' || values.decision === 'REVISION_REQUIRED') &&
      values.comment.trim().length === 0
    ) {
      context.addIssue({
        code: 'custom',
        path: ['comment'],
        message: 'Ret ve düzeltme kararlarında açıklama zorunludur.',
      })
    }
  })

type ReviewFormValues = z.infer<typeof reviewSchema>

interface DocumentReviewFormProps {
  studentDocumentId: number
  onSubmitReview: (request: CreateDocumentReviewRequest) => Promise<void>
}

export function DocumentReviewForm({ studentDocumentId, onSubmitReview }: DocumentReviewFormProps) {
  const {
    register,
    handleSubmit,
    reset,
    watch,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<ReviewFormValues>({
    resolver: zodResolver(reviewSchema),
    defaultValues: { decision: 'APPROVED', comment: '' },
  })
  const decision = watch('decision')
  const commentRequired = decision === 'REJECTED' || decision === 'REVISION_REQUIRED'

  const submit = async (values: ReviewFormValues) => {
    const normalizedComment = values.comment.trim()
    try {
      await onSubmitReview({
        studentDocumentId,
        decision: values.decision,
        comment: normalizedComment.length > 0 ? normalizedComment : null,
      })
      reset()
    } catch (error: unknown) {
      setError('root', {
        message: getApiErrorMessage(
          error,
          'Değerlendirme kaydedilemedi. Lütfen belge durumunu yenileyip tekrar deneyin.',
        ),
      })
    }
  }

  return (
    <form className="mt-5 border-t border-slate-200 pt-5" onSubmit={handleSubmit(submit)} noValidate>
      <h3 className="font-semibold text-slate-950">Belgeyi değerlendir</h3>
      <div className="mt-4">
        <label htmlFor={`review-decision-${studentDocumentId}`} className="block text-sm font-semibold text-slate-800">Karar</label>
        <select id={`review-decision-${studentDocumentId}`} {...register('decision')} disabled={isSubmitting} className="mt-2 min-h-11 w-full border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:border-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-100 disabled:opacity-60">
          <option value="APPROVED">Onayla</option>
          <option value="REJECTED">Reddet</option>
          <option value="REVISION_REQUIRED">Düzeltme iste</option>
        </select>
        {errors.decision && <p className="mt-2 text-sm text-red-700" role="alert">{errors.decision.message}</p>}
      </div>

      <div className="mt-4">
        <div className="flex items-center justify-between gap-3">
          <label htmlFor={`review-comment-${studentDocumentId}`} className="text-sm font-semibold text-slate-800">Açıklama {commentRequired ? '(zorunlu)' : '(isteğe bağlı)'}</label>
          <span className="text-xs text-slate-500">En fazla 1000 karakter</span>
        </div>
        <textarea id={`review-comment-${studentDocumentId}`} {...register('comment')} rows={5} disabled={isSubmitting} aria-invalid={errors.comment ? 'true' : 'false'} aria-describedby={errors.comment ? `review-comment-error-${studentDocumentId}` : undefined} className="mt-2 w-full resize-y border border-slate-300 px-3 py-2 text-sm leading-6 text-slate-900 focus:border-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-100 disabled:opacity-60" />
        {errors.comment && <p id={`review-comment-error-${studentDocumentId}`} className="mt-2 text-sm text-red-700" role="alert">{errors.comment.message}</p>}
      </div>

      {errors.root && <p className="mt-4 border border-red-200 bg-red-50 p-3 text-sm text-red-800" role="alert">{errors.root.message}</p>}

      <button type="submit" disabled={isSubmitting} className="mt-4 inline-flex min-h-10 items-center justify-center gap-2 bg-blue-800 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-900 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 disabled:cursor-not-allowed disabled:opacity-60">
        {isSubmitting ? <LoaderCircle className="animate-spin" aria-hidden="true" size={17} /> : <Send aria-hidden="true" size={17} />}
        {isSubmitting ? 'Kaydediliyor...' : 'Değerlendirmeyi kaydet'}
      </button>
    </form>
  )
}
