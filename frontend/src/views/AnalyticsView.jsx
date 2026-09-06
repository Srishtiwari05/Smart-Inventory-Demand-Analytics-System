import { useState, useEffect } from 'react';

const RISK_STYLES = {
  CRITICAL: { bg: 'rgba(239,68,68,0.15)', color: '#f87171', border: '#f87171', label: '🔴 CRITICAL' },
  RISK:     { bg: 'rgba(249,115,22,0.15)', color: '#fb923c', border: '#fb923c', label: '🟠 RISK' },
  WATCH:    { bg: 'rgba(234,179,8,0.15)',  color: '#facc15', border: '#facc15', label: '🟡 WATCH' },
  HEALTHY:  { bg: 'rgba(34,197,94,0.1)',   color: '#4ade80', border: '#4ade80', label: '🟢 HEALTHY' },
};

export default function AnalyticsView() {
  const [report, setReport] = useState(null);
  const [recommendation, setRecommendation] = useState(null);
  const [productId, setProductId] = useState('');
  const [sim, setSim] = useState({ productId: '', demandMultiplier: '1.5', extraLeadTime: '5' });
  const [simResult, setSimResult] = useState(null);
  const [simLoading, setSimLoading] = useState(false);
  const [riskReport, setRiskReport] = useState([]);
  const [predictionProductId, setPredictionProductId] = useState('');
  const [prediction, setPrediction] = useState(null);
  const [predictionLoading, setPredictionLoading] = useState(false);

  useEffect(() => { fetchReport(); fetchRiskReport(); }, []);

  const fetchReport = async () => {
    try {
      const res = await fetch('/api/sales/report', {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
      });
      const data = await res.json();
      setReport(data);
    } catch (err) { console.error(err); }
  };

  const fetchRiskReport = async () => {
    try {
      const res = await fetch('/api/analytics/risk', {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
      });
      if (res.ok) setRiskReport(await res.json());
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

  const fetchPrediction = async (e) => {
    e.preventDefault();
    if (!predictionProductId) return;
    setPredictionLoading(true);
    setPrediction(null);
    try {
      const res = await fetch(`/api/predictions/demand/${predictionProductId}`, {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
      });
      if (res.ok) setPrediction(await res.json());
      else setPrediction({ error: 'ML service unavailable. Make sure Flask is running on port 5000.' });
    } catch (err) {
      setPrediction({ error: 'Connection failed. Is the Python ML service running?' });
    } finally {
      setPredictionLoading(false);
    }
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

      {/* Intelligent Reorder Engine */}
      <div style={{ marginTop: '40px' }} className="glass-panel">
        <div style={{ padding: '24px', borderBottom: '1px solid var(--panel-border)' }}>
          <h2>⚡ Intelligent Reorder Engine</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '4px' }}>Automated reorder decision support calculating demand velocity, lead time, safety stock, and order deadlines.</p>
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
            <button type="submit" className="btn btn-primary">⚡ Analyze Reorder</button>
          </form>

          {recommendation && (
            <div style={{ marginTop: '24px', padding: '24px', background: 'rgba(0,0,0,0.25)', borderRadius: '12px', border: '1px solid var(--panel-border)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
                <h3 style={{ fontSize: '1.2rem', margin: 0 }}>{recommendation.product?.name} <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>(ID #{recommendation.product?.id})</span></h3>
                <span className={`badge ${recommendation.actionRequired ? 'badge-danger' : 'badge-success'}`} style={{ fontSize: '0.85rem', padding: '6px 14px' }}>
                  {recommendation.actionRequired ? '⚡ ACTION REQUIRED' : '✓ HEALTHY STOCK'}
                </span>
              </div>

              <div style={{ marginTop: '20px', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: '12px' }}>
                <div style={{ padding: '14px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--panel-border)' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', textTransform: 'uppercase' }}>Recommended Order Qty</div>
                  <div style={{ fontSize: '1.4rem', fontWeight: 700, color: recommendation.actionRequired ? '#f87171' : '#4ade80', marginTop: '4px' }}>
                    {recommendation.recommendedReorderQuantity} <span style={{ fontSize: '0.85rem' }}>units</span>
                  </div>
                </div>
                <div style={{ padding: '14px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--panel-border)' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', textTransform: 'uppercase' }}>Order Deadline</div>
                  <div style={{ fontSize: '1.4rem', fontWeight: 700, color: '#fb923c', marginTop: '4px' }}>
                    {recommendation.orderDeadlineDays === 0 ? 'IMMEDIATE' : `Within ${recommendation.orderDeadlineDays} days`}
                  </div>
                </div>
                <div style={{ padding: '14px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--panel-border)' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', textTransform: 'uppercase' }}>Expected Stockout</div>
                  <div style={{ fontSize: '1.4rem', fontWeight: 700, color: '#facc15', marginTop: '4px' }}>
                    In {recommendation.expectedStockoutDays} days
                  </div>
                </div>
                <div style={{ padding: '14px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--panel-border)' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', textTransform: 'uppercase' }}>Safety Stock / ROP</div>
                  <div style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--text-primary)', marginTop: '4px' }}>
                    {recommendation.safetyStock} / {recommendation.reorderPoint} <span style={{ fontSize: '0.85rem' }}>units</span>
                  </div>
                </div>
              </div>

              <div style={{ marginTop: '16px', padding: '14px 18px', background: 'rgba(0,0,0,0.3)', borderLeft: `4px solid ${recommendation.actionRequired ? '#f87171' : '#4ade80'}`, borderRadius: '0 8px 8px 0', color: 'var(--text-primary)', lineHeight: 1.5 }}>
                {recommendation.reason}
              </div>
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

      {/* AI Demand Prediction */}
      <div style={{ marginTop: '32px' }} className="glass-panel">
        <div style={{ padding: '24px', borderBottom: '1px solid var(--panel-border)' }}>
          <h2>🤖 AI Demand Prediction</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '4px' }}>ML-powered 30-day demand forecast using historical sales data (RandomForest model).</p>
        </div>
        <div style={{ padding: '24px' }}>
          <form onSubmit={fetchPrediction} style={{ display: 'flex', gap: '12px', maxWidth: '400px' }}>
            <input
              type="number"
              placeholder="Product ID (e.g. 1)"
              value={predictionProductId}
              onChange={e => setPredictionProductId(e.target.value)}
              required
            />
            <button type="submit" className="btn btn-primary" disabled={predictionLoading}>
              {predictionLoading ? 'Predicting...' : '🔮 Predict'}
            </button>
          </form>

          {prediction && !prediction.error && (
            <div style={{ marginTop: '24px', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '16px' }}>
              <div style={{ padding: '20px', background: 'rgba(139,92,246,0.15)', borderRadius: '12px', border: '1px solid #7c3aed' }}>
                <div style={{ fontSize: '0.8rem', color: '#a78bfa', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '8px' }}>Product ID</div>
                <div style={{ fontWeight: 700, fontSize: '1.5rem', color: '#c4b5fd' }}>#{prediction.productId}</div>
              </div>
              <div style={{ padding: '20px', background: 'rgba(139,92,246,0.2)', borderRadius: '12px', border: '1px solid #7c3aed' }}>
                <div style={{ fontSize: '0.8rem', color: '#a78bfa', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '8px' }}>Predicted Demand</div>
                <div style={{ fontWeight: 700, fontSize: '2rem', color: '#e9d5ff' }}>{prediction.predictedDemand} <span style={{ fontSize: '0.9rem', color: '#a78bfa' }}>units</span></div>
              </div>
              <div style={{ padding: '20px', background: 'rgba(139,92,246,0.15)', borderRadius: '12px', border: '1px solid #7c3aed' }}>
                <div style={{ fontSize: '0.8rem', color: '#a78bfa', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '8px' }}>Forecast Period</div>
                <div style={{ fontWeight: 700, fontSize: '1.1rem', color: '#c4b5fd' }}>Next 30 Days</div>
              </div>
              <div style={{ padding: '20px', background: 'rgba(139,92,246,0.15)', borderRadius: '12px', border: '1px solid #7c3aed' }}>
                <div style={{ fontSize: '0.8rem', color: '#a78bfa', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '8px' }}>Daily Rate</div>
                <div style={{ fontWeight: 700, fontSize: '1.5rem', color: '#c4b5fd' }}>{prediction.dailyDemandRate} <span style={{ fontSize: '0.9rem', color: '#a78bfa' }}>units/day</span></div>
              </div>
            </div>
          )}
          {prediction?.error && (
            <div style={{ marginTop: '16px', padding: '14px 18px', background: 'rgba(239,68,68,0.1)', border: '1px solid #f87171', borderRadius: '10px', color: '#f87171' }}>
              ⚠ {prediction.error}
            </div>
          )}
        </div>
      </div>

      {/* Inventory Risk Detection */}
      <div style={{ marginTop: '32px' }} className="glass-panel">
        <div style={{ padding: '24px', borderBottom: '1px solid var(--panel-border)' }}>
          <h2>🚦 Inventory Risk Detection</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '4px' }}>Real-time stock health across all products.</p>
        </div>
        <div style={{ padding: '24px' }}>
          {riskReport.length === 0 ? (
            <p style={{ color: 'var(--text-secondary)' }}>Loading risk data...</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {riskReport.map(item => {
                const style = RISK_STYLES[item.riskLevel] || RISK_STYLES.HEALTHY;
                return (
                  <div key={item.product?.id} style={{ display: 'flex', alignItems: 'center', gap: '16px', padding: '14px 18px', background: style.bg, border: `1px solid ${style.border}`, borderRadius: '10px' }}>
                    <span style={{ fontWeight: 700, color: style.color, minWidth: '110px', fontSize: '0.85rem' }}>{style.label}</span>
                    <span style={{ fontWeight: 600, flex: 1 }}>{item.product?.name}</span>
                    <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', minWidth: '80px', textAlign: 'right' }}>{item.product?.stockQuantity} units</span>
                    <span style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', flex: 2 }}>{item.explanation}</span>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
