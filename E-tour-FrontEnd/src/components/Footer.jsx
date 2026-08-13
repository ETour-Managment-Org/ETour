import { Link } from 'react-router-dom'
import { useI18n } from '../i18n/I18nContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'

export default function Footer() {
  const { t } = useI18n()
  const { isLoggedIn, isAdmin } = useAuth()

  return (
    <footer className="site-footer no-print">
      <div className="container">
        <div className="grid grid-4 mb32">
          <div>
            <h4>e-Tour</h4>
            <p>{t('footer.tagline')}</p>
          </div>
          <div>
            <h4>{t('footer.explore')}</h4>
            <div className="row" style={{ flexDirection: 'column', gap: 6 }}>
              <Link to="/home">{t('footer.browseCategories')}</Link>
              <Link to="/tours">{t('nav.tours')}</Link>
              <Link to="/search">{t('footer.searchBudget')}</Link>
              <Link to="/feedback">{t('nav.feedback')}</Link>
            </div>
          </div>
          <div>
            <h4>{t('footer.account')}</h4>
            <div className="row" style={{ flexDirection: 'column', gap: 6 }}>
              {!isLoggedIn && <Link to="/login">{t('nav.login')}</Link>}
              {!isLoggedIn && <Link to="/register">{t('footer.createAccount')}</Link>}

              {isLoggedIn && !isAdmin && <Link to="/dashboard">{t('nav.bookings')}</Link>}
              {isAdmin && <Link to="/admin">{t('nav.admin')}</Link>}
            </div>
          </div>
          <div>
            <h4>{t('footer.contact')}</h4>
            <p>177, Vidyanidhi, Vile Parle<br />Mumbai 400600</p>
            <p className="mt8">+91 98200 11223</p>
          </div>
        </div>
        <div className="between" style={{ borderTop: '1px solid rgba(255,251,244,.16)', paddingTop: 18 }}>
          <span className="tiny">© {new Date().getFullYear()} IndiaTour Pvt. Ltd. {t('footer.rights')}</span>
          <span className="tiny">{t('footer.twinNote')}</span>
        </div>
      </div>
    </footer>
  )
}
