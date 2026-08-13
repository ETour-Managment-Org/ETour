import { useEffect, useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { adminApi } from '../../api/adminApi.js'
import Loader from '../../components/Loader.jsx'
import ErrorBox from '../../components/ErrorBox.jsx'
import Modal from '../../components/Modal.jsx'
import { inr } from '../../utils/format.js'
import { imageUrl, onImageError, FALLBACK_IMAGE } from '../../utils/media.js'
import { useI18n } from '../../i18n/I18nContext.jsx'

export default function AdminToursPage() {
  const { locale } = useI18n()
  const navigate = useNavigate()
  const location = useLocation()

  const [tours, setTours] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [notice, setNotice] = useState(location.state?.notice || null)
  const [deleting, setDeleting] = useState(null)
  const [busy, setBusy] = useState(false)

  const load = () => {
    setLoading(true)
    adminApi.tours().then(setTours).catch(setError).finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
    if (location.state?.notice) {
      window.history.replaceState({}, '')
    }
  }, [])

  const confirmDelete = async () => {
    setBusy(true)
    setError(null)
    try {
      await adminApi.deleteTour(deleting.tourId)
      setNotice(`Tour "${deleting.tourName}" removed.`)
      setDeleting(null)
      load()
    } catch (err) {
      setError(err)
      setDeleting(null)
    } finally {
      setBusy(false)
    }
  }

  if (loading) return <Loader full message="Loading the catalogue…" />

  return (
    <>
      {notice && <div className="alert alert-ok">{notice}</div>}
      <ErrorBox error={error} />

      <div className="between wrap mb16">
        <h3>{tours.length} tours</h3>
        <div className="row" style={{ gap: 8 }}>
          <button className="btn btn-ghost" onClick={() => navigate('/admin/tours/import')}>
            Import from Excel
          </button>
          <button className="btn" onClick={() => navigate('/admin/tours/new')}>Add a tour</button>
        </div>
      </div>

      <div className="grid grid-2">
        {tours.map((t) => (
          <div key={t.tourId} className="card">
            <div className="thumb" style={{ aspectRatio: '16 / 6' }}>
              <img
                src={imageUrl((t.images && t.images[0] && t.images[0].source) || FALLBACK_IMAGE)}
                alt={t.tourName}
                onError={onImageError}
                loading="lazy"
              />
            </div>
            <div className="card-pad">
              <div className="between mb8">
                <h4>{t.tourName}</h4>
                <span className={t.tourType === 'DOMESTIC' ? 'badge badge-accent' : 'badge badge-primary'}>
                  {t.tourType || 'UNSET'}
                </span>
              </div>
              <div className="small muted mb8">{t.destination}</div>
              <div className="center wrap small muted mb16" style={{ gap: 12 }}>
                <span>{t.durationLabel}</span>
                <span>{t.categoryName || 'No category'}</span>
                <span className="price">{inr(t.price, locale)}</span>
              </div>
              <div className="row wrap">
                <Link to={`/tours/${t.tourId}`} className="btn btn-ghost btn-sm">View</Link>
                <button className="btn btn-ghost btn-sm"
                        onClick={() => navigate(`/admin/tours/${t.tourId}/edit`)}>
                  Edit
                </button>
                {t.bookingCount > 0 ? (
                  <span className="badge badge-slate" title="Tours with bookings cannot be deleted">
                    {t.bookingCount} booking{t.bookingCount === 1 ? '' : 's'} - locked
                  </span>
                ) : (
                  <button className="btn btn-danger btn-sm" onClick={() => setDeleting(t)}>
                    Remove
                  </button>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      <Modal open={deleting !== null}>
        <h3 className="mb8">Remove this tour?</h3>
        <p className="muted mb24">
          <strong>{deleting?.tourName}</strong> will be removed from the catalogue.
          A tour that already has bookings cannot be deleted.
        </p>
        <div className="row">
          <button className="btn btn-danger grow" onClick={confirmDelete} disabled={busy}>
            {busy ? 'Removing…' : 'Yes, remove it'}
          </button>
          <button className="btn btn-ghost" onClick={() => setDeleting(null)}>Keep it</button>
        </div>
      </Modal>
    </>
  )
}
