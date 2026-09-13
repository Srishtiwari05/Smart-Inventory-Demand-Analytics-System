import { useState, useEffect } from 'react';

const ACTION_COLORS = {
  CREATE_PRODUCT: { bg: 'rgba(16, 185, 129, 0.15)', color: '#34d399', border: '#10b981' },
  UPDATE_STOCK: { bg: 'rgba(59, 130, 246, 0.15)', color: '#60a5fa', border: '#3b82f6' },
  STOCK_ADJUSTMENT: { bg: 'rgba(6, 182, 212, 0.15)', color: '#22d3ee', border: '#06b6d4' },
  DELETE_PRODUCT: { bg: 'rgba(239, 68, 68, 0.15)', color: '#f87171', border: '#ef4444' },
  CREATE_PURCHASE_REQUEST: { bg: 'rgba(168, 85, 247, 0.15)', color: '#c084fc', border: '#a855f7' },
  APPROVE_PURCHASE_REQUEST: { bg: 'rgba(34, 197, 94, 0.15)', color: '#4ade80', border: '#22c55e' },
  REJECT_PURCHASE_REQUEST: { bg: 'rgba(244, 63, 94, 0.15)', color: '#fb7185', border: '#f43f5e' },
  CREATE_PURCHASE_ORDER: { bg: 'rgba(234, 179, 8, 0.15)', color: '#fde047', border: '#eab308' },
  UPDATE_PO_STATUS: { bg: 'rgba(99, 102, 241, 0.15)', color: '#818cf8', border: '#6366f1' },
  DEFAULT: { bg: 'rgba(148, 163, 184, 0.15)', color: '#cbd5e1', border: '#94a3b8' }
};

