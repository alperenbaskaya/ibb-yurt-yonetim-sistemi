import { Moon, Sun } from 'lucide-react'
import { useTheme } from '../../features/theme/useTheme'

interface ThemeToggleProps {
  className?: string
}

export function ThemeToggle({ className = '' }: ThemeToggleProps) {
  const { theme, toggleTheme } = useTheme()
  const isDark = theme === 'dark'
  const label = isDark ? 'Açık renk moduna geç' : 'Koyu renk moduna geç'

  return (
    <button
      type="button"
      onClick={toggleTheme}
      className={`flex size-10 shrink-0 items-center justify-center text-slate-600 transition-colors hover:bg-slate-100 hover:text-blue-800 focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-blue-700 ${className}`}
      aria-label={label}
      title={label}
    >
      {isDark ? <Sun aria-hidden="true" size={20} /> : <Moon aria-hidden="true" size={20} />}
    </button>
  )
}
