import { Link } from 'react-router-dom'
import { inr, duration, prettyDate } from '../utils/format.js'
import { tourImage, onImageError, imageUrl, FALLBACK_IMAGE } from '../utils/media.js'
import StarRating from './StarRating.jsx'
import { useI18n } from '../i18n/I18nContext.jsx'

export default function TourCard({ tour }) {
  const { t, tc, locale } = useI18n()
  const img = tourImage(tour) || imageUrl(FALLBACK_IMAGE)
  const nextDeparture = tour.nextDepartureDate || (tour.schedules || [])[0]?.startDate

  return (
    <Link to={`/tours/${tour.tourId}`} className="tour-card">
      <div className="tour-media">
        <img src={img} alt={`${tc(tour.tourName)} in ${tc(tour.destination) || ''}`} onError={onImageError} loading="lazy" />
        {tour.categoryName && <span className="badge badge-float">{tc(tour.categoryName)}</span>}
      </div>

      <div className="tour-body">
        <h3 style={{ fontSize: 17 }}>{tc(tour.tourName)}</h3>

        <div className="tour-meta">
          <span>{tc(tour.destination) || '—'}</span>
          <span>{tour.durationLabel || duration(tour.days, tour.nights)}</span>
          {nextDeparture && <span>{t('tour.nextDeparture')} {prettyDate(nextDeparture, locale)}</span>}
        </div>

        {tour.averageRating ? <StarRating value={tour.averageRating} count={tour.reviewCount} /> : null}

        <div className="tour-foot">
          <div>
            <div className="tiny muted">{t('tour.costFrom')}</div>
            <div className="price" style={{ fontSize: 21 }}>{inr(tour.startingPrice ?? tour.price, locale)}</div>
            <div className="tiny muted">{t('common.perPerson')}</div>
          </div>
          <span className="small strong" style={{ color: 'var(--accent)' }}>{t('common.viewDetails')} →</span>
        </div>
      </div>
    </Link>
  )
}
