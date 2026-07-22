import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons'
import { Button, Layout, theme, Typography } from 'antd'
import { useLayout } from '../hooks/useLayout'
import { useUser } from '../hooks/useUser'
import { APP_NAME } from '../utils/constants'

const { Header } = Layout
const { Title } = Typography

export default function AppHeader() {
  const { collapsed, setCollapsed, pageTitle } = useLayout()
  const { user } = useUser()
  const {
    token: { colorBgContainer },
  } = theme.useToken()

  return (
    <Header
      style={{ background: colorBgContainer }}
      className="flex items-center justify-between border-b border-gray-200 px-4"
    >
      <div className="flex items-center gap-3">
        <Button
          type="text"
          icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          onClick={() => setCollapsed(!collapsed)}
        />
        <Title level={4} className="!mb-0">
          {pageTitle}
        </Title>
      </div>

      <div className="text-right">
        <p className="m-0 text-sm font-medium">{user.name}</p>
        <p className="m-0 text-xs text-gray-500">{APP_NAME}</p>
      </div>
    </Header>
  )
}
