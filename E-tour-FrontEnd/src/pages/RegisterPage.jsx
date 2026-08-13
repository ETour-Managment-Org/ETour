import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useBooking } from '../context/BookingContext.jsx'
import ErrorBox from '../components/ErrorBox.jsx'
import { ButtonSpinner } from '../components/Loader.jsx'
import FieldError from '../components/FieldError.jsx'
import Breadcrumb from '../components/Breadcrumb.jsx'
import GenderField from '../components/GenderField.jsx'
import { useForm } from '../hooks/useForm.js'
import { registerSchema } from '../validation/schemas.js'
import { useI18n } from '../i18n/I18nContext.jsx'

const blank = {
  username: '', password: '', confirm: '', email: '',
  firstName: '', lastName: '', phoneNumber: '', address: '',
  city: '', gender: ''
}

const onlyDigits = (v) => String(v || '').replace(/\D/g, '').slice(0, 10)

export default function RegisterPage() {
  const { t } = useI18n()
  const { register } = useAuth()
  const { hasDraft } = useBooking()
  const navigate = useNavigate()
  const location = useLocation()

  const from = location.state?.from || (hasDraft ? '/booking/details' : '/dashboard')

  const f = useForm(blank, registerSchema)

  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  const submit = f.onSubmit(async (values) => {
    setBusy(true)
    setError(null)
    try {
      const { confirm, ...payload } = values
      await register(payload)
      navigate(from, { replace: true })
    } catch (err) {
      setError(err)
    } finally {
      setBusy(false)
    }
  })

  return (
    <div className="container page" style={{ maxWidth: 620 }}>
      <Breadcrumb items={[{ label: t('crumb.home'), to: '/' }, { label: t('crumb.register') }]} />
      <h1 className="mb8">{t('auth.createAccount')}</h1>
      <p className="muted mb24">
        {hasDraft ? t('auth.regSubDraft') : t('auth.regSubPlain')}
      </p>

      {hasDraft && <div className="alert alert-info">{t('auth.draftSavedRegister')}</div>}

      <form className="card card-pad" onSubmit={submit} noValidate>
        <ErrorBox error={error} />
        <div className="grid grid-2">
          <div className="field">
            <label className="label">
              {t('auth.firstName')}<span className="req">*</span>
            </label>
            <input className="input" {...f.field('firstName')} />
            <FieldError message={f.error('firstName')} />
          </div>
          <div className="field">
            <label className="label">{t('auth.lastName')}</label>
            <input className="input" {...f.field('lastName')} />
            <FieldError message={f.error('lastName')} />
          </div>
        </div>
        <div className="field">
          <label className="label">
            {t('auth.username')}<span className="req">*</span>
          </label>
          <input className="input" autoComplete="username" {...f.field('username')} />
          <FieldError message={f.error('username')} />
        </div>
        <div className="field">
          <label className="label">
            {t('auth.email')}<span className="req">*</span>
          </label>
          <input type="email" className="input" autoComplete="email" {...f.field('email')} />
          <FieldError message={f.error('email')} />
        </div>
        <div className="grid grid-2">
          <div className="field">
            <label className="label">
              {t('auth.password')}<span className="req">*</span>
            </label>
            <input type="password" className="input" autoComplete="new-password"
                   {...f.field('password')} />
            <FieldError message={f.error('password')} />
          </div>
          <div className="field">
            <label className="label">
              {t('auth.confirmPassword')}<span className="req">*</span>
            </label>
            <input type="password" className="input" autoComplete="new-password"
                   {...f.field('confirm')} />
            <FieldError message={f.error('confirm')} />
          </div>
        </div>
        <div className="field">
          <label className="label">
            {t('auth.phone')}<span className="req">*</span>
          </label>
          <input className="input" inputMode="numeric" maxLength={10} placeholder="10 digits"
                 {...f.field('phoneNumber')}
                 onChange={(e) => f.setValue('phoneNumber', onlyDigits(e.target.value))} />
          <FieldError message={f.error('phoneNumber')} />
        </div>
        <div className="field">
          <label className="label">{t('auth.address')}</label>
          <input className="input" {...f.field('address')} />
        </div>
        <div className="field">
          <label className="label">{t('auth.city')}</label>
          <input className="input" {...f.field('city')} />
        </div>
        <GenderField value={f.values.gender} onChange={(v) => f.setValue('gender', v)} />
        <FieldError message={f.error('gender')} />

        <button className="btn btn-block btn-lg mt8" disabled={busy}>
          {busy ? <ButtonSpinner label={t('auth.creating')} /> : t('nav.register')}
        </button>
        <div className="small muted mt16" style={{ textAlign: 'center' }}>
          {t('auth.haveAccount')}{' '}
          <Link to="/login" state={{ from }} style={{ color: 'var(--primary)', fontWeight: 600 }}>
            {t('nav.login')}
          </Link>
        </div>
      </form>
    </div>
  )
}
