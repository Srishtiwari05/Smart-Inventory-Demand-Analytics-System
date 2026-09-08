import { useState, useEffect } from 'react';
import OverviewView from './views/OverviewView';
import ProductsView from './views/ProductsView';
import OrdersView from './views/OrdersView';
import AnalyticsView from './views/AnalyticsView';
import CustomersView from './views/CustomersView';
import SuppliersView from './views/SuppliersView';
import TransactionsView from './views/TransactionsView';
import PurchaseOrdersView from './views/PurchaseOrdersView';
import AlertsView from './views/AlertsView';
import SupplierPortalView from './views/SupplierPortalView';

const ROLE_BADGE = {
  OWNER: { bg: 'rgba(239,68,68,0.2)', color: '#f87171', label: 'Owner' },
  ADMIN: { bg: 'rgba(239,68,68,0.2)', color: '#f87171', label: 'Owner' },
  MANAGER: { bg: 'rgba(139,92,246,0.2)', color: '#a78bfa', label: 'Manager' },
  STAFF: { bg: 'rgba(34,197,94,0.2)', color: '#4ade80', label: 'Staff' },
  SUPPLIER: { bg: 'rgba(234,179,8,0.2)', color: '#eab308', label: 'Supplier' }
};

export default function Dashboard({ user, onLogout }) {
  const role = user?.role;
  const isOwner = role === 'OWNER' || role === 'ADMIN';
  const isManager = isOwner || role === 'MANAGER';
  const badge = ROLE_BADGE[role] || ROLE_BADGE.STAFF;

  const [activeTab, setActiveTab] = useState(isManager ? 'overview' : 'products');
  const [unreadAlerts, setUnreadAlerts] = useState(0);

  if (role === 'SUPPLIER') {
    return (
      <div className="app-container">
        <nav className="sidebar glass-panel">
          <div className="brand">🐴 Supplier Portal</div>
          <div style={{ marginTop: 'auto' }}>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>
              {user.username}
            </div>
            <span style={{ display: 'inline-block', padding: '3px 10px', borderRadius: '20px', fontSize: '0.75rem', fontWeight: 600, background: ROLE_BADGE.SUPPLIER.bg, color: ROLE_BADGE.SUPPLIER.color, border: `1px solid ${ROLE_BADGE.SUPPLIER.color}`, marginBottom: '12px' }}>
              Supplier
            </span>
            <button className="btn btn-danger" style={{ width: '100%' }} onClick={onLogout}>
              Logout
            </button>
          </div>
        </nav>
        <main className="main-content">
          <SupplierPortalView user={user} />
        </main>
      </div>
    );
  }

  const fetchAlertCount = async () => {
    try {
      const token = localStorage.getItem('token');
      const res = await fetch('/api/alerts/summary', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        setUnreadAlerts(data.totalUnread || 0);
      }
    } catch (err) {
      console.error('Error fetching alert count:', err);
    }
  };

  useEffect(() => {
    fetchAlertCount();
    const interval = setInterval(fetchAlertCount, 15000);
    return () => clearInterval(interval);
  }, [activeTab]);

  const tabs = [
    { id: 'overview',        icon: '🏠', label: 'Overview',        show: isManager },
    { id: 'products',        icon: '📦', label: 'Products',        show: true },
    { id: 'orders',          icon: '🛒', label: 'Orders',          show: true },
    { id: 'purchase_orders', icon: '📝', label: 'Purchase Orders', show: isManager },
    { id: 'alerts',          icon: '🔔', label: 'Alerts',          show: true, badge: unreadAlerts },
    { id: 'customers',       icon: '👥', label: 'Customers',       show: true },
    { id: 'analytics',       icon: '📈', label: 'Analytics',       show: isManager },
    { id: 'suppliers',       icon: '🚚', label: 'Suppliers',       show: isManager },
    { id: 'transactions',    icon: '🔄', label: 'Transactions',    show: isManager },
  ].filter(t => t.show);

  return (
    <div className="app-container">
      <nav className="sidebar glass-panel">
        <div className="brand">🐴 Smart Inventory</div>

        <div className="nav-links">
          {tabs.map(tab => (
            <button
              key={tab.id}
              className={`nav-item ${activeTab === tab.id ? 'active' : ''}`}
              onClick={() => setActiveTab(tab.id)}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%' }}>
                <span>{tab.icon} {tab.label}</span>
                {tab.badge > 0 && (
                  <span
                    style={{
                      background: '#ef4444',
                      color: '#ffffff',
                      fontSize: '0.7rem',
                      fontWeight: 'bold',
                      padding: '2px 7px',
                      borderRadius: '10px'
                    }}
                  >
                    {tab.badge}
                  </span>
                )}
              </div>
            </button>
          ))}
        </div>

        <div style={{ marginTop: 'auto' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>
            {user.username}
          </div>
          <span style={{ display: 'inline-block', padding: '3px 10px', borderRadius: '20px', fontSize: '0.75rem', fontWeight: 600, background: badge.bg, color: badge.color, border: `1px solid ${badge.color}`, marginBottom: '12px' }}>
            {badge.label}
          </span>
          <button className="btn btn-danger" style={{ width: '100%' }} onClick={onLogout}>
            Logout
          </button>
        </div>
      </nav>

      <main className="main-content">
        {activeTab === 'overview'        && <OverviewView        user={user} onNavigate={setActiveTab} />}
        {activeTab === 'products'        && <ProductsView        user={user} />}
        {activeTab === 'orders'          && <OrdersView          user={user} />}
        {activeTab === 'purchase_orders' && <PurchaseOrdersView user={user} />}
        {activeTab === 'alerts'          && <AlertsView          user={user} onNavigate={setActiveTab} />}
        {activeTab === 'customers'       && <CustomersView       user={user} />}
        {activeTab === 'analytics'       && <AnalyticsView       user={user} />}
        {activeTab === 'suppliers'       && <SuppliersView       user={user} />}
        {activeTab === 'transactions'    && <TransactionsView    user={user} />}
      </main>
    </div>
  );
}
