import { useEffect, useMemo, useState } from 'react'
import { adminApi } from '../../api/adminApi.js'
import Loader from '../../components/Loader.jsx'
import ErrorBox from '../../components/ErrorBox.jsx'
import { inr, prettyDate, statusClass } from '../../utils/format.js'
import { useI18n } from '../../i18n/I18nContext.jsx'

const FILTERS = ['ALL', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'PENDING']

export default function AdminBookingsPage() {
  const { locale } = useI18n()
  const [bookings, setBookings] = useState([])
  const [filter, setFilter] = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    adminApi.bookings().then(setBookings).catch(setError).finally(() => setLoading(false))
  }, [])

  const shown = useMemo(() => {
    const rows = filter === 'ALL' ? bookings : bookings.filter((b) => b.bookingStatus === filter)
    return [...rows].sort((a, b) => (a.bookingId ?? 0) - (b.bookingId ?? 0))
  }, [bookings, filter])

  if (loading) return <Loader full message="Loading bookings…" />

  return (
    <>
      <ErrorBox error={error} />

      <div className="card card-pad">
        <div className="between wrap mb16">
          <h3>All bookings</h3>
          <div className="center wrap" style={{ gap: 6 }}>
            {FILTERS.map((f) => (
              <button
                key={f}
                className={filter === f ? 'btn btn-sm' : 'btn btn-ghost btn-sm'}
                onClick={() => setFilter(f)}
              >
                {f === 'ALL' ? 'All' : f.charAt(0) + f.slice(1).toLowerCase()}
                {' '}
                ({f === 'ALL' ? bookings.length : bookings.filter((b) => b.bookingStatus === f).length})
              </button>
            ))}
          </div>
        </div>

        {shown.length === 0 ? (
          <div className="empty">No bookings in this view.</div>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>#</th><th>Customer</th><th>Tour</th><th>Booked</th><th>Departs</th>
                <th className="right">Pax</th><th>Status</th><th>Payment</th>
                <th className="right">Amount</th><th className="right">Refund</th>
              </tr>
            </thead>
            <tbody>
              {shown.map((b) => (
                <tr key={b.bookingId}>
                  <td>{b.bookingId}</td>
                  <td>
                    <div className="strong small">{b.customerName}</div>
                    <div className="tiny muted">{b.customerEmail}</div>
                  </td>
                  <td className="small">{b.tourName}</td>
                  <td className="small">{prettyDate(b.bookingDate, locale)}</td>
                  <td className="small">{prettyDate(b.departureDate, locale)}</td>
                  <td className="right">{b.noOfPax}</td>
                  <td><span className={statusClass(b.bookingStatus)}>{b.bookingStatus}</span></td>
                  <td className="small">{b.paymentMethod || '—'}</td>
                  <td className="right strong">{inr(b.totalAmount, locale)}</td>
                  <td className="right small">{b.refundAmount ? inr(b.refundAmount, locale) : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  )
}
