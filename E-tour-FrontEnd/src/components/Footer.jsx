import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer className="site-footer no-print">
      <div className="container">
        <div className="grid grid-4 mb32">
          <div>
            <h4>e-Tour</h4>
            <p>Curated international, domestic and event-based group tours since 1998.</p>
          </div>
          <div>
            <h4>Explore</h4>
            <div className="row" style={{ flexDirection: 'column', gap: 6 }}>
              <Link to="/home">Browse categories</Link>
              <Link to="/tours">All tours</Link>
              <Link to="/search">Search by budget</Link>
            </div>
          </div>
          <div>
            <h4>Account</h4>
            <div className="row" style={{ flexDirection: 'column', gap: 6 }}>
              <Link to="/login">Log in</Link>
              <Link to="/register">Create an account</Link>
              <Link to="/dashboard">My bookings</Link>
            </div>
          </div>
          <div>
            <h4>Contact</h4>
            <p>111, L J Road, Dadar<br />Mumbai 400028</p>
            <p className="mt8">+91 98200 11223</p>
          </div>
        </div>
        <div className="between" style={{ borderTop: '1px solid rgba(255,251,244,.16)', paddingTop: 18 }}>
          <span className="tiny">© {new Date().getFullYear()} IndiaTour Pvt. Ltd. All rights reserved.</span>
          <span className="tiny">Prices quoted per person on twin sharing basis.</span>
        </div>
      </div>
    </footer>
  )
}
