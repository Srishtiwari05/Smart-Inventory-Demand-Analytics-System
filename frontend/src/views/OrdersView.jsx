import { useState, useEffect } from 'react';

export default function OrdersView() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [order, setOrder] = useState({customerId: '', productId: '', quantity: ''});

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      const res = await fetch('/api/orders', {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
      });
      const data = await res.json();
      setOrders(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handlePlaceOrder = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch('/api/orders', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        body: JSON.stringify({
          customerId: parseInt(order.customerId),
          productIds: [parseInt(order.productId)],
          quantities: [parseInt(order.quantity)]
        })
      });
      if (!res.ok) throw new Error("Failed");
      setShowForm(false);
      setOrder({customerId: '', productId: '', quantity: ''});
      fetchOrders();
    } catch(err) { alert('Error placing order'); }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Recent Orders</h1>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : 'Place Order'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handlePlaceOrder} className="glass-panel" style={{padding: '24px', marginTop: '24px', display: 'flex', gap: '12px', alignItems: 'flex-end'}}>
          <div className="input-group" style={{marginBottom: 0}}>
            <label>Customer ID</label>
            <input type="number" required value={order.customerId} onChange={e => setOrder({...order, customerId: e.target.value})} />
          </div>
          <div className="input-group" style={{marginBottom: 0}}>
            <label>Product ID</label>
            <input type="number" required value={order.productId} onChange={e => setOrder({...order, productId: e.target.value})} />
          </div>
          <div className="input-group" style={{marginBottom: 0}}>
            <label>Quantity</label>
            <input type="number" required value={order.quantity} onChange={e => setOrder({...order, quantity: e.target.value})} />
          </div>
          <button type="submit" className="btn btn-primary" style={{height: '46px'}}>Submit</button>
        </form>
      )}

      {loading ? <p>Loading orders...</p> : (
        <div className="table-container glass-panel" style={{marginTop: '24px'}}>
          <table>
            <thead>
              <tr>
                <th>Summary</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((o, idx) => (
                <tr key={idx}>
                  <td>{o}</td>
                </tr>
              ))}
              {orders.length === 0 && (
                <tr>
                  <td style={{textAlign: 'center'}}>No orders found.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
