import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useBooking } from '../context/BookingContext.jsx'
import ErrorBox from '../components/ErrorBox.jsx'
import Breadcrumb from '../components/Breadcrumb.jsx'

export default function LoginPage() {
  const { login } = useAuth()
  const { hasDraft } = useBooking()
  const navigate = useNavigate()
  const location = useLocation()

  const from = location.state?.from || (hasDraft ? '/booking/details' : '/dashboard')

  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      await login(form)
      navigate(from, { replace: true })
    } catch (err) {
      setError(err)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="container page" style={{ maxWidth: 460 }}>
      <Breadcrumb items={[{ label: 'Home', to: '/' }, { label: 'Log in' }]} />
      <h1 className="mb8">Welcome back</h1>
      <p className="muted mb24">
        {hasDraft ? 'Sign in to finish your booking.' : 'Sign in to manage your bookings.'}
      </p>

      {hasDraft && <div className="alert alert-info">Your selected tour is saved and will continue after login.</div>}

      <form className="card card-pad" onSubmit={submit}>
        <ErrorBox error={error} />

        <div className="field">
          <label className="label">Username or email</label>
          <input className="input" value={form.username} onChange={set('username')} required autoFocus />
        </div>
        <div className="field">
          <label className="label">Password</label>
          <input type="password" className="input" value={form.password} onChange={set('password')} required />
        </div>

        <button className="btn btn-block btn-lg mt8" disabled={busy}>
          {busy ? 'Signing in…' : 'Log in'}
        </button>

        <div className="small muted mt16" style={{ textAlign: 'center' }}>
          No account? <Link to="/register" state={{ from }} style={{ color: 'var(--primary)', fontWeight: 600 }}>Register here</Link>
        </div>

        
      </form>
    </div>
  )
}
