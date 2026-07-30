import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useBooking } from '../context/BookingContext.jsx'
import ErrorBox from '../components/ErrorBox.jsx'
import Breadcrumb from '../components/Breadcrumb.jsx'

const blank = {
  username: '', password: '', confirm: '', email: '',
  firstName: '', lastName: '', phoneNumber: '', address: ''
}

export default function RegisterPage() {
  const { register } = useAuth()
  const { hasDraft } = useBooking()
  const navigate = useNavigate()
  const location = useLocation()

  const from = location.state?.from || (hasDraft ? '/booking/details' : '/dashboard')

  const [form, setForm] = useState(blank)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    if (form.password !== form.confirm) {
      setError('Passwords do not match')
      return
    }
    setBusy(true)
    setError(null)
    try {
      const { confirm, ...payload } = form
      await register(payload)
      navigate(from, { replace: true })
    } catch (err) {
      setError(err)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="container page" style={{ maxWidth: 620 }}>
      <Breadcrumb items={[{ label: 'Home', to: '/' }, { label: 'Register' }]} />
      <h1 className="mb8">Create your account</h1>
      <p className="muted mb24">
        {hasDraft ? 'One quick step, then we will take you back to your booking.' : 'Register to book tours and track them.'}
      </p>

      {hasDraft && <div className="alert alert-info">Your selected tour is saved and will continue after registration.</div>}

      <form className="card card-pad" onSubmit={submit}>
        <ErrorBox error={error} />

        <div className="grid grid-2">
          <div className="field">
            <label className="label">First name</label>
            <input className="input" value={form.firstName} onChange={set('firstName')} required />
          </div>
          <div className="field">
            <label className="label">Last name</label>
            <input className="input" value={form.lastName} onChange={set('lastName')} required />
          </div>
        </div>

        <div className="field">
          <label className="label">Username</label>
          <input className="input" value={form.username} onChange={set('username')} required minLength={3} />
        </div>
        <div className="field">
          <label className="label">Email</label>
          <input type="email" className="input" value={form.email} onChange={set('email')} required />
        </div>

        <div className="grid grid-2">
          <div className="field">
            <label className="label">Password</label>
            <input type="password" className="input" value={form.password} onChange={set('password')} required minLength={8} />
            <div className="hint">At least 8 characters</div>
          </div>
          <div className="field">
            <label className="label">Confirm password</label>
            <input type="password" className="input" value={form.confirm} onChange={set('confirm')} required />
          </div>
        </div>

        <div className="field">
          <label className="label">Phone number</label>
          <input className="input" value={form.phoneNumber} onChange={set('phoneNumber')} />
        </div>
        <div className="field">
          <label className="label">Address</label>
          <input className="input" value={form.address} onChange={set('address')} />
        </div>

        <button className="btn btn-block btn-lg mt8" disabled={busy}>
          {busy ? 'Creating account…' : 'Register'}
        </button>

        <div className="small muted mt16" style={{ textAlign: 'center' }}>
          Already registered? <Link to="/login" state={{ from }} style={{ color: 'var(--primary)', fontWeight: 600 }}>Log in</Link>
        </div>
      </form>
    </div>
  )
}
