import { ChevronLeft, ChevronRight, Search, UserRound } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { PageHeader } from '../../components/common/PageHeader'
import type { StudentResponse } from '../../types/globalAdmin'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatDate } from '../../utils/formatDate'
import { AdminStudentInspectionDialog } from '../admin-student-inspection/AdminStudentInspectionDialog'
import { useAuth } from '../auth/useAuth'
import { AdminPageState } from '../dormitory-admin/AdminPageState'
import { useDormitories, useGlobalStudents } from './globalAdminQueries'

const PAGE_SIZE = 12

type PaginationItem = number | 'ellipsis'

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

export function GlobalStudentsPage() {
  const { user } = useAuth()
  const userId = user?.userId ?? 0
  const [dormitoryId, setDormitoryId] = useState<number | null>(null)
  const [search, setSearch] = useState('')
  const [currentPage, setCurrentPage] = useState(0)
  const [selectedStudent, setSelectedStudent] = useState<StudentResponse | null>(null)
  const dormitoriesQuery = useDormitories(userId)
  const studentsQuery = useGlobalStudents(userId, dormitoryId)
  const activeDormitories = (dormitoriesQuery.data ?? []).filter((dormitory) => dormitory.active)
  const selectedDormitory = activeDormitories.find((dormitory) => dormitory.id === dormitoryId) ?? null
  const students = studentsQuery.data ?? []
  const filteredStudents = useMemo(() => {
    const normalized = search.trim().toLocaleLowerCase('tr-TR')
    return students.filter((student) =>
      !normalized || `${student.firstName} ${student.lastName} ${student.email} ${student.identityNumber}`
        .toLocaleLowerCase('tr-TR')
        .includes(normalized),
    )
  }, [search, students])
  const totalPages = Math.ceil(filteredStudents.length / PAGE_SIZE)
  const visiblePage = Math.min(currentPage, Math.max(totalPages - 1, 0))
  const visibleStudents = useMemo(
    () => filteredStudents.slice(visiblePage * PAGE_SIZE, (visiblePage + 1) * PAGE_SIZE),
    [filteredStudents, visiblePage],
  )
  const paginationItems = useMemo(
    () => getPaginationItems(visiblePage, totalPages),
    [totalPages, visiblePage],
  )
  const rangeStart = filteredStudents.length === 0 ? 0 : visiblePage * PAGE_SIZE + 1
  const rangeEnd = Math.min((visiblePage + 1) * PAGE_SIZE, filteredStudents.length)

  useEffect(() => {
    setCurrentPage((page) => Math.min(page, Math.max(totalPages - 1, 0)))
  }, [totalPages])

  const changeDormitory = (value: string) => {
    setSelectedStudent(null)
    setCurrentPage(0)
    setDormitoryId(value ? Number(value) : null)
  }

  const changeSearch = (value: string) => {
    setSearch(value)
    setCurrentPage(0)
  }

  return <section><PageHeader title="Öğrenciler" description="Aktif dönemde seçilen yurtta onaylı kabulü bulunan öğrencileri görüntüleyin." />
    {dormitoriesQuery.isLoading && <AdminPageState state="loading" title="Yurtlar yükleniyor" message="Öğrenci listesini filtrelemek için aktif yurtlar alınıyor..." />}
    {dormitoriesQuery.isError && <AdminPageState state="error" title="Yurtlar alınamadı" message={getApiErrorMessage(dormitoriesQuery.error, 'Yurt listesi alınamadı.')} onRetry={() => void dormitoriesQuery.refetch()} />}
    {dormitoriesQuery.isSuccess && activeDormitories.length === 0 && <AdminPageState state="empty" title="Aktif yurt bulunmuyor" message="Öğrencileri görüntülemek için aktif bir yurt bulunmalıdır." />}

    {dormitoriesQuery.isSuccess && activeDormitories.length > 0 && <div className="mt-6"><label htmlFor="global-student-dormitory" className="block text-sm font-semibold text-slate-800">Yurt</label><select id="global-student-dormitory" required value={dormitoryId ?? ''} onChange={(event) => changeDormitory(event.target.value)} className="mt-2 min-h-11 w-full max-w-xl border border-slate-300 bg-white px-3 py-2 text-sm focus:border-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-100"><option value="">Yurt seçiniz</option>{activeDormitories.map((dormitory) => <option key={dormitory.id} value={dormitory.id}>{dormitory.name}</option>)}</select></div>}

    {dormitoriesQuery.isSuccess && activeDormitories.length > 0 && dormitoryId === null && <div className="mt-5 border border-slate-200 bg-white p-6 text-center text-sm text-slate-600">Öğrencileri görüntülemek için önce bir yurt seçin.</div>}
    {dormitoryId !== null && studentsQuery.isLoading && <AdminPageState state="loading" title="Öğrenciler yükleniyor" message="Seçilen yurdun aktif dönem onaylı öğrencileri alınıyor..." />}
    {dormitoryId !== null && studentsQuery.isError && <AdminPageState state="error" title="Öğrenciler alınamadı" message={getApiErrorMessage(studentsQuery.error, 'Öğrenciler alınamadı.')} onRetry={() => void studentsQuery.refetch()} />}
    {selectedDormitory && studentsQuery.isSuccess && students.length === 0 && <AdminPageState state="empty" title="Onaylı öğrenci bulunmuyor" message={`${selectedDormitory.name} için aktif dönemde onaylı kabulü bulunan öğrenci yok.`} />}

    {selectedDormitory && studentsQuery.isSuccess && students.length > 0 && <div className="mt-6 space-y-4"><div className="border border-slate-200 bg-white p-4 shadow-sm"><p className="font-semibold text-slate-950">{selectedDormitory.name}</p><p className="mt-1 text-sm text-slate-600">Aktif dönemde {students.length} onaylı öğrenci</p><div className="relative mt-4 max-w-md"><Search aria-hidden="true" className="absolute left-3 top-3 text-slate-400" size={18} /><label className="sr-only" htmlFor="global-student-search">Öğrenci ara</label><input id="global-student-search" type="search" value={search} onChange={(event) => changeSearch(event.target.value)} className="min-h-11 w-full border border-slate-300 bg-white pl-10 pr-3 text-sm focus:border-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-100" placeholder="Ad, e-posta veya kimlik no ara" /></div></div>
      {filteredStudents.length === 0 ? <div className="border border-slate-200 bg-white p-6 text-center text-sm text-slate-600">Aramanızla eşleşen öğrenci bulunamadı.</div> : <><div className="grid gap-3 lg:grid-cols-2">{visibleStudents.map((student) => <button type="button" key={student.id} onClick={() => setSelectedStudent(student)} aria-label={`${student.firstName} ${student.lastName} belge sürecini incele`} className="border border-slate-200 bg-white p-4 text-left shadow-sm hover:border-blue-300 hover:bg-blue-50/30 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700"><div className="flex gap-3"><UserRound aria-hidden="true" className="text-blue-700" /><div><h2 className="font-semibold">{student.firstName} {student.lastName}</h2><p className="text-sm text-slate-600">{student.email}</p></div></div><dl className="mt-3 grid grid-cols-2 gap-2 text-sm"><div><dt className="text-slate-500">Kimlik</dt><dd>{student.identityNumber}</dd></div><div><dt className="text-slate-500">Telefon</dt><dd>{student.phone}</dd></div><div><dt className="text-slate-500">Fakülte</dt><dd>{student.faculty}</dd></div><div><dt className="text-slate-500">Bölüm</dt><dd>{student.department}</dd></div><div><dt className="text-slate-500">Doğum tarihi</dt><dd>{formatDate(student.birthDate)}</dd></div></dl><p className="mt-4 text-sm font-semibold text-blue-700">Belge sürecini incele</p></button>)}</div>
        <div className="flex flex-wrap items-center justify-between gap-3 border-t border-slate-200 pt-4"><p className="text-sm font-medium text-slate-600">{rangeStart}–{rangeEnd} / {filteredStudents.length} öğrenci</p>{totalPages > 1 && <nav aria-label="Öğrenci listesi sayfaları" className="flex flex-wrap items-center justify-end gap-1"><button type="button" disabled={visiblePage === 0} onClick={() => setCurrentPage(visiblePage - 1)} aria-label="Önceki sayfa" className="inline-flex min-h-10 items-center gap-1 border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-45"><ChevronLeft aria-hidden="true" size={16} />Önceki</button>{paginationItems.map((item, index) => item === 'ellipsis' ? <span key={`ellipsis-${index}`} aria-hidden="true" className="px-2 text-slate-500">…</span> : <button type="button" key={item} onClick={() => setCurrentPage(item)} aria-label={`${item + 1}. sayfaya git`} aria-current={item === visiblePage ? 'page' : undefined} className={`min-h-10 min-w-10 border px-3 py-2 text-sm font-semibold ${item === visiblePage ? 'border-blue-700 bg-blue-700 text-white' : 'border-slate-300 bg-white text-slate-700 hover:bg-slate-50'}`}>{item + 1}</button>)}<button type="button" disabled={visiblePage >= totalPages - 1} onClick={() => setCurrentPage(visiblePage + 1)} aria-label="Sonraki sayfa" className="inline-flex min-h-10 items-center gap-1 border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-45">Sonraki<ChevronRight aria-hidden="true" size={16} /></button></nav>}</div></>}
    </div>}
    {selectedStudent && <AdminStudentInspectionDialog userId={userId} student={selectedStudent} onClose={() => setSelectedStudent(null)} />}
  </section>
}
