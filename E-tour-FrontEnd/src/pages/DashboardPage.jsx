import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { bookingApi } from '../api/bookingApi.js'
import { reviewApi } from '../api/reviewApi.js'
import { useAuth } from '../context/AuthContext.jsx'
import Loader from '../components/Loader.jsx'
import ErrorBox from '../components/ErrorBox.jsx'
import Modal from '../components/Modal.jsx'
import StarRating from '../components/StarRating.jsx'
import { inr, prettyDate, statusClass } from '../utils/format.js'
import Breadcrumb from '../components/Breadcrumb.jsx'

export default function DashboardPage() {
  const { user } = useAuth()
  const [bookings, setBookings] = useState([])
  const [reviewable, setReviewable] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [notice, setNotice] = useState(null)

  const [cancelId, setCancelId] = useState(null)
  const [reason, setReason] = useState('')
  const [reviewFor, setReviewFor] = useState(null)
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

  const doCancel = async () => {
    setBusy(true)
    try {
      const res = await bookingApi.cancel(cancelId, reason)
      setNotice(`Booking #${cancelId} cancelled. Refund of ${inr(res.refundAmount)} — ${res.remarks}`)
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

  const active = bookings.filter((b) => b.bookingStatus !== 'CANCELLED')
  const spent = active.reduce((s, b) => s + Number(b.totalAmount || 0), 0)

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
          <div className="serif" style={{ fontSize: 32, fontWeight: 700, color: 'var(--accent)' }}>{inr(spent)}</div>
        </div>
      </div>

      <h2 className="mb16">My bookings</h2>

      {loading ? (
        <Loader />
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
                    <h3>{b.tourName}</h3>
                    <span className={statusClass(b.bookingStatus)}>{b.bookingStatus}</span>
                  </div>
                  <div className="small muted">
                    Booking #{b.bookingId} · {b.destination} · Departs {prettyDate(b.departureDate)}
                  </div>
                </div>
                <div className="right">
                  <div style={{ fontSize: 20, fontWeight: 700 }}>{inr(b.totalAmount)}</div>
                  <div className="small muted">{b.noOfPax} passenger{b.noOfPax === 1 ? '' : 's'}</div>
                </div>
              </div>

              <div className="row wrap">
                <Link to={`/booking/confirmation/${b.bookingId}`} className="btn btn-ghost">View receipt</Link>
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

      <Modal open={cancelId !== null}>
        <h3 className="mb8">Cancel booking #{cancelId}</h3>
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
