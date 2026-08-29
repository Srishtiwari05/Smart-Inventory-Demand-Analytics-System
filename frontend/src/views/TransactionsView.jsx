import { useState, useEffect } from 'react';

export default function TransactionsView() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => { fetchTransactions(); }, []);

  const fetchTransactions = async () => {
    try {
      const res = await fetch('/api/transactions', {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
      });
      setTransactions(await res.json());
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const badgeStyle = (type) => {
    if (type === 'SALE') return { background: 'rgba(239,68,68,0.15)', color: '#f87171', border: '1px solid #f87171' };
    if (type === 'RESTOCK') return { background: 'rgba(34,197,94,0.15)', color: '#4ade80', border: '1px solid #4ade80' };
    return { background: 'rgba(234,179,8,0.15)', color: '#facc15', border: '1px solid #facc15' };
  };

  const filtered = filter === 'ALL' ? transactions : transactions.filter(t => t.transactionType === filter);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">🔄 Inventory Transactions</h1>
        <div style={{ display: 'flex', gap: '8px' }}>
          {['ALL', 'SALE', 'RESTOCK', 'ADJUSTMENT'].map(f => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`btn ${filter === f ? 'btn-primary' : ''}`}
              style={{ padding: '6px 14px', fontSize: '0.8rem', opacity: filter === f ? 1 : 0.6 }}
            >
              {f}
            </button>
          ))}
        </div>
      </div>

      {loading ? <p style={{ marginTop: '24px' }}>Loading transactions...</p> : (
        <div className="table-container glass-panel" style={{ marginTop: '24px' }}>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Product ID</th>
                <th>Type</th>
                <th>Quantity</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(t => (
                <tr key={t.id}>
                  <td>#{t.id}</td>
                  <td>Product #{t.productId}</td>
                  <td>
                    <span className="badge" style={badgeStyle(t.transactionType)}>
                      {t.transactionType}
                    </span>
                  </td>
                  <td>
                    <span style={{ color: t.transactionType === 'SALE' ? '#f87171' : '#4ade80', fontWeight: 600 }}>
                      {t.transactionType === 'SALE' ? '−' : '+'}{Math.abs(t.quantityChanged)}
                    </span>
                  </td>
                  <td style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
                    {t.transactionDate ? new Date(t.transactionDate).toLocaleString() : '—'}
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr><td colSpan="5" style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>No transactions found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
