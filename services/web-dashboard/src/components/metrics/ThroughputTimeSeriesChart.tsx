import { Card, CardContent, CardHeader } from '@mui/material';
import { Area, AreaChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import type { MetricsTimePoint } from '@/hooks/useMetricsTimeSeries';
import { EmptyState } from '@/components/common/QueryStates';

export default function ThroughputTimeSeriesChart({ data }: { data: MetricsTimePoint[] }) {
  return (
    <Card>
      <CardHeader
        title="Live throughput"
        subheader="Requests/sec and throughput over time"
        titleTypographyProps={{ variant: 'h6' }}
      />
      <CardContent>
        {data.length === 0 ? (
          <EmptyState
            title="Waiting for samples"
            description="Live rates will appear here as metrics are reported for this execution."
          />
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={data} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
              <defs>
                <linearGradient id="rpsGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#4f46e5" stopOpacity={0.35} />
                  <stop offset="95%" stopColor="#4f46e5" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="throughputGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#0ea5e9" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#0ea5e9" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#eef2f7" vertical={false} />
              <XAxis dataKey="label" tickLine={false} axisLine={false} minTickGap={32} />
              <YAxis tickLine={false} axisLine={false} width={48} />
              <Tooltip />
              <Legend />
              <Area
                type="monotone"
                dataKey="requestsPerSecond"
                name="Requests/sec"
                stroke="#4f46e5"
                fill="url(#rpsGradient)"
                strokeWidth={2}
              />
              <Area
                type="monotone"
                dataKey="throughput"
                name="Throughput"
                stroke="#0ea5e9"
                fill="url(#throughputGradient)"
                strokeWidth={2}
              />
            </AreaChart>
          </ResponsiveContainer>
        )}
      </CardContent>
    </Card>
  );
}
