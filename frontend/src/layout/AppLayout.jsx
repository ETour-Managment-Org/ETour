import { Breadcrumb, Layout } from 'antd'
import { Outlet } from 'react-router-dom'
import { useLayout } from '../hooks/useLayout'
import AppFooter from './AppFooter'
import AppHeader from './AppHeader'
import AppSider from './AppSider'

const { Content } = Layout

export default function AppLayout() {
  const { breadcrumbs } = useLayout()

  return (
    <Layout className="min-h-screen">
      <AppSider />

      <Layout>
        <AppHeader />

        <Content className="m-4">
          {breadcrumbs.length > 0 ? (
            <Breadcrumb className="mb-4" items={breadcrumbs} />
          ) : null}

          <div className="rounded-lg bg-white p-6 shadow-sm">
            <Outlet />
          </div>
        </Content>

        <AppFooter />
      </Layout>
    </Layout>
  )
}
