import { Card, CardContent, CardHeader } from '@mui/material';
import { CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import type { MetricsTimePoint } from '@/hooks/useMetricsTimeSeries';
import { EmptyState } from '@/components/common/QueryStates';

export default function LatencyTimeSeriesChart({ data }: { data: MetricsTimePoint[] }) {
  return (
    <Card>
      <CardHeader
        title="Latency over time"
        subheader="Average, p95 and p99 (ms)"
        titleTypographyProps={{ variant: 'h6' }}
      />
      <CardContent>
        {data.length === 0 ? (
          <EmptyState
            title="Waiting for samples"
            description="Latency trends will appear here as metrics are reported for this execution."
          />
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={data} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#eef2f7" vertical={false} />
              <XAxis dataKey="label" tickLine={false} axisLine={false} minTickGap={32} />
              <YAxis tickLine={false} axisLine={false} width={48} />
              <Tooltip formatter={(value) => `${value} ms`} />
              <Legend />
              <Line type="monotone" dataKey="averageLatencyMs" name="Avg" stroke="#4f46e5" strokeWidth={2} dot={false} />
              <Line type="monotone" dataKey="p95LatencyMs" name="p95" stroke="#f59e0b" strokeWidth={2} dot={false} />
              <Line type="monotone" dataKey="p99LatencyMs" name="p99" stroke="#dc2626" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        )}
      </CardContent>
    </Card>
  );
}
