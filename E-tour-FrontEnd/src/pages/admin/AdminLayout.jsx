import { NavLink, Outlet, Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext.jsx'

export default function AdminLayout() {
  const { user } = useAuth()

  return (
    <div className="container page">
      <div className="between wrap mb24">
        <div>
          <span className="eyebrow">Administration</span>
          <h1>Admin Panel</h1>
          <p className="muted small">Signed in as {user?.fullName || user?.username}</p>
        </div>
        <Link to="/" className="btn btn-ghost">Back to site</Link>
      </div>

      <div className="tabs mb24">
        <NavLink to="/admin" end className={({ isActive }) => (isActive ? 'tab active' : 'tab')}>
          Overview
        </NavLink>
        <NavLink to="/admin/users" className={({ isActive }) => (isActive ? 'tab active' : 'tab')}>
          Users
        </NavLink>
        <NavLink to="/admin/bookings" className={({ isActive }) => (isActive ? 'tab active' : 'tab')}>
          Bookings
        </NavLink>
        <NavLink to="/admin/tours" className={({ isActive }) => (isActive ? 'tab active' : 'tab')}>
          Tours
        </NavLink>
        <NavLink to="/admin/feedback" className={({ isActive }) => (isActive ? 'tab active' : 'tab')}>
          Feedback
        </NavLink>
      </div>

      <Outlet />
    </div>
  )
}
