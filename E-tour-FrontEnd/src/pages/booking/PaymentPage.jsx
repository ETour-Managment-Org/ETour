import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useBooking } from '../../context/BookingContext.jsx'
import { bookingApi } from '../../api/bookingApi.js'
import { paymentApi } from '../../api/paymentApi.js'
import { openCheckout, loadRazorpay } from '../../utils/razorpay.js'
import Stepper from '../../components/Stepper.jsx'
import Breadcrumb from '../../components/Breadcrumb.jsx'
import ErrorBox from '../../components/ErrorBox.jsx'
import { ButtonSpinner } from '../../components/Loader.jsx'
import { inr, prettyDate } from '../../utils/format.js'
import { pickCost, quote } from '../../utils/pricing.js'
import { useI18n } from '../../i18n/I18nContext.jsx'

const METHODS = [
  { id: 'UPI', label: 'UPI', hint: 'Google Pay, PhonePe, Paytm', icon: '₹' },
  { id: 'CARD', label: 'Card', hint: 'Credit or debit card', icon: '▤' },
  { id: 'NET_BANKING', label: 'Net banking', hint: 'All major banks', icon: '⌂' }
]

export default function PaymentPage() {
  const { tc, locale } = useI18n()
  const { draft, setDraft, hasDraft } = useBooking()
  const navigate = useNavigate()
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)
  const [stage, setStage] = useState('')
  const [provider, setProvider] = useState(null)

  useEffect(() => {
    if (!hasDraft) navigate('/tours', { replace: true })
  }, [hasDraft, navigate])

  useEffect(() => {
    let alive = true
    paymentApi.config()
      .then((cfg) => {
        if (!alive) return
        setProvider(cfg.provider)
        if (cfg.provider === 'razorpay') loadRazorpay().catch(() => {})
      })
      .catch(() => alive && setProvider('mock'))
    return () => { alive = false }
  }, [])

  const departure = draft.schedule?.startDate
  const cost = useMemo(() => pickCost(draft.tour?.costs, departure), [draft.tour, departure])
  const { total } = useMemo(
    () => quote(draft.passengers, cost, departure),
    [draft.passengers, cost, departure]
  )

  if (!hasDraft) return null

  const basePayload = () => ({
    tourId: draft.tour.tourId,
    scheduleId: draft.scheduleId,
    paymentMethod: draft.paymentMethod,
    contactName: draft.customer.fullName || null,
    contactEmail: draft.customer.email || null,
    contactPhone: draft.customer.phone || null,
    passengers: draft.passengers.map((p) => ({
      fullName: p.fullName,
      birthDate: p.birthDate,
      gender: p.gender || null,
      email: draft.customer.email,
      passportNumber: p.passportNumber || null,
      withBed: p.withBed !== false,
      occupancy: p.occupancy || 'TWIN_SHARING'
    }))
  })

  const confirm = async (payload) => {
    const booking = await bookingApi.place(payload)
    navigate(`/booking/confirmation/${booking.bookingId}`, {
      replace: true, state: { booking }
    })
  }

  const pay = async () => {
    setBusy(true)
    setError(null)

    try {
      if (provider !== 'razorpay') {
        setStage('Processing payment…')
        await confirm(basePayload())
        return
      }

      setStage('Contacting the payment gateway…')
      const order = await paymentApi.createOrder(total, draft.tour.tourId)

      setStage('Waiting for payment…')
      const result = await openCheckout({
        order,
        customer: {
          name: draft.customer.fullName,
          email: draft.customer.email,
          phone: draft.customer.phone
        },
        tourName: tc(draft.tour.tourName)
      })

      if (!result.ok) {
        setError({ message: result.message })
        setBusy(false)
        setStage('')
        return
      }

      setStage('Confirming your booking…')
      await confirm({
        ...basePayload(),
        razorpayOrderId: result.razorpayOrderId,
        razorpayPaymentId: result.razorpayPaymentId,
        razorpaySignature: result.razorpaySignature
      })

    } catch (err) {
      setError(err)
      setBusy(false)
      setStage('')
    }
  }

  const payDisabled = busy || provider === null

  return (
    <div className="container page" style={{ maxWidth: 720 }}>
      <Breadcrumb
        items={[
          { label: 'Home', to: '/' },
          { label: tc(draft.tour.tourName), to: `/tours/${draft.tour.tourId}` },
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
          <span className="muted">{tc(draft.tour.tourName)}</span>
          <span className="small muted">{prettyDate(departure, locale)}</span>
        </div>
        <div className="inv-total" style={{ marginTop: 0, borderTop: 'none' }}>
          <span>Amount payable</span><span>{inr(total, locale)}</span>
        </div>
      </div>

      <div className="card card-pad">
        <h3 className="mb16">
          {provider === 'razorpay' ? 'Payment details' : 'Payment method'}
        </h3>

        {provider === 'razorpay' ? (
          <div className="pay-summary mb24">
            <div className="inv-row">
              <span className="muted">Tour</span>
              <span>{tc(draft.tour.tourName)}</span>
            </div>
            <div className="inv-row">
              <span className="muted">Departure</span>
              <span>{prettyDate(departure, locale)}</span>
            </div>
            <div className="inv-row">
              <span className="muted">Travellers</span>
              <span>{draft.passengers.length}</span>
            </div>
            <div className="inv-row">
              <span className="muted">Billed to</span>
              <span>{draft.customer.email || draft.customer.fullName || '-'}</span>
            </div>
            <div className="inv-total">
              <span>Amount payable</span><span>{inr(total, locale)}</span>
            </div>
          </div>
        ) : (
          <div className="choice mb24">
            {METHODS.map((m) => (
              <label key={m.id}
                     className={draft.paymentMethod === m.id ? 'choice-opt sel' : 'choice-opt'}>
                <input
                  type="radio"
                  name="paymentMethod"
                  value={m.id}
                  checked={draft.paymentMethod === m.id}
                  onChange={() => setDraft({ paymentMethod: m.id })}
                />
                <span className="grow">
                  <span className="choice-title">{m.label}</span>
                  <br />
                  <span className="choice-desc">{m.hint}</span>
                </span>
                <span className="pay-icon" aria-hidden="true">{m.icon}</span>
              </label>
            ))}
          </div>
        )}
        <ErrorBox error={error} />

        <button className="btn btn-block btn-lg" onClick={pay} disabled={payDisabled}>
          {busy
            ? <ButtonSpinner label={stage || 'Processing…'} />
            : `Pay ${inr(total, locale)}`}
        </button>
        <div className="hint mt8" style={{ textAlign: 'center' }}>
          Your seats are reserved the moment payment succeeds.
        </div>
      </div>
    </div>
  )
}
