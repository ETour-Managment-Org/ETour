import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { bookingApi } from '../api/bookingApi.js'
import { reviewApi } from '../api/reviewApi.js'
import { useAuth } from '../context/AuthContext.jsx'
import Loader from '../components/Loader.jsx'
import ErrorBox from '../components/ErrorBox.jsx'
import Modal from '../components/Modal.jsx'
import StarRating from '../components/StarRating.jsx'
import { inr, prettyDate, statusClass, bandLabel } from '../utils/format.js'
import Breadcrumb from '../components/Breadcrumb.jsx'
import EditProfilePanel from '../components/EditProfilePanel.jsx'
import { useI18n } from '../i18n/I18nContext.jsx'

export default function DashboardPage() {
  const { tc, locale, lang } = useI18n()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const [bookings, setBookings] = useState([])
  const [reviewable, setReviewable] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [notice, setNotice] = useState(null)

  const [cancelId, setCancelId] = useState(null)
  const [reason, setReason] = useState('')
  const [reviewFor, setReviewFor] = useState(null)
  const [paxFor, setPaxFor] = useState(null)
  const [review, setReview] = useState({ rating: 5, reviewTitle: '', reviewDescription: '' })
  const [busy, setBusy] = useState(false)

  const load = () => {
    setLoading(true)
    Promise.all([bookingApi.mine(), reviewApi.reviewable().catch(() => [])])
      .then(([b, r]) => {
        setBookings(b || [])
        setReviewable(r || [])
      })
      .catch(setError)
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  useEffect(() => {
    const wanted = Number(searchParams.get('review'))
    if (!wanted || loading) return

    if (reviewable.includes(wanted)) {
      setReviewFor(wanted)
    } else if (bookings.some((b) => b.bookingId === wanted)) {
      setNotice('That tour has already been reviewed. Thank you.')
    }
    searchParams.delete('review')
    setSearchParams(searchParams, { replace: true })
  }, [loading, reviewable, bookings])

  const doCancel = async () => {
    setBusy(true)
    try {
      const res = await bookingApi.cancel(cancelId, reason)
      setNotice(`Booking #${cancelId} cancelled. Refund of ${inr(res.refundAmount, locale)} — ${res.remarks}`)
      setCancelId(null)
      setReason('')
      load()
    } catch (err) {
      setError(err)
    } finally {
      setBusy(false)
    }
  }

  const doReview = async () => {
    setBusy(true)
    try {
      await reviewApi.submit({ bookingId: reviewFor, ...review })
      setNotice('Thanks — your review has been published.')
      setReviewFor(null)
      setReview({ rating: 5, reviewTitle: '', reviewDescription: '' })
      load()
    } catch (err) {
      setError(err)
    } finally {
      setBusy(false)
    }
  }

  const CLOSED = ['CANCELLED', 'COMPLETED']
  const active = bookings.filter((b) => !CLOSED.includes(b.bookingStatus))
  const spent = bookings
    .filter((b) => b.bookingStatus !== 'CANCELLED')
    .reduce((s, b) => s + Number(b.totalAmount || 0), 0)
  const bookingNo = (b) => b.customerBookingNumber ?? b.bookingId

  return (
    <div className="container page">
      <Breadcrumb items={[{ label: 'Home', to: '/' }, { label: 'My bookings' }]} />
      <h1 className="mb8">Welcome back, {user?.fullName || user?.username}</h1>
      <p className="muted mb24">Manage your bookings and share your experience.</p>

      {notice && <div className="alert alert-ok">{notice}</div>}
      <ErrorBox error={error} />

      <div className="grid grid-3 mb32">
        <div className="card card-pad">
          <div className="tiny muted">Total bookings</div>
          <div className="serif" style={{ fontSize: 32, fontWeight: 700 }}>{bookings.length}</div>
        </div>
        <div className="card card-pad">
          <div className="tiny muted">Active bookings</div>
          <div className="price" style={{ fontSize: 32 }}>{active.length}</div>
        </div>
        <div className="card card-pad">
          <div className="tiny muted">Total spent</div>
          <div className="serif" style={{ fontSize: 32, fontWeight: 700, color: 'var(--accent)' }}>{inr(spent, locale)}</div>
        </div>
      </div>

      <EditProfilePanel />

      <h2 className="mb16">My bookings</h2>

      {loading ? (
        <Loader full message="Loading your bookings…" />
      ) : bookings.length === 0 ? (
        <div className="card card-pad empty">
          <p className="mb16">You have not booked anything yet.</p>
          <Link to="/tours" className="btn">Browse tours</Link>
        </div>
      ) : (
        bookings.map((b) => {
          const canReview = reviewable.includes(b.bookingId)
          const canCancel = b.bookingStatus === 'CONFIRMED' || b.bookingStatus === 'PENDING'
          return (
            <div key={b.bookingId} className="card card-pad mb16">
              <div className="between wrap mb16">
                <div>
                  <div className="center mb8" style={{ gap: 10 }}>
                    <h3>{tc(b.tourName)}</h3>
                    <span className={statusClass(b.bookingStatus)}>{tc(b.bookingStatus)}</span>
                  </div>
                  <div className="small muted">
                    Booking #{bookingNo(b)} · {tc(b.destination)} · Departs {prettyDate(b.departureDate, locale)}
                  </div>
                </div>
                <div className="right">
                  <div style={{ fontSize: 20, fontWeight: 700 }}>{inr(b.totalAmount, locale)}</div>
                  <div className="small muted">{b.noOfPax} passenger{b.noOfPax === 1 ? '' : 's'}</div>
                </div>
              </div>

              <div className="row wrap">
                <Link to={`/booking/confirmation/${b.bookingId}`} className="btn btn-ghost">View receipt</Link>
                <button className="btn btn-ghost" onClick={() => setPaxFor(b)}>
                  View passengers
                </button>
                <button className="btn btn-ghost" onClick={() => navigate(`/tours/${b.tourId}`)}>
                  View tour
                </button>
                {canReview && (
                  <button className="btn" onClick={() => setReviewFor(b.bookingId)}>Write a review</button>
                )}
                {canCancel && (
                  <button className="btn btn-danger" onClick={() => setCancelId(b.bookingId)}>Cancel booking</button>
                )}
              </div>
            </div>
          )
        })
      )}

      <Modal open={paxFor !== null}>
        {paxFor && (
          <>
            <h3 className="mb8">Passengers on this tour</h3>
            <div className="small muted mb16">
              {tc(paxFor.tourName)} · Booking #{bookingNo(paxFor)} · Departs {prettyDate(paxFor.departureDate, locale)}
            </div>

            {(paxFor.passengers || []).length === 0 ? (
              <div className="empty">No passenger details recorded.</div>
            ) : (
              <table className="table" style={{ textAlign: 'left' }}>
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Age at departure</th>
                    <th>Fare band</th>
                    <th className="right">Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {paxFor.passengers.map((p) => (
                    <tr key={p.paxId}>
                      <td className="strong">{p.fullName}</td>
                      <td>{p.ageAtDeparture ?? '-'}</td>
                      <td><span className="badge badge-slate">{bandLabel(p.paxType, lang)}</span></td>
                      <td className="right">{inr(p.paxAmount, locale)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}

            <div className="inv-row mt16">
              <span className="muted small">Rooms and extra beds</span>
              <span className="strong">
                {paxFor.roomsRequired ?? '-'} room{paxFor.roomsRequired === 1 ? '' : 's'}
                {paxFor.extraBeds ? `, ${paxFor.extraBeds} extra bed${paxFor.extraBeds === 1 ? '' : 's'}` : ''}
              </span>
            </div>

            <div className="row mt16">
              <button className="btn grow" onClick={() => navigate(`/tours/${paxFor.tourId}`)}>
                View tour
              </button>
              <button className="btn btn-ghost" onClick={() => setPaxFor(null)}>Close</button>
            </div>
          </>
        )}
      </Modal>

      <Modal open={cancelId !== null}>
        <h3 className="mb8">
          Cancel booking #{(() => {
            const b = bookings.find((x) => x.bookingId === cancelId)
            return b ? bookingNo(b) : cancelId
          })()}
        </h3>
        <p className="small muted mb16">
          Refund depends on how many days remain before departure. 7 days or more gets a full refund.
        </p>
        <textarea className="textarea" rows={3} placeholder="Reason for cancelling"
                  value={reason} onChange={(e) => setReason(e.target.value)} />
        <div className="row mt16">
          <button className="btn btn-danger grow" onClick={doCancel} disabled={busy || !reason.trim()}>
            {busy ? 'Cancelling…' : 'Confirm cancellation'}
          </button>
          <button className="btn btn-ghost" onClick={() => setCancelId(null)}>Keep it</button>
        </div>
      </Modal>

      <Modal open={reviewFor !== null}>
        <h3 className="mb16">Rate your trip</h3>
        <div className="field" style={{ textAlign: 'left' }}>
          <label className="label">Rating</label>
          <select className="select" value={review.rating}
                  onChange={(e) => setReview({ ...review, rating: Number(e.target.value) })}>
            {[5, 4, 3, 2, 1].map((n) => <option key={n} value={n}>{n} star{n === 1 ? '' : 's'}</option>)}
          </select>
          <div className="mt8"><StarRating value={review.rating} /></div>
        </div>
        <div className="field" style={{ textAlign: 'left' }}>
          <label className="label">Title</label>
          <input className="input" value={review.reviewTitle}
                 onChange={(e) => setReview({ ...review, reviewTitle: e.target.value })} />
        </div>
        <div className="field" style={{ textAlign: 'left' }}>
          <label className="label">Your review</label>
          <textarea className="textarea" rows={4} value={review.reviewDescription}
                    onChange={(e) => setReview({ ...review, reviewDescription: e.target.value })} />
        </div>
        <div className="row">
          <button className="btn grow" onClick={doReview} disabled={busy}>
            {busy ? 'Publishing…' : 'Publish review'}
          </button>
          <button className="btn btn-ghost" onClick={() => setReviewFor(null)}>Cancel</button>
        </div>
      </Modal>
    </div>
  )
}
