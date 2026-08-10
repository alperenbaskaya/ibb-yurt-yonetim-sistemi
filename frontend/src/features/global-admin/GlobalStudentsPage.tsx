import { Search, UserRound } from 'lucide-react'
import { useState } from 'react'
import { PageHeader } from '../../components/common/PageHeader'
import type { StudentResponse } from '../../types/globalAdmin'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatDate } from '../../utils/formatDate'
import { AdminStudentInspectionDialog } from '../admin-student-inspection/AdminStudentInspectionDialog'
import { useAuth } from '../auth/useAuth'
import { AdminPageState } from '../dormitory-admin/AdminPageState'
import { useDormitories, useGlobalStudents } from './globalAdminQueries'

export function GlobalStudentsPage() {
  const { user } = useAuth()
  const userId = user?.userId ?? 0
  const [dormitoryId, setDormitoryId] = useState<number | null>(null)
  const [search, setSearch] = useState('')
  const [selectedStudent, setSelectedStudent] = useState<StudentResponse | null>(null)
  const dormitoriesQuery = useDormitories(userId)
  const studentsQuery = useGlobalStudents(userId, dormitoryId)
  const activeDormitories = (dormitoriesQuery.data ?? []).filter((dormitory) => dormitory.active)
  const selectedDormitory = activeDormitories.find((dormitory) => dormitory.id === dormitoryId) ?? null
  const normalized = search.trim().toLocaleLowerCase('tr-TR')
  const students = studentsQuery.data ?? []
  const visibleStudents = students.filter((student) =>
    !normalized || `${student.firstName} ${student.lastName} ${student.email} ${student.identityNumber}`
      .toLocaleLowerCase('tr-TR')
      .includes(normalized),
  )

  const changeDormitory = (value: string) => {
    setSelectedStudent(null)
    setDormitoryId(value ? Number(value) : null)
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

    {selectedDormitory && studentsQuery.isSuccess && students.length > 0 && <div className="mt-6 space-y-4"><div className="border border-slate-200 bg-white p-4 shadow-sm"><p className="font-semibold text-slate-950">{selectedDormitory.name}</p><p className="mt-1 text-sm text-slate-600">Aktif dönemde {students.length} onaylı öğrenci</p><div className="relative mt-4 max-w-md"><Search aria-hidden="true" className="absolute left-3 top-3 text-slate-400" size={18} /><label className="sr-only" htmlFor="global-student-search">Öğrenci ara</label><input id="global-student-search" type="search" value={search} onChange={(event) => setSearch(event.target.value)} className="min-h-11 w-full border border-slate-300 bg-white pl-10 pr-3 text-sm focus:border-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-100" placeholder="Ad, e-posta veya kimlik no ara" /></div></div>
      {visibleStudents.length === 0 ? <div className="border border-slate-200 bg-white p-6 text-center text-sm text-slate-600">Aramayla eşleşen öğrenci yok.</div> : <div className="grid gap-3 lg:grid-cols-2">{visibleStudents.map((student) => <button type="button" key={student.id} onClick={() => setSelectedStudent(student)} aria-label={`${student.firstName} ${student.lastName} belge sürecini incele`} className="border border-slate-200 bg-white p-4 text-left shadow-sm hover:border-blue-300 hover:bg-blue-50/30 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700"><div className="flex gap-3"><UserRound aria-hidden="true" className="text-blue-700" /><div><h2 className="font-semibold">{student.firstName} {student.lastName}</h2><p className="text-sm text-slate-600">{student.email}</p></div></div><dl className="mt-3 grid grid-cols-2 gap-2 text-sm"><div><dt className="text-slate-500">Kimlik</dt><dd>{student.identityNumber}</dd></div><div><dt className="text-slate-500">Telefon</dt><dd>{student.phone}</dd></div><div><dt className="text-slate-500">Fakülte</dt><dd>{student.faculty}</dd></div><div><dt className="text-slate-500">Bölüm</dt><dd>{student.department}</dd></div><div><dt className="text-slate-500">Doğum tarihi</dt><dd>{formatDate(student.birthDate)}</dd></div></dl><p className="mt-4 text-sm font-semibold text-blue-700">Belge sürecini incele</p></button>)}</div>}
    </div>}
    {selectedStudent && <AdminStudentInspectionDialog userId={userId} student={selectedStudent} onClose={() => setSelectedStudent(null)} />}
  </section>
}
