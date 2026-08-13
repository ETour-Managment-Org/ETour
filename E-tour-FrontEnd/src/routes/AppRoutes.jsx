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
import ForbiddenPage from '../pages/ForbiddenPage.jsx'
import OAuthCallbackPage from '../pages/OAuthCallbackPage.jsx'
import FeedbackPage from '../pages/FeedbackPage.jsx'
import NotFoundPage from '../pages/NotFoundPage.jsx'

import AdminLayout from '../pages/admin/AdminLayout.jsx'
import AdminOverviewPage from '../pages/admin/AdminOverviewPage.jsx'
import AdminUsersPage from '../pages/admin/AdminUsersPage.jsx'
import AdminBookingsPage from '../pages/admin/AdminBookingsPage.jsx'
import AdminToursPage from '../pages/admin/AdminToursPage.jsx'
import AdminTourFormPage from '../pages/admin/AdminTourFormPage.jsx'
import AdminTourImportPage from '../pages/admin/AdminTourImportPage.jsx'
import AdminFeedbackPage from '../pages/admin/AdminFeedbackPage.jsx'

const signedIn = (el) => <ProtectedRoute>{el}</ProtectedRoute>
const adminOnly = (el) => <ProtectedRoute role="ADMIN">{el}</ProtectedRoute>

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
        <Route path="/feedback" element={<FeedbackPage />} />

        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/oauth/callback" element={<OAuthCallbackPage />} />

        <Route path="/booking/details" element={signedIn(<BookingDetailsPage />)} />
        <Route path="/booking/review" element={signedIn(<BookingReviewPage />)} />
        <Route path="/booking/payment" element={signedIn(<PaymentPage />)} />
        <Route path="/booking/confirmation/:bookingId" element={signedIn(<ConfirmationPage />)} />

        <Route path="/dashboard" element={signedIn(<DashboardPage />)} />

        <Route path="/admin" element={adminOnly(<AdminLayout />)}>
          <Route index element={<AdminOverviewPage />} />
          <Route path="users" element={<AdminUsersPage />} />
          <Route path="bookings" element={<AdminBookingsPage />} />
          <Route path="tours" element={<AdminToursPage />} />
          <Route path="tours/new" element={<AdminTourFormPage />} />
          <Route path="tours/import" element={<AdminTourImportPage />} />
          <Route path="tours/:tourId/edit" element={<AdminTourFormPage />} />
          <Route path="feedback" element={<AdminFeedbackPage />} />
        </Route>

        <Route path="/forbidden" element={<ForbiddenPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Layout>
  )
}
