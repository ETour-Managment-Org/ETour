import { useEffect, useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { authApi } from '../api/authApi.js'
import { useBooking } from '../context/BookingContext.jsx'
import ErrorBox from '../components/ErrorBox.jsx'
import { ButtonSpinner } from '../components/Loader.jsx'
import FieldError from '../components/FieldError.jsx'
import { useForm } from '../hooks/useForm.js'
import { loginSchema } from '../validation/schemas.js'
import Breadcrumb from '../components/Breadcrumb.jsx'
import { useI18n } from '../i18n/I18nContext.jsx'

export default function LoginPage() {
  const { t } = useI18n()
  const { login } = useAuth()
  const { hasDraft } = useBooking()
  const navigate = useNavigate()
  const location = useLocation()

  const from = location.state?.from || (hasDraft ? '/booking/details' : '/dashboard')

  const f = useForm({ username: '', password: '' }, loginSchema)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)
  const [providers, setProviders] = useState({ google: false })

  useEffect(() => {
    authApi.providers().then(setProviders).catch(() => setProviders({ google: false }))
  }, [])

  const signInWithGoogle = () => {
    window.location.href = '/oauth2/authorization/google'
  }

  const submit = f.onSubmit(async (values) => {
    setBusy(true)
    setError(null)
    try {
      await login(values)
      navigate(from, { replace: true })
    } catch (err) {
      setError(err)
    } finally {
      setBusy(false)
    }
  })

  return (
    <div className="container page" style={{ maxWidth: 460 }}>
      <Breadcrumb items={[{ label: t('crumb.home'), to: '/' }, { label: t('crumb.login') }]} />
      <h1 className="mb8">{t('auth.welcomeBack')}</h1>
      <p className="muted mb24">
        {hasDraft ? t('auth.subFinishBooking') : t('auth.subManage')}
      </p>

      {hasDraft && <div className="alert alert-info">{t('auth.draftSavedLogin')}</div>}

      <form noValidate className="card card-pad" onSubmit={submit}>
        <ErrorBox error={error} />

        <div className="field">
          <label className="label">{t('auth.username')}</label>
          <input className="input" placeholder="Username" autoFocus
                 autoComplete="username" {...f.field('username')} />
          <FieldError message={f.error('username')} />
        </div>
        <div className="field">
          <label className="label">{t('auth.password')}</label>
          <input type="password" placeholder="Password" className="input"
                 autoComplete="current-password" {...f.field('password')} />
          <FieldError message={f.error('password')} />
        </div>

        <button className="btn btn-block btn-lg mt8" disabled={busy}>
          {busy ? <ButtonSpinner label={t('auth.signingIn')} /> : t('nav.login')}
        </button>

        {providers.google && (
          <>
            <div className="center mt16 mb16" style={{ gap: 12 }}>
              <span style={{ flex: 1, height: 1, background: 'var(--border)' }} />
              <span className="tiny muted">{t('auth.or')}</span>
              <span style={{ flex: 1, height: 1, background: 'var(--border)' }} />
            </div>

            <button type="button" className="btn btn-ghost btn-block btn-lg"
                    onClick={signInWithGoogle} disabled={busy}>
              <svg width="18" height="18" viewBox="0 0 48 48" aria-hidden="true">
                <path fill="#4285F4" d="M45.1 24.5c0-1.6-.1-3.2-.4-4.7H24v8.9h11.8c-.5 2.7-2 5-4.4 6.6v5.5h7.1c4.1-3.8 6.6-9.4 6.6-16.3z"/>
                <path fill="#34A853" d="M24 46c5.9 0 10.9-2 14.5-5.2l-7.1-5.5c-2 1.3-4.5 2.1-7.4 2.1-5.7 0-10.5-3.8-12.2-9H4.5v5.7C8.1 41.3 15.5 46 24 46z"/>
                <path fill="#FBBC05" d="M11.8 28.4c-.4-1.3-.7-2.7-.7-4.4s.3-3.1.7-4.4v-5.7H4.5A22 22 0 0 0 2 24c0 3.6.9 7 2.5 10.1l7.3-5.7z"/>
                <path fill="#EA4335" d="M24 10.6c3.2 0 6.1 1.1 8.4 3.3l6.3-6.3C34.9 4 29.9 2 24 2 15.5 2 8.1 6.7 4.5 13.9l7.3 5.7c1.7-5.2 6.5-9 12.2-9z"/>
              </svg>
              {t('auth.google')}
            </button>

            <div className="hint mt8" style={{ textAlign: 'center' }}>
              {t('auth.googleNote')}
            </div>
          </>
        )}

        <div className="small muted mt16" style={{ textAlign: 'center' }}>
          {t('auth.noAccount')} <Link to="/register" state={{ from }} style={{ color: 'var(--primary)', fontWeight: 600 }}>{t('auth.registerHere')}</Link>
        </div>

      </form>
    </div>
  )
}
