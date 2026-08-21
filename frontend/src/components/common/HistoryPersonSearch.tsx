import { Search, X } from 'lucide-react'

interface HistoryPersonSearchProps {
  value: string
  onChange: (value: string) => void
}

export function HistoryPersonSearch({ value, onChange }: HistoryPersonSearchProps) {
  return (
    <div className="relative mt-4 max-w-xl">
      <Search aria-hidden="true" className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
      <label htmlFor="history-person-search" className="sr-only">Geçmiş kayıtlarında kişi ara</label>
      <input id="history-person-search" type="search" value={value} onChange={(event) => onChange(event.target.value)} placeholder="Ad, e-posta veya TC kimlik no ara" className="min-h-11 w-full border border-slate-300 bg-white py-2 pl-10 pr-24 text-sm text-slate-950 focus:border-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-100" />
      {value && <button type="button" onClick={() => onChange('')} className="absolute right-2 top-1/2 inline-flex min-h-8 -translate-y-1/2 items-center gap-1 px-2 text-xs font-semibold text-slate-600 hover:text-slate-950" aria-label="Aramayı temizle"><X aria-hidden="true" size={15} />Temizle</button>}
    </div>
  )
}
