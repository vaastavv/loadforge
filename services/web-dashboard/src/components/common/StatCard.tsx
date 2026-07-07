import { Box, Card, CardContent, Stack, Typography } from '@mui/material';
import type { ReactNode } from 'react';

interface StatCardProps {
  title: string;
  value: ReactNode;
  icon: ReactNode;
  accentColor?: string;
  subtitle?: string;
  loading?: boolean;
}

export default function StatCard({
  title,
  value,
  icon,
  accentColor = '#4f46e5',
  subtitle,
  loading = false,
}: StatCardProps) {
  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Stack direction="row" justifyContent="space-between" alignItems="flex-start" spacing={2}>
          <Box sx={{ minWidth: 0 }}>
            <Typography variant="body2" color="text.secondary" fontWeight={600}>
              {title}
            </Typography>
            <Typography variant="h4" sx={{ mt: 1, fontWeight: 700 }}>
              {loading ? '—' : value}
            </Typography>
            {subtitle && (
              <Typography variant="caption" color="text.secondary" noWrap sx={{ display: 'block', mt: 0.5 }}>
                {subtitle}
              </Typography>
            )}
          </Box>
          <Box
            sx={{
              width: 48,
              height: 48,
              flexShrink: 0,
              borderRadius: 3,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: accentColor,
              bgcolor: `${accentColor}14`,
            }}
          >
            {icon}
          </Box>
        </Stack>
      </CardContent>
    </Card>
  );
}
