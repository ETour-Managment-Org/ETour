import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import Loader from '../components/Loader.jsx'

const MESSAGES = {
  no_email: 'Google did not share an email address, so we could not sign you in.',
  account_disabled: 'That account has been disabled. Please contact support.'
}

export default function OAuthCallbackPage() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const { adoptToken } = useAuth()
  const [error, setError] = useState(null)

  useEffect(() => {
    const token = params.get('token')
    const failure = params.get('error')

    if (failure) {
      setError(MESSAGES[failure] || 'Sign-in with Google did not complete.')
      return
    }
    if (!token) {
      setError('No sign-in token was returned.')
      return
    }

    adoptToken(token)
      .then(() => navigate('/dashboard', { replace: true }))
      .catch(() => setError('We could not complete your sign-in. Please try again.'))
  }, [])

  if (error) {
    return (
      <div className="container page">
        <div className="empty">
          <h1 className="mb8">Sign-in failed</h1>
          <p className="mb24 muted">{error}</p>
          <button className="btn btn-lg" onClick={() => navigate('/login', { replace: true })}>
            Back to login
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="container page">
      <div className="empty">
        <Loader full message="Signing you in with Google…" />
        <p className="muted">Completing your sign-in…</p>
      </div>
    </div>
  )
}
