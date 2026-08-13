import {
  required, minLength, maxLength, email, phone10, password,
  matches, number, min, max, integer, notInPast, notInFuture, oneOf
} from './rules.js'

export const registerSchema = (values) => ({
  username:  [required('Username'), minLength(3, 'Username'), maxLength(50, 'Username')],
  password:  [required('Password'), password()],
  confirm:   [required('Confirm password'), matches(values.password, 'Passwords')],
  email:     [required('Email'), email()],
  firstName: [required('First name'), maxLength(50, 'First name')],
  lastName:  [maxLength(50, 'Last name')],
  phoneNumber: [required('Phone number'), phone10()],
  gender:    [oneOf(['MALE', 'FEMALE', 'OTHER', ''], 'Gender')]
})
export const loginSchema = () => ({
  username: [required('Username')],
  password: [required('Password')]
})
export const profileSchema = () => ({
  firstName:   [required('First name')],
  email:       [required('Email'), email()],
  phoneNumber: [phone10()]
})
export const changePasswordSchema = (values) => ({
  currentPassword: [required('Current password')],
  newPassword: [
    required('New password'),
    password(),
    (v) => v && v === values.currentPassword
      ? 'The new password must be different from the current one' : null
  ],
  confirmPassword: [required('Confirm password'), matches(values.newPassword, 'Passwords')]
})
export const contactSchema = () => ({
  fullName: [required('Full name'), maxLength(120, 'Full name')],
  email:    [required('Email'), email()],
  phone:    [required('Phone number'), phone10()]
})
export const passengerSchema = () => ({
  fullName:  [required('Traveller name'), maxLength(120, 'Traveller name')],
  birthDate: [required('Date of birth'), notInFuture('Date of birth')],
  email:     [email()]
})
export const feedbackSchema = () => ({
  category: [required('Category')],
  rating:   [required('Rating'), integer('Rating'), min(1, 'Rating'), max(5, 'Rating')],
  message:  [required('Message'), minLength(5, 'Message'), maxLength(2000, 'Message')],
  email:    [email()]
})
export const reviewSchema = () => ({
  rating:            [required('Rating'), min(1, 'Rating'), max(5, 'Rating')],
  reviewTitle:       [maxLength(150, 'Title')],
  reviewDescription: [maxLength(1000, 'Review')]
})
export const tourFormSchema = (values) => ({
  tourName:    [required('Tour name'), maxLength(150, 'Tour name')],
  destination: [required('Destination'), maxLength(150, 'Destination')],
  days:        [required('Days'), integer('Days'), min(1, 'Days')],
  nights: [
    required('Nights'), integer('Nights'), min(0, 'Nights'),
    (v) => v !== '' && values.days !== '' && Number(v) >= Number(values.days)
      ? `Nights (${v}) must be fewer than days (${values.days})` : null
  ],
  price:      [required('Price'), number('Price'), min(1, 'Price')],
  categoryId: [required('Category')],
  tourType:   [oneOf(['DOMESTIC', 'INTERNATIONAL', ''], 'Tour type')]
})
export const fareBandSchema = () => ({
  adultPrice: [required('Twin sharing fare'), number('Twin sharing fare'), min(1, 'Twin sharing fare')]
})
export const departureSchema = (values) => ({
  startDate:  [required('Departure date'), notInPast('Departure date')],
  totalSeats: [required('Total seats'), integer('Total seats'), min(1, 'Total seats')],
  availableSeats: [
    (v) => v !== '' && values.totalSeats !== '' && Number(v) > Number(values.totalSeats)
      ? 'Seats left cannot exceed total seats' : null
  ]
})
export const searchSchema = (values) => ({
  maxPrice: [
    (v) => v !== '' && values.minPrice !== '' && Number(v) < Number(values.minPrice)
      ? 'Maximum price cannot be below the minimum' : null
  ],
  maxDuration: [
    (v) => v !== '' && values.minDuration !== '' && Number(v) < Number(values.minDuration)
      ? 'Maximum duration cannot be below the minimum' : null
  ],
  endDate: [
    (v) => v && values.startDate && new Date(v) < new Date(values.startDate)
      ? 'End date cannot be before the start date' : null
  ]
})
