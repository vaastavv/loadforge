import { Box, List, ListItemButton, ListItemIcon, ListItemText, Stack, Toolbar, Typography } from '@mui/material';
import SpaceDashboardOutlinedIcon from '@mui/icons-material/SpaceDashboardOutlined';
import DnsOutlinedIcon from '@mui/icons-material/DnsOutlined';
import ScienceOutlinedIcon from '@mui/icons-material/ScienceOutlined';
import HistoryOutlinedIcon from '@mui/icons-material/HistoryOutlined';
import InsightsOutlinedIcon from '@mui/icons-material/InsightsOutlined';
import BoltIcon from '@mui/icons-material/Bolt';
import { NavLink } from 'react-router-dom';
import type { ReactNode } from 'react';

interface NavItem {
  label: string;
  to: string;
  icon: ReactNode;
}

const NAV_ITEMS: NavItem[] = [
  { label: 'Overview', to: '/', icon: <SpaceDashboardOutlinedIcon /> },
  { label: 'Workers', to: '/workers', icon: <DnsOutlinedIcon /> },
  { label: 'Tests', to: '/tests', icon: <ScienceOutlinedIcon /> },
  { label: 'Executions', to: '/executions', icon: <HistoryOutlinedIcon /> },
  { label: 'Metrics', to: '/metrics', icon: <InsightsOutlinedIcon /> },
];

export const SIDEBAR_WIDTH = 256;

export default function Sidebar() {
  return (
    <Box sx={{ height: '100%', bgcolor: '#0f172a', color: '#e2e8f0', display: 'flex', flexDirection: 'column' }}>
      <Toolbar sx={{ px: 3 }}>
        <Stack direction="row" alignItems="center" spacing={1.5}>
          <Box
            sx={{
              width: 36,
              height: 36,
              borderRadius: 2,
              bgcolor: 'primary.main',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <BoltIcon sx={{ color: '#fff' }} />
          </Box>
          <Box>
            <Typography variant="subtitle1" fontWeight={700} sx={{ color: '#fff', lineHeight: 1.1 }}>
              LoadForge
            </Typography>
            <Typography variant="caption" sx={{ color: '#94a3b8' }}>
              Control Plane
            </Typography>
          </Box>
        </Stack>
      </Toolbar>

      <List sx={{ px: 2, py: 1, flexGrow: 1 }}>
        {NAV_ITEMS.map((item) => (
          <ListItemButton
            key={item.to}
            component={NavLink}
            to={item.to}
            end={item.to === '/'}
            sx={{
              borderRadius: 2,
              mb: 0.5,
              color: '#cbd5e1',
              '& .MuiListItemIcon-root': { color: '#94a3b8', minWidth: 40 },
              '&:hover': { bgcolor: 'rgba(148,163,184,0.12)' },
              '&.active': {
                bgcolor: 'rgba(99,102,241,0.16)',
                color: '#ffffff',
                '& .MuiListItemIcon-root': { color: '#a5b4fc' },
              },
            }}
          >
            <ListItemIcon>{item.icon}</ListItemIcon>
            <ListItemText primaryTypographyProps={{ fontWeight: 600, fontSize: 14 }} primary={item.label} />
          </ListItemButton>
        ))}
      </List>

      <Box sx={{ p: 3 }}>
        <Typography variant="caption" sx={{ color: '#64748b' }}>
          v1.0.0 · Distributed Load Testing
        </Typography>
      </Box>
    </Box>
  );
}
