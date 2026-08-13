import { Link } from 'react-router-dom'
import Breadcrumb from '../components/Breadcrumb.jsx'
import { useI18n } from '../i18n/I18nContext.jsx'

export default function NotFoundPage() {
  const { t } = useI18n()

  return (
    <div className="container page">
      <Breadcrumb items={[{ label: t('crumb.home'), to: '/' }, { label: t('crumb.notFound') }]} />
      <div className="empty">
        <h1 className="mb8">{t('common.notFound')}</h1>
        <p className="mb24">{t('notfound.body')}</p>
        <Link to="/" className="btn">{t('notfound.back')}</Link>
      </div>
    </div>
  )
}
