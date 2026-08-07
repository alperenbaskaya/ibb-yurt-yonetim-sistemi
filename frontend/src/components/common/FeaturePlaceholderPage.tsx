import { Construction } from 'lucide-react'
import { PageHeader } from './PageHeader'

interface FeaturePlaceholderPageProps {
  title: string
  description: string
}

export function FeaturePlaceholderPage({
  title,
  description,
}: FeaturePlaceholderPageProps) {
  return (
    <div>
      <PageHeader title={title} description={description} />

      <section className="mt-7 border border-slate-200 bg-white p-7 shadow-sm sm:p-9">
        <div className="flex size-11 items-center justify-center bg-amber-50 text-amber-700">
          <Construction aria-hidden="true" size={22} />
        </div>
        <h2 className="mt-5 text-lg font-semibold text-slate-950">
          Modül hazırlanıyor
        </h2>
        <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-600">
          Bu modül sonraki geliştirme adımında gerçek backend verileriyle
          bağlanacaktır.
        </p>
      </section>
    </div>
  )
}
