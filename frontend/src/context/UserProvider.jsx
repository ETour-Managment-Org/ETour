import { useCallback, useMemo, useState } from 'react'
import { getStorageItem, setStorageItem } from '../helpers/storage'
import { UserContext } from './userContext'

const USER_STORAGE_KEY = 'demoUser'

const defaultUser = {
  id: null,
  name: 'Guest User',
  email: 'guest@example.com',
  role: 'guest',
}

export function UserProvider({ children }) {
  const [user, setUserState] = useState(
    () => getStorageItem(USER_STORAGE_KEY, defaultUser),
  )

  const setUser = useCallback((nextUser) => {
    setUserState(nextUser)
    setStorageItem(USER_STORAGE_KEY, nextUser)
  }, [])

  const login = useCallback(
    (credentials = {}) => {
      setUser({
        id: 1,
        name: credentials.name || 'Demo User',
        email: credentials.email || 'demo@example.com',
        role: 'member',
      })
    },
    [setUser],
  )

  const logout = useCallback(() => {
    setUser(defaultUser)
  }, [setUser])

  const value = useMemo(
    () => ({
      user,
      setUser,
      isAuthenticated: Boolean(user?.id),
      login,
      logout,
    }),
    [user, setUser, login, logout],
  )

  return <UserContext.Provider value={value}>{children}</UserContext.Provider>
}
