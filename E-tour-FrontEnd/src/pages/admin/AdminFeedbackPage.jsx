import { useEffect, useState } from 'react'
import { feedbackApi } from '../../api/feedbackApi.js'
import Loader from '../../components/Loader.jsx'
import ErrorBox from '../../components/ErrorBox.jsx'
import StarRating from '../../components/StarRating.jsx'
import { prettyDate } from '../../utils/format.js'
import { useI18n } from '../../i18n/I18nContext.jsx'

const FILTERS = ['ALL', 'NEW', 'REVIEWED', 'CLOSED']

const CATEGORY_CLASS = {
  PROBLEM: 'badge badge-red',
  SUGGESTION: 'badge badge-accent',
  COMPLIMENT: 'badge badge-green',
  OTHER: 'badge badge-slate'
}

export default function AdminFeedbackPage() {
  const { locale } = useI18n()
  const [items, setItems] = useState([])
  const [filter, setFilter] = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [busyId, setBusyId] = useState(null)

  const load = (status = filter) => {
    setLoading(true)
    feedbackApi.adminAll(status).then(setItems).catch(setError).finally(() => setLoading(false))
  }

  useEffect(() => { load(filter) }, [filter])

  const setPublished = async (id, published) => {
    setBusyId(id)
    setError(null)
    try {
      await feedbackApi.adminSetPublished(id, published)
      load(filter)
    } catch (err) {
      setError(err)
    } finally {
      setBusyId(null)
    }
  }

  const setStatus = async (id, status) => {
    setBusyId(id)
    setError(null)
    try {
      await feedbackApi.adminSetStatus(id, status)
      load(filter)
    } catch (err) {
      setError(err)
    } finally {
      setBusyId(null)
    }
  }

  if (loading) return <Loader />

  const publishedCount = items.filter((f) => f.published).length

  return (
    <>
      <ErrorBox error={error} />

      <div className="between wrap mb16">
        <div>
          <h3>Website feedback</h3>
          <div className="tiny muted">{publishedCount} of {items.length} shown publicly</div>
        </div>
        <div className="center wrap" style={{ gap: 6 }}>
          {FILTERS.map((f) => (
            <button key={f}
                    className={filter === f ? 'btn btn-sm' : 'btn btn-ghost btn-sm'}
                    onClick={() => setFilter(f)}>
              {f === 'ALL' ? 'All' : f.charAt(0) + f.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </div>

      {items.length === 0 ? (
        <div className="card card-pad empty">No feedback in this view.</div>
      ) : (
        items.map((f) => (
          <div key={f.feedbackId} className="card card-pad mb16">
            <div className="between wrap mb8">
              <div className="center wrap" style={{ gap: 10 }}>
                <span className={CATEGORY_CLASS[f.category] || 'badge badge-slate'}>
                  {f.category}
                </span>
                {f.rating ? <StarRating value={f.rating} /> : null}
                <span className={f.status === 'NEW' ? 'badge badge-amber' : 'badge badge-slate'}>
                  {f.status}
                </span>
                {f.published && <span className="badge badge-green">On the website</span>}
              </div>
              <span className="tiny muted">
                #{f.feedbackId} · {prettyDate(f.createdAt, locale)}
              </span>
            </div>

            <p className="mb16" style={{ whiteSpace: 'pre-wrap' }}>{f.message}</p>

            <div className="between wrap">
              <div className="small muted">
                {f.name || 'Anonymous'}
                {f.email && <> · {f.email}</>}
                {f.fromRegisteredUser
                  ? <span className="badge badge-primary" style={{ marginLeft: 8 }}>customer</span>
                  : <span className="badge badge-slate" style={{ marginLeft: 8 }}>guest</span>}
              </div>

              <div className="row wrap">
                <button className={f.published ? 'btn btn-ghost btn-sm' : 'btn btn-sm'}
                        disabled={busyId === f.feedbackId}
                        onClick={() => setPublished(f.feedbackId, !f.published)}>
                  {f.published ? 'Remove from site' : 'Publish to site'}
                </button>
                {f.status !== 'REVIEWED' && (
                  <button className="btn btn-ghost btn-sm" disabled={busyId === f.feedbackId}
                          onClick={() => setStatus(f.feedbackId, 'REVIEWED')}>
                    Mark reviewed
                  </button>
                )}
                {f.status !== 'CLOSED' && (
                  <button className="btn btn-ghost btn-sm" disabled={busyId === f.feedbackId}
                          onClick={() => setStatus(f.feedbackId, 'CLOSED')}>
                    Close
                  </button>
                )}
                {f.email && (
                  <a className="btn btn-sm" href={`mailto:${f.email}?subject=Re: your e-Tour feedback`}>
                    Reply
                  </a>
                )}
              </div>
            </div>
          </div>
        ))
      )}
    </>
  )
}
