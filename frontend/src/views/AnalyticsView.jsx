import { useState, useEffect } from 'react';

export default function AnalyticsView() {
  const [report, setReport] = useState(null);
  const [recommendation, setRecommendation] = useState(null);
  const [productId, setProductId] = useState('');
  const [sim, setSim] = useState({ productId: '', demandMultiplier: '1.5', extraLeadTime: '5' });
  const [simResult, setSimResult] = useState(null);
  const [simLoading, setSimLoading] = useState(false);

  useEffect(() => { fetchReport(); }, []);

  const fetchReport = async () => {
    try {
      const res = await fetch('/api/sales/report', {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
      });
      const data = await res.json();
      setReport(data);
    } catch (err) { console.error(err); }
  };

  const checkRecommendation = async (e) => {
    e.preventDefault();
    if (!productId) return;
    try {
      const res = await fetch(`/api/analytics/recommendations/${productId}`, {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
      });
      if (res.ok) setRecommendation(await res.json());
      else alert('Product not found or error occurred');
    } catch (err) { console.error(err); }
  };

  const runSimulation = async (e) => {
    e.preventDefault();
    if (!sim.productId) return;
    setSimLoading(true);
    setSimResult(null);
    try {
      const params = new URLSearchParams({
        demandMultiplier: sim.demandMultiplier,
        extraLeadTime: sim.extraLeadTime
      });
      const res = await fetch(`/api/analytics/simulate/${sim.productId}?${params}`, {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
      });
      if (res.ok) setSimResult(await res.json());
      else alert('Simulation failed. Check product ID.');
    } catch (err) { console.error(err); }
    finally { setSimLoading(false); }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">📈 Demand Analytics</h1>
      </div>

      {/* KPI Cards */}
      {report && (
        <div className="grid-cards" style={{ marginTop: '24px' }}>
          <div className="stat-card glass-panel">
            <div className="stat-title">Total Revenue</div>
            <div className="stat-value text-success">${report.totalRevenue?.toFixed(2)}</div>
          </div>
          <div className="stat-card glass-panel">
            <div className="stat-title">Total Orders</div>
            <div className="stat-value">{report.totalOrders}</div>
          </div>
          <div className="stat-card glass-panel">
            <div className="stat-title">Avg Order Value</div>
            <div className="stat-value">${report.averageOrderValue?.toFixed(2)}</div>
          </div>
        </div>
      )}

      {/* Reorder Recommendation */}
      <div style={{ marginTop: '40px' }} className="glass-panel">
        <div style={{ padding: '24px', borderBottom: '1px solid var(--panel-border)' }}>
          <h2>🔁 Reorder Recommendation</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '4px' }}>Check if a product needs restocking based on 30-day demand history.</p>
        </div>
        <div style={{ padding: '24px' }}>
          <form onSubmit={checkRecommendation} style={{ display: 'flex', gap: '12px', maxWidth: '400px' }}>
            <input
              type="number"
              placeholder="Product ID (e.g. 1)"
              value={productId}
              onChange={e => setProductId(e.target.value)}
              required
            />
            <button type="submit" className="btn btn-primary">Analyze</button>
          </form>

          {recommendation && (
            <div style={{ marginTop: '24px', padding: '20px', background: 'rgba(0,0,0,0.2)', borderRadius: '12px', border: '1px solid var(--panel-border)' }}>
              <h3 style={{ marginBottom: '12px' }}>{recommendation.product?.name} <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>(ID #{recommendation.product?.id})</span></h3>
              <span className={`badge ${recommendation.needsReorder ? 'badge-danger' : 'badge-success'}`} style={{ fontSize: '0.9rem', padding: '6px 14px' }}>
                {recommendation.needsReorder ? '⚠ Reorder Needed' : '✓ Stock Healthy'}
              </span>
              <p style={{ marginTop: '12px', color: 'var(--text-secondary)' }}>{recommendation.reason}</p>
              {recommendation.needsReorder && (
                <p style={{ marginTop: '8px', fontWeight: 700, color: 'var(--warning)', fontSize: '1.1rem' }}>
                  Recommended Reorder: {recommendation.recommendedAmount} units
                </p>
              )}
            </div>
          )}
        </div>
      </div>

      {/* What-If Simulator */}
      <div style={{ marginTop: '32px' }} className="glass-panel">
        <div style={{ padding: '24px', borderBottom: '1px solid var(--panel-border)' }}>
          <h2>🧪 What-If Simulator</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '4px' }}>Simulate a demand spike or supply chain delay and see the projected impact.</p>
        </div>
        <div style={{ padding: '24px' }}>
          <form onSubmit={runSimulation} style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'flex-end' }}>
            <div className="input-group" style={{ marginBottom: 0, width: '140px' }}>
              <label>Product ID</label>
              <input type="number" required value={sim.productId} onChange={e => setSim({ ...sim, productId: e.target.value })} />
            </div>
            <div className="input-group" style={{ marginBottom: 0, width: '180px' }}>
              <label>Demand Multiplier (e.g. 1.5)</label>
              <input type="number" step="0.1" min="0.1" max="10" value={sim.demandMultiplier} onChange={e => setSim({ ...sim, demandMultiplier: e.target.value })} />
            </div>
            <div className="input-group" style={{ marginBottom: 0, width: '180px' }}>
              <label>Extra Lead Time (days)</label>
              <input type="number" min="0" max="60" value={sim.extraLeadTime} onChange={e => setSim({ ...sim, extraLeadTime: e.target.value })} />
            </div>
            <button type="submit" className="btn btn-primary" style={{ height: '46px' }} disabled={simLoading}>
              {simLoading ? 'Running...' : '▶ Run Simulation'}
            </button>
          </form>

          {simResult && (
            <div style={{ marginTop: '28px', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
              <div style={{ padding: '20px', background: 'rgba(0,0,0,0.25)', borderRadius: '12px', border: '1px solid var(--panel-border)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '8px' }}>Product</div>
                <div style={{ fontWeight: 700, fontSize: '1.1rem' }}>{simResult.product?.name}</div>
              </div>
              <div style={{ padding: '20px', background: 'rgba(0,0,0,0.25)', borderRadius: '12px', border: '1px solid var(--panel-border)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '8px' }}>Current Stock</div>
                <div style={{ fontWeight: 700, fontSize: '1.5rem' }}>{simResult.currentStock} <span style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>units</span></div>
              </div>
              <div style={{ padding: '20px', background: 'rgba(139,92,246,0.15)', borderRadius: '12px', border: '1px solid #7c3aed' }}>
                <div style={{ fontSize: '0.8rem', color: '#a78bfa', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '8px' }}>Projected Demand</div>
                <div style={{ fontWeight: 700, fontSize: '1.5rem', color: '#c4b5fd' }}>{simResult.projectedDemand?.toFixed(1)} <span style={{ fontSize: '0.9rem' }}>units/day</span></div>
              </div>
              <div style={{ padding: '20px', background: simResult.stockoutRisk ? 'rgba(239,68,68,0.15)' : 'rgba(34,197,94,0.1)', borderRadius: '12px', border: `1px solid ${simResult.stockoutRisk ? '#f87171' : '#4ade80'}` }}>
                <div style={{ fontSize: '0.8rem', color: simResult.stockoutRisk ? '#f87171' : '#4ade80', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '8px' }}>Stockout Risk</div>
                <div style={{ fontWeight: 700, fontSize: '1.5rem', color: simResult.stockoutRisk ? '#fca5a5' : '#86efac' }}>
                  {simResult.stockoutRisk ? '⚠ HIGH' : '✓ LOW'}
                </div>
              </div>
              <div style={{ padding: '20px', background: 'rgba(0,0,0,0.25)', borderRadius: '12px', border: '1px solid var(--panel-border)', gridColumn: 'span 2' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '8px' }}>Recommendation</div>
                <div style={{ color: 'var(--text-primary)', lineHeight: 1.6 }}>{simResult.recommendation}</div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
