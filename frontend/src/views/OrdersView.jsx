import { useState, useEffect } from 'react';

// Parses "Order #5 | Customer: Alice | Total: $120.00 | Date: 2026-08-25 12:00:00.0"
function parseOrder(str, idx) {
  try {
    const idMatch = str.match(/Order #(\d+)/);
    const custMatch = str.match(/Customer: ([^|]+)/);
    const totalMatch = str.match(/Total: \$([0-9.]+)/);
    const dateMatch = str.match(/Date: (.+)$/);
    return {
      id: idMatch ? idMatch[1] : idx,
      customer: custMatch ? custMatch[1].trim() : '—',
      total: totalMatch ? parseFloat(totalMatch[1]) : 0,
      date: dateMatch ? dateMatch[1].trim() : '—',
    };
  } catch {
    return { id: idx, customer: '—', total: 0, date: '—' };
  }
}

export default function OrdersView() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [order, setOrder] = useState({ customerId: '', productId: '', quantity: '' });
  const [msg, setMsg] = useState(null);

  useEffect(() => { fetchOrders(); }, []);

  const fetchOrders = async () => {
    try {
      const res = await fetch('/api/orders', {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
      });
      setOrders(await res.json());
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handlePlaceOrder = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch('/api/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') },
        body: JSON.stringify({
          customerId: parseInt(order.customerId),
          productIds: [parseInt(order.productId)],
          quantities: [parseInt(order.quantity)]
        })
      });
      if (!res.ok) throw new Error();
      setMsg({ type: 'success', text: 'Order placed successfully!' });
      setShowForm(false);
      setOrder({ customerId: '', productId: '', quantity: '' });
      fetchOrders();
    } catch { setMsg({ type: 'error', text: 'Failed. Check stock & IDs.' }); }
  };

  const parsed = orders.map((o, i) => parseOrder(o, i));

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">🛒 Orders</h1>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : '+ Place Order'}
        </button>
      </div>

      {msg && (
        <div style={{ marginTop: '16px', padding: '12px 16px', borderRadius: '8px', background: msg.type === 'success' ? 'rgba(34,197,94,0.15)' : 'rgba(239,68,68,0.15)', color: msg.type === 'success' ? '#4ade80' : '#f87171', border: `1px solid ${msg.type === 'success' ? '#4ade80' : '#f87171'}` }}>
          {msg.text}
        </div>
      )}

      {showForm && (
        <form onSubmit={handlePlaceOrder} className="glass-panel" style={{ padding: '24px', marginTop: '24px', display: 'flex', gap: '12px', alignItems: 'flex-end', flexWrap: 'wrap' }}>
          <div className="input-group" style={{ marginBottom: 0 }}>
            <label>Customer ID</label>
            <input type="number" required value={order.customerId} onChange={e => setOrder({ ...order, customerId: e.target.value })} />
          </div>
          <div className="input-group" style={{ marginBottom: 0 }}>
            <label>Product ID</label>
            <input type="number" required value={order.productId} onChange={e => setOrder({ ...order, productId: e.target.value })} />
          </div>
          <div className="input-group" style={{ marginBottom: 0 }}>
            <label>Quantity</label>
            <input type="number" required min="1" value={order.quantity} onChange={e => setOrder({ ...order, quantity: e.target.value })} />
          </div>
          <button type="submit" className="btn btn-primary" style={{ height: '46px' }}>Submit</button>
        </form>
      )}

      {loading ? <p style={{ marginTop: '24px' }}>Loading orders...</p> : (
        <div className="table-container glass-panel" style={{ marginTop: '24px' }}>
          <table>
            <thead>
              <tr>
                <th>Order ID</th>
                <th>Customer</th>
                <th>Total</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {parsed.map(o => (
                <tr key={o.id}>
                  <td>#{o.id}</td>
                  <td><strong>{o.customer}</strong></td>
                  <td className="text-success">${o.total.toFixed(2)}</td>
                  <td style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{o.date}</td>
                </tr>
              ))}
              {parsed.length === 0 && (
                <tr><td colSpan="4" style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>No orders found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}


