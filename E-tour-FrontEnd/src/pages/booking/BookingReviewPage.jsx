import { useEffect, useMemo } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useBooking } from '../../context/BookingContext.jsx'
import Stepper from '../../components/Stepper.jsx'
import Breadcrumb from '../../components/Breadcrumb.jsx'
import { inr, prettyDate, bandLabel } from '../../utils/format.js'
import { pickCost, quote } from '../../utils/pricing.js'

export default function BookingReviewPage() {
  const { draft, hasDraft } = useBooking()
  const navigate = useNavigate()

  useEffect(() => {
    if (!hasDraft) navigate('/tours', { replace: true })
  }, [hasDraft, navigate])

  const departure = draft.schedule?.startDate
  const cost = useMemo(() => pickCost(draft.tour?.costs, departure), [draft.tour, departure])
  const { lines, total, rooming } = useMemo(
    () => quote(draft.passengers, cost, departure),
    [draft.passengers, cost, departure]
  )

  if (!hasDraft) return null

  return (
    <div className="container page">
      <Breadcrumb
        items={[
          { label: 'Home', to: '/' },
          { label: draft.tour.tourName, to: `/tours/${draft.tour.tourId}` },
          { label: 'Travellers', to: '/booking/details' },
          { label: 'Review' }
        ]}
      />
      <Stepper current={2} />
      <h1 className="mb8">Review your booking</h1>
      <p className="muted mb24">Check everything below before you pay.</p>

      <div className="grid grid-side">
        <div>
          <div className="card card-pad mb24">
            <h3 className="mb16">Tour</h3>
            <div className="between mb8">
              <span className="muted small">Tour</span>
              <span className="strong">{draft.tour.tourName}</span>
            </div>
            <div className="between mb8">
              <span className="muted small">Category</span>
              <span>{draft.tour.categoryName || '—'}</span>
            </div>
            <div className="between mb8">
              <span className="muted small">Destination</span>
              <span>{draft.tour.destination}</span>
            </div>
            <div className="between mb8">
              <span className="muted small">Departure</span>
              <span className="strong">{prettyDate(departure)}</span>
            </div>
            <div className="between">
              <span className="muted small">Duration</span>
              <span>{draft.tour.durationLabel}</span>
            </div>
          </div>

          <div className="card card-pad mb24">
            <h3 className="mb16">Your details</h3>
            <div className="between mb8"><span className="muted small">Name</span><span>{draft.customer.fullName}</span></div>
            <div className="between mb8"><span className="muted small">Email</span><span>{draft.customer.email}</span></div>
            <div className="between mb8"><span className="muted small">Phone</span><span>{draft.customer.phone}</span></div>
            <div className="between"><span className="muted small">Address</span>
              <span>{[draft.customer.address, draft.customer.city].filter(Boolean).join(', ') || '—'}</span></div>
          </div>

          <div className="card card-pad mb24">
            <h3 className="mb16">Rooming</h3>
            <div className="between mb8">
              <span className="muted small">Travellers</span>
              <span className="strong">{draft.passengers.length}</span>
            </div>
            <div className="between mb8">
              <span className="muted small">Rooms required</span>
              <span className="strong">{rooming.rooms}</span>
            </div>
            <div className="between mb8">
              <span className="muted small">Extra beds</span>
              <span className="strong">{rooming.extraBeds}</span>
            </div>
            <div className="between">
              <span className="muted small">Twin sharing / single</span>
              <span>{rooming.twinSharing} / {rooming.single}</span>
            </div>
            {draft.thirdPersonChoice && (
              <div className="alert alert-info small mt16" style={{ marginBottom: 0 }}>
                {draft.thirdPersonChoice === 'EXTRA_BED_SAME_ROOM'
                  ? 'Third traveller takes an extra bed in the same room.'
                  : 'Third traveller has a different room on single occupancy.'}
              </div>
            )}
          </div>

          <div className="card card-pad">
            <h3 className="mb16">Passengers and fares</h3>
            <table className="table">
              <thead>
                <tr><th>Name</th><th>Date of birth</th><th>Age</th><th>Fare band</th><th className="right">Amount</th></tr>
              </thead>
              <tbody>
                {lines.map((l, i) => (
                  <tr key={i}>
                    <td className="strong">{l.name}</td>
                    <td className="small muted">{prettyDate(draft.passengers[i].birthDate)}</td>
                    <td>{l.age}</td>
                    <td><span className="badge badge-slate">{bandLabel(l.band)}</span></td>
                    <td className="right strong">{inr(l.amount)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="hint mt8">
              Ages are calculated on the departure date, not today, so fares stay correct for future travel.
            </div>
          </div>
        </div>

        <aside className="card card-pad sticky-side">
          <h3 className="mb16">Invoice</h3>
          {lines.map((l, i) => (
            <div key={i} className="inv-row">
              <span className="small muted">{l.name}<br /><span className="tiny">{bandLabel(l.band)}</span></span>
              <span>{inr(l.amount)}</span>
            </div>
          ))}
          <div className="inv-row"><span className="muted small">Rooms / extra beds</span>
            <span>{rooming.rooms} / {rooming.extraBeds}</span></div>
          <div className="inv-row"><span className="muted small">Service fee</span>
            <span className="badge badge-green">Free</span></div>
          <div className="inv-total"><span>Total payable</span><span>{inr(total)}</span></div>

          <button className="btn btn-block btn-lg mt24" onClick={() => navigate('/booking/payment')}>
            Confirm and pay
          </button>
          <Link to="/booking/details" className="btn btn-ghost btn-block mt8">Back to details</Link>
        </aside>
      </div>
    </div>
  )
}
