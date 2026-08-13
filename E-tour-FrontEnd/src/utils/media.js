const IMAGE_BASE = import.meta.env.VITE_IMAGE_BASE_URL || ''

export const FALLBACK_IMAGE = '/images/tours/kerala-backwaters.png'

export function imageUrl(path) {
  if (!path) return null
  if (/^https?:\/\//i.test(path)) return path
  const clean = path.startsWith('/') ? path : `/${path}`
  return `${IMAGE_BASE}${clean}`
}

export function tourImage(tour) {
  if (!tour) return null
  if (tour.primaryImageUrl) return imageUrl(tour.primaryImageUrl)
  const list = tour.images || []
  const primary = list.find((i) => i.isPrimary) || list[0]
  return primary ? imageUrl(primary.source) : null
}

export function tourGallery(tour) {
  const list = (tour && tour.images) || []
  return list.map((i) => ({ ...i, url: imageUrl(i.source) })).filter((i) => i.url)
}

export function onImageError(e) {
  if (e.currentTarget.dataset.fallbackApplied) return
  e.currentTarget.dataset.fallbackApplied = '1'
  e.currentTarget.src = imageUrl(FALLBACK_IMAGE)
}
