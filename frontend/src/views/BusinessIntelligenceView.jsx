import { useState, useEffect } from 'react';

const API = (path, token) =>
  fetch(path, { headers: { Authorization: `Bearer ${token}` } }).then(r => r.json());

// ── Helpers ──────────────────────────────────────────────────────────────────

function AbcBadge({ cls }) {
  const map = {
    A: { bg: 'rgba(34,197,94,0.15)', color: '#4ade80', border: '#22c55e' },
    B: { bg: 'rgba(251,191,36,0.15)', color: '#fbbf24', border: '#f59e0b' },
    C: { bg: 'rgba(239,68,68,0.15)', color: '#f87171', border: '#ef4444' },
  };
  const s = map[cls] || map.C;
  return (
    <span className="abc-badge" style={{ background: s.bg, color: s.color, border: `1px solid ${s.border}` }}>
      Class {cls}
    </span>
  );
}

function ZScoreMeter({ zScore }) {
  const abs = Math.min(Math.abs(zScore), 5);
  const pct = (abs / 5) * 100;
  const color = abs >= 3.5 ? '#ef4444' : abs >= 2.5 ? '#f97316' : '#fbbf24';
  return (
    <div className="zscore-meter">
      <div className="zscore-bar" style={{ width: `${pct}%`, background: color }} />
    </div>
  );
}

function SparklineBar({ points, maxVal }) {
  if (!points.length) return <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>No data</span>;
  const peak = maxVal || Math.max(...points.map(p => p.totalUnitsSold), 1);
  return (
    <div className="sparkline-wrap">
      {points.map((p, i) => {
        const h = Math.max(4, Math.round((p.totalUnitsSold / peak) * 48));
        return (
          <div key={i} className="sparkline-bar-group" title={`${p.yearMonth}: ${p.totalUnitsSold} units`}>
            <div className="sparkline-bar" style={{ height: `${h}px` }} />
            <div className="sparkline-label">{p.yearMonth.slice(5)}</div>
          </div>
        );
      })}
    </div>
  );
}

// ── Main Component ────────────────────────────────────────────────────────────

