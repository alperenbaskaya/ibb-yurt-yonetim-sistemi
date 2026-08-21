import {
  Bell,
  Building2,
  CalendarRange,
  ClipboardCheck,
  ClipboardList,
  FileCog,
  Files,
  FileText,
  GraduationCap,
  Home,
  LayoutDashboard,
  ListChecks,
  History,
  FlaskConical,
  UserCheck,
  UserRound,
  Users,
  type LucideIcon,
} from 'lucide-react'
import type { UserCategory } from '../types/userCategory'

export interface NavigationItem {
  label: string
  path: string
  icon: LucideIcon
}

export const testCenterEnabled = import.meta.env.DEV

const sharedItems = {
  home: { label: 'Ana Sayfa', path: '/home', icon: Home },
  notifications: {
    label: 'Bildirimler',
    path: '/notifications',
    icon: Bell,
  },
  profile: { label: 'Profil', path: '/profile', icon: UserRound },
} satisfies Record<string, NavigationItem>

export const navigationByCategory: Record<
  UserCategory,
  readonly NavigationItem[]
> = {
  STUDENT: [
    sharedItems.home,
    {
      label: 'Kontrol Paneli',
      path: '/student/dashboard',
      icon: LayoutDashboard,
    },
    { label: 'Belgelerim', path: '/student/documents', icon: FileText },
    sharedItems.notifications,
    sharedItems.profile,
  ],
  REVIEWER: [
    sharedItems.home,
    {
      label: 'Kontrol Paneli',
      path: '/reviewer/dashboard',
      icon: LayoutDashboard,
    },
    {
      label: 'Bekleyen Belgeler',
      path: '/reviewer/documents',
      icon: ClipboardCheck,
    },
    {
      label: 'Öğrenciler',
      path: '/reviewer/students',
      icon: GraduationCap,
    },
    {
      label: 'Değerlendirmelerim',
      path: '/reviewer/reviews',
      icon: ListChecks,
    },
    sharedItems.notifications,
    sharedItems.profile,
  ],
  DORMITORY_ADMIN: [
    sharedItems.home,
    {
      label: 'Kontrol Paneli',
      path: '/admin/dashboard',
      icon: LayoutDashboard,
    },
    { label: 'Kabuller', path: '/admin/admissions', icon: ClipboardList },
    {
      label: 'Öğrenciler',
      path: '/admin/students',
      icon: GraduationCap,
    },
    {
      label: 'Değerlendiriciler',
      path: '/admin/reviewers',
      icon: UserCheck,
    },
    { label: 'Belgeler', path: '/admin/documents', icon: Files },
    { label: 'İşlem Geçmişi', path: '/admin/history', icon: History },
    sharedItems.notifications,
    sharedItems.profile,
  ],
  GLOBAL_ADMIN: [
    sharedItems.home,
    {
      label: 'Genel Kontrol Paneli',
      path: '/global/dashboard',
      icon: LayoutDashboard,
    },
    { label: 'Yurtlar', path: '/global/dormitories', icon: Building2 },
    { label: 'Kullanıcılar', path: '/global/users', icon: Users },
    {
      label: 'Öğrenciler',
      path: '/global/students',
      icon: GraduationCap,
    },
    { label: 'Kabuller', path: '/global/admissions', icon: ClipboardList },
    {
      label: 'Yurt Dönemleri',
      path: '/global/terms',
      icon: CalendarRange,
    },
    {
      label: 'Belge Türleri',
      path: '/global/document-types',
      icon: FileCog,
    },
    {
      label: 'Belge Gereksinimleri',
      path: '/global/document-requirements',
      icon: FileText,
    },
    { label: 'Sistem Geçmişi', path: '/global/history', icon: History },
    ...(testCenterEnabled
      ? [{ label: 'Test Merkezi', path: '/global/test-center', icon: FlaskConical }]
      : []),
    sharedItems.notifications,
    sharedItems.profile,
  ],
}

const pageTitles = Object.values(navigationByCategory)
  .flat()
  .reduce<Record<string, string>>((titles, item) => {
    titles[item.path] = item.label
    return titles
  }, {})

pageTitles['/forbidden'] = 'Erişim Engellendi'

export function getPageTitle(pathname: string): string {
  return pageTitles[pathname] ?? 'Yurt Belge Yönetim Sistemi'
}
