import ibbLogo from '../../assets/ibb-logo.png'

type AppBrandVariant = 'compact' | 'full'
type AppBrandSize = 'sm' | 'md' | 'lg'
type AppBrandTone = 'default' | 'inverse'

interface AppBrandProps {
  variant?: AppBrandVariant
  size?: AppBrandSize
  tone?: AppBrandTone
  className?: string
}

const imageSizeClasses: Record<AppBrandSize, string> = {
  sm: 'size-10',
  md: 'size-14',
  lg: 'size-20',
}

export function AppBrand({
  variant = 'full',
  size = 'md',
  tone = 'default',
  className = '',
}: AppBrandProps) {
  const isInverse = tone === 'inverse'

  return (
    <span className={`flex min-w-0 items-center gap-3 ${className}`}>
      <img
        src={ibbLogo}
        alt="İstanbul Büyükşehir Belediyesi"
        className={`app-brand-logo ${imageSizeClasses[size]} shrink-0 object-contain ${
          isInverse ? 'brightness-0 invert' : ''
        }`}
      />
      {variant === 'full' && (
        <span className="min-w-0 leading-tight">
          <span
            className={`block text-sm font-semibold ${isInverse ? 'text-white' : 'text-slate-950'}`}
          >
            Yurt Belge
          </span>
          <span
            className={`block text-sm font-normal ${isInverse ? 'text-blue-100' : 'text-slate-600'}`}
          >
            Yönetim Sistemi
          </span>
        </span>
      )}
    </span>
  )
}
