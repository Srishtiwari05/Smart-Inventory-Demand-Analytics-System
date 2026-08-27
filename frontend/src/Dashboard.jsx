import { useState } from 'react';
import ProductsView from './views/ProductsView';
import OrdersView from './views/OrdersView';
import AnalyticsView from './views/AnalyticsView';

export default function Dashboard({ user, onLogout }) {
  const [activeTab, setActiveTab] = useState('products');

  return (
    <div className="app-container">
      {/* Sidebar Navigation */}
      <nav className="sidebar glass-panel">
        <div className="brand">
          🐴 Smart Inventory
        </div>
        
        <div className="nav-links">
          <button 
            className={`nav-item ${activeTab === 'products' ? 'active' : ''}`}
            onClick={() => setActiveTab('products')}
          >
            📦 Products
          </button>
          
          <button 
            className={`nav-item ${activeTab === 'orders' ? 'active' : ''}`}
            onClick={() => setActiveTab('orders')}
          >
            🛒 Orders
          </button>
          
          <button 
            className={`nav-item ${activeTab === 'analytics' ? 'active' : ''}`}
            onClick={() => setActiveTab('analytics')}
          >
            📈 Analytics
          </button>
        </div>

        <div style={{ marginTop: 'auto' }}>
          <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '12px' }}>
            Logged in as <strong>{user.username}</strong> ({user.role})
          </div>
          <button className="btn btn-danger" style={{width: '100%'}} onClick={onLogout}>
            Logout
          </button>
        </div>
      </nav>

      {/* Main Content */}
      <main className="main-content">
        {activeTab === 'products' && <ProductsView />}
        {activeTab === 'orders' && <OrdersView />}
        {activeTab === 'analytics' && <AnalyticsView />}
      </main>
    </div>
  );
}