export default function BusinessIntelligenceView({ user }) {
  const token = localStorage.getItem('token');

  const [activePanel, setActivePanel] = useState('abc');
  const [abc, setAbc] = useState(null);
  const [anomalies, setAnomalies] = useState(null);
  const [trends, setTrends] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const load = async (panel) => {
    setLoading(true);
    setError('');
    try {
      if (panel === 'abc' && !abc) {
        const data = await API('/api/bi/abc-analysis', token);
        setAbc(data);
      } else if (panel === 'anomalies' && !anomalies) {
        const data = await API('/api/bi/demand-anomalies', token);
        setAnomalies(data);
      } else if (panel === 'trends' && !trends) {
        const data = await API('/api/bi/seasonal-trends', token);
        setTrends(data);
      }
    } catch {
      setError('Failed to load data.');
    }
    setLoading(false);
  };

  useEffect(() => { load(activePanel); }, [activePanel]);

  const panels = [
    { id: 'abc',       icon: '📊', label: 'ABC Classification' },
    { id: 'anomalies', icon: '⚠️', label: 'Demand Anomalies' },
    { id: 'trends',    icon: '📅', label: 'Seasonal Trends' },
  ];

  // ── ABC Panel ──────────────────────────────────────────────────
  function AbcPanel() {
    if (!abc) return null;
    const { items = [], totalRevenue = 0, classACount = 0, classBCount = 0, classCCount = 0 } = abc;
    return (
      <div className="bi-panel">
        <h3 className="bi-panel-title">ABC Inventory Classification <span className="bi-subtitle">(Last 90 days, Pareto principle)</span></h3>

        {/* Summary cards */}
        <div className="bi-summary-row">
          {[
            { cls: 'A', count: classACount, label: '≤80% revenue', color: '#4ade80' },
            { cls: 'B', count: classBCount, label: '80–95% revenue', color: '#fbbf24' },
            { cls: 'C', count: classCCount, label: '95–100% revenue', color: '#f87171' },
          ].map(c => (
            <div key={c.cls} className="bi-summary-card" style={{ borderColor: c.color }}>
              <div className="bi-summary-class" style={{ color: c.color }}>Class {c.cls}</div>
              <div className="bi-summary-count">{c.count}</div>
              <div className="bi-summary-sub">{c.label}</div>
            </div>
          ))}
          <div className="bi-summary-card" style={{ borderColor: 'var(--accent)' }}>
            <div className="bi-summary-class" style={{ color: 'var(--accent)' }}>Total Revenue</div>
            <div className="bi-summary-count">${totalRevenue.toLocaleString(undefined, { maximumFractionDigits: 0 })}</div>
            <div className="bi-summary-sub">90-day period</div>
          </div>
        </div>

        {/* Classification table */}
        <div className="bi-table-wrap">
          <table className="bi-table">
            <thead>
              <tr>
                <th>Class</th><th>Product</th><th>Revenue (90d)</th><th>Units Sold</th><th>Share %</th><th>Cumulative %</th>
              </tr>
            </thead>
            <tbody>
              {items.length === 0 ? (
                <tr><td colSpan={6} className="bi-empty">No sales data found for this period. Record some sales first.</td></tr>
              ) : items.map((item, i) => (
                <tr key={i} className={`bi-row abc-row-${item.abcClass?.toLowerCase()}`}>
                  <td><AbcBadge cls={item.abcClass} /></td>
                  <td className="bi-product-name">{item.productName}</td>
                  <td>${item.revenueLast90Days?.toFixed(2)}</td>
                  <td>{item.unitsLast90Days}</td>
                  <td>{item.revenuePercent?.toFixed(1)}%</td>
                  <td>
                    <div className="cumulative-bar-wrap">
                      <div className="cumulative-bar" style={{ width: `${Math.min(item.cumulativePercent || 0, 100)}%` }} />
                      <span>{item.cumulativePercent?.toFixed(1)}%</span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  }

  // ── Anomalies Panel ────────────────────────────────────────────
  function AnomaliesPanel() {
    if (!anomalies) return null;
    const { anomalies: list = [], count = 0 } = anomalies;
    return (
      <div className="bi-panel">
        <h3 className="bi-panel-title">Demand Anomaly Detection <span className="bi-subtitle">(Z-score ≥ 2.0 vs 60-day baseline)</span></h3>
        {count === 0 ? (
          <div className="bi-empty-state">
            <div className="bi-empty-icon">✅</div>
            <div className="bi-empty-msg">No anomalies detected — all products are within normal demand variance.</div>
          </div>
        ) : (
          <div className="anomaly-grid">
            {list.map((a, i) => {
              const isSpike = a.type === 'SPIKE';
              const color = isSpike ? '#f97316' : '#818cf8';
              return (
                <div key={i} className="anomaly-card" style={{ borderColor: color }}>
                  <div className="anomaly-header">
                    <span className="anomaly-icon">{isSpike ? '🔺' : '🔻'}</span>
                    <span className="anomaly-type" style={{ color }}>{a.type}</span>
                    <span className="anomaly-name">{a.productName}</span>
                  </div>
                  <div className="anomaly-stats">
                    <div className="anomaly-stat">
                      <span className="anomaly-stat-label">Current (7d avg)</span>
                      <span className="anomaly-stat-value">{a.currentAvgDailyDemand?.toFixed(1)} <small>units/day</small></span>
                    </div>
                    <div className="anomaly-stat">
                      <span className="anomaly-stat-label">Historical (60d avg)</span>
                      <span className="anomaly-stat-value">{a.historicalAvgDailyDemand?.toFixed(1)} <small>units/day</small></span>
                    </div>
                    <div className="anomaly-stat">
                      <span className="anomaly-stat-label">Deviation</span>
                      <span className="anomaly-stat-value" style={{ color: isSpike ? '#f97316' : '#818cf8' }}>
                        {a.deviationPct >= 0 ? '+' : ''}{a.deviationPct?.toFixed(1)}%
                      </span>
                    </div>
                  </div>
                  <div className="anomaly-zscore-row">
                    <span className="anomaly-stat-label">Z-Score: {a.zScore?.toFixed(2)}</span>
                    <ZScoreMeter zScore={a.zScore} />
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    );
  }

  // ── Trends Panel ───────────────────────────────────────────────
  function TrendsPanel() {
    if (!trends) return null;
    const { trends: list = [] } = trends;

    // Group by productId
    const byProduct = {};
    for (const point of list) {
      if (!byProduct[point.productId]) byProduct[point.productId] = { name: point.productName, points: [] };
      byProduct[point.productId].points.push(point);
    }
    const products = Object.values(byProduct);
    const globalMax = Math.max(...list.map(p => p.totalUnitsSold), 1);

    return (
      <div className="bi-panel">
        <h3 className="bi-panel-title">Seasonal Demand Trends <span className="bi-subtitle">(Monthly units sold — last 6 months)</span></h3>
        {products.length === 0 ? (
          <div className="bi-empty-state">
            <div className="bi-empty-icon">📅</div>
            <div className="bi-empty-msg">No sales recorded in the last 6 months yet.</div>
          </div>
        ) : (
          <div className="trends-grid">
            {products.map((prod, i) => {
              const peak = Math.max(...prod.points.map(p => p.totalUnitsSold), 1);
              const total = prod.points.reduce((s, p) => s + p.totalUnitsSold, 0);
              return (
                <div key={i} className="trend-card">
                  <div className="trend-header">
                    <span className="trend-name">{prod.name}</span>
                    <span className="trend-total">{total} total units</span>
                  </div>
                  <SparklineBar points={prod.points} maxVal={globalMax} />
                </div>
              );
            })}
          </div>
        )}
      </div>
    );
  }

  // ── Render ─────────────────────────────────────────────────────
  return (
    <div className="bi-view">
      <div className="bi-header">
        <h2 className="bi-title">💡 Business Intelligence</h2>
        <p className="bi-desc">ABC inventory classification, demand anomaly detection, and seasonal trend analysis — all computed from your real sales data.</p>
      </div>

      {/* Panel switcher */}
      <div className="bi-tabs">
        {panels.map(p => (
          <button
            key={p.id}
            className={`bi-tab ${activePanel === p.id ? 'active' : ''}`}
            onClick={() => setActivePanel(p.id)}
          >
            {p.icon} {p.label}
          </button>
        ))}
      </div>

      {loading && <div className="bi-loading"><div className="loading-spinner" />Loading analysis…</div>}
      {error && <div className="bi-error">{error}</div>}

      {!loading && !error && (
        <>
          {activePanel === 'abc' && <AbcPanel />}
          {activePanel === 'anomalies' && <AnomaliesPanel />}
          {activePanel === 'trends' && <TrendsPanel />}
        </>
      )}
    </div>
  );
}
