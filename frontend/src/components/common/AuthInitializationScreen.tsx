import { Building2, LoaderCircle, RotateCw } from 'lucide-react'

interface AuthInitializationScreenProps {
  errorMessage?: string | null
  onRetry?: () => void
}

export function AuthInitializationScreen({
  errorMessage,
  onRetry,
}: AuthInitializationScreenProps) {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-50 px-6">
      <section
        className="w-full max-w-md border border-slate-200 bg-white p-8 text-center shadow-sm"
        aria-live="polite"
      >
        <div className="mx-auto mb-5 flex size-12 items-center justify-center bg-blue-50 text-blue-700">
          <Building2 aria-hidden="true" size={25} />
        </div>

        {errorMessage ? (
          <>
            <h1 className="text-xl font-semibold text-slate-950">
              Oturum bilgileri alınamadı
            </h1>
            <p className="mt-3 text-sm leading-6 text-slate-600">
              {errorMessage}
            </p>
            {onRetry && (
              <button
                type="button"
                onClick={onRetry}
                className="mt-6 inline-flex items-center justify-center gap-2 bg-blue-700 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-blue-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700"
              >
                <RotateCw aria-hidden="true" size={17} />
                Tekrar dene
              </button>
            )}
          </>
        ) : (
          <>
            <LoaderCircle
              aria-hidden="true"
              className="mx-auto animate-spin text-blue-700"
              size={25}
            />
            <h1 className="mt-4 text-lg font-semibold text-slate-950">
              Oturum kontrol ediliyor
            </h1>
            <p className="mt-2 text-sm text-slate-600">
              Lütfen kısa bir süre bekleyin.
            </p>
          </>
        )}
      </section>
    </main>
  )
}
