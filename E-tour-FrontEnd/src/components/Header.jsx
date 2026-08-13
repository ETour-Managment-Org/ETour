import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useI18n } from '../i18n/I18nContext.jsx'
import LanguageSelect from './LanguageSelect.jsx'

export default function Header() {
  const { user, isLoggedIn, isAdmin, logout } = useAuth()
  const { t } = useI18n()
  const navigate = useNavigate()

  const onLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <header className="site-header no-print">
      <div className="container inner">
        <Link to="/" className="logo">
          <span className="logo-mark">eT</span>
          <span className="logo-text">
            <span className="logo-name">e-Tour</span>
            <span className="logo-sub">by IndiaTour Pvt. Ltd.</span>
          </span>
        </Link>

        <nav className="nav">
          <NavLink to="/" end>{t('nav.welcome')}</NavLink>
          <NavLink to="/home">{t('nav.browse')}</NavLink>
          <NavLink to="/tours">{t('nav.tours')}</NavLink>
          <NavLink to="/search">{t('nav.search')}</NavLink>
          {isLoggedIn && !isAdmin && <NavLink to="/dashboard">{t('nav.bookings')}</NavLink>}
          {isAdmin && <NavLink to="/admin">{t('nav.admin')}</NavLink>}
        </nav>

        <div className="center">
          <LanguageSelect />
          {isLoggedIn ? (
            <>
              <span className="small muted">{t('common.hi')}, {user.fullName || user.username}</span>
              <button className="btn btn-ghost btn-sm" onClick={onLogout}>{t('nav.logout')}</button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-ghost btn-sm">{t('nav.login')}</Link>
              <Link to="/register" className="btn btn-sm">{t('nav.register')}</Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
