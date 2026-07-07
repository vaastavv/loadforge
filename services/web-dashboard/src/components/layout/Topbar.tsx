import { AppBar, Box, Chip, IconButton, Toolbar, Tooltip, Typography } from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import RefreshIcon from '@mui/icons-material/Refresh';
import CircleIcon from '@mui/icons-material/Circle';
import { useIsFetching, useQueryClient } from '@tanstack/react-query';
import { API_BASE_URL } from '@/api/config';
import { SIDEBAR_WIDTH } from './Sidebar';

interface TopbarProps {
  onMenuClick: () => void;
}

export default function Topbar({ onMenuClick }: TopbarProps) {
  const isFetching = useIsFetching();
  const queryClient = useQueryClient();

  return (
    <AppBar
      position="fixed"
      elevation={0}
      color="inherit"
      sx={{
        width: { md: `calc(100% - ${SIDEBAR_WIDTH}px)` },
        ml: { md: `${SIDEBAR_WIDTH}px` },
        borderBottom: '1px solid',
        borderColor: 'divider',
        bgcolor: 'background.paper',
      }}
    >
      <Toolbar sx={{ gap: 2 }}>
        <IconButton edge="start" onClick={onMenuClick} sx={{ display: { md: 'none' } }} aria-label="Open navigation">
          <MenuIcon />
        </IconButton>

        <Box sx={{ flexGrow: 1, minWidth: 0 }}>
          <Typography variant="subtitle1" fontWeight={700} noWrap>
            Load Testing Dashboard
          </Typography>
          <Typography variant="caption" color="text.secondary" noWrap>
            Real-time monitoring · {API_BASE_URL || 'same origin'}
          </Typography>
        </Box>

        <Chip
          size="small"
          color={isFetching ? 'info' : 'success'}
          variant="outlined"
          icon={<CircleIcon sx={{ fontSize: 10 }} />}
          label={isFetching ? 'Syncing' : 'Live'}
        />
        <Tooltip title="Refresh all data">
          <IconButton onClick={() => queryClient.invalidateQueries()} aria-label="Refresh">
            <RefreshIcon />
          </IconButton>
        </Tooltip>
      </Toolbar>
    </AppBar>
  );
}
