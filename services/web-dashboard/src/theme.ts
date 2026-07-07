import { createTheme } from '@mui/material/styles';

/**
 * A modern, professional light theme: soft slate surfaces, an indigo primary,
 * generous radii and a clean Inter type scale.
 */
const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#4f46e5', light: '#6366f1', dark: '#4338ca', contrastText: '#ffffff' },
    secondary: { main: '#0ea5e9' },
    success: { main: '#16a34a' },
    warning: { main: '#f59e0b' },
    error: { main: '#dc2626' },
    info: { main: '#0284c7' },
    background: { default: '#f4f6fb', paper: '#ffffff' },
    text: { primary: '#0f172a', secondary: '#64748b' },
    divider: '#e2e8f0',
  },
  shape: { borderRadius: 12 },
  typography: {
    fontFamily: '"Inter", "Segoe UI", system-ui, -apple-system, sans-serif',
    h4: { fontWeight: 700, fontSize: '1.6rem' },
    h5: { fontWeight: 700 },
    h6: { fontWeight: 600 },
    subtitle1: { fontWeight: 600 },
    subtitle2: { fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: { backgroundColor: '#f4f6fb' },
      },
    },
    MuiPaper: {
      styleOverrides: { root: { backgroundImage: 'none' } },
    },
    MuiCard: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: {
          border: '1px solid #e2e8f0',
          borderRadius: 16,
          boxShadow: '0 1px 2px 0 rgba(15, 23, 42, 0.04)',
        },
      },
    },
    MuiCardHeader: {
      styleOverrides: { root: { paddingBottom: 8 } },
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: { root: { borderRadius: 10 } },
    },
    MuiTableCell: {
      styleOverrides: {
        head: { fontWeight: 600, color: '#475569', backgroundColor: '#f8fafc' },
      },
    },
    MuiChip: {
      styleOverrides: { root: { fontWeight: 600 } },
    },
  },
});

export default theme;
