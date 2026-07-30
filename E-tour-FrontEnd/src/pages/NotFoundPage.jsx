import { Link } from 'react-router-dom'
import Breadcrumb from '../components/Breadcrumb.jsx'

export default function NotFoundPage() {
  return (
    <div className="container page">
      <Breadcrumb items={[{ label: 'Home', to: '/' }, { label: 'Not found' }]} />
      <div className="empty">
        <h1 className="mb8">Page not found</h1>
        <p className="mb24">That page does not exist.</p>
        <Link to="/" className="btn">Back to welcome</Link>
      </div>
    </div>
  )
}