export default function AuditLogsView() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [actionFilter, setActionFilter] = useState('');
  const limit = 15;

  const fetchLogs = async () => {
    setLoading(true);
    setError(null);
    try {
      const token = localStorage.getItem('token');
      const res = await fetch(`/api/audit-logs?page=${page}&limit=${limit}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.status === 403) {
        setError('Forbidden: You do not have permission to view system audit logs.');
        setLoading(false);
        return;
      }
      if (!res.ok) {
        throw new Error('Failed to fetch audit logs');
      }
      const data = await res.json();
      setLogs(data.logs || []);
      setTotal(data.total || 0);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, [page]);

  const totalPages = Math.ceil(total / limit) || 1;

  const filteredLogs = actionFilter
    ? logs.filter(l => l.action.toLowerCase().includes(actionFilter.toLowerCase()))
    : logs;

  return (
    <div style={{ padding: '24px', maxWidth: '1200px', margin: '0 auto' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '1.8rem', fontWeight: 'bold', margin: 0, color: 'var(--text-primary, #f1f5f9)' }}>
            📜 Audit Trail & Security Logs
          </h1>
          <p style={{ color: 'var(--text-secondary, #94a3b8)', marginTop: '4px', fontSize: '0.9rem' }}>
            System-wide audit trail tracking stock changes, purchase orders, approvals, and user actions.
          </p>
        </div>
        <button
          onClick={fetchLogs}
          className="btn"
          style={{
            background: 'rgba(59, 130, 246, 0.15)',
            color: '#60a5fa',
            border: '1px solid #3b82f6',
            padding: '8px 16px',
            borderRadius: '8px',
            cursor: 'pointer'
          }}
        >
          🔄 Refresh
        </button>
      </div>

      {/* Filter bar */}
      <div className="glass-panel" style={{ padding: '16px', marginBottom: '20px', display: 'flex', gap: '12px', alignItems: 'center' }}>
        <span style={{ fontSize: '0.9rem', color: 'var(--text-secondary, #94a3b8)' }}>Filter Action:</span>
        <input
          type="text"
          placeholder="Filter by action (e.g. ADJUSTMENT, PRODUCT)..."
          value={actionFilter}
          onChange={(e) => setActionFilter(e.target.value)}
          style={{
            flex: 1,
            background: 'rgba(15, 23, 42, 0.6)',
            border: '1px solid rgba(148, 163, 184, 0.2)',
            color: '#f8fafc',
            padding: '8px 12px',
            borderRadius: '6px',
            outline: 'none'
          }}
        />
        {actionFilter && (
          <button
            onClick={() => setActionFilter('')}
            style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer', fontSize: '0.85rem' }}
          >
            Clear
          </button>
        )}
      </div>

      {/* Error state */}
      {error && (
        <div style={{ background: 'rgba(239,68,68,0.15)', border: '1px solid #ef4444', color: '#f87171', padding: '16px', borderRadius: '8px', marginBottom: '20px' }}>
          ⚠️ {error}
        </div>
      )}

      {/* Loading state */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '48px', color: '#94a3b8' }}>
          <div style={{ fontSize: '1.5rem', marginBottom: '12px' }}>⏳ Loading audit logs...</div>
        </div>
      ) : filteredLogs.length === 0 ? (
        <div className="glass-panel" style={{ padding: '48px', textAlign: 'center', color: '#94a3b8' }}>
          🔍 No audit logs recorded yet for this criteria.
        </div>
      ) : (
        <div className="glass-panel" style={{ overflow: 'hidden', borderRadius: '12px' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ background: 'rgba(15, 23, 42, 0.8)', borderBottom: '1px solid rgba(148, 163, 184, 0.2)' }}>
                <th style={{ padding: '12px 16px', color: '#94a3b8' }}>Timestamp</th>
                <th style={{ padding: '12px 16px', color: '#94a3b8' }}>User</th>
                <th style={{ padding: '12px 16px', color: '#94a3b8' }}>Action</th>
                <th style={{ padding: '12px 16px', color: '#94a3b8' }}>Entity</th>
                <th style={{ padding: '12px 16px', color: '#94a3b8' }}>Details</th>
              </tr>
            </thead>
            <tbody>
              {filteredLogs.map((log) => {
                const style = ACTION_COLORS[log.action] || ACTION_COLORS.DEFAULT;
                const formattedTime = log.createdAt
                  ? new Date(log.createdAt).toLocaleString()
                  : 'N/A';

                return (
                  <tr
                    key={log.id}
                    style={{
                      borderBottom: '1px solid rgba(148, 163, 184, 0.1)',
                      transition: 'background 0.2s ease'
                    }}
                  >
                    <td style={{ padding: '12px 16px', color: '#cbd5e1', whiteSpace: 'nowrap' }}>
                      {formattedTime}
                    </td>
                    <td style={{ padding: '12px 16px', color: '#f8fafc', fontWeight: 600 }}>
                      {log.username ? `👤 ${log.username}` : `User #${log.userId || 'System'}`}
                    </td>
                    <td style={{ padding: '12px 16px' }}>
                      <span
                        style={{
                          display: 'inline-block',
                          padding: '4px 10px',
                          borderRadius: '12px',
                          fontSize: '0.75rem',
                          fontWeight: 700,
                          background: style.bg,
                          color: style.color,
                          border: `1px solid ${style.border}`
                        }}
                      >
                        {log.action}
                      </span>
                    </td>
                    <td style={{ padding: '12px 16px', color: '#94a3b8' }}>
                      {log.entityType} {log.entityId ? `#${log.entityId}` : ''}
                    </td>
                    <td style={{ padding: '12px 16px', color: '#e2e8f0' }}>
                      {log.details}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination Controls */}
      {!loading && !error && total > limit && (
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '20px' }}>
          <span style={{ fontSize: '0.85rem', color: '#94a3b8' }}>
            Showing page {page} of {totalPages} ({total} total entries)
          </span>
          <div style={{ display: 'flex', gap: '8px' }}>
            <button
              disabled={page <= 1}
              onClick={() => setPage(p => p - 1)}
              style={{
                padding: '6px 14px',
                borderRadius: '6px',
                background: page <= 1 ? 'rgba(51, 65, 85, 0.4)' : 'rgba(59, 130, 246, 0.2)',
                color: page <= 1 ? '#64748b' : '#60a5fa',
                border: '1px solid rgba(148, 163, 184, 0.2)',
                cursor: page <= 1 ? 'not-allowed' : 'pointer'
              }}
            >
              Previous
            </button>
            <button
              disabled={page >= totalPages}
              onClick={() => setPage(p => p + 1)}
              style={{
                padding: '6px 14px',
                borderRadius: '6px',
                background: page >= totalPages ? 'rgba(51, 65, 85, 0.4)' : 'rgba(59, 130, 246, 0.2)',
                color: page >= totalPages ? '#64748b' : '#60a5fa',
                border: '1px solid rgba(148, 163, 184, 0.2)',
                cursor: page >= totalPages ? 'not-allowed' : 'pointer'
              }}
            >
              Next
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
