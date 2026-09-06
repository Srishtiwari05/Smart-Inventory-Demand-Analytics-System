import { useState, useEffect } from 'react';

const PO_STATUS_BADGES = {
  DRAFT: { bg: 'rgba(148,163,184,0.15)', color: '#94a3b8', border: '#94a3b8' },
  SUBMITTED: { bg: 'rgba(59,130,246,0.15)', color: '#60a5fa', border: '#60a5fa' },
  CONFIRMED: { bg: 'rgba(168,85,247,0.15)', color: '#c084fc', border: '#c084fc' },
  SHIPPED: { bg: 'rgba(234,179,8,0.15)', color: '#facc15', border: '#facc15' },
  RECEIVED: { bg: 'rgba(34,197,94,0.15)', color: '#4ade80', border: '#4ade80' },
  COMPLETED: { bg: 'rgba(34,197,94,0.2)', color: '#86efac', border: '#86efac' },
  CANCELLED: { bg: 'rgba(239,68,68,0.15)', color: '#f87171', border: '#f87171' }
};

const PR_STATUS_BADGES = {
  PENDING_APPROVAL: { bg: 'rgba(234,179,8,0.15)', color: '#facc15', border: '#facc15' },
  APPROVED: { bg: 'rgba(34,197,94,0.15)', color: '#4ade80', border: '#4ade80' },
  REJECTED: { bg: 'rgba(239,68,68,0.15)', color: '#f87171', border: '#f87171' },
  CONVERTED_TO_PO: { bg: 'rgba(168,85,247,0.15)', color: '#c084fc', border: '#c084fc' }
};

const QUOTE_STATUS_BADGES = {
  SUBMITTED: { bg: 'rgba(59,130,246,0.15)', color: '#60a5fa', border: '#60a5fa' },
  ACCEPTED: { bg: 'rgba(34,197,94,0.15)', color: '#4ade80', border: '#4ade80' },
  REJECTED: { bg: 'rgba(239,68,68,0.15)', color: '#f87171', border: '#f87171' }
};

function Badge({ status, map }) {
  const style = map[status] || { bg: 'rgba(148,163,184,0.15)', color: '#94a3b8', border: '#94a3b8' };
  return (
    <span style={{ padding: '4px 10px', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 700, background: style.bg, color: style.color, border: `1px solid ${style.border}` }}>
      {status?.replace(/_/g, ' ')}
    </span>
  );
}

