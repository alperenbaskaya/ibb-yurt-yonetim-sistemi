import { Search, UserRound } from 'lucide-react'
import { useState } from 'react'
import type { StudentResponse } from '../../types/dormitoryAdmin'
import { PageHeader } from '../../components/common/PageHeader'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatDate } from '../../utils/formatDate'
import { useAuth } from '../auth/useAuth'
import { AdminPageState } from './AdminPageState'
import { useDormitoryStudents } from './dormitoryAdminQueries'
import { AdminStudentInspectionDialog } from '../admin-student-inspection/AdminStudentInspectionDialog'

export function DormitoryStudentsPage() {
  const { user } = useAuth()
  const [search, setSearch] = useState('')
  const [selectedStudent, setSelectedStudent] = useState<StudentResponse | null>(null)
  const query = useDormitoryStudents(user?.userId ?? 0)
  const students = query.data ?? []
  const normalized = search.trim().toLocaleLowerCase('tr-TR')
  const visible = normalized ? students.filter((student) => `${student.firstName} ${student.lastName} ${student.email} ${student.identityNumber}`.toLocaleLowerCase('tr-TR').includes(normalized)) : students

  return <section><PageHeader title="Öğrenciler" description="Aktif dönemde yurdunuza kabulü bulunan öğrencilerin gerçek profil bilgilerini görüntüleyin." />
    {query.isLoading && <AdminPageState state="loading" title="Öğrenciler yükleniyor" message="Yurdunuzdaki aktif dönem öğrencileri alınıyor..." />}
    {query.isError && <AdminPageState state="error" title="Öğrenciler yüklenemedi" message={getApiErrorMessage(query.error, 'Öğrenci listesi alınamadı.')} onRetry={() => void query.refetch()} />}
    {query.isSuccess && students.length === 0 && <AdminPageState state="empty" title="Öğrenci bulunmuyor" message="Aktif dönemde yurdunuza bağlı öğrenci bulunmuyor." />}
    {query.isSuccess && students.length > 0 && <div className="mt-6 space-y-5"><div className="relative max-w-md"><label htmlFor="admin-student-search" className="sr-only">Öğrenci ara</label><Search aria-hidden="true" className="absolute left-3 top-3 text-slate-400" size={18} /><input id="admin-student-search" type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Ad, e-posta veya kimlik numarası ara" className="min-h-11 w-full border border-slate-300 bg-white py-2 pl-10 pr-3 text-sm focus:border-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-100" /></div>{visible.length === 0 ? <div className="border border-slate-200 bg-white p-6 text-center text-sm text-slate-600">Aramayla eşleşen öğrenci bulunamadı.</div> : <div className="grid gap-4 lg:grid-cols-2">{visible.map((student) => <button type="button" key={student.id} onClick={() => setSelectedStudent(student)} aria-label={`${student.firstName} ${student.lastName} belge sürecini incele`} className="border border-slate-200 bg-white p-5 text-left shadow-sm hover:border-blue-300 hover:bg-blue-50/30 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700"><div className="flex items-start gap-3"><span className="flex size-10 shrink-0 items-center justify-center bg-blue-50 text-blue-700"><UserRound aria-hidden="true" size={20} /></span><div className="min-w-0"><h2 className="font-semibold text-slate-950">{student.firstName} {student.lastName}</h2><p className="mt-1 break-all text-sm text-slate-600">{student.email}</p></div></div><dl className="mt-4 grid gap-3 text-sm sm:grid-cols-2"><div><dt className="text-slate-500">Kimlik numarası</dt><dd className="mt-1 font-medium text-slate-900">{student.identityNumber}</dd></div><div><dt className="text-slate-500">Telefon</dt><dd className="mt-1 font-medium text-slate-900">{student.phone}</dd></div><div><dt className="text-slate-500">Fakülte</dt><dd className="mt-1 font-medium text-slate-900">{student.faculty}</dd></div><div><dt className="text-slate-500">Bölüm</dt><dd className="mt-1 font-medium text-slate-900">{student.department}</dd></div><div><dt className="text-slate-500">Doğum tarihi</dt><dd className="mt-1 font-medium text-slate-900">{formatDate(student.birthDate)}</dd></div></dl><p className="mt-4 text-sm font-semibold text-blue-700">Belge sürecini incele</p></button>)}</div>}</div>}
    {selectedStudent && <AdminStudentInspectionDialog userId={user?.userId ?? 0} student={selectedStudent} onClose={() => setSelectedStudent(null)} />}
  </section>
}
