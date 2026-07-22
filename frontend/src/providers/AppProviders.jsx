import { ConfigProvider } from 'antd'
import { BrowserRouter } from 'react-router-dom'
import { LayoutProvider } from '../context/LayoutProvider'
import { UserProvider } from '../context/UserProvider'

export default function AppProviders({ children }) {
  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: '#1677ff',
          borderRadius: 6,
        },
      }}
    >
      <BrowserRouter>
        <UserProvider>
          <LayoutProvider>{children}</LayoutProvider>
        </UserProvider>
      </BrowserRouter>
    </ConfigProvider>
  )
}
