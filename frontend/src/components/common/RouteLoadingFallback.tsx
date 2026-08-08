import { LoaderCircle } from 'lucide-react'

export function RouteLoadingFallback() {
  return (
    <div
      className="flex min-h-64 items-center justify-center border border-slate-200 bg-white p-8 text-slate-700 shadow-sm"
      role="status"
      aria-live="polite"
    >
      <LoaderCircle className="mr-3 animate-spin text-blue-700" aria-hidden="true" size={22} />
      <span className="text-sm font-medium">Sayfa yükleniyor...</span>
    </div>
  )
}
