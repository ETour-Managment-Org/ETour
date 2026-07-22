import { useMemo, useState } from 'react'
import { LayoutContext } from './layoutContext'

const defaultLayoutState = {
  pageTitle: 'Dashboard',
  breadcrumbs: [],
  showFooter: true,
}

export function LayoutProvider({ children }) {
  const [collapsed, setCollapsed] = useState(false)
  const [pageTitle, setPageTitle] = useState(defaultLayoutState.pageTitle)
  const [breadcrumbs, setBreadcrumbs] = useState(defaultLayoutState.breadcrumbs)
  const [showFooter, setShowFooter] = useState(defaultLayoutState.showFooter)

  const resetLayout = () => {
    setPageTitle(defaultLayoutState.pageTitle)
    setBreadcrumbs(defaultLayoutState.breadcrumbs)
    setShowFooter(defaultLayoutState.showFooter)
  }

  const value = useMemo(
    () => ({
      collapsed,
      setCollapsed,
      pageTitle,
      setPageTitle,
      breadcrumbs,
      setBreadcrumbs,
      showFooter,
      setShowFooter,
      resetLayout,
    }),
    [collapsed, pageTitle, breadcrumbs, showFooter],
  )

  return (
    <LayoutContext.Provider value={value}>{children}</LayoutContext.Provider>
  )
}
