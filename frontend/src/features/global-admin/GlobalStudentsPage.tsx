import { Search, UserRound } from 'lucide-react'
import { useState } from 'react'
import { PageHeader } from '../../components/common/PageHeader'
import type { StudentResponse } from '../../types/globalAdmin'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatDate } from '../../utils/formatDate'
import { AdminStudentInspectionDialog } from '../admin-student-inspection/AdminStudentInspectionDialog'
import { useAuth } from '../auth/useAuth'
import { AdminPageState } from '../dormitory-admin/AdminPageState'
import { useGlobalStudents } from './globalAdminQueries'

export function GlobalStudentsPage() {
  const { user } = useAuth()
  const query = useGlobalStudents(user?.userId ?? 0)
  const [search, setSearch] = useState('')
  const [selectedStudent, setSelectedStudent] = useState<StudentResponse | null>(null)
  const normalized = search.trim().toLocaleLowerCase('tr-TR')
  const students = query.data ?? []
  const visibleStudents = students.filter((student) =>
    !normalized || `${student.firstName} ${student.lastName} ${student.email} ${student.identityNumber}`
      .toLocaleLowerCase('tr-TR')
      .includes(normalized),
  )

  return <section><PageHeader title="Öğrenciler" description="Sistemde kayıtlı tüm öğrenci profillerini görüntüleyin." />
    {query.isLoading && <AdminPageState state="loading" title="Öğrenciler yükleniyor" message="Öğrenci profilleri alınıyor..." />}
    {query.isError && <AdminPageState state="error" title="Öğrenciler alınamadı" message={getApiErrorMessage(query.error, 'Öğrenciler alınamadı.')} onRetry={() => void query.refetch()} />}
    {query.isSuccess && students.length === 0 && <AdminPageState state="empty" title="Öğrenci yok" message="Sistemde öğrenci profili bulunmuyor." />}
    {query.isSuccess && students.length > 0 && <div className="mt-6 space-y-4"><div className="relative max-w-md"><Search aria-hidden="true" className="absolute left-3 top-3 text-slate-400" size={18} /><label className="sr-only" htmlFor="global-student-search">Öğrenci ara</label><input id="global-student-search" type="search" value={search} onChange={(event) => setSearch(event.target.value)} className="min-h-11 w-full border border-slate-300 bg-white pl-10 pr-3 text-sm focus:border-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-100" placeholder="Ad, e-posta veya kimlik no ara" /></div>
      {visibleStudents.length === 0 ? <div className="border border-slate-200 bg-white p-6 text-center text-sm text-slate-600">Eşleşen öğrenci yok.</div> : <div className="grid gap-3 lg:grid-cols-2">{visibleStudents.map((student) => <button type="button" key={student.id} onClick={() => setSelectedStudent(student)} aria-label={`${student.firstName} ${student.lastName} belge sürecini incele`} className="border border-slate-200 bg-white p-4 text-left shadow-sm hover:border-blue-300 hover:bg-blue-50/30 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700"><div className="flex gap-3"><UserRound aria-hidden="true" className="text-blue-700" /><div><h2 className="font-semibold">{student.firstName} {student.lastName}</h2><p className="text-sm text-slate-600">{student.email}</p></div></div><dl className="mt-3 grid grid-cols-2 gap-2 text-sm"><div><dt className="text-slate-500">Kimlik</dt><dd>{student.identityNumber}</dd></div><div><dt className="text-slate-500">Telefon</dt><dd>{student.phone}</dd></div><div><dt className="text-slate-500">Fakülte</dt><dd>{student.faculty}</dd></div><div><dt className="text-slate-500">Bölüm</dt><dd>{student.department}</dd></div><div><dt className="text-slate-500">Doğum tarihi</dt><dd>{formatDate(student.birthDate)}</dd></div></dl><p className="mt-4 text-sm font-semibold text-blue-700">Belge sürecini incele</p></button>)}</div>}
    </div>}
    {selectedStudent && <AdminStudentInspectionDialog userId={user?.userId ?? 0} student={selectedStudent} onClose={() => setSelectedStudent(null)} />}
  </section>
}
