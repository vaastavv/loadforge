import { useState } from 'react';
import { Box, Drawer, Toolbar } from '@mui/material';
import { Outlet } from 'react-router-dom';
import Sidebar, { SIDEBAR_WIDTH } from './Sidebar';
import Topbar from './Topbar';

export default function AppLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <Box component="nav" sx={{ width: { md: SIDEBAR_WIDTH }, flexShrink: { md: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: 'block', md: 'none' },
            '& .MuiDrawer-paper': { width: SIDEBAR_WIDTH, borderRight: 0 },
          }}
        >
          <Sidebar />
        </Drawer>
        <Drawer
          variant="permanent"
          open
          sx={{
            display: { xs: 'none', md: 'block' },
            '& .MuiDrawer-paper': { width: SIDEBAR_WIDTH, borderRight: 0 },
          }}
        >
          <Sidebar />
        </Drawer>
      </Box>

      <Box sx={{ flexGrow: 1, width: { md: `calc(100% - ${SIDEBAR_WIDTH}px)` } }}>
        <Topbar onMenuClick={() => setMobileOpen(true)} />
        <Toolbar />
        <Box component="main" sx={{ p: { xs: 2, md: 4 } }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
