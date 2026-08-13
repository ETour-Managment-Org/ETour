import { useEffect, useState } from 'react'
import { authApi } from '../api/authApi.js'
import GenderField from './GenderField.jsx'
import Loader from './Loader.jsx'
import ErrorBox from './ErrorBox.jsx'

const blank = {
  firstName: '', lastName: '', email: '',
  phoneNumber: '', address: '', city: '', gender: ''
}

const onlyDigits = (v) => String(v || '').replace(/\D/g, '').slice(0, 10)

export default function EditProfilePanel() {
  const [form, setForm] = useState(blank)
  const [original, setOriginal] = useState(blank)
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)
  const [notice, setNotice] = useState(null)

  const [pwOpen, setPwOpen] = useState(false)
  const [pw, setPw] = useState({ currentPassword: '', newPassword: '', confirm: '' })
  const [pwBusy, setPwBusy] = useState(false)
  const [pwError, setPwError] = useState(null)
  const [pwNotice, setPwNotice] = useState(null)

  const setPwField = (k) => (e) => setPw({ ...pw, [k]: e.target.value })

  const submitPassword = async (e) => {
    e.preventDefault()
    setPwError(null)
    setPwNotice(null)

    if (pw.newPassword !== pw.confirm) {
      setPwError('The two new passwords do not match')
      return
    }
    if (pw.newPassword.length < 8) {
      setPwError('New password must be at least 8 characters')
      return
    }

    setPwBusy(true)
    try {
      await authApi.changePassword({
        currentPassword: pw.currentPassword,
        newPassword: pw.newPassword
      })
      setPwNotice('Your password has been changed.')
      setPw({ currentPassword: '', newPassword: '', confirm: '' })
      setPwOpen(false)
    } catch (err) {
      setPwError(err)
    } finally {
      setPwBusy(false)
    }
  }

  const load = () => {
    setLoading(true)
    authApi.me()
      .then((p) => {
        const next = {
          firstName: p.firstName || '',
          lastName: p.lastName || '',
          email: p.email || '',
          phoneNumber: p.phoneNumber || '',
          address: p.address || '',
          city: p.city || '',
          gender: p.gender || ''
        }
        setForm(next)
        setOriginal(next)
      })
      .catch(setError)
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })
  const setPhone = (e) => setForm({ ...form, phoneNumber: onlyDigits(e.target.value) })

  const cancel = () => {
    setForm(original)
    setEditing(false)
    setError(null)
  }

  const save = async (e) => {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      const updated = await authApi.updateMe(form)
      const next = {
        firstName: updated.firstName || '',
        lastName: updated.lastName || '',
        email: updated.email || '',
        phoneNumber: updated.phoneNumber || '',
        address: updated.address || '',
        city: updated.city || '',
        gender: updated.gender || ''
      }
      setForm(next)
      setOriginal(next)
      setEditing(false)
      setNotice('Your profile has been updated.')
    } catch (err) {
      setError(err)
    } finally {
      setBusy(false)
    }
  }

  if (loading) return <div className="card card-pad"><Loader /></div>

  const row = (label, value) => (
    <div className="inv-row">
      <span className="muted small">{label}</span>
      <span className="strong">{value || '—'}</span>
    </div>
  )

  const genderLabel = { MALE: 'Male', FEMALE: 'Female', OTHER: 'Other' }[form.gender] || '—'

  return (
    <div className="card card-pad mb32">
      <div className="between mb16">
        <h2>My profile</h2>
        {!editing && (
          <button className="btn btn-ghost" onClick={() => { setEditing(true); setNotice(null) }}>
            Edit profile
          </button>
        )}
      </div>

      {notice && <div className="alert alert-ok">{notice}</div>}
      <ErrorBox error={error} />

      {!editing ? (
        <>
          {row('Name', `${form.firstName} ${form.lastName}`.trim())}
          {row('Email', form.email)}
          {row('Phone', form.phoneNumber)}
          {row('Gender', genderLabel)}
          {row('City', form.city)}
          {row('Address', form.address)}
        </>
      ) : (
        <form onSubmit={save}>
          <div className="grid grid-2">
            <div className="field">
              <label className="label">First name</label>
              <input className="input" value={form.firstName} onChange={set('firstName')} required />
            </div>
            <div className="field">
              <label className="label">Last name</label>
              <input className="input" value={form.lastName} onChange={set('lastName')} />
            </div>
            <div className="field">
              <label className="label">Email</label>
              <input type="email" className="input" value={form.email} onChange={set('email')} required />
            </div>
            <div className="field">
              <label className="label">Phone</label>
              <input className="input" value={form.phoneNumber} onChange={setPhone}
                     inputMode="numeric" maxLength={10} pattern="[6-9][0-9]{9}"
                     title="10 digits, starting 6 to 9" placeholder="10 digits" />
            </div>
            <div className="field">
              <label className="label">City</label>
              <input className="input" value={form.city} onChange={set('city')} />
            </div>
          </div>

          <div className="field">
            <label className="label">Address</label>
            <input className="input" value={form.address} onChange={set('address')} />
          </div>

          <GenderField value={form.gender} onChange={(v) => setForm({ ...form, gender: v })} />

          <div className="hint mb16">
            Your username cannot be changed.
          </div>

          <div className="row">
            <button className="btn grow" disabled={busy}>
              {busy ? 'Saving…' : 'Save changes'}
            </button>
            <button type="button" className="btn btn-ghost" onClick={cancel} disabled={busy}>
              Cancel
            </button>
          </div>
        </form>
      )}

      <div className="card card-pad mt16">
        <div className="between wrap">
          <div>
            <h4>Password</h4>
            <div className="small muted">Change the password you sign in with.</div>
          </div>
          {!pwOpen && (
            <button className="btn btn-ghost btn-sm" onClick={() => { setPwOpen(true); setPwNotice(null) }}>
              Change password
            </button>
          )}
        </div>

        {pwNotice && <div className="alert alert-ok mt16">{pwNotice}</div>}

        {pwOpen && (
          <form className="mt16" onSubmit={submitPassword}>
            <ErrorBox error={pwError} />

            <div className="field">
              <label className="label">Current password</label>
              <input type="password" className="input" value={pw.currentPassword}
                     onChange={setPwField('currentPassword')} required autoFocus />
            </div>

            <div className="grid grid-2">
              <div className="field">
                <label className="label">New password</label>
                <input type="password" className="input" value={pw.newPassword}
                       onChange={setPwField('newPassword')} required minLength={8} />
                <div className="hint">At least 8 characters</div>
              </div>
              <div className="field">
                <label className="label">Confirm new password</label>
                <input type="password" className="input" value={pw.confirm}
                       onChange={setPwField('confirm')} required />
              </div>
            </div>

            <div className="row">
              <button className="btn grow" disabled={pwBusy}>
                {pwBusy ? 'Changing…' : 'Change password'}
              </button>
              <button type="button" className="btn btn-ghost" disabled={pwBusy}
                      onClick={() => { setPwOpen(false); setPwError(null) }}>
                Cancel
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
