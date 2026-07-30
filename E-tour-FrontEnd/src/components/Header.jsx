import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function Header() {
  const { user, isLoggedIn, logout } = useAuth()
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
          <NavLink to="/" end>Welcome</NavLink>
          <NavLink to="/home">Browse</NavLink>
          <NavLink to="/tours">All tours</NavLink>
          <NavLink to="/search">Search</NavLink>
          {isLoggedIn && <NavLink to="/dashboard">My bookings</NavLink>}
        </nav>

        <div className="center">
          {isLoggedIn ? (
            <>
              <span className="small muted">Hi, {user.fullName || user.username}</span>
              <button className="btn btn-ghost btn-sm" onClick={onLogout}>Log out</button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-ghost btn-sm">Log in</Link>
              <Link to="/register" className="btn btn-sm">Register</Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
