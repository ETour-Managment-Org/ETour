import { useEffect, useState } from 'react'
import { useParams, useLocation, useNavigate, Link } from 'react-router-dom'
import { bookingApi } from '../../api/bookingApi.js'
import { useBooking } from '../../context/BookingContext.jsx'
import Stepper from '../../components/Stepper.jsx'
import Modal from '../../components/Modal.jsx'
import Loader, { SuccessState, ButtonSpinner } from '../../components/Loader.jsx'
import ErrorBox from '../../components/ErrorBox.jsx'
import { inr, prettyDate, bandLabel, statusClass } from '../../utils/format.js'
import { downloadReceipt } from '../../utils/receiptPdf.js'
import { useI18n } from '../../i18n/I18nContext.jsx'

export default function ConfirmationPage() {
  const { tc, locale, lang } = useI18n()
  const { bookingId } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const { clearDraft } = useBooking()

  const justBooked = !!location.state?.booking
  const [saving, setSaving] = useState(false)

  const [booking, setBooking] = useState(location.state?.booking || null)
  const [loading, setLoading] = useState(!justBooked)
  const [error, setError] = useState(null)
  const [showModal, setShowModal] = useState(justBooked)

  useEffect(() => {
    if (justBooked) clearDraft()
    if (!booking) {
      bookingApi.byId(bookingId).then(setBooking).catch(setError).finally(() => setLoading(false))
    }
  }, [bookingId])

  if (loading) return <div className="container page"><Loader full message="Fetching your receipt…" /></div>
  if (error) return <div className="container page"><ErrorBox error={error} /></div>
  if (!booking) return null

  const savePdf = async () => {
    setSaving(true)
    try {
      await downloadReceipt(booking, { bandLabel: (t) => bandLabel(t, lang) })
    } catch (e) {
      window.print()
    } finally {
      setSaving(false)
    }
  }
  return (
    <div className="container page" style={{ maxWidth: 780 }}>
      <Modal open={showModal}>
        <div className="tick">✓</div>
        <h2 className="mb8">Booking confirmed</h2>
        <p className="muted mb24">
          Booking #{booking.customerBookingNumber ?? booking.bookingId} is confirmed.
          A confirmation email is on its way.
        </p>
        <button className="btn btn-block btn-lg" onClick={() => setShowModal(false)}>
          View receipt
        </button>
        <button className="btn btn-ghost btn-block mt8" onClick={() => navigate('/dashboard')}>
          Go to dashboard
        </button>
      </Modal>
      {justBooked && <div className="no-print"><Stepper current={4} /></div>}
      {justBooked ? (
        <div className="card card-pad no-print mb24">
          <SuccessState
            title="Booking confirmed"
            message={booking.receiptMessage || 'Your receipt has been generated and emailed to you.'}
          />
        </div>
      ) : booking.bookingStatus === 'CANCELLED' ? (
        <div className="alert alert-err no-print">
          This booking was cancelled. The receipt below is kept for your records.
        </div>
      ) : booking.bookingStatus === 'COMPLETED' ? (
        <div className="alert alert-info no-print">
          This tour has been completed. The receipt below is kept for your records.
        </div>
      ) : null}
      <div className="card card-pad">
        <div className="between mb24">
          <div>
            <h1 style={{ fontSize: 24 }}>Booking receipt</h1>
            <div className="small muted">
              Booking reference #{booking.customerBookingNumber ?? booking.bookingId}
            </div>
          </div>
          <span className={statusClass(booking.bookingStatus)}>{tc(booking.bookingStatus)}</span>
        </div>
        <div className="grid grid-2 mb24">
          <div>
            <div className="tiny muted">Tour</div>
            <div className="strong">{tc(booking.tourName)}</div>
            <div className="small muted">{tc(booking.destination)}</div>
          </div>
          <div>
            <div className="tiny muted">Departure</div>
            <div className="strong">{prettyDate(booking.departureDate, locale)}</div>
          </div>
          <div>
            <div className="tiny muted">Booked by</div>
            <div className="strong">{booking.customerName}</div>
          </div>
          <div>
            <div className="tiny muted">Booking date</div>
            <div className="strong">{prettyDate(booking.bookingDate, locale)}</div>
          </div>
          <div>
            <div className="tiny muted">Travellers</div>
            <div className="strong">{booking.noOfPax}</div>
          </div>
          <div>
            <div className="tiny muted">Rooms and extra beds</div>
            <div className="strong">
              {booking.roomsRequired ?? '-'} room{booking.roomsRequired === 1 ? '' : 's'}
              {booking.extraBeds ? `, ${booking.extraBeds} extra bed${booking.extraBeds === 1 ? '' : 's'}` : ''}
            </div>
          </div>
        </div>
        <h3 className="mb8">Passengers</h3>
        <table className="table mb24">
          <thead>
            <tr><th>Name</th><th>Age at departure</th><th>Fare band</th><th className="right">Amount</th></tr>
          </thead>
          <tbody>
            {(booking.passengers || []).map((p) => (
              <tr key={p.paxId}>
                <td className="strong">{p.fullName}</td>
                <td>{p.ageAtDeparture}</td>
                <td><span className="badge badge-slate">{bandLabel(p.paxType, lang)}</span></td>
                <td className="right">{inr(p.paxAmount, locale)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="inv-row"><span className="muted">Payment method</span><span>{booking.paymentMethod}</span></div>
        <div className="inv-row"><span className="muted">Payment status</span>
          <span className="badge badge-green">{booking.paymentStatus}</span></div>
        <div className="inv-total"><span>Total paid</span><span>{inr(booking.totalAmount, locale)}</span></div>
      </div>

      <div className="row mt24 no-print">
        <button className="btn btn-lg" onClick={savePdf} disabled={saving}>
          {saving ? <ButtonSpinner label="Preparing…" /> : 'Download PDF receipt'}
        </button>
        <Link to="/dashboard" className="btn btn-ghost btn-lg">My bookings</Link>
        <Link to="/" className="btn btn-ghost btn-lg">Back to welcome</Link>
      </div>
    </div>
  )
}
