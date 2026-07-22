import { Layout, Typography } from 'antd'
import { useLayout } from '../hooks/useLayout'

const { Footer } = Layout
const { Text } = Typography

export default function AppFooter() {
  const { showFooter } = useLayout()

  if (!showFooter) {
    return null
  }

  return (
    <Footer className="border-t border-gray-200 bg-white text-center">
      <Text type="secondary">
        CDAC Final Project © {new Date().getFullYear()} — Built with React, Ant
        Design & Tailwind
      </Text>
    </Footer>
  )
}
