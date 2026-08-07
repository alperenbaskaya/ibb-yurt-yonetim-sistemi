const turkishDateFormatter = new Intl.DateTimeFormat('tr-TR', {
  dateStyle: 'long',
})

export function formatDate(value: string): string {
  const date = new Date(`${value}T00:00:00`)

  return Number.isNaN(date.getTime())
    ? 'Tarih bilgisi kullanılamıyor'
    : turkishDateFormatter.format(date)
}
