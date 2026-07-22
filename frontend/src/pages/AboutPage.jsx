import { Space, Typography } from 'antd'
import { useEffect } from 'react'
import DemoCard from '../components/DemoCard'

const { Paragraph, Title } = Typography

export default function AboutPage() {
  useEffect(() => {
    document.title = 'About | CDAC Project'
  }, [])

  return (
    <Space direction="vertical" size="large" className="w-full">
      <div>
        <Title level={3}>About This Skeleton</Title>
        <Paragraph type="secondary">
          A shared Ant Design layout (Sider, Header, Content, Footer) wraps all
          routed pages. Layout metadata is driven from route config and synced
          into <code>LayoutContext</code>.
        </Paragraph>
      </div>

      <DemoCard title="Folder Structure">
        <ul className="list-disc pl-5 text-sm leading-7">
          <li>
            <code>src/pages</code> — route-level screens
          </li>
          <li>
            <code>src/components</code> — reusable UI components (e.g.{' '}
            <code>DemoCard.js</code>)
          </li>
          <li>
            <code>src/layout</code> — app shell (Sider, Header, Content,
            Footer)
          </li>
          <li>
            <code>src/context</code> — React Context definitions
          </li>
          <li>
            <code>src/providers</code> — combined app providers
          </li>
          <li>
            <code>src/hooks</code> — custom hooks (e.g. <code>useFetch</code>)
          </li>
          <li>
            <code>src/helpers</code> — small pure helper functions
          </li>
          <li>
            <code>src/utils</code> — fetch helper & constants
          </li>
          <li>
            <code>src/routes</code> — React Router setup
          </li>
        </ul>
      </DemoCard>
    </Space>
  )
}
