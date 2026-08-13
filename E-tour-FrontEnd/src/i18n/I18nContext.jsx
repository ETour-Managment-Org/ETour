import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { LANGUAGES, translations } from './translations.js'
import { CONTENT_MAPS, PLACES } from './content/index.js'

const KEY = 'etour_lang'
const CODES = LANGUAGES.map(l => l.code)

const LOCALES = { en: 'en-IN', hi: 'hi-IN', mr: 'mr-IN' }

function initialLang() {
  try {
    const saved = localStorage.getItem(KEY)
    if (CODES.includes(saved)) return saved
  } catch {}

  const browser = String(navigator.language || '').toLowerCase().split('-')[0]
  return CODES.includes(browser) ? browser : 'en'
}

const I18nContext = createContext(null)

export function I18nProvider({ children }) {
  const [lang, setLangState] = useState(initialLang)

  useEffect(() => {
    document.documentElement.setAttribute('lang', lang)
    try { localStorage.setItem(KEY, lang) } catch {}
  }, [lang])

  const setLang = code => { if (CODES.includes(code)) setLangState(code) }

  const value = useMemo(() => {
    const table = translations[lang] || translations.en
    const isEnglish = lang === 'en'

    const t = key => table[key] ?? translations.en[key] ?? key

    const tc = text => {
      if (isEnglish || text == null) return text

      const key = String(text).trim()
      if (!key) return text

      for (const map of CONTENT_MAPS) {
        const hit = map[key]
        if (hit && hit[lang]) return hit[lang]
      }

      if (key.includes(' - ')) {
        const parts = key.split(' - ').map(p => p.trim())
        let translatedAny = false

        const mapped = parts.map(p => {
          const hit = PLACES[p]
          if (hit && hit[lang]) { translatedAny = true; return hit[lang] }
          return p
        })

        if (translatedAny) return mapped.join(' - ')
      }

      return text
    }

    return {
      t, tc, lang, setLang,
      languages: LANGUAGES,
      isEnglish,

      locale: LOCALES[lang] || 'en-IN'
    }
  }, [lang])

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>
}

export function useI18n() {
  const ctx = useContext(I18nContext)
  if (!ctx) throw new Error('useI18n must be used inside I18nProvider')
  return ctx
}

export function useLocale() {
  return useI18n().locale
}
