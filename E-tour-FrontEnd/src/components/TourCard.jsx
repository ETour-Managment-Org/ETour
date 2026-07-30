import { Link } from 'react-router-dom'
import { inr, duration, prettyDate } from '../utils/format.js'
import { tourImage, onImageError, imageUrl, FALLBACK_IMAGE } from '../utils/media.js'
import StarRating from './StarRating.jsx'

export default function TourCard({ tour }) {
  const img = tourImage(tour) || imageUrl(FALLBACK_IMAGE)
  const nextDeparture = tour.nextDepartureDate || (tour.schedules || [])[0]?.startDate

  return (
    <Link to={`/tours/${tour.tourId}`} className="tour-card">
      <div className="tour-media">
        <img src={img} alt={`${tour.tourName} in ${tour.destination || ''}`} onError={onImageError} loading="lazy" />
        {tour.categoryName && <span className="badge badge-float">{tour.categoryName}</span>}
      </div>

      <div className="tour-body">
        <h3 style={{ fontSize: 17 }}>{tour.tourName}</h3>

        <div className="tour-meta">
          <span>{tour.destination || '—'}</span>
          <span>{tour.durationLabel || duration(tour.days, tour.nights)}</span>
          {nextDeparture && <span>Next departure {prettyDate(nextDeparture)}</span>}
        </div>

        {tour.averageRating ? <StarRating value={tour.averageRating} count={tour.reviewCount} /> : null}

        <div className="tour-foot">
          <div>
            <div className="tiny muted">Tour cost from</div>
            <div className="price" style={{ fontSize: 21 }}>{inr(tour.startingPrice ?? tour.price)}</div>
            <div className="tiny muted">per person, twin sharing</div>
          </div>
          <span className="small strong" style={{ color: 'var(--accent)' }}>View details →</span>
        </div>
      </div>
    </Link>
  )
}
