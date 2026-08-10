import { GraduationCap, Mail, Search } from 'lucide-react'
import { useState } from 'react'
import { PageHeader } from '../../components/common/PageHeader'
import { getApiErrorMessage } from '../../utils/apiError'
import { useAuth } from '../auth/useAuth'
import { ReviewerPageState } from './ReviewerPageState'
import { useReviewerStudents } from './reviewerQueries'

export function ReviewerStudentsPage() {
  const { user } = useAuth()
  const studentsQuery = useReviewerStudents(user?.userId ?? 0)
  const [search, setSearch] = useState('')
  const normalizedSearch = search.trim().toLocaleLowerCase('tr-TR')
  const students = studentsQuery.data ?? []
  const visibleStudents = students.filter((student) => {
    if (!normalizedSearch) return true

    return [
      student.firstName,
      student.lastName,
      student.email,
      student.identityNumber,
    ].some((value) => value.toLocaleLowerCase('tr-TR').includes(normalizedSearch))
  })

  return (
    <section>
      <PageHeader
        title="Öğrenciler"
        description="Yurdunuzun aktif döneminde onaylı kabulü bulunan öğrencileri görüntüleyin."
      />

      {studentsQuery.isLoading && (
        <ReviewerPageState
          state="loading"
          title="Öğrenciler yükleniyor"
          message="Aktif dönemde yurdunuza bağlı öğrenciler alınıyor..."
        />
      )}
      {studentsQuery.isError && (
        <ReviewerPageState
          state="error"
          title="Öğrenciler yüklenemedi"
          message={getApiErrorMessage(
            studentsQuery.error,
            'Öğrenci listesi alınamadı.',
          )}
          onRetry={() => void studentsQuery.refetch()}
        />
      )}
      {studentsQuery.isSuccess && students.length === 0 && (
        <ReviewerPageState
          state="empty"
          title="Öğrenci bulunmuyor"
          message="Yurdunuzda aktif dönem için onaylı kabulü bulunan öğrenci yok."
        />
      )}

      {studentsQuery.isSuccess && students.length > 0 && (
        <div className="mt-6 space-y-4">
          <div className="border border-slate-200 bg-white p-4 shadow-sm">
            <label htmlFor="reviewer-student-search" className="block text-sm font-semibold text-slate-800">
              Öğrenci ara
            </label>
            <div className="relative mt-2 max-w-lg">
              <Search className="pointer-events-none absolute left-3 top-3 text-slate-400" aria-hidden="true" size={18} />
              <input
                id="reviewer-student-search"
                type="search"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                className="min-h-11 w-full border border-slate-300 bg-white pl-10 pr-3 text-sm focus:border-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-100"
                placeholder="Ad, e-posta veya kimlik numarası"
              />
            </div>
            <p className="mt-2 text-xs text-slate-500">Toplam {students.length} öğrenci</p>
          </div>

          {visibleStudents.length === 0 ? (
            <div className="border border-slate-200 bg-white p-6 text-center text-sm text-slate-600">
              Arama ölçütüyle eşleşen öğrenci bulunamadı.
            </div>
          ) : (
            <div className="grid gap-4 lg:grid-cols-2">
              {visibleStudents.map((student) => (
                <article key={student.studentId} className="border border-slate-200 bg-white p-5 shadow-sm">
                  <div className="flex items-start gap-3">
                    <span className="flex size-10 shrink-0 items-center justify-center bg-blue-50 text-blue-700">
                      <GraduationCap aria-hidden="true" size={20} />
                    </span>
                    <div className="min-w-0 flex-1">
                      <h2 className="font-semibold text-slate-950">{student.firstName} {student.lastName}</h2>
                      <p className="mt-1 flex items-center gap-2 break-all text-sm text-slate-600">
                        <Mail aria-hidden="true" size={15} /> {student.email}
                      </p>
                    </div>
                  </div>
                  <dl className="mt-4 grid gap-3 border-t border-slate-200 pt-4 text-sm sm:grid-cols-2">
                    <div><dt className="font-medium text-slate-500">Kimlik numarası</dt><dd className="mt-1 text-slate-900">{student.identityNumber}</dd></div>
                    <div><dt className="font-medium text-slate-500">Telefon</dt><dd className="mt-1 text-slate-900">{student.phone}</dd></div>
                    <div><dt className="font-medium text-slate-500">Fakülte</dt><dd className="mt-1 text-slate-900">{student.faculty}</dd></div>
                    <div><dt className="font-medium text-slate-500">Bölüm</dt><dd className="mt-1 text-slate-900">{student.department}</dd></div>
                  </dl>
                </article>
              ))}
            </div>
          )}
        </div>
      )}
    </section>
  )
}
