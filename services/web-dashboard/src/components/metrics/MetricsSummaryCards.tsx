import { Grid } from '@mui/material';
import SpeedOutlinedIcon from '@mui/icons-material/SpeedOutlined';
import TimelineOutlinedIcon from '@mui/icons-material/TimelineOutlined';
import ErrorOutlineOutlinedIcon from '@mui/icons-material/ErrorOutlineOutlined';
import AvTimerOutlinedIcon from '@mui/icons-material/AvTimerOutlined';
import StatCard from '@/components/common/StatCard';
import type { ExecutionMetricsSummary } from '@/types/api';
import { formatDecimal, formatNumber } from '@/utils/format';

export default function MetricsSummaryCards({ summary }: { summary: ExecutionMetricsSummary }) {
  const errorRate = summary.totalRequests > 0 ? (summary.failures / summary.totalRequests) * 100 : 0;

  return (
    <Grid container spacing={3}>
      <Grid item xs={12} sm={6} md={3}>
        <StatCard
          title="Requests / sec"
          value={formatDecimal(summary.requestsPerSecond)}
          icon={<SpeedOutlinedIcon />}
          accentColor="#4f46e5"
          subtitle={`${formatNumber(summary.totalRequests)} total requests`}
        />
      </Grid>
      <Grid item xs={12} sm={6} md={3}>
        <StatCard
          title="Throughput"
          value={formatDecimal(summary.throughput)}
          icon={<TimelineOutlinedIcon />}
          accentColor="#0ea5e9"
          subtitle={`${formatNumber(summary.successfulRequests)} successful`}
        />
      </Grid>
      <Grid item xs={12} sm={6} md={3}>
        <StatCard
          title="Avg latency"
          value={`${formatDecimal(summary.averageLatencyMs)} ms`}
          icon={<AvTimerOutlinedIcon />}
          accentColor="#f59e0b"
          subtitle={`p95 ${formatDecimal(summary.p95LatencyMs)} · p99 ${formatDecimal(summary.p99LatencyMs)} ms`}
        />
      </Grid>
      <Grid item xs={12} sm={6} md={3}>
        <StatCard
          title="Failures"
          value={formatNumber(summary.failures)}
          icon={<ErrorOutlineOutlinedIcon />}
          accentColor="#dc2626"
          subtitle={`${formatDecimal(errorRate)}% error rate`}
        />
      </Grid>
    </Grid>
  );
}
