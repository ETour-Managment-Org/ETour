import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { adminApi } from '../../api/adminApi.js'
import Loader from '../../components/Loader.jsx'
import ErrorBox from '../../components/ErrorBox.jsx'
import { inr } from '../../utils/format.js'
import { useI18n } from '../../i18n/I18nContext.jsx'

export default function AdminOverviewPage() {
  const { locale } = useI18n()
  const [users, setUsers] = useState([])
  const [bookings, setBookings] = useState([])
  const [tours, setTours] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    Promise.all([adminApi.users(), adminApi.bookings(), adminApi.tours()])
      .then(([u, b, t]) => {
        setUsers(u || [])
        setBookings(b || [])
        setTours(t || [])
      })
      .catch(setError)
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <Loader />
  if (error) return <ErrorBox error={error} />

  const customers = users.filter((u) => u.role === 'CUSTOMER')
  const live = bookings.filter((b) => b.bookingStatus === 'CONFIRMED')
  const cancelled = bookings.filter((b) => b.bookingStatus === 'CANCELLED')
  const revenue = bookings
    .filter((b) => b.bookingStatus !== 'CANCELLED')
    .reduce((s, b) => s + Number(b.totalAmount || 0), 0)

  const cards = [
    { label: 'Customers', value: customers.length, to: '/admin/users' },
    { label: 'Tours live', value: tours.length, to: '/admin/tours' },
    { label: 'Bookings total', value: bookings.length, to: '/admin/bookings' },
    { label: 'Confirmed', value: live.length, to: '/admin/bookings' },
    { label: 'Cancelled', value: cancelled.length, to: '/admin/bookings' },
    { label: 'Revenue', value: inr(revenue, locale), to: '/admin/bookings', accent: true }
  ]

  return (
    <>
      <div className="grid grid-3 mb32">
        {cards.map((c) => (
          <Link key={c.label} to={c.to} className="card card-pad">
            <div className="tiny muted">{c.label}</div>
            <div className={c.accent ? 'price' : 'serif'}
                 style={{ fontSize: 30, fontWeight: 700 }}>
              {c.value}
            </div>
          </Link>
        ))}
      </div>

      <div className="card card-pad">
        <h3 className="mb16">Latest bookings</h3>
        {bookings.length === 0 ? (
          <div className="empty">No bookings yet.</div>
        ) : (
          <table className="table">
            <thead>
              <tr><th>#</th><th>Customer</th><th>Tour</th><th>Status</th><th className="right">Amount</th></tr>
            </thead>
            <tbody>
              {bookings.slice(0, 8).map((b) => (
                <tr key={b.bookingId}>
                  <td>{b.bookingId}</td>
                  <td>{b.customerName}</td>
                  <td>{b.tourName}</td>
                  <td><span className="badge badge-slate">{b.bookingStatus}</span></td>
                  <td className="right">{inr(b.totalAmount, locale)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  )
}
