import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function ProtectedRoute({ children, role }) {
  const { isLoggedIn, isAdmin, user } = useAuth()
  const location = useLocation()

  if (!isLoggedIn) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  if (role === 'ADMIN' && !isAdmin) {
    return <Navigate to="/forbidden" replace />
  }

  if (role === 'CUSTOMER' && isAdmin) {
    return <Navigate to="/admin" replace />
  }

  return children
}
