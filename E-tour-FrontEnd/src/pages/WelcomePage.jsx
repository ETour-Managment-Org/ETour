import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { categoryApi } from '../api/categoryApi.js'
import { tourApi } from '../api/tourApi.js'
import CategoryCard from '../components/CategoryCard.jsx'
import TourCard from '../components/TourCard.jsx'
import Loader from '../components/Loader.jsx'
import ErrorBox from '../components/ErrorBox.jsx'
import { imageUrl, onImageError } from '../utils/media.js'

const HERO = '/images/tours/kerala-backwaters.png'

const SHOWCASE = [
  { src: '/images/tours/himalayas-leh.png', title: 'Himalayan Ladakh',
    copy: 'Pangong, Nubra and Khardung La', to: '/tours?category=27' },
  { src: '/images/tours/rajasthan-palace.png', title: 'Royal Rajasthan',
    copy: 'Forts, palaces and desert camps', to: '/tours?category=20' },
  { src: '/images/tours/bali-temple.png', title: 'South East Asia',
    copy: 'Temples, terraces and island reefs', to: '/tours?category=7' },
  { src: '/images/tours/goa-festival.png', title: 'Events & Carnivals',
    copy: 'Parades, festivals and match days', to: '/categories/32' },
  { src: '/images/tours/middle-east-desert.png', title: 'Middle East',
    copy: 'Dubai skyline and desert safari', to: '/tours?category=28' },
  { src: '/images/tours/kerala-backwaters.png', title: 'Kerala Backwaters',
    copy: 'Houseboats and tea country', to: '/tours?category=21' }
]

const STEPS = [
  { n: '1', t: 'Pick a sector', d: 'Domestic, International, Adventure, Pilgrimage or Events — drill down to the exact circuit.' },
  { n: '2', t: 'Compare the fare bands', d: 'Twin sharing, single occupancy, extra person and child rates are published on every tour.' },
  { n: '3', t: 'Enter your travellers', d: 'Fare band is derived from each passenger age on the departure date, not on the booking date.' },
  { n: '4', t: 'Pay and download', d: 'Pay through the gateway and download a PDF receipt with your booking reference.' }
]

export default function WelcomePage() {
  const [roots, setRoots] = useState([])
  const [tours, setTours] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    Promise.all([categoryApi.roots(), tourApi.all()])
      .then(([r, t]) => {
        setRoots(r || [])
        setTours((t || []).slice(0, 4))
      })
      .catch(setError)
      .finally(() => setLoading(false))
  }, [])

  return (
    <>
      <section className="hero">
        <img className="hero-img" src={imageUrl(HERO)} alt="Houseboat on the Kerala backwaters at golden hour" onError={onImageError} />
        <div className="hero-scrim" />
        <div className="container">
          <div className="hero-body">
            <span className="hero-chip">International · Domestic · Event-based group tours</span>
            <h1>Discover India and beyond, one unforgettable tour at a time.</h1>
            <p>
              Browse curated group tours, compare prices across passenger types, and book your next
              journey in minutes.
            </p>
            <div className="hero-actions">
              <Link to="/tours" className="btn btn-lg btn-light">Explore all tours</Link>
              <Link to="/home" className="btn btn-lg btn-outline-light">Browse by category</Link>
            </div>

            <div className="hero-stats">
              <div>
                <div className="hero-stat-num">{roots.length || '—'}</div>
                <div className="hero-stat-lbl">Sectors to explore</div>
              </div>
              <div>
                <div className="hero-stat-num">28</div>
                <div className="hero-stat-lbl">Years of operation</div>
              </div>
              <div>
                <div className="hero-stat-num">5</div>
                <div className="hero-stat-lbl">Published fare bands</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="section">
        <div className="container">
          <div className="section-head">
            <span className="eyebrow">Where to next</span>
            <h2>A destination for every kind of traveller</h2>
            <p className="muted">
              From high-altitude Himalayan passes to island reefs and carnival parades.
            </p>
          </div>

          <div className="grid grid-3">
            {SHOWCASE.map((s) => (
              <Link key={s.title} to={s.to} className="tour-card">
                <div className="tour-media">
                  <img src={imageUrl(s.src)} alt={s.title} onError={onImageError} loading="lazy" />
                </div>
                <div className="tour-body">
                  <h4>{s.title}</h4>
                  <div className="small muted">{s.copy}</div>
                  <div className="tour-foot">
                    <span className="small strong" style={{ color: 'var(--accent)' }}>Explore →</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      <section className="section section-alt">
        <div className="container">
          <div className="section-head">
            <span className="eyebrow">Browse by category</span>
            <h2>Every sector in the catalogue</h2>
            <p className="muted">
              A category either drills down to its sub-sectors or jumps straight to the tour listing.
            </p>
          </div>

          <ErrorBox error={error} />
          {loading ? <Loader /> : (
            <div className="grid grid-4">
              {roots.map((c) => <CategoryCard key={c.categoryId} node={c} />)}
            </div>
          )}
        </div>
      </section>

      <section className="section">
        <div className="container">
          <div className="between mb24">
            <div className="section-head" style={{ marginBottom: 0 }}>
              <span className="eyebrow">Featured</span>
              <h2>Popular right now</h2>
            </div>
            <Link to="/tours" className="btn btn-ghost">View all tours</Link>
          </div>

          {loading ? <Loader /> : (
            <div className="grid grid-4">
              {tours.map((t) => <TourCard key={t.tourId} tour={t} />)}
            </div>
          )}
        </div>
      </section>

      <section className="section section-alt">
        <div className="container">
          <div className="section-head">
            <span className="eyebrow">How booking works</span>
            <h2>Four steps from browsing to receipt</h2>
          </div>

          <div className="grid grid-4">
            {STEPS.map((s) => (
              <div key={s.n} className="card card-pad">
                <div className="tile-icon mb16">{s.n}</div>
                <h4 className="mb8">{s.t}</h4>
                <div className="small muted">{s.d}</div>
              </div>
            ))}
          </div>

          <div className="card card-pad mt32">
            <div className="between wrap">
              <div>
                <h3 className="mb8">Ready when you are</h3>
                <div className="small muted">Create an account to save bookings and download receipts.</div>
              </div>
              <div className="center wrap">
                <Link to="/register" className="btn btn-lg">Create an account</Link>
                <Link to="/search" className="btn btn-lg btn-ghost">Search by budget</Link>
              </div>
            </div>
          </div>
        </div>
      </section>
    </>
  )
}
