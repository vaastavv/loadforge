import { Alert, Box, Button, CircularProgress, Stack, Typography } from '@mui/material';
import InboxOutlinedIcon from '@mui/icons-material/InboxOutlined';
import type { ReactNode } from 'react';

export function LoadingState({ label = 'Loading…' }: { label?: string }) {
  return (
    <Stack alignItems="center" justifyContent="center" spacing={2} sx={{ py: 8 }}>
      <CircularProgress />
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
    </Stack>
  );
}

interface ErrorStateProps {
  message: string;
  onRetry?: () => void;
}

export function ErrorState({ message, onRetry }: ErrorStateProps) {
  return (
    <Box sx={{ py: 2 }}>
      <Alert
        severity="error"
        action={
          onRetry ? (
            <Button color="inherit" size="small" onClick={onRetry}>
              Retry
            </Button>
          ) : undefined
        }
      >
        {message}
      </Alert>
    </Box>
  );
}

interface EmptyStateProps {
  title: string;
  description?: string;
  icon?: ReactNode;
}

export function EmptyState({ title, description, icon }: EmptyStateProps) {
  return (
    <Stack alignItems="center" justifyContent="center" spacing={1.5} sx={{ py: 8, color: 'text.secondary' }}>
      <Box sx={{ fontSize: 48, display: 'flex' }}>{icon ?? <InboxOutlinedIcon fontSize="inherit" />}</Box>
      <Typography variant="subtitle1" fontWeight={600} color="text.primary">
        {title}
      </Typography>
      {description && (
        <Typography variant="body2" textAlign="center" sx={{ maxWidth: 380 }}>
          {description}
        </Typography>
      )}
    </Stack>
  );
}
