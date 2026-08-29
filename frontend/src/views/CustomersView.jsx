import { useState, useEffect } from 'react';

export default function CustomersView() {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [newCustomer, setNewCustomer] = useState({ name: '', email: '', phone: '' });
  const [msg, setMsg] = useState(null);

  useEffect(() => { fetchCustomers(); }, []);

  const fetchCustomers = async () => {
    try {
      const res = await fetch('/api/customers', {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
      });
      setCustomers(await res.json());
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleAdd = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch('/api/customers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') },
        body: JSON.stringify(newCustomer)
      });
      if (!res.ok) throw new Error();
      setMsg({ type: 'success', text: 'Customer added!' });
      setShowForm(false);
      setNewCustomer({ name: '', email: '', phone: '' });
      fetchCustomers();
    } catch { setMsg({ type: 'error', text: 'Failed to add customer.' }); }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">👥 Customers</h1>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : '+ Add Customer'}
        </button>
      </div>

      {msg && (
        <div style={{ marginTop: '16px', padding: '12px 16px', borderRadius: '8px', background: msg.type === 'success' ? 'rgba(34,197,94,0.15)' : 'rgba(239,68,68,0.15)', color: msg.type === 'success' ? '#4ade80' : '#f87171', border: `1px solid ${msg.type === 'success' ? '#4ade80' : '#f87171'}` }}>
          {msg.text}
        </div>
      )}

      {showForm && (
        <form onSubmit={handleAdd} className="glass-panel" style={{ padding: '24px', marginTop: '24px', display: 'flex', gap: '12px', alignItems: 'flex-end', flexWrap: 'wrap' }}>
          <div className="input-group" style={{ marginBottom: 0, flex: 1, minWidth: '160px' }}>
            <label>Name</label>
            <input type="text" required value={newCustomer.name} onChange={e => setNewCustomer({ ...newCustomer, name: e.target.value })} />
          </div>
          <div className="input-group" style={{ marginBottom: 0, flex: 1, minWidth: '200px' }}>
            <label>Email</label>
            <input type="email" required value={newCustomer.email} onChange={e => setNewCustomer({ ...newCustomer, email: e.target.value })} />
          </div>
          <div className="input-group" style={{ marginBottom: 0, width: '140px' }}>
            <label>Phone</label>
            <input type="text" value={newCustomer.phone} onChange={e => setNewCustomer({ ...newCustomer, phone: e.target.value })} />
          </div>
          <button type="submit" className="btn btn-primary" style={{ height: '46px' }}>Save</button>
        </form>
      )}

      {loading ? <p style={{ marginTop: '24px' }}>Loading customers...</p> : (
        <div className="table-container glass-panel" style={{ marginTop: '24px' }}>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
              </tr>
            </thead>
            <tbody>
              {customers.map(c => (
                <tr key={c.id}>
                  <td>#{c.id}</td>
                  <td><strong>{c.name}</strong></td>
                  <td>{c.email}</td>
                  <td>{c.phone || '—'}</td>
                </tr>
              ))}
              {customers.length === 0 && (
                <tr><td colSpan="4" style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>No customers found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
