import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { feedbackApi } from '../api/feedbackApi.js'
import { useAuth } from '../context/AuthContext.jsx'
import Breadcrumb from '../components/Breadcrumb.jsx'
import ErrorBox from '../components/ErrorBox.jsx'
import StarRating from '../components/StarRating.jsx'
import FeedbackWall from '../components/FeedbackWall.jsx'

const CATEGORIES = [
  { value: 'SUGGESTION', label: 'A suggestion', hint: 'Something you would like us to add or change' },
  { value: 'PROBLEM', label: 'A problem', hint: 'Something did not work as expected' },
  { value: 'COMPLIMENT', label: 'A compliment', hint: 'Something you liked' },
  { value: 'OTHER', label: 'Something else', hint: '' }
]

const blank = { name: '', email: '', category: '', rating: 0, message: '' }

export default function FeedbackPage() {
  const { isLoggedIn, user } = useAuth()
  const location = useLocation()

  const [form, setForm] = useState(blank)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)
  const [done, setDone] = useState(false)

  useEffect(() => {
    if (isLoggedIn && user) {
      setForm((f) => ({ ...f, name: user.fullName || user.username || '', email: user.email || '' }))
    }
  }, [isLoggedIn, user])

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      await feedbackApi.submit({
        ...form,
        rating: form.rating || null,
        pageUrl: location.state?.from || document.referrer || '/'
      })
      setDone(true)
    } catch (err) {
      setError(err)
    } finally {
      setBusy(false)
    }
  }

  if (done) {
    return (
      <div className="container page" style={{ maxWidth: 620 }}>
        <Breadcrumb items={[{ label: 'Home', to: '/' }, { label: 'Feedback' }]} />
        <div className="card card-pad" style={{ textAlign: 'center' }}>
          <div className="tick">✓</div>
          <h2 className="mb8">Thank you</h2>
          <p className="muted mb24">
            Your feedback has been sent to the team. We read every one.
          </p>
          <Link to="/" className="btn btn-lg">Back to the site</Link>
        </div>
      </div>
    )
  }

  return (
    <div className="container page" style={{ maxWidth: 680 }}>
      <Breadcrumb items={[{ label: 'Home', to: '/' }, { label: 'Feedback' }]} />

      <div className="section-head">
        <span className="eyebrow">We are listening</span>
        <h1>Tell us about the website</h1>
        <p className="muted">
          This is for feedback about the site itself. To review a tour you travelled on,
          open <Link to="/dashboard" style={{ color: 'var(--primary)' }}>My bookings</Link>.
        </p>
      </div>

      <form className="card card-pad" onSubmit={submit}>
        <ErrorBox error={error} />

        <div className="field">
          <label className="label">What is this about?</label>
          <div className="choice">
            {CATEGORIES.map((c) => (
              <label key={c.value}
                     className={form.category === c.value ? 'choice-opt sel' : 'choice-opt'}>
                <input type="radio" name="category" value={c.value}
                       checked={form.category === c.value}
                       onChange={set('category')} required />
                <span className="grow">
                  <span className="choice-title">{c.label}</span>
                  {c.hint && <><br /><span className="choice-desc">{c.hint}</span></>}
                </span>
              </label>
            ))}
          </div>
        </div>

        <div className="field">
          <label className="label">How would you rate the site overall? (optional)</label>
          <div className="center" style={{ gap: 10 }}>
            {[1, 2, 3, 4, 5].map((n) => (
              <button key={n} type="button"
                      className={form.rating === n ? 'btn btn-sm' : 'btn btn-ghost btn-sm'}
                      onClick={() => setForm({ ...form, rating: n })}>
                {n}
              </button>
            ))}
            {form.rating > 0 && (
              <>
                <StarRating value={form.rating} />
                <button type="button" className="btn btn-ghost btn-sm"
                        onClick={() => setForm({ ...form, rating: 0 })}>
                  Clear
                </button>
              </>
            )}
          </div>
        </div>

        <div className="field">
          <label className="label">Your feedback</label>
          <textarea className="textarea" rows={5} value={form.message}
                    onChange={set('message')} required maxLength={2000}
                    placeholder="What worked, what did not, what you would change..." />
          <div className="hint">{form.message.length} / 2000</div>
        </div>

        {!isLoggedIn && (
          <div className="grid grid-2">
            <div className="field">
              <label className="label">Your name (optional)</label>
              <input className="input" value={form.name} onChange={set('name')} />
            </div>
            <div className="field">
              <label className="label">Your email (optional)</label>
              <input type="email" className="input" value={form.email} onChange={set('email')} />
              <div className="hint">Only if you would like a reply.</div>
            </div>
          </div>
        )}

        {isLoggedIn && (
          <div className="hint mb16">
            Sending as <strong>{form.name}</strong> ({form.email}).
          </div>
        )}

        <button className="btn btn-block btn-lg" disabled={busy}>
          {busy ? 'Sending…' : 'Send feedback'}
        </button>

        <div className="hint mt8" style={{ textAlign: 'center' }}>
          You do not need an account to send feedback.
        </div>
      </form>

      <FeedbackWall limit={9} title="Published feedback" />
    </div>
  )
}
