export const inr = (n) => {
  if (n === null || n === undefined || isNaN(n)) return '—'
  return '₹' + Number(n).toLocaleString('en-IN', { maximumFractionDigits: 0 })
}

export const inrExact = (n) => {
  if (n === null || n === undefined || isNaN(n)) return '—'
  return '₹' + Number(n).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

export const prettyDate = (iso) => {
  if (!iso) return '—'
  const d = new Date(iso)
  if (isNaN(d)) return iso
  return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
}

export const duration = (days, nights) =>
  days && nights ? `${nights}N / ${days}D` : days ? `${days} days` : '—'

export const statusClass = (status) => {
  switch ((status || '').toUpperCase()) {
    case 'CONFIRMED': return 'badge badge-blue'
    case 'COMPLETED': return 'badge badge-green'
    case 'CANCELLED': return 'badge badge-red'
    case 'PENDING': return 'badge badge-amber'
    default: return 'badge badge-slate'
  }
}

export const bandLabel = (band) => ({
  TWIN_SHARING: 'Twin sharing',
  SINGLE: 'Single occupancy',
  EXTRA_PERSON: 'Extra person on an extra bed',
  CHILD_WITH_BED: 'Child with bed',
  CHILD_WITHOUT_BED: 'Child without bed',
  ADULT: 'Twin sharing',
  SINGLE_PERSON: 'Single occupancy'
}[band] || band || '-')

export const pluralise = (n, one, many) => `${n} ${n === 1 ? one : many || one + 's'}`
