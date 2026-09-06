import { useState } from 'react';
import ProductsView from './views/ProductsView';
import OrdersView from './views/OrdersView';
import AnalyticsView from './views/AnalyticsView';
import CustomersView from './views/CustomersView';
import SuppliersView from './views/SuppliersView';
import TransactionsView from './views/TransactionsView';
import PurchaseOrdersView from './views/PurchaseOrdersView';

const ROLE_BADGE = {
  OWNER: { bg: 'rgba(239,68,68,0.2)', color: '#f87171', label: 'Owner' },
  ADMIN: { bg: 'rgba(239,68,68,0.2)', color: '#f87171', label: 'Owner' },
  MANAGER: { bg: 'rgba(139,92,246,0.2)', color: '#a78bfa', label: 'Manager' },
  STAFF: { bg: 'rgba(34,197,94,0.2)', color: '#4ade80', label: 'Staff' },
};

export default function Dashboard({ user, onLogout }) {
  const [activeTab, setActiveTab] = useState('products');
  const role = user?.role;
  const isOwner = role === 'OWNER' || role === 'ADMIN';
  const isManager = isOwner || role === 'MANAGER';
  const badge = ROLE_BADGE[role] || ROLE_BADGE.STAFF;

  const tabs = [
    { id: 'products',        icon: '📦', label: 'Products',        show: true },
    { id: 'orders',          icon: '🛒', label: 'Orders',          show: true },
    { id: 'purchase_orders', icon: '📝', label: 'Purchase Orders', show: isManager },
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
              {tab.icon} {tab.label}
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
        {activeTab === 'products'        && <ProductsView        user={user} />}
        {activeTab === 'orders'          && <OrdersView          user={user} />}
        {activeTab === 'purchase_orders' && <PurchaseOrdersView user={user} />}
        {activeTab === 'analytics'       && <AnalyticsView       user={user} />}
        {activeTab === 'customers'       && <CustomersView       user={user} />}
        {activeTab === 'suppliers'       && <SuppliersView       user={user} />}
        {activeTab === 'transactions'    && <TransactionsView    user={user} />}
      </main>
    </div>
  );
}
