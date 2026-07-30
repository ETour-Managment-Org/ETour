import { createContext, useContext, useState, useCallback } from 'react'
import { tokenStore } from '../api/client.js'
import { authApi } from '../api/authApi.js'

const USER_KEY = 'etour_user'
const AuthContext = createContext(null)

function readStoredUser() {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser)

  const persist = useCallback((res) => {
    tokenStore.set(res.token)
    const u = {
      userId: res.userId,
      username: res.username,
      email: res.email,
      fullName: res.fullName,
      role: res.role
    }
    localStorage.setItem(USER_KEY, JSON.stringify(u))
    setUser(u)
    return u
  }, [])

  const login = useCallback(async (payload) => persist(await authApi.login(payload)), [persist])
  const register = useCallback(async (payload) => persist(await authApi.register(payload)), [persist])

  const logout = useCallback(() => {
    tokenStore.clear()
    localStorage.removeItem(USER_KEY)
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider
      value={{ user, isLoggedIn: !!user, isAdmin: user?.role === 'ADMIN', login, register, logout }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
