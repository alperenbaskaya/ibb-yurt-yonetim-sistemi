import { ChevronLeft, ChevronRight } from 'lucide-react'

type PaginationItem = number | 'ellipsis'

interface PaginationProps {
  currentPage: number
  totalPages: number
  totalElements: number
  pageSize: number
  onPageChange: (page: number) => void
  itemLabel: string
  disabled?: boolean
  className?: string
}

function getPaginationItems(currentPage: number, totalPages: number): PaginationItem[] {
  if (totalPages <= 7) return Array.from({ length: totalPages }, (_, index) => index)

  const pages = Array.from(new Set([0, totalPages - 1, currentPage - 1, currentPage, currentPage + 1]))
    .filter((page) => page >= 0 && page < totalPages)
    .sort((left, right) => left - right)

  return pages.flatMap((page, index) => {
    const previousPage = pages[index - 1]
    return index > 0 && page - previousPage > 1 ? ['ellipsis', page] : [page]
  })
}

export function Pagination({ currentPage, totalPages, totalElements, pageSize, onPageChange, itemLabel, disabled = false, className = '' }: PaginationProps) {
  if (totalElements === 0) return null

  const safePage = Math.min(currentPage, Math.max(totalPages - 1, 0))
  const rangeStart = safePage * pageSize + 1
  const rangeEnd = Math.min((safePage + 1) * pageSize, totalElements)
  const items = getPaginationItems(safePage, totalPages)

  return <div className={`flex flex-wrap items-center justify-between gap-3 border-t border-slate-200 pt-4 ${className}`}>
    <p className="text-sm font-medium text-slate-600">{rangeStart}–{rangeEnd} / {totalElements} {itemLabel}</p>
    {totalPages > 1 && <nav aria-label={`${itemLabel} sayfaları`} className="flex flex-wrap items-center justify-end gap-1">
      <button type="button" disabled={disabled || safePage === 0} onClick={() => onPageChange(safePage - 1)} aria-label="Önceki sayfa" className="inline-flex min-h-10 items-center gap-1 border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-45"><ChevronLeft aria-hidden="true" size={16} />Önceki</button>
      {items.map((item, index) => item === 'ellipsis'
        ? <span key={`ellipsis-${index}`} aria-hidden="true" className="px-2 text-slate-500">…</span>
        : <button type="button" key={item} disabled={disabled} onClick={() => onPageChange(item)} aria-label={`${item + 1}. sayfaya git`} aria-current={item === safePage ? 'page' : undefined} className={`min-h-10 min-w-10 border px-3 py-2 text-sm font-semibold disabled:cursor-not-allowed disabled:opacity-45 ${item === safePage ? 'border-blue-700 bg-blue-700 text-white' : 'border-slate-300 bg-white text-slate-700 hover:bg-slate-50'}`}>{item + 1}</button>)}
      <button type="button" disabled={disabled || safePage >= totalPages - 1} onClick={() => onPageChange(safePage + 1)} aria-label="Sonraki sayfa" className="inline-flex min-h-10 items-center gap-1 border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-45">Sonraki<ChevronRight aria-hidden="true" size={16} /></button>
    </nav>}
  </div>
}
