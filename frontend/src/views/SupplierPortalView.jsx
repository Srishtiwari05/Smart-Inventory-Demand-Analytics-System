import { useState, useEffect } from 'react';

export default function SupplierPortalView({ user }) {
  const [activeSubTab, setActiveSubTab] = useState('requests');
  const [requests, setRequests] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  // Quote Submission Modal State
  const [selectedPR, setSelectedPR] = useState(null);
  const [quotePrice, setQuotePrice] = useState('');
  const [quoteQty, setQuoteQty] = useState('');
  const [quoteDeliveryDate, setQuoteDeliveryDate] = useState('');
  const [quoteNotes, setQuoteNotes] = useState('');
  const [message, setMessage] = useState('');

  const token = localStorage.getItem('token');

  const fetchData = async () => {
    setLoading(true);
    try {
      if (activeSubTab === 'requests') {
        const res = await fetch('/api/supplier-portal/requests', {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        if (res.ok) {
          const data = await res.json();
          setRequests(data);
        }
      } else if (activeSubTab === 'orders') {
        const res = await fetch('/api/supplier-portal/orders', {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        if (res.ok) {
          const data = await res.json();
          setOrders(data);
        }
      }
    } catch (err) {
      console.error('Error fetching supplier portal data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [activeSubTab]);

  const handleOpenQuoteModal = (pr) => {
    setSelectedPR(pr);
    // Auto-calculate suggested quantity
    const totalQty = pr.items ? pr.items.reduce((sum, item) => sum + item.quantity, 0) : 10;
    const avgEstPrice = pr.items && pr.items.length > 0 ? pr.items[0].estimatedUnitCost || 25.0 : 25.0;

    setQuoteQty(totalQty);
    setQuotePrice(avgEstPrice);
    setQuoteDeliveryDate(new Date(Date.now() + 5 * 86400000).toISOString().split('T')[0]);
    setQuoteNotes('Standard delivery guaranteed.');
  };

  const handleSubmitQuote = async (e) => {
    e.preventDefault();
    if (!selectedPR) return;

    try {
      const payload = {
        purchaseRequestId: selectedPR.id,
        quotedUnitPrice: parseFloat(quotePrice),
        availableQuantity: parseInt(quoteQty, 10),
        promisedDeliveryDate: quoteDeliveryDate,
        notes: quoteNotes
      };

      const res = await fetch('/api/supplier-portal/quotations', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        setMessage(`Quotation submitted successfully for PR #${selectedPR.requestNumber || selectedPR.id}`);
        setTimeout(() => setMessage(''), 3000);
        setSelectedPR(null);
        fetchData();
      } else {
        const errText = await res.text();
        alert('Failed to submit quote: ' + errText);
      }
    } catch (err) {
      console.error('Submit quote error:', err);
    }
  };

  const handleMarkShipped = async (poId) => {
    try {
      const res = await fetch(`/api/supplier-portal/orders/${poId}/ship`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        setMessage(`Purchase Order #${poId} marked as SHIPPED!`);
        setTimeout(() => setMessage(''), 3000);
        fetchData();
      }
    } catch (err) {
      console.error('Ship order error:', err);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <div>
          <h2>🏬 Supplier Portal Dashboard</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            Welcome, <strong>{user?.username}</strong>. View assigned Requests for Quotation (RFQs), submit prices, and update order shipping status.
          </p>
        </div>
      </div>

      {message && (
        <div style={{ padding: '10px 16px', background: 'rgba(34, 197, 94, 0.15)', border: '1px solid #22c55e', color: '#4ade80', borderRadius: '8px', marginBottom: '16px' }}>
          {message}
        </div>
      )}

      {/* Navigation Sub-Tabs */}
      <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
        <button
          className={`btn ${activeSubTab === 'requests' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setActiveSubTab('requests')}
        >
          📋 Assigned Requests ({requests.length})
        </button>
        <button
          className={`btn ${activeSubTab === 'orders' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setActiveSubTab('orders')}
        >
          📦 Purchase Orders ({orders.length})
        </button>
      </div>

      {/* Tab Content */}
      {loading ? (
        <div className="glass-panel" style={{ padding: '30px', textAlign: 'center' }}>Loading supplier portal data...</div>
      ) : activeSubTab === 'requests' ? (
        requests.length === 0 ? (
          <div className="glass-panel" style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>
            No purchase requests assigned to your vendor account at this time.
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            {requests.map(pr => (
              <div key={pr.id} className="glass-panel" style={{ padding: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
                    <h4 style={{ margin: 0 }}>Request #{pr.requestNumber || pr.id}</h4>
                    <span style={{ fontSize: '0.75rem', padding: '2px 8px', borderRadius: '12px', background: 'rgba(59,130,246,0.15)', color: '#3b82f6', border: '1px solid #3b82f6' }}>
                      {pr.status}
                    </span>
                  </div>
                  <p style={{ margin: '0 0 8px 0', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                    Requested on {pr.createdAt ? new Date(pr.createdAt).toLocaleDateString() : 'Today'}
                  </p>
                  <div style={{ fontSize: '0.85rem' }}>
                    <strong>Items Requested:</strong>
                    <ul style={{ margin: '4px 0 0 18px', padding: 0 }}>
                      {pr.items && pr.items.map((item, idx) => (
                        <li key={idx}>
                          {item.productName || `Product #${item.productId}`} — Quantity: <strong>{item.quantity}</strong> (Est. ${item.estimatedUnitCost})
                        </li>
                      ))}
                    </ul>
                  </div>
                </div>

                <div>
                  <button
                    className="btn btn-primary"
                    style={{ fontSize: '0.85rem' }}
                    onClick={() => handleOpenQuoteModal(pr)}
                  >
                    💬 Submit Quotation
                  </button>
                </div>
              </div>
            ))}
          </div>
        )
      ) : (
        orders.length === 0 ? (
          <div className="glass-panel" style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>
            No active purchase orders found for your vendor account.
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            {orders.map(po => (
              <div key={po.id} className="glass-panel" style={{ padding: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '4px' }}>
                    <h4 style={{ margin: 0 }}>PO #{po.poNumber}</h4>
                    <span style={{
                      fontSize: '0.75rem',
                      padding: '2px 8px',
                      borderRadius: '12px',
                      fontWeight: 'bold',
                      background: po.status === 'SHIPPED' ? 'rgba(34,197,94,0.15)' : 'rgba(234,179,8,0.15)',
                      color: po.status === 'SHIPPED' ? '#4ade80' : '#eab308',
                      border: `1px solid ${po.status === 'SHIPPED' ? '#4ade80' : '#eab308'}`
                    }}>
                      {po.status}
                    </span>
                  </div>
                  <p style={{ margin: '0 0 4px 0', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                    Total Value: <strong>${po.totalCost?.toFixed(2)}</strong> | Expected Delivery: <strong>{po.expectedDeliveryDate || 'N/A'}</strong>
                  </p>
                </div>

                <div>
                  {po.status !== 'SHIPPED' && po.status !== 'RECEIVED' && po.status !== 'COMPLETED' ? (
                    <button
                      className="btn btn-primary"
                      onClick={() => handleMarkShipped(po.id)}
                    >
                      🚚 Mark as SHIPPED
                    </button>
                  ) : (
                    <span style={{ fontSize: '0.85rem', color: '#4ade80', fontWeight: 'bold' }}>✓ {po.status}</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        )
      )}

      {/* Quote Submission Modal */}
      {selectedPR && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.7)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000
        }}>
          <div className="glass-panel" style={{ width: '450px', padding: '24px', background: '#1e1b4b', border: '1px solid #6366f1' }}>
            <h3 style={{ marginTop: 0, marginBottom: '14px' }}>Submit Quotation for PR #{selectedPR.requestNumber || selectedPR.id}</h3>
            <form onSubmit={handleSubmitQuote} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <div>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Quoted Unit Price ($)</label>
                <input
                  type="number" step="0.01" required
                  className="input-field"
                  value={quotePrice}
                  onChange={(e) => setQuotePrice(e.target.value)}
                  style={{ width: '100%', marginTop: '4px' }}
                />
              </div>

              <div>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Available Quantity</label>
                <input
                  type="number" required
                  className="input-field"
                  value={quoteQty}
                  onChange={(e) => setQuoteQty(e.target.value)}
                  style={{ width: '100%', marginTop: '4px' }}
                />
              </div>

              <div>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Promised Delivery Date</label>
                <input
                  type="date" required
                  className="input-field"
                  value={quoteDeliveryDate}
                  onChange={(e) => setQuoteDeliveryDate(e.target.value)}
                  style={{ width: '100%', marginTop: '4px' }}
                />
              </div>

              <div>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Notes / Terms</label>
                <textarea
                  className="input-field"
                  value={quoteNotes}
                  onChange={(e) => setQuoteNotes(e.target.value)}
                  style={{ width: '100%', marginTop: '4px', height: '60px' }}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
                <button type="button" className="btn btn-secondary" onClick={() => setSelectedPR(null)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Submit Quote</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
