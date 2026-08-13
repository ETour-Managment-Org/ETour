import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { feedbackApi } from '../api/feedbackApi.js'
import StarRating from './StarRating.jsx'
import { prettyDate } from '../utils/format.js'
import { useI18n } from '../i18n/I18nContext.jsx'

export default function FeedbackWall({ limit = 6, title = 'What visitors say' }) {
  const { locale } = useI18n()
  const [items, setItems] = useState([])
  const [loaded, setLoaded] = useState(false)

  useEffect(() => {
    let alive = true
    feedbackApi.published()
      .then((rows) => { if (alive) setItems(Array.isArray(rows) ? rows : []) })
      .catch(() => { if (alive) setItems([]) })
      .finally(() => { if (alive) setLoaded(true) })

    return () => { alive = false }
  }, [])

  if (!loaded || items.length === 0) return null

  const shown = items.slice(0, limit)

  const rated = items.filter((f) => f.rating > 0)
  const average = rated.length
    ? (rated.reduce((sum, f) => sum + f.rating, 0) / rated.length).toFixed(1)
    : null

  return (
    <section className="mt32">
      <div className="between mb16">
        <div>
          <span className="eyebrow">From our visitors</span>
          <h2>{title}</h2>
        </div>
        {average && (
          <div className="right">
            <div className="price" style={{ fontSize: 26 }}>{average} / 5</div>
            <div className="tiny muted">{rated.length} rated the site</div>
          </div>
        )}
      </div>

      <div className="grid grid-3">
        {shown.map((f) => (
          <article key={f.feedbackId} className="card card-pad">
            {f.rating > 0 && <StarRating value={f.rating} />}

            <p className="mt8">{f.message}</p>

            <div className="between mt16">
              <span className="small strong">{f.name}</span>
              {f.fromRegisteredUser && (
                <span className="badge badge-green">Verified customer</span>
              )}
            </div>
            {f.submittedOn && (
              <div className="tiny muted mt8">{prettyDate(f.submittedOn, locale)}</div>
            )}
          </article>
        ))}
      </div>

      <div className="center mt16" style={{ justifyContent: 'center' }}>
        <Link to="/feedback" className="btn btn-ghost btn-sm">Share your feedback</Link>
      </div>
    </section>
  )
}
