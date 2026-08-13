import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { yupResolver } from '@hookform/resolvers/yup'
import { Controller, useForm } from 'react-hook-form'
import * as yup from 'yup'
import { Alert, Button, Form, Input, Typography } from 'antd'
import { LockOutlined, MailOutlined, UserOutlined } from '@ant-design/icons'
import { registerUser } from '../api/authApi'
import { ROUTES } from '../utils/constants'

const { Title, Paragraph } = Typography

// Mirrors backend SignupRequest validation (username 3-50 chars, valid email, password min 6)
const registerSchema = yup.object({
  username: yup
    .string()
    .trim()
    .required('Username is required')
    .min(3, 'Username must be at least 3 characters')
    .max(50, 'Username must be at most 50 characters'),
  email: yup
    .string()
    .trim()
    .required('Email is required')
    .email('Enter a valid email address'),
  password: yup
    .string()
    .required('Password is required')
    .min(6, 'Password must be at least 6 characters'),
  confirmPassword: yup
    .string()
    .required('Please confirm your password')
    .oneOf([yup.ref('password')], 'Passwords do not match'),
})

export default function RegisterPage() {
  const navigate = useNavigate()
  const [submitError, setSubmitError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(registerSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
    },
  })

  // Called only when React Hook Form + Yup validation passes ("Valid" branch of the flow)
  const onSubmit = async (values) => {
    setSubmitError(null)
    setSubmitting(true)

    try {
      // Create JSON request -> Axios POST /api/auth/register
      await registerUser({
        username: values.username,
        email: values.email,
        password: values.password,
      })

      navigate(ROUTES.HOME, {
        replace: true,
        state: { registered: true },
      })
    } catch (err) {
      const backendMessage = err.response?.data?.message
      setSubmitError(backendMessage || 'Registration failed. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto w-full max-w-md">
      <Title level={3}>Create an account</Title>
      <Paragraph type="secondary">
        Already have an account? <Link to={ROUTES.HOME}>Sign in</Link>
      </Paragraph>

      {submitError && (
        <Alert
          type="error"
          message={submitError}
          showIcon
          closable
          onClose={() => setSubmitError(null)}
          className="mb-4"
        />
      )}

      <Form layout="vertical" onFinish={handleSubmit(onSubmit)}>
        <Form.Item
          label="Username"
          validateStatus={errors.username ? 'error' : ''}
          help={errors.username?.message}
        >
          <Controller
            name="username"
            control={control}
            render={({ field }) => (
              <Input {...field} prefix={<UserOutlined />} placeholder="john" autoComplete="username" />
            )}
          />
        </Form.Item>

        <Form.Item
          label="Email"
          validateStatus={errors.email ? 'error' : ''}
          help={errors.email?.message}
        >
          <Controller
            name="email"
            control={control}
            render={({ field }) => (
              <Input {...field} prefix={<MailOutlined />} placeholder="john@example.com" autoComplete="email" />
            )}
          />
        </Form.Item>

        <Form.Item
          label="Password"
          validateStatus={errors.password ? 'error' : ''}
          help={errors.password?.message}
        >
          <Controller
            name="password"
            control={control}
            render={({ field }) => (
              <Input.Password {...field} prefix={<LockOutlined />} placeholder="••••••" autoComplete="new-password" />
            )}
          />
        </Form.Item>

        <Form.Item
          label="Confirm Password"
          validateStatus={errors.confirmPassword ? 'error' : ''}
          help={errors.confirmPassword?.message}
        >
          <Controller
            name="confirmPassword"
            control={control}
            render={({ field }) => (
              <Input.Password {...field} prefix={<LockOutlined />} placeholder="••••••" autoComplete="new-password" />
            )}
          />
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit" block loading={submitting}>
            Register
          </Button>
        </Form.Item>
      </Form>
    </div>
  )
}
