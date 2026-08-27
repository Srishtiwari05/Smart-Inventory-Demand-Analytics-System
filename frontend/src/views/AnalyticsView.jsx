import { useState, useEffect } from 'react';

export default function AnalyticsView() {
  const [report, setReport] = useState(null);
  const [recommendation, setRecommendation] = useState(null);
  const [productId, setProductId] = useState('');

  useEffect(() => {
    fetchReport();
  }, []);

  const fetchReport = async () => {
    try {
      const res = await fetch('/api/sales/report', {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
      });
      const data = await res.json();
      setReport(data);
    } catch (err) {
      console.error(err);
    }
  };

  const checkRecommendation = async (e) => {
    e.preventDefault();
    if (!productId) return;
    
    try {
      const res = await fetch(`/api/analytics/recommendations/${productId}`, {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
      });
      if (res.ok) {
        const data = await res.json();
        setRecommendation(data);
      } else {
        alert("Product not found or error occurred");
      }
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Demand Analytics</h1>
      </div>

      {report && (
        <div className="grid-cards" style={{marginTop: '24px'}}>
          <div className="stat-card glass-panel">
            <div className="stat-title">Total Revenue</div>
            <div className="stat-value text-success">${report.totalRevenue.toFixed(2)}</div>
          </div>
          <div className="stat-card glass-panel">
            <div className="stat-title">Total Orders</div>
            <div className="stat-value">{report.totalOrders}</div>
          </div>
          <div className="stat-card glass-panel">
            <div className="stat-title">Average Order Value</div>
            <div className="stat-value">${report.averageOrderValue.toFixed(2)}</div>
          </div>
        </div>
      )}

      <div style={{marginTop: '40px'}} className="glass-panel">
        <div style={{padding: '24px', borderBottom: '1px solid var(--panel-border)'}}>
          <h2>Reorder Recommendations</h2>
          <p>Check if a specific product needs to be restocked based on historical demand.</p>
        </div>
        
        <div style={{padding: '24px'}}>
          <form onSubmit={checkRecommendation} style={{display: 'flex', gap: '12px', maxWidth: '400px'}}>
            <input 
              type="number" 
              placeholder="Enter Product ID (e.g. 1)" 
              value={productId}
              onChange={e => setProductId(e.target.value)}
              required
            />
            <button type="submit" className="btn btn-primary">Analyze</button>
          </form>

          {recommendation && (
            <div style={{marginTop: '24px', padding: '16px', background: 'rgba(0,0,0,0.2)', borderRadius: '8px'}}>
              <h3>{recommendation.product.name} (ID: #{recommendation.product.id})</h3>
              <p style={{margin: '12px 0'}}>
                <span className={`badge ${recommendation.needsReorder ? 'badge-danger' : 'badge-success'}`}>
                  {recommendation.needsReorder ? 'Reorder Needed' : 'Stock Healthy'}
                </span>
              </p>
              <p>{recommendation.reason}</p>
              {recommendation.needsReorder && (
                <p style={{marginTop: '8px', fontWeight: 'bold', color: 'var(--warning)'}}>
                  Recommended Reorder Amount: {recommendation.recommendedAmount} units
                </p>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
