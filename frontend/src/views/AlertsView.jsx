import { useState, useEffect } from 'react';

const SEVERITY_COLORS = {
  CRITICAL: { bg: 'rgba(239, 68, 68, 0.15)', text: '#ef4444', border: '#ef4444' },
  HIGH:     { bg: 'rgba(249, 115, 22, 0.15)', text: '#f97316', border: '#f97316' },
  MEDIUM:   { bg: 'rgba(234, 179, 8, 0.15)',  text: '#eab308', border: '#eab308' },
  LOW:      { bg: 'rgba(59, 130, 246, 0.15)',  text: '#3b82f6', border: '#3b82f6' }
};

const TYPE_ICONS = {
  EXPECTED_STOCKOUT: '🚨',
  CRITICAL_STOCK_RISK: '⚠️',
  LOW_STOCK: '📉',
  OVERDUE_PO: '⏰',
  DEMAND_SPIKE: '⚡',
  SLOW_MOVING: '🐢'
};

export default function AlertsView({ user, onNavigate }) {
  const [alerts, setAlerts] = useState([]);
  const [summary, setSummary] = useState({ totalUnread: 0, criticalCount: 0, highCount: 0, mediumCount: 0, lowCount: 0 });
  const [loading, setLoading] = useState(true);
  const [filterSeverity, setFilterSeverity] = useState('ALL');
  const [unreadOnly, setUnreadOnly] = useState(false);
  const [message, setMessage] = useState('');

  const token = localStorage.getItem('token');

  const fetchAlertsData = async () => {
    setLoading(true);
    try {
      const summaryRes = await fetch('/api/alerts/summary', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (summaryRes.ok) {
        const sumData = await summaryRes.json();
        setSummary(sumData);
      }

      const listRes = await fetch(`/api/alerts?unreadOnly=${unreadOnly}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (listRes.ok) {
        const listData = await listRes.json();
        setAlerts(listData);
      }
    } catch (err) {
      console.error('Failed to fetch alerts:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAlertsData();
  }, [unreadOnly]);

  const handleMarkAsRead = async (id) => {
    try {
      const res = await fetch(`/api/alerts/${id}/read`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        fetchAlertsData();
      }
    } catch (err) {
      console.error('Error marking as read:', err);
    }
  };

  const handleDismiss = async (id) => {
    try {
      const res = await fetch(`/api/alerts/${id}/dismiss`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        setMessage('Alert dismissed');
        setTimeout(() => setMessage(''), 3000);
        fetchAlertsData();
      }
    } catch (err) {
      console.error('Error dismissing alert:', err);
    }
  };

  const handleDismissAll = async () => {
    try {
      const res = await fetch('/api/alerts/dismiss-all', {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        setMessage('All alerts dismissed');
        setTimeout(() => setMessage(''), 3000);
        fetchAlertsData();
      }
    } catch (err) {
      console.error('Error dismissing all alerts:', err);
    }
  };

  const handleEvaluate = async () => {
    try {
      setLoading(true);
      const res = await fetch('/api/alerts/evaluate', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        setMessage('System alert evaluation complete');
        setTimeout(() => setMessage(''), 3000);
        fetchAlertsData();
      }
    } catch (err) {
      console.error('Error evaluating alerts:', err);
    } finally {
      setLoading(false);
    }
  };

  const filteredAlerts = alerts.filter(alert => {
    if (filterSeverity !== 'ALL' && alert.severity !== filterSeverity) return false;
    return true;
  });

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <div>
          <h2>🔔 Business Alerts & Notifications</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            Real-time automated detection for stockouts, low stock, overdue purchase orders, and demand anomalies.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn btn-secondary" onClick={handleEvaluate} disabled={loading}>
            🔄 Scan & Refresh
          </button>
          {summary.totalUnread > 0 && (
            <button className="btn btn-danger" onClick={handleDismissAll}>
              🧹 Dismiss All
            </button>
          )}
        </div>
      </div>

      {message && (
        <div style={{ padding: '10px 16px', background: 'rgba(34, 197, 94, 0.15)', border: '1px solid #22c55e', color: '#4ade80', borderRadius: '8px', marginBottom: '16px' }}>
          {message}
        </div>
      )}

      {/* Summary KPI Bar */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '12px', marginBottom: '20px' }}>
        <div className="glass-panel" style={{ padding: '12px', textAlign: 'center', borderColor: summary.totalUnread > 0 ? '#ef4444' : 'var(--glass-border)' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', textTransform: 'uppercase' }}>Unread Alerts</div>
          <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: summary.totalUnread > 0 ? '#ef4444' : 'var(--text-primary)' }}>{summary.totalUnread}</div>
        </div>
        <div className="glass-panel" style={{ padding: '12px', textAlign: 'center' }}>
          <div style={{ fontSize: '0.75rem', color: '#ef4444', textTransform: 'uppercase' }}>Critical</div>
          <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#ef4444' }}>{summary.criticalCount}</div>
        </div>
        <div className="glass-panel" style={{ padding: '12px', textAlign: 'center' }}>
          <div style={{ fontSize: '0.75rem', color: '#f97316', textTransform: 'uppercase' }}>High</div>
          <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#f97316' }}>{summary.highCount}</div>
        </div>
        <div className="glass-panel" style={{ padding: '12px', textAlign: 'center' }}>
          <div style={{ fontSize: '0.75rem', color: '#eab308', textTransform: 'uppercase' }}>Medium</div>
          <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#eab308' }}>{summary.mediumCount}</div>
        </div>
        <div className="glass-panel" style={{ padding: '12px', textAlign: 'center' }}>
          <div style={{ fontSize: '0.75rem', color: '#3b82f6', textTransform: 'uppercase' }}>Low</div>
          <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#3b82f6' }}>{summary.lowCount}</div>
        </div>
      </div>

      {/* Filter Tabs */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
        <div style={{ display: 'flex', gap: '8px' }}>
          {['ALL', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'].map(sev => (
            <button
              key={sev}
              className={`btn ${filterSeverity === sev ? 'btn-primary' : 'btn-secondary'}`}
              style={{ fontSize: '0.8rem', padding: '6px 12px' }}
              onClick={() => setFilterSeverity(sev)}
            >
              {sev}
            </button>
          ))}
        </div>
        <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.85rem', cursor: 'pointer' }}>
          <input
            type="checkbox"
            checked={unreadOnly}
            onChange={(e) => setUnreadOnly(e.target.checked)}
          />
          Show Unread Only
        </label>
      </div>

      {/* Alerts List */}
      {loading ? (
        <div className="glass-panel" style={{ padding: '30px', textAlign: 'center' }}>Loading active alerts...</div>
      ) : filteredAlerts.length === 0 ? (
        <div className="glass-panel" style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>
          ✅ No active alerts found matching the current filters. Your inventory system is in good health!
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          {filteredAlerts.map(alert => {
            const color = SEVERITY_COLORS[alert.severity] || SEVERITY_COLORS.LOW;
            const icon = TYPE_ICONS[alert.alertType] || '🔔';

            return (
              <div
                key={alert.id}
                className="glass-panel"
                style={{
                  padding: '16px',
                  display: 'flex',
                  justify: 'space-between',
                  alignItems: 'flex-start',
                  borderLeft: `4px solid ${color.border}`,
                  opacity: alert.isRead ? 0.75 : 1
                }}
              >
                <div style={{ display: 'flex', gap: '14px', alignItems: 'flex-start' }}>
                  <div style={{ fontSize: '1.8rem', lineHeight: 1 }}>{icon}</div>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '4px' }}>
                      <h4 style={{ margin: 0, fontSize: '1rem', color: alert.isRead ? 'var(--text-secondary)' : 'var(--text-primary)' }}>
                        {alert.title}
                      </h4>
                      <span
                        style={{
                          fontSize: '0.7rem',
                          padding: '2px 8px',
                          borderRadius: '12px',
                          fontWeight: 600,
                          background: color.bg,
                          color: color.text,
                          border: `1px solid ${color.border}`
                        }}
                      >
                        {alert.severity}
                      </span>
                      {!alert.isRead && (
                        <span style={{ fontSize: '0.7rem', color: '#ef4444', fontWeight: 'bold' }}>● UNREAD</span>
                      )}
                    </div>
                    <p style={{ margin: '0 0 8px 0', fontSize: '0.88rem', color: 'var(--text-secondary)' }}>
                      {alert.message}
                    </p>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', display: 'flex', gap: '12px' }}>
                      <span>Category: {alert.alertType}</span>
                      <span>Target: {alert.entityType} #{alert.entityId}</span>
                      <span>{new Date(alert.createdAt).toLocaleString()}</span>
                    </div>
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                  {!alert.isRead && (
                    <button
                      className="btn btn-secondary"
                      style={{ fontSize: '0.75rem', padding: '4px 10px' }}
                      onClick={() => handleMarkAsRead(alert.id)}
                    >
                      ✓ Read
                    </button>
                  )}
                  <button
                    className="btn btn-danger"
                    style={{ fontSize: '0.75rem', padding: '4px 10px' }}
                    onClick={() => handleDismiss(alert.id)}
                  >
                    Dismiss
                  </button>
                  {onNavigate && alert.entityType === 'PRODUCT' && (
                    <button
                      className="btn btn-primary"
                      style={{ fontSize: '0.75rem', padding: '4px 10px' }}
                      onClick={() => onNavigate('purchase_orders')}
                    >
                      Reorder
                    </button>
                  )}
                  {onNavigate && alert.entityType === 'PURCHASE_ORDER' && (
                    <button
                      className="btn btn-primary"
                      style={{ fontSize: '0.75rem', padding: '4px 10px' }}
                      onClick={() => onNavigate('purchase_orders')}
                    >
                      View PO
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
