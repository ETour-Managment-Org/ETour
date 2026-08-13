import { useEffect, useState } from 'react'
import { adminApi } from '../../api/adminApi.js'
import Loader from '../../components/Loader.jsx'
import ErrorBox from '../../components/ErrorBox.jsx'
import Modal from '../../components/Modal.jsx'
import { inr, prettyDate, statusClass } from '../../utils/format.js'
import { useI18n } from '../../i18n/I18nContext.jsx'

export default function AdminUsersPage() {
  const { locale } = useI18n()
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [notice, setNotice] = useState(null)

  const [viewing, setViewing] = useState(null)
  const [bookings, setBookings] = useState([])
  const [loadingBookings, setLoadingBookings] = useState(false)

  const [pwUser, setPwUser] = useState(null)
  const [pwValue, setPwValue] = useState('')
  const [pwBusy, setPwBusy] = useState(false)

  const submitReset = async () => {
    if (pwValue.length < 8) {
      setError('New password must be at least 8 characters')
      return
    }
    setPwBusy(true)
    setError(null)
    try {
      await adminApi.resetPassword(pwUser.userId, pwValue)
      setNotice(`Password reset for ${pwUser.username}.`)
      setPwUser(null)
      setPwValue('')
    } catch (err) {
      setError(err)
    } finally {
      setPwBusy(false)
    }
  }

  const load = () => {
    setLoading(true)
    adminApi.users().then(setUsers).catch(setError).finally(() => setLoading(false))
  }
  useEffect(load, [])

  const openBookings = (u) => {
    setViewing(u)
    setLoadingBookings(true)
    adminApi.userBookings(u.userId)
      .then(setBookings)
      .catch(setError)
      .finally(() => setLoadingBookings(false))
  }

  const toggleActive = async (u) => {
    setError(null)
    try {
      await adminApi.setUserActive(u.userId, !u.active)
      setNotice(`${u.username} is now ${!u.active ? 'active' : 'deactivated'}.`)
      load()
    } catch (err) {
      setError(err)
    }
  }

  if (loading) return <Loader full message="Loading customers…" />

  return (
    <>
      {notice && <div className="alert alert-ok">{notice}</div>}
      <ErrorBox error={error} />

      <div className="card card-pad">
        <div className="between mb16">
          <h3>Users</h3>
          <span className="small muted">{users.length} accounts</span>
        </div>

        <table className="table">
          <thead>
            <tr>
              <th>User</th>
              <th>Contact</th>
              <th>Role</th>
              <th className="right">Bookings</th>
              <th className="right">Spent</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.userId}>
                <td>
                  <div className="strong">{u.fullName}</div>
                  <div className="tiny muted">@{u.username}</div>
                </td>
                <td className="small">
                  {u.email}
                  <br />
                  <span className="tiny muted">{[u.phoneNumber, u.city].filter(Boolean).join(' · ') || '—'}</span>
                </td>
                <td>
                  <span className={u.role === 'ADMIN' ? 'badge badge-primary' : 'badge badge-slate'}>
                    {u.role}
                  </span>
                </td>
                <td className="right">
                  <span className="strong">{u.totalBookings}</span>
                  {u.cancelledBookings > 0 && (
                    <span className="tiny muted"> ({u.cancelledBookings} cancelled)</span>
                  )}
                </td>
                <td className="right strong">{inr(u.totalSpent, locale)}</td>
                <td>
                  <span className={u.active ? 'badge badge-green' : 'badge badge-red'}>
                    {u.active ? 'Active' : 'Disabled'}
                  </span>
                </td>
                <td className="right">
                  <button className="btn btn-ghost btn-sm" onClick={() => openBookings(u)}>
                    Bookings
                  </button>
                  <button className="btn btn-ghost btn-sm" style={{ marginLeft: 6 }}
                          onClick={() => { setPwUser(u); setPwValue(''); setError(null) }}>
                    Reset password
                  </button>
                  {u.role !== 'ADMIN' && (
                    <button
                      className={u.active ? 'btn btn-danger btn-sm' : 'btn btn-sm'}
                      style={{ marginLeft: 6 }}
                      onClick={() => toggleActive(u)}
                    >
                      {u.active ? 'Disable' : 'Enable'}
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal open={pwUser !== null}>
        <h3 className="mb8">Reset password</h3>
        <div className="small muted mb16">
          Set a new password for <strong>{pwUser?.username}</strong>. They can change it
          themselves afterwards from their profile.
        </div>

        <div className="field">
          <label className="label">New password</label>
          <input type="text" className="input" value={pwValue}
                 onChange={(e) => setPwValue(e.target.value)}
                 minLength={8} placeholder="At least 8 characters" autoFocus />
          <div className="hint">Shown in plain text so you can pass it on, then tell them to change it.</div>
        </div>

        <div className="row mt16">
          <button className="btn grow" onClick={submitReset} disabled={pwBusy}>
            {pwBusy ? 'Resetting…' : 'Reset password'}
          </button>
          <button className="btn btn-ghost" onClick={() => setPwUser(null)} disabled={pwBusy}>
            Cancel
          </button>
        </div>
      </Modal>

      <Modal open={viewing !== null}>
        {viewing && (
          <>
            <h3 className="mb8">Bookings by {viewing.fullName}</h3>
            <div className="small muted mb16">
              {viewing.email} · {viewing.totalBookings} booking
              {viewing.totalBookings === 1 ? '' : 's'} to date
            </div>

            {loadingBookings ? (
              <Loader />
            ) : bookings.length === 0 ? (
              <div className="empty">This customer has not booked anything yet.</div>
            ) : (
              <table className="table" style={{ textAlign: 'left' }}>
                <thead>
                  <tr>
                    <th>#</th><th>Tour</th><th>Departs</th><th>Status</th><th className="right">Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {bookings.map((b, i) => (
                    <tr key={b.bookingId}>
                      <td>{i + 1}</td>
                      <td className="strong">{b.tourName}</td>
                      <td className="small">{prettyDate(b.departureDate, locale)}</td>
                      <td><span className={statusClass(b.bookingStatus)}>{b.bookingStatus}</span></td>
                      <td className="right">{inr(b.totalAmount, locale)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}

            <button className="btn btn-ghost btn-block mt16" onClick={() => setViewing(null)}>
              Close
            </button>
          </>
        )}
      </Modal>
    </>
  )
}
