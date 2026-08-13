import { useI18n } from '../i18n/I18nContext.jsx'

export default function LanguageSelect() {
  const { lang, setLang, languages, t } = useI18n()

  return (
    <select
      className="lang-select"
      value={lang}
      onChange={e => setLang(e.target.value)}
      title={t('lang.label')}
      aria-label={t('lang.label')}
    >
      {languages.map(l => (
        <option key={l.code} value={l.code}>{l.native}</option>
      ))}
    </select>
  )
}
