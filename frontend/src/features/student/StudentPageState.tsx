import { AlertCircle, Inbox, LoaderCircle } from 'lucide-react'

interface StudentPageStateProps {
  state: 'loading' | 'error' | 'empty'
  title: string
  message: string
  onRetry?: () => void
}

export function StudentPageState({ state, title, message, onRetry }: StudentPageStateProps) {
  const Icon = state === 'loading' ? LoaderCircle : state === 'error' ? AlertCircle : Inbox

  return (
    <div
      className={`mt-6 flex min-h-64 flex-col items-center justify-center border px-6 text-center shadow-sm ${
        state === 'error' ? 'border-red-200 bg-red-50' : 'border-slate-200 bg-white'
      }`}
      role={state === 'error' ? 'alert' : 'status'}
      aria-live="polite"
    >
      <Icon
        className={state === 'loading' ? 'animate-spin text-blue-700' : state === 'error' ? 'text-red-700' : 'text-slate-500'}
        aria-hidden="true"
        size={30}
      />
      <h2 className={`mt-4 font-semibold ${state === 'error' ? 'text-red-900' : 'text-slate-900'}`}>
        {title}
      </h2>
      <p className={`mt-2 max-w-xl text-sm leading-6 ${state === 'error' ? 'text-red-800' : 'text-slate-600'}`}>
        {message}
      </p>
      {state === 'error' && onRetry && (
        <button
          type="button"
          onClick={onRetry}
          className="mt-4 min-h-10 border border-red-700 bg-white px-4 py-2 text-sm font-semibold text-red-800 hover:bg-red-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-700"
        >
          Tekrar dene
        </button>
      )}
    </div>
  )
}
