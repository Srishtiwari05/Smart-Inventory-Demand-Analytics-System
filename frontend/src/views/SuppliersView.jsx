import { useState, useEffect } from 'react';

export default function SuppliersView() {
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchSuppliers(); }, []);

  const fetchSuppliers = async () => {
    try {
      const res = await fetch('/api/suppliers', {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
      });
      setSuppliers(await res.json());
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">🚚 Suppliers</h1>
      </div>

      {loading ? <p style={{ marginTop: '24px' }}>Loading suppliers...</p> : (
        <div className="table-container glass-panel" style={{ marginTop: '24px' }}>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Contact Info</th>
                <th>Lead Time (days)</th>
              </tr>
            </thead>
            <tbody>
              {suppliers.map(s => (
                <tr key={s.id}>
                  <td>#{s.id}</td>
                  <td><strong>{s.name}</strong></td>
                  <td>{s.contactInfo || s.contact_info || '—'}</td>
                  <td>
                    <span className="badge" style={{ background: 'rgba(139,92,246,0.2)', color: '#a78bfa', border: '1px solid #a78bfa' }}>
                      {s.leadTimeDays || s.lead_time_days || 5} days
                    </span>
                  </td>
                </tr>
              ))}
              {suppliers.length === 0 && (
                <tr><td colSpan="4" style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>No suppliers found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
