import { Card, CardContent, CardHeader } from '@mui/material';
import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';
import type { ExecutionMetricsSummary } from '@/types/api';
import { formatNumber } from '@/utils/format';

export default function RequestsChart({ summary }: { summary: ExecutionMetricsSummary }) {
  const isEmpty = summary.totalRequests === 0;
  const data = [
    { name: 'Successful', value: summary.successfulRequests, color: '#16a34a' },
    { name: 'Failed', value: summary.failures, color: '#dc2626' },
  ];
  const placeholder = [{ name: 'No data', value: 1, color: '#e2e8f0' }];

  return (
    <Card sx={{ height: '100%' }}>
      <CardHeader
        title="Request outcomes"
        subheader={`${formatNumber(summary.totalRequests)} total`}
        titleTypographyProps={{ variant: 'h6' }}
      />
      <CardContent>
        <ResponsiveContainer width="100%" height={280}>
          <PieChart>
            <Pie
              data={isEmpty ? placeholder : data}
              dataKey="value"
              nameKey="name"
              innerRadius={64}
              outerRadius={100}
              paddingAngle={2}
            >
              {(isEmpty ? placeholder : data).map((entry, index) => (
                <Cell key={index} fill={entry.color} />
              ))}
            </Pie>
            {!isEmpty && <Tooltip formatter={(value, name) => [formatNumber(Number(value)), String(name)]} />}
            {!isEmpty && <Legend />}
          </PieChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}
