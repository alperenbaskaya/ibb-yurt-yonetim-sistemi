const turkishDateTimeFormatter = new Intl.DateTimeFormat('tr-TR', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

export function formatDateTime(value: string): string {
  const date = new Date(value)

  return Number.isNaN(date.getTime())
    ? 'Tarih bilgisi kullanılamıyor'
    : turkishDateTimeFormatter.format(date)
}
