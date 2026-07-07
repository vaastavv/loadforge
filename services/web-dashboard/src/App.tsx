import { Navigate, Route, Routes } from 'react-router-dom';
import AppLayout from '@/components/layout/AppLayout';
import DashboardPage from '@/pages/DashboardPage';
import WorkersPage from '@/pages/WorkersPage';
import TestsPage from '@/pages/TestsPage';
import ExecutionsPage from '@/pages/ExecutionsPage';
import MetricsPage from '@/pages/MetricsPage';

export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/workers" element={<WorkersPage />} />
        <Route path="/tests" element={<TestsPage />} />
        <Route path="/executions" element={<ExecutionsPage />} />
        <Route path="/metrics" element={<MetricsPage />} />
        <Route path="/metrics/:executionId" element={<MetricsPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
