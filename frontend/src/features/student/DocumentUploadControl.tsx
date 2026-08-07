import { FileUp, LoaderCircle, X } from 'lucide-react'
import { useId, useRef, useState, type ChangeEvent } from 'react'
import type { StudentDocumentUploadRequest } from '../../types/student'
import { getApiErrorMessage } from '../../utils/apiError'

const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024
const ACCEPTED_FILE_TYPES = '.pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png'

interface DocumentUploadControlProps {
  documentTypeId: number
  documentTypeName: string
  actionLabel: string
  isPending: boolean
  onUpload: (request: StudentDocumentUploadRequest) => Promise<void>
}

export function DocumentUploadControl({
  documentTypeId,
  documentTypeName,
  actionLabel,
  isPending,
  onUpload,
}: DocumentUploadControlProps) {
  const inputId = useId()
  const inputRef = useRef<HTMLInputElement>(null)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const clearSelection = () => {
    setSelectedFile(null)
    setErrorMessage(null)
    if (inputRef.current) inputRef.current.value = ''
  }

  const handleFileChange = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0] ?? null
    setErrorMessage(null)

    if (file && file.size > MAX_FILE_SIZE_BYTES) {
      setSelectedFile(null)
      event.target.value = ''
      setErrorMessage('Dosya boyutu en fazla 5 MB olabilir.')
      return
    }

    setSelectedFile(file)
  }

  const handleSubmit = async () => {
    if (!selectedFile) {
      setErrorMessage('Lütfen yüklenecek dosyayı seçin.')
      return
    }

    setErrorMessage(null)
    try {
      await onUpload({ documentTypeId, file: selectedFile })
      clearSelection()
    } catch (error: unknown) {
      setErrorMessage(
        getApiErrorMessage(error, 'Belge yüklenemedi. Lütfen tekrar deneyin.'),
      )
    }
  }

  return (
    <div className="mt-4 border-t border-slate-200 pt-4">
      <label htmlFor={inputId} className="block text-sm font-semibold text-slate-800">
        {documentTypeName} dosyası
      </label>
      <p className="mt-1 text-xs leading-5 text-slate-500">
        PDF, JPEG veya PNG — en fazla 5 MB
      </p>
      <input
        ref={inputRef}
        id={inputId}
        type="file"
        accept={ACCEPTED_FILE_TYPES}
        onChange={handleFileChange}
        disabled={isPending}
        className="mt-3 block w-full text-sm text-slate-700 file:mr-3 file:border-0 file:bg-slate-100 file:px-3 file:py-2 file:font-semibold file:text-slate-700 hover:file:bg-slate-200 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 disabled:opacity-60"
      />

      {selectedFile && (
        <div className="mt-3 flex flex-wrap items-center justify-between gap-2 bg-slate-50 px-3 py-2 text-sm">
          <span className="min-w-0 truncate font-medium text-slate-700">{selectedFile.name}</span>
          <button
            type="button"
            onClick={clearSelection}
            disabled={isPending}
            className="inline-flex items-center gap-1 text-xs font-semibold text-slate-600 hover:text-red-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 disabled:opacity-60"
            aria-label={`${selectedFile.name} seçimini kaldır`}
          >
            <X aria-hidden="true" size={15} /> Kaldır
          </button>
        </div>
      )}

      {errorMessage && <p className="mt-3 text-sm font-medium text-red-700" role="alert">{errorMessage}</p>}

      <button
        type="button"
        onClick={() => void handleSubmit()}
        disabled={!selectedFile || isPending}
        className="mt-3 inline-flex min-h-10 items-center justify-center gap-2 bg-blue-800 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-900 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {isPending ? (
          <LoaderCircle className="animate-spin" aria-hidden="true" size={17} />
        ) : (
          <FileUp aria-hidden="true" size={17} />
        )}
        {isPending ? 'Yükleniyor...' : actionLabel}
      </button>
    </div>
  )
}
