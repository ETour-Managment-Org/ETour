import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useBooking } from '../../context/BookingContext.jsx'
import { bookingApi } from '../../api/bookingApi.js'
import Stepper from '../../components/Stepper.jsx'
import Breadcrumb from '../../components/Breadcrumb.jsx'
import ErrorBox from '../../components/ErrorBox.jsx'
import { inr, prettyDate } from '../../utils/format.js'
import { pickCost, quote } from '../../utils/pricing.js'

const METHODS = [
  { id: 'UPI', label: 'UPI', hint: 'Google Pay, PhonePe, Paytm' },
  { id: 'CARD', label: 'Card', hint: 'Credit or debit card' },
  { id: 'NET_BANKING', label: 'Net banking', hint: 'All major banks' }
]

export default function PaymentPage() {
  const { draft, setDraft, hasDraft } = useBooking()
  const navigate = useNavigate()
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (!hasDraft) navigate('/tours', { replace: true })
  }, [hasDraft, navigate])

  const departure = draft.schedule?.startDate
  const cost = useMemo(() => pickCost(draft.tour?.costs, departure), [draft.tour, departure])
  const { total } = useMemo(() => quote(draft.passengers, cost, departure), [draft.passengers, cost, departure])

  if (!hasDraft) return null

  const pay = async () => {
    setBusy(true)
    setError(null)
    try {
      const payload = {
        tourId: draft.tour.tourId,
        scheduleId: draft.scheduleId,
        paymentMethod: draft.paymentMethod,
        passengers: draft.passengers.map((p) => ({
          fullName: p.fullName,
          birthDate: p.birthDate,
          gender: p.gender || null,
          email: draft.customer.email,
          passportNumber: p.passportNumber || null,
          withBed: p.withBed !== false,
          occupancy: p.occupancy || 'TWIN_SHARING'
        }))
      }
      const booking = await bookingApi.place(payload)
      navigate(`/booking/confirmation/${booking.bookingId}`, { replace: true, state: { booking } })
    } catch (err) {
      setError(err)
      setBusy(false)
    }
  }

  return (
    <div className="container page" style={{ maxWidth: 720 }}>
      <Breadcrumb
        items={[
          { label: 'Home', to: '/' },
          { label: draft.tour.tourName, to: `/tours/${draft.tour.tourId}` },
          { label: 'Travellers', to: '/booking/details' },
          { label: 'Review', to: '/booking/review' },
          { label: 'Payment' }
        ]}
      />
      <Stepper current={3} />
      <h1 className="mb8">Payment</h1>
      <p className="muted mb24">Choose how you would like to pay.</p>

      

      <div className="card card-pad mb24">
        <div className="between mb16">
          <span className="muted">{draft.tour.tourName}</span>
          <span className="small muted">{prettyDate(departure)}</span>
        </div>
        <div className="inv-total" style={{ marginTop: 0, borderTop: 'none' }}>
          <span>Amount payable</span><span>{inr(total)}</span>
        </div>
      </div>

      <div className="card card-pad">
        <h3 className="mb16">Payment method</h3>
        {METHODS.map((m) => (
          <label key={m.id} className="tile mb8"
                 style={{
                   display: 'flex', alignItems: 'center', gap: 12, padding: 14,
                   borderColor: draft.paymentMethod === m.id ? 'var(--primary)' : undefined,
                   background: draft.paymentMethod === m.id ? 'var(--primary-soft)' : undefined
                 }}>
            <input type="radio" name="pm" checked={draft.paymentMethod === m.id}
                   onChange={() => setDraft({ paymentMethod: m.id })} />
            <span className="grow">
              <span className="strong">{m.label}</span>
              <br /><span className="small muted">{m.hint}</span>
            </span>
          </label>
        ))}

        <ErrorBox error={error} />

        <button className="btn btn-success btn-block btn-lg mt16" onClick={pay} disabled={busy}>
          {busy ? 'Processing payment…' : `Pay ${inr(total)}`}
        </button>
        <div className="hint mt8" style={{ textAlign: 'center' }}>
          Your seats are reserved the moment payment succeeds.
        </div>
      </div>
    </div>
  )
}
