import { Button, List, Space, Typography } from "antd";
import { useEffect } from "react";
import DemoCard from "../components/DemoCard";
import { useUser } from "../hooks/useUser";
import { useFetch } from "../hooks/useFetch";
import { formatDate } from "../helpers/formatDate";

const { Paragraph, Title } = Typography;

export default function HomePage() {
  const { user, isAuthenticated, login, logout } = useUser();
  const { data, loading, error, refetch } = useFetch("/posts?_limit=5");

  useEffect(() => {
    document.title = "Home | CDAC Project";
  }, []);

  return (
    <Space direction="vertical" size="large" className="w-full">
      <div>
        <Title level={3}>Welcome, {user.name}</Title>
        <Paragraph type="secondary">
          This is the starter home page. Add your feature pages under{" "}
          <code>src/pages</code> and register them in{" "}
          <code>src/routes/routeConfig.js</code>.
        </Paragraph>
      </div>

      <DemoCard
        title="User Context Demo"
        description="Login/logout updates shared user state via React Context."
      >
        <Space>
          <Button
            type="primary"
            disabled={isAuthenticated}
            onClick={() => login({ name: "Team Member" })}
          >
            Demo Login
          </Button>
          <Button danger disabled={!isAuthenticated} onClick={logout}>
            Logout
          </Button>
        </Space>
        <Paragraph className="mt-4">
          Signed in as: <strong>{user.email}</strong> ({user.role})
        </Paragraph>
      </DemoCard>

      <DemoCard
        title="useFetch Hook Demo"
        description={`Fetched from JSONPlaceholder on ${formatDate(new Date())}.`}
        loading={loading}
        error={error}
      >
        <List
          dataSource={data || []}
          renderItem={(item) => (
            <List.Item>
              <List.Item.Meta
                title={item.title}
                description={`Post #${item.id}`}
              />
            </List.Item>
          )}
        />
        <Button onClick={refetch} className="mt-2">
          Refetch
        </Button>
      </DemoCard>
    </Space>

    // slkdfj
  );
}
