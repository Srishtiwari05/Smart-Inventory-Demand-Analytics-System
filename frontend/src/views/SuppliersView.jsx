import { useState, useEffect } from 'react';

const RATING_BADGES = {
  EXCELLENT: { bg: 'rgba(34,197,94,0.15)', color: '#4ade80', border: '#4ade80', label: '🟢 EXCELLENT' },
  MODERATE: { bg: 'rgba(234,179,8,0.15)', color: '#facc15', border: '#facc15', label: '🟡 MODERATE' },
  DELAY_PRONE: { bg: 'rgba(239,68,68,0.15)', color: '#f87171', border: '#f87171', label: '🔴 DELAY PRONE' }
};

export default function SuppliersView() {
  const [intelligence, setIntelligence] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchIntelligence(); }, []);

  const fetchIntelligence = async () => {
    try {
      const res = await fetch('/api/suppliers/intelligence', {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
      });
      if (res.ok) {
        setIntelligence(await res.json());
      }
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">🚚 Supplier Intelligence & Performance</h1>
      </div>

      <p style={{ color: 'var(--text-secondary)', marginTop: '8px' }}>
        Real-time supplier delivery reliability, lead time compliance, order count, and spend volume.
      </p>

      {loading ? <p style={{ marginTop: '24px' }}>Loading supplier intelligence...</p> : (
        <div className="table-container glass-panel" style={{ marginTop: '24px' }}>
          <table>
            <thead>
              <tr>
                <th>Supplier</th>
                <th>Reliability Score</th>
                <th>On-Time Delivery</th>
                <th>Actual Lead Time</th>
                <th>Promised Lead Time</th>
                <th>Total POs</th>
                <th>Total Spend</th>
              </tr>
            </thead>
            <tbody>
              {intelligence.map(item => {
                const s = item.supplier;
                const badge = RATING_BADGES[item.ratingCategory] || RATING_BADGES.EXCELLENT;
                return (
                  <tr key={s.id}>
                    <td>
                      <div><strong>{s.name}</strong></div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{s.contactInfo || '—'}</div>
                    </td>
                    <td>
                      <span style={{ padding: '4px 10px', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 700, background: badge.bg, color: badge.color, border: `1px solid ${badge.border}` }}>
                        {badge.label} ({item.reliabilityScore?.toFixed(0)}%)
                      </span>
                    </td>
                    <td style={{ fontWeight: 600, color: item.onTimeDeliveryRate >= 90 ? '#4ade80' : '#facc15' }}>
                      {item.onTimeDeliveryRate?.toFixed(1)}%
                    </td>
                    <td>{item.actualLeadTimeDays?.toFixed(1)} days</td>
                    <td style={{ color: 'var(--text-secondary)' }}>{s.leadTimeDays || 5} days</td>
                    <td>{item.totalOrders} POs</td>
                    <td className="text-success" style={{ fontWeight: 700 }}>
                      ${item.totalPurchaseValue?.toFixed(2)}
                    </td>
                  </tr>
                );
              })}
              {intelligence.length === 0 && (
                <tr><td colSpan="7" style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>No supplier performance data available.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
