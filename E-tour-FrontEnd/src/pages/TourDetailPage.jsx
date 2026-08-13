import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { tourApi } from '../api/tourApi.js'
import { reviewApi } from '../api/reviewApi.js'
import { categoryApi } from '../api/categoryApi.js'
import { useBooking } from '../context/BookingContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import Breadcrumb from '../components/Breadcrumb.jsx'
import StarRating from '../components/StarRating.jsx'
import Loader from '../components/Loader.jsx'
import ErrorBox from '../components/ErrorBox.jsx'
import { inr, prettyDate, duration } from '../utils/format.js'
import { tourImage, tourGallery, onImageError, imageUrl, FALLBACK_IMAGE } from '../utils/media.js'
import { useI18n } from '../i18n/I18nContext.jsx'

const TABS = ['Overview', 'Itinerary', 'Cost & dates', 'Travel info', 'Reviews']

const isUpcoming = (s) => {
  if (!s?.startDate) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return new Date(s.startDate) >= today
}

export default function TourDetailPage() {
  const { t, tc, locale } = useI18n()
  const { id } = useParams()
  const navigate = useNavigate()
  const { startBooking } = useBooking()
  const { isLoggedIn } = useAuth()

  const [tour, setTour] = useState(null)
  const [reviews, setReviews] = useState(null)
  const [trail, setTrail] = useState([])
  const [tab, setTab] = useState('Overview')
  const [scheduleId, setScheduleId] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    setLoading(true)
    tourApi
      .byId(id)
      .then((t) => {
        setTour(t)
        const open = (t.schedules || []).find((s) => isUpcoming(s) && (s.availableSeats ?? 0) > 0)
        if (open) setScheduleId(String(open.scheduleId))
        if (t.categoryId) categoryApi.breadcrumb(t.categoryId).then(setTrail).catch(() => {})
        return reviewApi.forTour(id).then(setReviews).catch(() => {})
      })
      .catch(setError)
      .finally(() => setLoading(false))
  }, [id])

  const book = () => {
    if (!scheduleId) return
    startBooking(tour, scheduleId)
    if (isLoggedIn) navigate('/booking/details')
    else navigate('/login', { state: { from: '/booking/details' } })
  }

  if (loading) return <div className="container page"><Loader full message="Loading this tour…" /></div>
  if (error) return <div className="container page"><ErrorBox error={error} /></div>
  if (!tour) return null

  const upcoming = (tour.schedules || []).filter(isUpcoming)
  const selected = upcoming.find((s) => s.scheduleId === Number(scheduleId))
  const bookable = upcoming.filter((s) => (s.availableSeats ?? 0) > 0)
  const gallery = tourGallery(tour)
  const activeCost = (tour.costs || [])[0]

  return (
    <div className="container page">
      <Breadcrumb
        items={[
          { label: 'Home', to: '/' },
          { label: 'Browse', to: '/home' },
          ...trail.map((c, i) => ({
            label: tc(c.categoryName),
            to: i === trail.length - 1
              ? `/tours?category=${c.categoryId}`
              : `/categories/${c.categoryId}`
          })),
          { label: tc(tour.tourName) }
        ]}
      />

      <div className="grid grid-side">
        <div>
          <div className="card mb24">
            <div className="thumb">
              <img
                src={tourImage(tour) || imageUrl(FALLBACK_IMAGE)}
                alt={`${tc(tour.tourName)} in ${tc(tour.destination) || ''}`}
                onError={onImageError}
              />
            </div>
            <div className="card-pad">
              {tour.categoryName && <span className="badge badge-accent mb8">{tc(tour.categoryName)}</span>}
              <h1 className="mb8">{tc(tour.tourName)}</h1>
              <div className="center wrap" style={{ gap: 14 }}>
                <span className="small muted">{tc(tour.destination)}</span>
                <span className="badge badge-slate">{tour.durationLabel || duration(tour.days, tour.nights)}</span>
                {tour.averageRating ? <StarRating value={tour.averageRating} count={tour.reviewCount} /> : null}
              </div>
              {gallery.length > 1 && (
                <div className="gallery">
                  {gallery.slice(0, 4).map((img) => (
                    <img key={img.imageId} src={img.url} alt={img.imageTitle || tc(tour.tourName)}
                         onError={onImageError} loading="lazy" />
                  ))}
                </div>
              )}
            </div>
          </div>

          <div className="tabs">
            {TABS.map((t) => (
              <button key={t} className={tab === t ? 'tab active' : 'tab'} onClick={() => setTab(t)}>{t}</button>
            ))}
          </div>

          {tab === 'Overview' && (
            <div className="card card-pad">
              <p>{tour.description || 'No description available.'}</p>
              {tour.location && <p className="small muted mt16">Location: {tour.location}</p>}
            </div>
          )}

          {tab === 'Itinerary' && (
            <div className="card card-pad">
              {(tour.itineraries || []).length === 0 && <div className="muted">No itinerary published.</div>}
              {(tour.itineraries || []).map((d) => (
                <div key={d.itineraryId} className="mb16">
                  <div className="center mb8">
                    <span className="badge badge-blue">Day {d.dayNumber}</span>
                    {d.location && <span className="small muted">{d.location}</span>}
                  </div>
                  <div className="small">{d.description}</div>
                </div>
              ))}
            </div>
          )}

          {tab === 'Cost & dates' && (
            <div className="card card-pad">
              <h4 className="mb8">Fare bands</h4>
              <p className="small muted mb16">
                Twin sharing is the default basis. All rates are per person.
              </p>
              <table className="table mb24">
                <thead>
                  <tr>
                    <th>Twin sharing</th>
                    <th>Single occupancy</th>
                    <th>Extra person</th>
                    <th>Child with bed</th>
                    <th>Child no bed</th>
                    <th>Valid</th>
                  </tr>
                </thead>
                <tbody>
                  {(tour.costs || []).map((c) => (
                    <tr key={c.costId}>
                      <td className="strong">{inr(c.adultPrice, locale)}</td>
                      <td>{inr(c.singlePersonPrice, locale)}</td>
                      <td>{inr(c.extraPersonPrice, locale)}</td>
                      <td>{inr(c.childWithBedPrice, locale)}</td>
                      <td>{inr(c.childWithoutBedPrice, locale)}</td>
                      <td className="small muted">{prettyDate(c.validFrom, locale)} - {prettyDate(c.validTo, locale)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <div className="alert alert-info small">
                One room holds 2 people in 2 beds plus 1 extra bed. A third traveller can take an extra
                bed in the same room, or a different room on single occupancy.
              </div>

              <h4 className="mb16">Departure dates</h4>
              <table className="table">
                <thead><tr><th>Departs</th><th>Seats left</th><th>Status</th></tr></thead>
                <tbody>
                  {upcoming.map((s) => (
                    <tr key={s.scheduleId}>
                      <td>{prettyDate(s.startDate, locale)}</td>
                      <td>{s.availableSeats} / {s.totalSeats}</td>
                      <td>
                        <span className={s.availableSeats > 0 ? 'badge badge-green' : 'badge badge-red'}>
                          {s.availableSeats > 0 ? 'Open' : 'Full'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {upcoming.length === 0 && (
                <div className="muted mt16">No upcoming departures scheduled.</div>
              )}
            </div>
          )}

          {tab === 'Travel info' && (
            <div className="card card-pad">
              {[
                ['Stay and meals', tour.stayAndMeals],
                ['Add-ons', tour.addOns],
                ['Passport and visa', tour.passportAndVisa],
                ['Weather', tour.weather],
                ["Do's and don'ts", tour.doAndDont]
              ].map(([label, val]) => (
                <div key={label} className="mb16">
                  <h4 className="mb8">{label}</h4>
                  <div className="small muted">{val || 'Not specified.'}</div>
                </div>
              ))}
            </div>
          )}

          {tab === 'Reviews' && (
            <div className="card card-pad">
              {!reviews || reviews.totalReviews === 0 ? (
                <div className="muted">No reviews yet.</div>
              ) : (
                <>
                  <div className="center mb24" style={{ gap: 24 }}>
                    <div>
                      <div className="price" style={{ fontSize: 42 }}>
                        {reviews.averageRating?.toFixed(1)}
                      </div>
                      <div className="small muted">{reviews.totalReviews} reviews</div>
                    </div>
                    <div className="grow">
                      {(reviews.distribution || []).map((b) => (
                        <div key={b.stars} className="center mb8" style={{ gap: 10 }}>
                          <span className="small" style={{ width: 40 }}>{b.stars} ★</span>
                          <div className="bar"><div className="bar-fill" style={{ width: `${b.percentage}%` }} /></div>
                          <span className="small muted" style={{ width: 28 }}>{b.count}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                  {(reviews.reviews || []).map((r) => (
                    <div key={r.reviewId} className="mb16" style={{ borderTop: '1px solid var(--border)', paddingTop: 14 }}>
                      <div className="between mb8">
                        <span className="strong">{r.customerName}</span>
                        <StarRating value={r.rating} />
                      </div>
                      {r.reviewTitle && <div className="strong small mb8">{r.reviewTitle}</div>}
                      <div className="small muted">{r.reviewDescription}</div>
                      <div className="tiny muted mt8">{prettyDate(r.reviewDate, locale)}</div>
                    </div>
                  ))}
                </>
              )}
            </div>
          )}
        </div>

        <aside className="card card-pad sticky-side">
          <div className="small muted">Tour cost from</div>
          <div className="price" style={{ fontSize: 32, lineHeight: 1.2 }}>
            {inr(activeCost?.adultPrice || tour.price, locale)}
          </div>
          <div className="small muted mb16">per person, twin sharing basis</div>

          <div className="field">
            <label className="label">Departure date</label>
            <select className="select" value={scheduleId} onChange={(e) => setScheduleId(e.target.value)}>
              <option value="">Select a date</option>
              {bookable.map((s) => (
                <option key={s.scheduleId} value={s.scheduleId}>
                  {prettyDate(s.startDate, locale)} — {s.availableSeats} seats
                </option>
              ))}
            </select>
            {bookable.length === 0 && (
              <div className="err-text">

                {upcoming.length === 0
                  ? 'No upcoming departures for this tour. Please check back later.'
                  : 'All upcoming departures are sold out.'}
              </div>
            )}
          </div>

          {selected && (
            <div className="alert alert-info small">
              {selected.availableSeats} of {selected.totalSeats} seats remaining on this departure.
            </div>
          )}

          <button className="btn btn-lg btn-block" onClick={book} disabled={!scheduleId}>
            Book now
          </button>
          <div className="hint" style={{ textAlign: 'center' }}>
            {isLoggedIn ? 'You will not be charged yet' : 'You will be asked to sign in'}
          </div>
        </aside>
      </div>
    </div>
  )
}