function PurchaseOrdersTab() {
  const [pos, setPos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [msg, setMsg] = useState(null);
  const [form, setForm] = useState({ supplierId: '1', expectedDeliveryDate: '2026-09-15', productId: '1', quantity: '20', unitCost: '15.00' });

  useEffect(() => { fetchPOs(); }, []);

  const authHeaders = () => ({ 'Authorization': 'Bearer ' + localStorage.getItem('token'), 'Content-Type': 'application/json' });

  const fetchPOs = async () => {
    try {
      const res = await fetch('/api/purchase-orders', { headers: authHeaders() });
      if (res.ok) setPos(await res.json());
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleCreatePO = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch('/api/purchase-orders', {
        method: 'POST', headers: authHeaders(),
        body: JSON.stringify({ supplierId: parseInt(form.supplierId), expectedDeliveryDate: form.expectedDeliveryDate, items: [{ productId: parseInt(form.productId), quantity: parseInt(form.quantity), unitCost: parseFloat(form.unitCost) }] })
      });
      if (!res.ok) throw new Error();
      setMsg({ type: 'success', text: 'Purchase order created!' });
      setShowForm(false); fetchPOs();
    } catch { setMsg({ type: 'error', text: 'Failed to create purchase order.' }); }
  };

  const handleUpdateStatus = async (poId, newStatus) => {
    try {
      const res = await fetch(`/api/purchase-orders/${poId}/status`, { method: 'PUT', headers: authHeaders(), body: JSON.stringify({ status: newStatus }) });
      if (res.ok) { setMsg({ type: 'success', text: `PO #${poId} → ${newStatus}` }); fetchPOs(); }
    } catch (err) { console.error(err); }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '16px' }}>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>{showForm ? 'Cancel' : '+ Create PO'}</button>
      </div>
      {msg && <div style={{ marginBottom: '16px', padding: '12px 16px', borderRadius: '8px', background: msg.type === 'success' ? 'rgba(34,197,94,0.15)' : 'rgba(239,68,68,0.15)', color: msg.type === 'success' ? '#4ade80' : '#f87171', border: `1px solid ${msg.type === 'success' ? '#4ade80' : '#f87171'}` }}>{msg.text}</div>}
      {showForm && (
        <form onSubmit={handleCreatePO} className="glass-panel" style={{ padding: '24px', marginBottom: '24px', display: 'flex', gap: '12px', alignItems: 'flex-end', flexWrap: 'wrap' }}>
          {[['Supplier ID', 'supplierId', 'number'], ['Product ID', 'productId', 'number'], ['Quantity', 'quantity', 'number'], ['Unit Cost ($)', 'unitCost', 'number'], ['Expected Delivery', 'expectedDeliveryDate', 'date']].map(([label, key, type]) => (
            <div key={key} className="input-group" style={{ marginBottom: 0 }}>
              <label>{label}</label>
              <input type={type} required value={form[key]} onChange={e => setForm({ ...form, [key]: e.target.value })} step={key === 'unitCost' ? '0.01' : undefined} min={key === 'quantity' ? '1' : undefined} />
            </div>
          ))}
          <button type="submit" className="btn btn-primary" style={{ height: '46px' }}>Submit PO</button>
        </form>
      )}
      {loading ? <p>Loading...</p> : (
        <div className="table-container glass-panel">
          <table>
            <thead><tr><th>PO Number</th><th>Supplier</th><th>Status</th><th>Total Cost</th><th>Expected Delivery</th><th>Items</th><th>Action</th></tr></thead>
            <tbody>
              {pos.map(po => (
                <tr key={po.id}>
                  <td><strong>{po.poNumber}</strong></td>
                  <td>{po.supplierName || `Supplier #${po.supplierId}`}</td>
                  <td><Badge status={po.status} map={PO_STATUS_BADGES} /></td>
                  <td style={{ fontWeight: 600 }}>${po.totalCost?.toFixed(2)}</td>
                  <td style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{po.expectedDeliveryDate || '—'}</td>
                  <td style={{ fontSize: '0.85rem' }}>{po.items?.map(it => `${it.productName || 'Product #' + it.productId} (x${it.quantity})`).join(', ') || '—'}</td>
                  <td>
                    {(po.status === 'SUBMITTED' || po.status === 'SHIPPED' || po.status === 'CONFIRMED') && <button className="btn btn-secondary" style={{ padding: '6px 12px', fontSize: '0.8rem' }} onClick={() => handleUpdateStatus(po.id, 'RECEIVED')}>📥 Receive</button>}
                    {po.status === 'DRAFT' && <button className="btn btn-primary" style={{ padding: '6px 12px', fontSize: '0.8rem' }} onClick={() => handleUpdateStatus(po.id, 'SUBMITTED')}>🚀 Submit</button>}
                  </td>
                </tr>
              ))}
              {pos.length === 0 && <tr><td colSpan="7" style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>No purchase orders found.</td></tr>}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function PurchaseRequestsTab() {
  const [prs, setPrs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [selectedPr, setSelectedPr] = useState(null);
  const [quotations, setQuotations] = useState([]);
  const [showQuoteForm, setShowQuoteForm] = useState(false);
  const [msg, setMsg] = useState(null);
  const [form, setForm] = useState({ supplierId: '1', productId: '1', quantity: '10', estimatedUnitCost: '12.00' });
  const [quoteForm, setQuoteForm] = useState({ supplierId: '1', quotedUnitPrice: '11.50', availableQuantity: '50', promisedDeliveryDate: '2026-09-20', notes: 'Discounted rate' });

  useEffect(() => { fetchPRs(); }, []);

  const authHeaders = () => ({ 'Authorization': 'Bearer ' + localStorage.getItem('token'), 'Content-Type': 'application/json' });

  const fetchPRs = async () => {
    try {
      const res = await fetch('/api/purchase-requests', { headers: authHeaders() });
      if (res.ok) setPrs(await res.json());
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const fetchQuotations = async (prId) => {
    try {
      const res = await fetch(`/api/quotations/pr/${prId}`, { headers: authHeaders() });
      if (res.ok) setQuotations(await res.json());
    } catch (err) { console.error(err); }
  };

  const openQuotesModal = (pr) => {
    setSelectedPr(pr);
    fetchQuotations(pr.id);
  };

  const handleCreatePR = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch('/api/purchase-requests', {
        method: 'POST', headers: authHeaders(),
        body: JSON.stringify({ supplierId: parseInt(form.supplierId), items: [{ productId: parseInt(form.productId), quantity: parseInt(form.quantity), estimatedUnitCost: parseFloat(form.estimatedUnitCost) }] })
      });
      if (!res.ok) throw new Error();
      setMsg({ type: 'success', text: 'Purchase request submitted for approval!' });
      setShowForm(false); fetchPRs();
    } catch { setMsg({ type: 'error', text: 'Failed to create purchase request.' }); }
  };

  const handleCreateQuote = async (e) => {
    e.preventDefault();
    if (!selectedPr) return;
    try {
      const res = await fetch('/api/quotations', {
        method: 'POST', headers: authHeaders(),
        body: JSON.stringify({
          purchaseRequestId: selectedPr.id,
          supplierId: parseInt(quoteForm.supplierId),
          quotedUnitPrice: parseFloat(quoteForm.quotedUnitPrice),
          availableQuantity: parseInt(quoteForm.availableQuantity),
          promisedDeliveryDate: quoteForm.promisedDeliveryDate,
          notes: quoteForm.notes
        })
      });
      if (!res.ok) throw new Error();
      setMsg({ type: 'success', text: 'Supplier quotation submitted successfully!' });
      setShowQuoteForm(false);
      fetchQuotations(selectedPr.id);
    } catch { setMsg({ type: 'error', text: 'Failed to submit supplier quotation.' }); }
  };

  const handleAcceptQuote = async (quoteId) => {
    try {
      const res = await fetch(`/api/quotations/${quoteId}/accept`, { method: 'POST', headers: authHeaders() });
      if (res.ok) {
        setMsg({ type: 'success', text: `Quotation accepted & Purchase Order generated!` });
        fetchQuotations(selectedPr.id);
        fetchPRs();
      } else setMsg({ type: 'error', text: 'Failed to accept quote.' });
    } catch (err) { console.error(err); }
  };

  const handleApprove = async (id) => {
    try {
      const res = await fetch(`/api/purchase-requests/${id}/approve`, { method: 'POST', headers: authHeaders() });
      if (res.ok) { setMsg({ type: 'success', text: `PR #${id} approved!` }); fetchPRs(); }
      else setMsg({ type: 'error', text: 'Approval failed.' });
    } catch (err) { console.error(err); }
  };

  const handleReject = async (id) => {
    const reason = prompt('Enter rejection reason:');
    if (reason === null) return;
    try {
      const res = await fetch(`/api/purchase-requests/${id}/reject`, { method: 'POST', headers: authHeaders(), body: JSON.stringify({ reason }) });
      if (res.ok) { setMsg({ type: 'success', text: `PR #${id} rejected.` }); fetchPRs(); }
    } catch (err) { console.error(err); }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '16px' }}>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>{showForm ? 'Cancel' : '+ New Purchase Request'}</button>
      </div>
      {msg && <div style={{ marginBottom: '16px', padding: '12px 16px', borderRadius: '8px', background: msg.type === 'success' ? 'rgba(34,197,94,0.15)' : 'rgba(239,68,68,0.15)', color: msg.type === 'success' ? '#4ade80' : '#f87171', border: `1px solid ${msg.type === 'success' ? '#4ade80' : '#f87171'}` }}>{msg.text}</div>}
      {showForm && (
        <form onSubmit={handleCreatePR} className="glass-panel" style={{ padding: '24px', marginBottom: '24px', display: 'flex', gap: '12px', alignItems: 'flex-end', flexWrap: 'wrap' }}>
          {[['Supplier ID', 'supplierId', 'number'], ['Product ID', 'productId', 'number'], ['Quantity', 'quantity', 'number'], ['Est. Unit Cost ($)', 'estimatedUnitCost', 'number']].map(([label, key, type]) => (
            <div key={key} className="input-group" style={{ marginBottom: 0 }}>
              <label>{label}</label>
              <input type={type} required value={form[key]} onChange={e => setForm({ ...form, [key]: e.target.value })} step={key === 'estimatedUnitCost' ? '0.01' : undefined} min={key === 'quantity' ? '1' : undefined} />
            </div>
          ))}
          <button type="submit" className="btn btn-primary" style={{ height: '46px' }}>Submit Request</button>
        </form>
      )}
      {loading ? <p>Loading...</p> : (
        <div className="table-container glass-panel">
          <table>
            <thead><tr><th>PR Number</th><th>Requested By</th><th>Supplier</th><th>Status</th><th>Items</th><th>Created</th><th>Actions</th></tr></thead>
            <tbody>
              {prs.map(pr => (
                <tr key={pr.id}>
                  <td><strong>{pr.requestNumber}</strong></td>
                  <td>{pr.requestedByUsername || '—'}</td>
                  <td>{pr.supplierName || (pr.supplierId ? `Supplier #${pr.supplierId}` : '—')}</td>
                  <td><Badge status={pr.status} map={PR_STATUS_BADGES} /></td>
                  <td style={{ fontSize: '0.85rem' }}>{pr.items?.map(it => `${it.productName || 'Product #' + it.productId} (x${it.quantity})`).join(', ') || '—'}</td>
                  <td style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>{pr.createdAt?.slice(0, 10) || '—'}</td>
                  <td style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                    {pr.status === 'PENDING_APPROVAL' && (
                      <>
                        <button className="btn btn-primary" style={{ padding: '5px 10px', fontSize: '0.78rem' }} onClick={() => handleApprove(pr.id)}>✅ Approve</button>
                        <button className="btn btn-secondary" style={{ padding: '5px 10px', fontSize: '0.78rem', color: '#f87171' }} onClick={() => handleReject(pr.id)}>❌ Reject</button>
                      </>
                    )}
                    <button className="btn btn-secondary" style={{ padding: '5px 10px', fontSize: '0.78rem' }} onClick={() => openQuotesModal(pr)}>🏷️ Quotes</button>
                  </td>
                </tr>
              ))}
              {prs.length === 0 && <tr><td colSpan="7" style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>No purchase requests found.</td></tr>}
            </tbody>
          </table>
        </div>
      )}

      {selectedPr && (
        <div className="glass-panel" style={{ marginTop: '24px', padding: '24px', border: '1px solid var(--accent-primary)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <h3 style={{ margin: 0 }}>🏷️ Quotations for PR #{selectedPr.requestNumber}</h3>
            <div style={{ display: 'flex', gap: '8px' }}>
              <button className="btn btn-primary" style={{ padding: '6px 12px', fontSize: '0.8rem' }} onClick={() => setShowQuoteForm(!showQuoteForm)}>
                {showQuoteForm ? 'Cancel Quote' : '+ Submit Supplier Quote'}
              </button>
              <button className="btn btn-secondary" style={{ padding: '6px 12px', fontSize: '0.8rem' }} onClick={() => setSelectedPr(null)}>Close</button>
            </div>
          </div>

          {showQuoteForm && (
            <form onSubmit={handleCreateQuote} className="glass-panel" style={{ padding: '16px', marginBottom: '16px', display: 'flex', gap: '10px', flexWrap: 'wrap', alignItems: 'flex-end' }}>
              <div className="input-group" style={{ marginBottom: 0 }}><label>Supplier ID</label><input type="number" required value={quoteForm.supplierId} onChange={e => setQuoteForm({ ...quoteForm, supplierId: e.target.value })} /></div>
              <div className="input-group" style={{ marginBottom: 0 }}><label>Quoted Price ($)</label><input type="number" step="0.01" required value={quoteForm.quotedUnitPrice} onChange={e => setQuoteForm({ ...quoteForm, quotedUnitPrice: e.target.value })} /></div>
              <div className="input-group" style={{ marginBottom: 0 }}><label>Avail. Qty</label><input type="number" required value={quoteForm.availableQuantity} onChange={e => setQuoteForm({ ...quoteForm, availableQuantity: e.target.value })} /></div>
              <div className="input-group" style={{ marginBottom: 0 }}><label>Promised Date</label><input type="date" required value={quoteForm.promisedDeliveryDate} onChange={e => setQuoteForm({ ...quoteForm, promisedDeliveryDate: e.target.value })} /></div>
              <div className="input-group" style={{ marginBottom: 0 }}><label>Notes</label><input type="text" value={quoteForm.notes} onChange={e => setQuoteForm({ ...quoteForm, notes: e.target.value })} /></div>
              <button type="submit" className="btn btn-primary" style={{ height: '42px' }}>Submit Quote</button>
            </form>
          )}

          <div className="table-container">
            <table>
              <thead><tr><th>Supplier</th><th>Quoted Unit Price</th><th>Avail. Qty</th><th>Promised Date</th><th>Status</th><th>Notes</th><th>Action</th></tr></thead>
              <tbody>
                {quotations.map(q => (
                  <tr key={q.id}>
                    <td><strong>{q.supplierName || `Supplier #${q.supplierId}`}</strong></td>
                    <td style={{ fontWeight: 600, color: '#4ade80' }}>${q.quotedUnitPrice?.toFixed(2)}</td>
                    <td>{q.availableQuantity}</td>
                    <td>{q.promisedDeliveryDate || '—'}</td>
                    <td><Badge status={q.status} map={QUOTE_STATUS_BADGES} /></td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{q.notes || '—'}</td>
                    <td>
                      {q.status === 'SUBMITTED' && selectedPr.status !== 'CONVERTED_TO_PO' && (
                        <button className="btn btn-primary" style={{ padding: '4px 10px', fontSize: '0.78rem' }} onClick={() => handleAcceptQuote(q.id)}>
                          🏆 Accept & Create PO
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
                {quotations.length === 0 && <tr><td colSpan="7" style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>No quotes submitted yet for this request.</td></tr>}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

export default function PurchaseOrdersView() {
  const [tab, setTab] = useState('pos');
  const tabStyle = (active) => ({
    padding: '10px 22px', borderRadius: '8px', fontWeight: 600, fontSize: '0.9rem', cursor: 'pointer', border: 'none',
    background: active ? 'var(--accent-primary)' : 'rgba(255,255,255,0.05)',
    color: active ? '#fff' : 'var(--text-secondary)', transition: 'all 0.2s'
  });
  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">📦 Procurement</h1>
        <div style={{ display: 'flex', gap: '8px' }}>
          <button id="tab-pos" style={tabStyle(tab === 'pos')} onClick={() => setTab('pos')}>Purchase Orders</button>
          <button id="tab-prs" style={tabStyle(tab === 'prs')} onClick={() => setTab('prs')}>Purchase Requests</button>
        </div>
      </div>
      <div style={{ marginTop: '24px' }}>
        {tab === 'pos' ? <PurchaseOrdersTab /> : <PurchaseRequestsTab />}
      </div>
    </div>
  );
}
