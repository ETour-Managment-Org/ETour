import { Routes, Route } from 'react-router-dom'
import Layout from '../components/Layout.jsx'
import ProtectedRoute from '../components/ProtectedRoute.jsx'

import WelcomePage from '../pages/WelcomePage.jsx'
import HomePage from '../pages/HomePage.jsx'
import CategoryPage from '../pages/CategoryPage.jsx'
import TourListPage from '../pages/TourListPage.jsx'
import TourDetailPage from '../pages/TourDetailPage.jsx'
import SearchPage from '../pages/SearchPage.jsx'
import LoginPage from '../pages/LoginPage.jsx'
import RegisterPage from '../pages/RegisterPage.jsx'
import BookingDetailsPage from '../pages/booking/BookingDetailsPage.jsx'
import BookingReviewPage from '../pages/booking/BookingReviewPage.jsx'
import PaymentPage from '../pages/booking/PaymentPage.jsx'
import ConfirmationPage from '../pages/booking/ConfirmationPage.jsx'
import DashboardPage from '../pages/DashboardPage.jsx'
import NotFoundPage from '../pages/NotFoundPage.jsx'

const guard = (el) => <ProtectedRoute>{el}</ProtectedRoute>

export default function AppRoutes() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<WelcomePage />} />
        <Route path="/home" element={<HomePage />} />
        <Route path="/categories/:id" element={<CategoryPage />} />
        <Route path="/tours" element={<TourListPage />} />
        <Route path="/tours/:id" element={<TourDetailPage />} />
        <Route path="/search" element={<SearchPage />} />

        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        <Route path="/booking/details" element={guard(<BookingDetailsPage />)} />
        <Route path="/booking/review" element={guard(<BookingReviewPage />)} />
        <Route path="/booking/payment" element={guard(<PaymentPage />)} />
        <Route path="/booking/confirmation/:bookingId" element={guard(<ConfirmationPage />)} />

        <Route path="/dashboard" element={guard(<DashboardPage />)} />

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Layout>
  )
}
