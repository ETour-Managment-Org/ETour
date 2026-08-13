import { Link } from 'react-router-dom'

export default function ForbiddenPage() {
  return (
    <div className="container page">
      <div className="empty">
        <h1 className="mb24">Sorry , This page is not accessible to you!!</h1>
        <Link to="/" className="btn btn-lg">Go to home page</Link>
      </div>
    </div>
  )
}
