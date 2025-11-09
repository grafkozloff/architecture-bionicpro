import React, { useState } from 'react';
import { useKeycloak } from '@react-keycloak/web';

interface ReportData {
  username: string;
  reportDate: string;
  data: any;
}

interface HistoryReportData {
  startDate: string;
  endDate: string;
  reports: any[];
}

const ReportPage: React.FC = () => {
  const { keycloak, initialized } = useKeycloak();
  const [loading, setLoading] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [reportData, setReportData] = useState<ReportData | null>(null);
  const [historyData, setHistoryData] = useState<HistoryReportData | null>(null);
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');

  const downloadReport = async () => {
    if (!keycloak?.token) {
      setError('Not authenticated');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      setReportData(null);

      const response = await fetch(`${process.env.REACT_APP_API_URL}/reports/user-report`, {
        headers: {
          'Authorization': `Bearer ${keycloak.token}`
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data: ReportData = await response.json();
      setReportData(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  const downloadHistoryReport = async () => {
    if (!keycloak?.token) {
      setError('Not authenticated');
      return;
    }

    // Валидация дат
    if (!startDate || !endDate) {
      setError('Please enter both start date and end date');
      return;
    }

    if (startDate > endDate) {
      setError('Start date cannot be after end date');
      return;
    }

    try {
      setHistoryLoading(true);
      setError(null);
      setHistoryData(null);

      const url = `${process.env.REACT_APP_API_URL}/reports/user-report/history?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`;

      const response = await fetch(url, {
        headers: {
          'Authorization': `Bearer ${keycloak.token}`
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data: HistoryReportData = await response.json();
      setHistoryData(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setHistoryLoading(false);
    }
  };

  if (!initialized) {
    return <div>Loading...</div>;
  }

  if (!keycloak.authenticated) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
        <button
          onClick={() => keycloak.login()}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          Login
        </button>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100 p-4">
      <div className="p-8 bg-white rounded-lg shadow-md w-full max-w-4xl">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold">Usage Reports</h1>
          <button
            onClick={() => keycloak.logout()}
            className="px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600 text-sm"
          >
            Logout
          </button>
        </div>

        {/* Секция для основного отчета */}
        <div className="mb-8 p-4 border border-gray-200 rounded">
          <h2 className="text-xl font-semibold mb-4">Current User Report</h2>
          <button
            onClick={downloadReport}
            disabled={loading}
            className={`px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 ${
              loading ? 'opacity-50 cursor-not-allowed' : ''
            }`}
          >
            {loading ? 'Loading Report...' : 'Get User Report'}
          </button>

          {reportData && (
            <div className="mt-4 p-4 bg-gray-50 rounded">
              <h3 className="font-semibold">Report Data:</h3>
              <pre className="mt-2 whitespace-pre-wrap">
                {JSON.stringify(reportData, null, 2)}
              </pre>
            </div>
          )}
        </div>

        {/* Секция для истории отчетов */}
        <div className="p-4 border border-gray-200 rounded">
          <h2 className="text-xl font-semibold mb-4">User Report History</h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Start Date *
              </label>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                End Date *
              </label>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <button
            onClick={downloadHistoryReport}
            disabled={historyLoading || !startDate || !endDate}
            className={`px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600 ${
              (historyLoading || !startDate || !endDate) ? 'opacity-50 cursor-not-allowed' : ''
            }`}
          >
            {historyLoading ? 'Loading History...' : 'Get Report History'}
          </button>

          {historyData && (
            <div className="mt-4 p-4 bg-gray-50 rounded">
              <h3 className="font-semibold">History Data:</h3>
              <pre className="mt-2 whitespace-pre-wrap">
                {JSON.stringify(historyData, null, 2)}
              </pre>
            </div>
          )}
        </div>

        {error && (
          <div className="mt-4 p-4 bg-red-100 text-red-700 rounded">
            {error}
          </div>
        )}
      </div>
    </div>
  );
};

export default ReportPage;