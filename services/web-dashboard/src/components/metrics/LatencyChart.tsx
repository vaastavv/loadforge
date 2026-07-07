import { Card, CardContent, CardHeader } from '@mui/material';
import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import type { ExecutionMetricsSummary } from '@/types/api';

const COLORS = ['#4f46e5', '#f59e0b', '#dc2626'];

export default function LatencyChart({ summary }: { summary: ExecutionMetricsSummary }) {
  const data = [
    { name: 'Avg', value: Number(summary.averageLatencyMs.toFixed(2)) },
    { name: 'p95', value: Number(summary.p95LatencyMs.toFixed(2)) },
    { name: 'p99', value: Number(summary.p99LatencyMs.toFixed(2)) },
  ];

  return (
    <Card sx={{ height: '100%' }}>
      <CardHeader title="Latency distribution" subheader="Milliseconds" titleTypographyProps={{ variant: 'h6' }} />
      <CardContent>
        <ResponsiveContainer width="100%" height={280}>
          <BarChart data={data} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#eef2f7" vertical={false} />
            <XAxis dataKey="name" tickLine={false} axisLine={false} />
            <YAxis tickLine={false} axisLine={false} width={48} />
            <Tooltip formatter={(value) => [`${value} ms`, 'Latency']} />
            <Bar dataKey="value" radius={[6, 6, 0, 0]} maxBarSize={64}>
              {data.map((_, index) => (
                <Cell key={index} fill={COLORS[index]} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}
