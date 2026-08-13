const DEFAULT_LOCALE = 'en-IN'

const cache = new Map()

const formatter = (key, build) => {
  let f = cache.get(key)
  if (!f) { f = build(); cache.set(key, f) }
  return f
}

const isBlank = (n) => n === null || n === undefined || n === '' || isNaN(n)

const INDIAN = new Set(['en-IN', 'hi-IN', 'mr-IN'])
const moneyLocale = (locale) => (INDIAN.has(locale) ? 'en-IN' : locale)

export const inr = (n, locale = DEFAULT_LOCALE) => {
  if (isBlank(n)) return '—'
  const loc = moneyLocale(locale)
  return formatter(`cur0:${loc}`, () => new Intl.NumberFormat(loc, {
    style: 'currency', currency: 'INR', maximumFractionDigits: 0
  })).format(Number(n))
}

export const inrExact = (n, locale = DEFAULT_LOCALE) => {
  if (isBlank(n)) return '—'
  const loc = moneyLocale(locale)
  return formatter(`cur2:${loc}`, () => new Intl.NumberFormat(loc, {
    style: 'currency', currency: 'INR',
    minimumFractionDigits: 2, maximumFractionDigits: 2
  })).format(Number(n))
}

export const number = (n, locale = DEFAULT_LOCALE) => {
  if (isBlank(n)) return '—'
  const loc = moneyLocale(locale)
  return formatter(`num:${loc}`, () => new Intl.NumberFormat(loc)).format(Number(n))
}

export const prettyDate = (iso, locale = DEFAULT_LOCALE) => {
  if (!iso) return '—'
  const d = new Date(iso)
  if (isNaN(d)) return iso
  return formatter(`date:${locale}`, () => new Intl.DateTimeFormat(locale, {
    day: 'numeric', month: 'short', year: 'numeric'
  })).format(d)
}

export const longDate = (iso, locale = DEFAULT_LOCALE) => {
  if (!iso) return '—'
  const d = new Date(iso)
  if (isNaN(d)) return iso
  return formatter(`datelong:${locale}`, () => new Intl.DateTimeFormat(locale, {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
  })).format(d)
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

const BANDS = {
  en: {
    TWIN_SHARING: 'Twin sharing',
    SINGLE: 'Single occupancy',
    EXTRA_PERSON: 'Extra person on an extra bed',
    CHILD_WITH_BED: 'Child with bed',
    CHILD_WITHOUT_BED: 'Child without bed',
    ADULT: 'Twin sharing',
    SINGLE_PERSON: 'Single occupancy'
  },
  hi: {
    TWIN_SHARING: 'ट्विन शेयरिंग',
    SINGLE: 'सिंगल रूम',
    EXTRA_PERSON: 'अतिरिक्त बिस्तर पर अतिरिक्त व्यक्ति',
    CHILD_WITH_BED: 'बिस्तर सहित बच्चा',
    CHILD_WITHOUT_BED: 'बिना बिस्तर बच्चा',
    ADULT: 'ट्विन शेयरिंग',
    SINGLE_PERSON: 'सिंगल रूम'
  },
  mr: {
    TWIN_SHARING: 'ट्विन शेअरिंग',
    SINGLE: 'सिंगल रूम',
    EXTRA_PERSON: 'अतिरिक्त बेडवर अतिरिक्त व्यक्ती',
    CHILD_WITH_BED: 'बेडसह मूल',
    CHILD_WITHOUT_BED: 'बेडशिवाय मूल',
    ADULT: 'ट्विन शेअरिंग',
    SINGLE_PERSON: 'सिंगल रूम'
  }
}

export const bandLabel = (band, lang = 'en') =>
  (BANDS[lang] || BANDS.en)[band] || BANDS.en[band] || band || '-'

export const pluralise = (n, one, many) => `${n} ${n === 1 ? one : many || one + 's'}`
