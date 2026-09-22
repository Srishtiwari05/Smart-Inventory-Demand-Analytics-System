import { useState, useEffect } from 'react';

export default function OverviewView({ user, onNavigate, onLogout }) {
  const [kpi, setKpi] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchKpis = async () => {
    setLoading(true);
    setError(null);
    try {
      const token = localStorage.getItem('token');
      const res = await fetch('/api/analytics/kpi', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        setKpi(data);
      } else if (res.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('rememberedUser');
        setError('Session Expired (Status 401)');
      } else {
        setError(`Failed to load KPIs (Status ${res.status})`);
      }
    } catch (err) {
      console.error('Error fetching operational KPIs:', err);
      setError('Network error while loading operational KPIs.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchKpis();
  }, []);

  const formatCurrency = (amount) => {
    if (amount === undefined || amount === null) return '$0.00';
    return `$${Number(amount).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  const formatNumber = (num) => {
    if (num === undefined || num === null) return '0';
    return Number(num).toLocaleString('en-US');
  };

  if (loading) {
    return (
      <div style={{ padding: '32px 0', display: 'flex', flexDirection: 'column', gap: '24px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h1 style={{ fontSize: '1.875rem', fontWeight: 700, margin: 0 }}>Operational Dashboard</h1>
            <p style={{ color: 'var(--text-secondary)', marginTop: '4px' }}>Loading real-time operational KPIs...</p>
          </div>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '16px' }}>
          {[1, 2, 3, 4, 5, 6, 7, 8].map((i) => (
            <div key={i} className="glass-panel" style={{ height: '110px', padding: '20px' }}>
              <div style={{ height: '16px', width: '60%', background: 'rgba(255,255,255,0.06)', borderRadius: '4px', marginBottom: '12px' }}></div>
              <div style={{ height: '28px', width: '40%', background: 'rgba(255,255,255,0.1)', borderRadius: '4px' }}></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    const is401 = error.includes('401') || error.includes('Expired');
    return (
      <div style={{ padding: '32px 0' }}>
        <div className="glass-panel" style={{ padding: '28px', borderColor: 'rgba(239, 68, 68, 0.4)', background: 'rgba(239, 68, 68, 0.04)', borderRadius: '12px' }}>
          <h3 style={{ margin: '0 0 8px 0', color: '#f87171', fontSize: '1.2rem' }}>
            {is401 ? 'Session Expired (Status 401)' : 'Error Loading Operational Metrics'}
          </h3>
          <p style={{ margin: 0, color: 'var(--text-secondary)', fontSize: '0.95rem', lineHeight: '1.5' }}>
            {is401
              ? 'Your active session token has expired or the backend server was restarted. Please log in again to authenticate.'
              : error}
          </p>
          <div style={{ display: 'flex', gap: '12px', marginTop: '20px' }}>
            {is401 && onLogout ? (
              <button
                onClick={onLogout}
                style={{
                  padding: '10px 22px',
                  background: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
                  color: '#fff',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: 'pointer',
                  fontWeight: 600,
                  fontSize: '0.9rem',
                  boxShadow: '0 4px 14px rgba(99, 102, 241, 0.35)'
                }}
              >
                Log In Again
              </button>
            ) : (
              <button
                onClick={fetchKpis}
                style={{
                  padding: '10px 20px',
                  background: 'var(--accent-primary)',
                  color: '#fff',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: 'pointer',
                  fontWeight: 600
                }}
              >
                Retry Connection
              </button>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ paddingBottom: '32px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 700, margin: 0 }}>
            Operational Dashboard
          </h1>
          <p style={{ color: 'var(--text-secondary)', marginTop: '4px', fontSize: '0.95rem' }}>
            Real-time operational summary, inventory risks, and procurement pipeline for {user?.orgName || 'your organization'}.
          </p>
        </div>
        <button
          onClick={fetchKpis}
          className="glass-panel"
          style={{
            padding: '10px 18px',
            color: 'var(--text-primary)',
            background: 'rgba(255, 255, 255, 0.05)',
            border: '1px solid var(--panel-border)',
            borderRadius: '8px',
            cursor: 'pointer',
            fontWeight: 500,
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            transition: 'var(--transition)'
          }}
        >
          Refresh Metrics
        </button>
      </div>

      {/* Row 1: Key Performance Indicators */}
      <div>
        <h2 style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '12px', letterSpacing: '0.03em' }}>
          OVERVIEW & REVENUE (30D)
        </h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(230px, 1fr))', gap: '16px' }}>
          
          <div className="glass-panel" style={{ padding: '20px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', fontWeight: 500 }}>Total Inventory Value</span>
            </div>
            <div style={{ marginTop: '12px' }}>
              <div style={{ fontSize: '1.65rem', fontWeight: 700, color: 'var(--text-primary)' }}>
                {formatCurrency(kpi?.totalInventoryValue)}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Valuation across all products</span>
            </div>
          </div>

          <div
            className="glass-panel"
            onClick={() => onNavigate && onNavigate('products')}
            style={{
              padding: '20px',
              display: 'flex',
              flexDirection: 'column',
              justify: 'space-between',
              cursor: onNavigate ? 'pointer' : 'default',
              transition: 'var(--transition)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', fontWeight: 500 }}>Active Products</span>
            </div>
            <div style={{ marginTop: '12px' }}>
              <div style={{ fontSize: '1.65rem', fontWeight: 700, color: 'var(--accent-primary)' }}>
                {formatNumber(kpi?.totalProducts)}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Catalog size</span>
            </div>
          </div>

          <div className="glass-panel" style={{ padding: '20px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', fontWeight: 500 }}>30-Day Sales Revenue</span>
            </div>
            <div style={{ marginTop: '12px' }}>
              <div style={{ fontSize: '1.65rem', fontWeight: 700, color: 'var(--success)' }}>
                {formatCurrency(kpi?.totalRevenue30d)}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Completed order sales</span>
            </div>
          </div>

          <div
            className="glass-panel"
            onClick={() => onNavigate && onNavigate('orders')}
            style={{
              padding: '20px',
              display: 'flex',
              flexDirection: 'column',
              justify: 'space-between',
              cursor: onNavigate ? 'pointer' : 'default',
              transition: 'var(--transition)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', fontWeight: 500 }}>30-Day Orders Count</span>
            </div>
            <div style={{ marginTop: '12px' }}>
              <div style={{ fontSize: '1.65rem', fontWeight: 700, color: 'var(--text-primary)' }}>
                {formatNumber(kpi?.totalOrders30d)}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Total orders placed</span>
            </div>
          </div>

        </div>
      </div>

      {/* Row 2: Inventory Risk & Alerts */}
      <div>
        <h2 style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '12px', letterSpacing: '0.03em' }}>
          INVENTORY RISK & ALERTS
        </h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(230px, 1fr))', gap: '16px' }}>

          <div
            className="glass-panel"
            onClick={() => onNavigate && onNavigate('analytics')}
            style={{
              padding: '20px',
              borderLeft: '4px solid var(--danger)',
              cursor: onNavigate ? 'pointer' : 'default',
              transition: 'var(--transition)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', fontWeight: 500 }}>Critical Stock Items</span>
            </div>
            <div style={{ marginTop: '12px' }}>
              <div style={{ fontSize: '1.65rem', fontWeight: 700, color: (kpi?.criticalStockCount || 0) > 0 ? 'var(--danger)' : 'var(--text-primary)' }}>
                {formatNumber(kpi?.criticalStockCount)}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>At or below safety stock</span>
            </div>
          </div>

          <div
            className="glass-panel"
            onClick={() => onNavigate && onNavigate('products')}
            style={{
              padding: '20px',
              borderLeft: '4px solid var(--warning)',
              cursor: onNavigate ? 'pointer' : 'default',
              transition: 'var(--transition)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', fontWeight: 500 }}>Low Stock / Reorder</span>
            </div>
            <div style={{ marginTop: '12px' }}>
              <div style={{ fontSize: '1.65rem', fontWeight: 700, color: (kpi?.lowStockCount || 0) > 0 ? 'var(--warning)' : 'var(--text-primary)' }}>
                {formatNumber(kpi?.lowStockCount)}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Below reorder point</span>
            </div>
          </div>

          <div
            className="glass-panel"
            onClick={() => onNavigate && onNavigate('analytics')}
            style={{
              padding: '20px',
              borderLeft: '4px solid #eab308',
              cursor: onNavigate ? 'pointer' : 'default',
              transition: 'var(--transition)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', fontWeight: 500 }}>Stockout in ≤7 Days</span>
            </div>
            <div style={{ marginTop: '12px' }}>
              <div style={{ fontSize: '1.65rem', fontWeight: 700, color: (kpi?.expectedStockoutsCount || 0) > 0 ? '#eab308' : 'var(--text-primary)' }}>
                {formatNumber(kpi?.expectedStockoutsCount)}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>High risk of run-out</span>
            </div>
          </div>

          <div
            className="glass-panel"
            onClick={() => onNavigate && onNavigate('alerts')}
            style={{
              padding: '20px',
              borderLeft: '4px solid #8b5cf6',
              cursor: onNavigate ? 'pointer' : 'default',
              transition: 'var(--transition)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', fontWeight: 500 }}>Unread Alerts</span>
            </div>
            <div style={{ marginTop: '12px' }}>
              <div style={{ fontSize: '1.65rem', fontWeight: 700, color: (kpi?.unreadAlertsCount || 0) > 0 ? '#8b5cf6' : 'var(--text-primary)' }}>
                {formatNumber(kpi?.unreadAlertsCount)}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Requires attention</span>
            </div>
          </div>

        </div>
      </div>

      {/* Row 3: Procurement & Approvals */}
      <div>
        <h2 style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '12px', letterSpacing: '0.03em' }}>
          PROCUREMENT PIPELINE
        </h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px' }}>

          <div
            className="glass-panel"
            onClick={() => onNavigate && onNavigate('purchase_orders')}
            style={{
              padding: '20px',
              borderLeft: (kpi?.overduePOCount || 0) > 0 ? '4px solid var(--danger)' : '4px solid var(--panel-border)',
              cursor: onNavigate ? 'pointer' : 'default',
              transition: 'var(--transition)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', fontWeight: 500 }}>Overdue Purchase Orders</span>
            </div>
            <div style={{ marginTop: '12px' }}>
              <div style={{ fontSize: '1.65rem', fontWeight: 700, color: (kpi?.overduePOCount || 0) > 0 ? 'var(--danger)' : 'var(--text-primary)' }}>
                {formatNumber(kpi?.overduePOCount)}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                Total value: {formatCurrency(kpi?.overduePOValue)}
              </span>
            </div>
          </div>

          <div
            className="glass-panel"
            onClick={() => onNavigate && onNavigate('purchase_orders')}
            style={{
              padding: '20px',
              cursor: onNavigate ? 'pointer' : 'default',
              transition: 'var(--transition)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', fontWeight: 500 }}>Pending Purchase Orders</span>
            </div>
            <div style={{ marginTop: '12px' }}>
              <div style={{ fontSize: '1.65rem', fontWeight: 700, color: 'var(--accent-primary)' }}>
                {formatNumber(kpi?.pendingPOCount)}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>In-flight / awaiting delivery</span>
            </div>
          </div>

          <div
            className="glass-panel"
            onClick={() => onNavigate && onNavigate('purchase_orders')}
            style={{
              padding: '20px',
              cursor: onNavigate ? 'pointer' : 'default',
              transition: 'var(--transition)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', fontWeight: 500 }}>Pending PR Approvals</span>
            </div>
            <div style={{ marginTop: '12px' }}>
              <div style={{ fontSize: '1.65rem', fontWeight: 700, color: 'var(--accent-secondary)' }}>
                {formatNumber(kpi?.pendingApprovalsCount)}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Requisitions awaiting review</span>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
