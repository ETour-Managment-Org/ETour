import { Card, Spin, Typography } from "antd";

const { Text } = Typography;

export default function DemoCard({
  title,
  description,
  loading,
  error,
  children,
}) {
  return (
    <Card title={title} className="shadow-sm">
      {description ? (
        <Text type="secondary" className="mb-4 block">
          {description}
        </Text>
      ) : null}

      {loading ? (
        <div className="flex justify-center py-8">
          <Spin />
        </div>
      ) : null}

      {error ? <Text type="danger">{error}</Text> : null}

      {!loading && !error ? children : null}
    </Card>
  );
}
