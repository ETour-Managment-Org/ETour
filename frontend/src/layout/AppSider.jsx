import { Layout, Menu } from 'antd'
import { useMemo } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useLayout } from '../hooks/useLayout'
import { APP_NAME } from '../utils/constants'
import { routeConfig } from '../routes/routeConfig'

const { Sider } = Layout

export default function AppSider() {
  const { collapsed } = useLayout()
  const navigate = useNavigate()
  const location = useLocation()

  const menuItems = useMemo(
    () =>
      routeConfig
        .filter((route) => route.showInMenu)
        .map((route) => ({
          key: route.path,
          icon: route.icon,
          label: route.label,
        })),
    [],
  )

  const selectedKey = useMemo(() => {
    const match = routeConfig.find((route) => route.path === location.pathname)
    return match ? match.path : location.pathname
  }, [location.pathname])

  return (
    <Sider
      trigger={null}
      collapsible
      collapsed={collapsed}
      className="!bg-[#001529] min-h-screen"
    >
      <div className="flex h-16 items-center justify-center px-4 text-white">
        <span className="truncate text-base font-semibold">
          {collapsed ? 'CP' : APP_NAME}
        </span>
      </div>

      <Menu
        theme="dark"
        mode="inline"
        selectedKeys={[selectedKey]}
        items={menuItems}
        onClick={({ key }) => navigate(key)}
      />
    </Sider>
  )
}
