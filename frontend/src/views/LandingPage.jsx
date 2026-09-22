import React, { useState, useEffect } from 'react';

const DEMO_PRODUCTS = [
  { id: 1, name: 'Wireless Ergonomic Mouse', price: 29.99, currentStock: 45, baseDemand: 1.8, leadTime: 4 },
  { id: 2, name: 'Mechanical Gaming Keyboard', price: 79.99, currentStock: 12, baseDemand: 2.5, leadTime: 5 },
  { id: 3, name: 'USB-C Ultra Docking Station', price: 119.99, currentStock: 5, baseDemand: 1.2, leadTime: 7 },
  { id: 4, name: 'Heavy Duty Ergonomic Chair', price: 249.99, currentStock: 8, baseDemand: 0.9, leadTime: 10 }
];

export default function LandingPage({ onOpenLogin, onOpenRegister }) {
  const [stats, setStats] = useState({
    totalOrganizations: 2,
    totalProductsTracked: 6,
    totalTransactionsProcessed: 17,
    totalOrdersFilled: 8,
    totalForecastsGenerated: 25,
    averageStockoutReductionPct: 38.5
  });
  const [statsLoading, setStatsLoading] = useState(true);

  // Interactive Demo Sandbox State
  const [selectedProduct, setSelectedProduct] = useState(DEMO_PRODUCTS[0]);
  const [demandMultiplier, setDemandMultiplier] = useState(1.5);
  const [extraLeadTime, setExtraLeadTime] = useState(3);
  const [priceAdjustmentPct, setPriceAdjustmentPct] = useState(10);

  // Hero Preview Mockup Active Tab
  const [activeHeroTab, setActiveHeroTab] = useState('overview');

  // Pricing Toggle
  const [isAnnual, setIsAnnual] = useState(true);

  // Fetch anonymous live platform stats
  useEffect(() => {
    fetch('/api/platform/stats')
      .then(res => res.ok ? res.json() : null)
      .then(data => {
        if (data) {
          setStats(data);
        }
      })
      .catch(err => {
        console.warn('Using default platform stats:', err);
      })
      .finally(() => {
        setStatsLoading(false);
      });
  }, []);

  // Sandbox calculations
  const baseDailyDemand = selectedProduct.baseDemand;
  const simDailyDemand = +(baseDailyDemand * demandMultiplier).toFixed(2);
  const baseLeadTime = selectedProduct.leadTime;
  const simLeadTime = baseLeadTime + extraLeadTime;

  const baseSafetyStock = Math.ceil(baseLeadTime * baseDailyDemand * 0.15);
  const simSafetyStock = Math.ceil(simLeadTime * simDailyDemand * 0.15);

  const baseROP = Math.ceil(baseLeadTime * baseDailyDemand) + baseSafetyStock;
  const simROP = Math.ceil(simLeadTime * simDailyDemand) + simSafetyStock;

  const baseStockoutDays = Math.max(1, Math.floor(selectedProduct.currentStock / baseDailyDemand));
  const simStockoutDays = Math.max(1, Math.floor(selectedProduct.currentStock / simDailyDemand));

  const baseRequiredReorder = selectedProduct.currentStock <= baseROP ? Math.max(20, (baseROP * 2) - selectedProduct.currentStock) : 0;
  const simRequiredReorder = selectedProduct.currentStock <= simROP ? Math.max(35, (simROP * 2) - selectedProduct.currentStock) : 0;

  const reorderQtyDelta = simRequiredReorder - baseRequiredReorder;
  const stockoutDeltaDays = simStockoutDays - baseStockoutDays;
  const effectiveSimPrice = +(selectedProduct.price * (1 + priceAdjustmentPct / 100)).toFixed(2);
  const estimatedCostDelta = +((simRequiredReorder * effectiveSimPrice) - (baseRequiredReorder * selectedProduct.price)).toFixed(2);

  const getRiskLevel = (stock, safety, rop) => {
    if (stock <= safety) return { label: 'CRITICAL', color: '#ef4444' };
    if (stock <= rop) return { label: 'RISK', color: '#f59e0b' };
    if (stock <= rop * 1.5) return { label: 'WATCH', color: '#eab308' };
    return { label: 'HEALTHY', color: '#10b981' };
  };

  const baseRisk = getRiskLevel(selectedProduct.currentStock, baseSafetyStock, baseROP);
  const simRisk = getRiskLevel(selectedProduct.currentStock, simSafetyStock, simROP);

  return (
    <div className="landing-wrapper">
      {/* Background Animated Gradient Mesh & Matrix Backdrop */}
      <div className="landing-bg-animation">
        <div className="bg-glow-orb orb-1"></div>
        <div className="bg-glow-orb orb-2"></div>
        <div className="bg-glow-orb orb-3"></div>
        <div className="bg-perspective-grid"></div>
      </div>

      {/* Top Announcement Bar */}
      <div className="landing-announcement-bar">
        <div className="announcement-content">
          <span className="announcement-pill">NEW RELEASE</span>
          <span className="announcement-text">
            StockWise 2.0 — Enterprise Business Intelligence, Z-Score Anomaly Alerts & Automated Procurement Engine
          </span>
          <span className="announcement-badge">Backed by BUSZ</span>
        </div>
      </div>

      {/* Sticky Corporate Navigation Bar */}
      <nav className="landing-nav">
        <div className="landing-nav-container">
          <div className="landing-brand">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" className="brand-icon-svg">
              <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" fill="url(#bolt-nav-grad)"/>
              <defs>
                <linearGradient id="bolt-nav-grad" x1="3" y1="2" x2="21" y2="22" gradientUnits="userSpaceOnUse">
                  <stop stopColor="#a78bfa"/>
                  <stop offset="1" stopColor="#6366f1"/>
                </linearGradient>
              </defs>
            </svg>
            <span className="brand-name">StockWise</span>
            <span className="brand-subpill">BACKED BY BUSZ</span>
          </div>

          <div className="landing-nav-links">
            <a href="#solutions">Solutions</a>
            <a href="#simulator">Live Sandbox</a>
            <a href="#preview">Platform Preview</a>
            <a href="#architecture">Architecture</a>
            <a href="#pricing">Pricing</a>
          </div>

          <div className="landing-nav-actions">
            <div className="live-status-pill">
              <span className="status-dot"></span>
              <span>System Operational</span>
            </div>
            <button className="btn-secondary-landing" onClick={onOpenLogin}>
              Sign In
            </button>
            <button className="btn-primary-landing" onClick={onOpenRegister}>
              Launch Workspace
            </button>
          </div>
        </div>
      </nav>

      {/* Modern Split-Grid Hero Section */}
      <header className="landing-hero">
        <div className="hero-split-grid">
          {/* Left Column: Headline & Action Group */}
          <div className="hero-text-side">
            <div className="hero-badge">
              <span className="badge-sparkle">✦</span>
              <span>ENTERPRISE OPERATING SYSTEM • BACKED BY BUSZ</span>
            </div>

            <h1 className="hero-title">
              The Operating System for <br />
              <span className="hero-gradient-text">Modern Supply Chains.</span>
            </h1>

            <p className="hero-subtitle">
              StockWise unifies multi-location sales streams, inventory movements, and supplier lead times into real-time operational decision intelligence for commercial enterprises.
            </p>

            <div className="hero-cta-group">
              <a href="#simulator" className="btn-hero-primary">
                <span>Explore Decision Engine →</span>
              </a>
              <button onClick={onOpenRegister} className="btn-hero-secondary">
                <span>Start Free 14-Day Pilot</span>
              </button>
            </div>

            <div className="hero-trust-bar">
              <span>SOC-2 Isolated</span>
              <span>Sub-10ms Raw JDBC</span>
              <span>Salted SHA-256</span>
              <span>Docker Ready</span>
            </div>
          </div>

          {/* Right Column: Live Interactive Workspace Dashboard Preview */}
          <div id="preview" className="hero-preview-side">
            <div className="preview-window-wrapper">
              <div className="preview-window glass-panel">
                <div className="preview-window-header">
                  <div className="window-dots">
                    <span className="dot red"></span>
                    <span className="dot yellow"></span>
                    <span className="dot green"></span>
                  </div>
                  <div className="window-title-bar">
                    <span>stockwise.busz.app/workspace/demo-enterprise</span>
                  </div>
                  <div className="window-status-badge">LIVE STREAM</div>
                </div>

                <div className="preview-tab-bar">
                  <button 
                    className={`preview-tab-btn ${activeHeroTab === 'overview' ? 'active' : ''}`}
                    onClick={() => setActiveHeroTab('overview')}
                  >
                    Overview
                  </button>
                  <button 
                    className={`preview-tab-btn ${activeHeroTab === 'bi' ? 'active' : ''}`}
                    onClick={() => setActiveHeroTab('bi')}
                  >
                    ABC Pareto
                  </button>
                  <button 
                    className={`preview-tab-btn ${activeHeroTab === 'suppliers' ? 'active' : ''}`}
                    onClick={() => setActiveHeroTab('suppliers')}
                  >
                    Suppliers & POs
                  </button>
                  <button 
                    className={`preview-tab-btn ${activeHeroTab === 'telemetry' ? 'active' : ''}`}
                    onClick={() => setActiveHeroTab('telemetry')}
                  >
                    ML Telemetry
                  </button>
                </div>

                <div className="preview-content-viewport">
                  {activeHeroTab === 'overview' && (
                    <div className="preview-screen-content">
                      <div className="preview-metrics-grid">
                        <div className="preview-metric-card">
                          <span className="metric-title">Portfolio Valuation</span>
                          <strong className="metric-val">$29,324.75</strong>
                          <span className="metric-change positive">↑ +14.2% vs last month</span>
                        </div>
                        <div className="preview-metric-card">
                          <span className="metric-title">Active SKUs Monitored</span>
                          <strong className="metric-val">1,248 SKUs</strong>
                          <span className="metric-change positive">100% Tenant Isolated</span>
                        </div>
                        <div className="preview-metric-card">
                          <span className="metric-title">Stockout Prevention</span>
                          <strong className="metric-val">98.6%</strong>
                          <span className="metric-change positive">Zero Unplanned Outages</span>
                        </div>
                        <div className="preview-metric-card">
                          <span className="metric-title">Active POs</span>
                          <strong className="metric-val">4 Orders</strong>
                          <span className="metric-change neutral">Auto-Reordered</span>
                        </div>
                      </div>

                      <div className="preview-chart-row">
                        <div className="preview-chart-card">
                          <div className="chart-header-mini">
                            <span>Demand Velocity & Rolling Reorder Threshold</span>
                            <span className="badge-mini green">LIVE STREAM</span>
                          </div>
                          <div className="chart-bars-simulated">
                            {[65, 80, 45, 90, 110, 85, 130, 95, 120, 140, 115, 150].map((h, i) => (
                              <div key={i} className="sim-bar-wrapper">
                                <div className="sim-bar" style={{ height: `${h * 0.5}px` }}></div>
                                <span className="sim-bar-lbl">D{i + 1}</span>
                              </div>
                            ))}
                          </div>
                        </div>
                      </div>
                    </div>
                  )}

                  {activeHeroTab === 'bi' && (
                    <div className="preview-screen-content">
                      <div className="preview-bi-row">
                        <div className="preview-bi-card">
                          <h4>Pareto ABC Revenue Segmentation</h4>
                          <div className="abc-seg-bar">
                            <div className="seg-part class-a" style={{ width: '80%' }}>Class A (80% Rev)</div>
                            <div className="seg-part class-b" style={{ width: '15%' }}>Class B (15%)</div>
                            <div className="seg-part class-c" style={{ width: '5%' }}>Class C</div>
                          </div>
                          <p className="preview-subtext">Class A SKUs generate 80% of revenue and receive 3x safety stock allocation.</p>
                        </div>

                        <div className="preview-bi-card">
                          <h4>Demand Anomaly Radar (|Z| &ge; 2.0)</h4>
                          <div className="anomaly-preview-item">
                            <span className="anomaly-badge spike">SPIKE ALERT</span>
                            <span>Mechanical Gaming Keyboard (Z = +2.45)</span>
                            <strong className="anomaly-action">Reorder Triggered</strong>
                          </div>
                        </div>
                      </div>
                    </div>
                  )}

                  {activeHeroTab === 'suppliers' && (
                    <div className="preview-screen-content">
                      <div className="preview-table-wrapper">
                        <table className="preview-table">
                          <thead>
                            <tr>
                              <th>Supplier Name</th>
                              <th>Lead Time</th>
                              <th>On-Time %</th>
                              <th>Quotation Status</th>
                              <th>Action</th>
                            </tr>
                          </thead>
                          <tbody>
                            <tr>
                              <td><strong>TechCorp Logistics</strong></td>
                              <td>4 Days</td>
                              <td><span className="pill-green">96.8%</span></td>
                              <td><span className="pill-purple">Quote Received ($24.50)</span></td>
                              <td><button className="btn-xs">Generate PO</button></td>
                            </tr>
                            <tr>
                              <td><strong>Global Hardware Hub</strong></td>
                              <td>7 Days</td>
                              <td><span className="pill-yellow">88.2%</span></td>
                              <td><span className="pill-gray">RFQ Pending</span></td>
                              <td><button className="btn-xs-outline">Send RFQ</button></td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                    </div>
                  )}

                  {activeHeroTab === 'telemetry' && (
                    <div className="preview-screen-content">
                      <div className="preview-telemetry-grid">
                        <div className="telemetry-card-mini">
                          <span>Model MAPE (Error Rate)</span>
                          <strong>4.12%</strong>
                          <small>Target &lt; 5.0%</small>
                        </div>
                        <div className="telemetry-card-mini">
                          <span>Forecast Accuracy</span>
                          <strong className="text-green">95.88%</strong>
                          <small>Calibrated vs actuals</small>
                        </div>
                        <div className="telemetry-card-mini">
                          <span>Acceptance Rate</span>
                          <strong>94.2%</strong>
                          <small>Manager approvals</small>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Enterprise Industry Marquee Bar */}
      <section className="landing-marquee-section">
        <div className="marquee-label">TRUSTED BY COMMERCIAL OPERATIONS WORLDWIDE</div>
        <div className="marquee-container">
          <div className="marquee-track">
            <span>SUPERMARKET CHAINS</span>
            <span className="dot">•</span>
            <span>ELECTRONICS DISTRIBUTORS</span>
            <span className="dot">•</span>
            <span>HARDWARE & INDUSTRIAL NETWORKS</span>
            <span className="dot">•</span>
            <span>GENERAL WHOLESALE HUB</span>
            <span className="dot">•</span>
            <span>RESTAURANT & F&B LOGISTICS</span>
            <span className="dot">•</span>
            <span>PHARMACEUTICAL SUPPLY</span>
            <span className="dot">•</span>
            <span>SUPERMARKET CHAINS</span>
            <span className="dot">•</span>
            <span>ELECTRONICS DISTRIBUTORS</span>
            <span className="dot">•</span>
            <span>HARDWARE & INDUSTRIAL NETWORKS</span>
            <span className="dot">•</span>
            <span>GENERAL WHOLESALE HUB</span>
            <span className="dot">•</span>
            <span>RESTAURANT & F&B LOGISTICS</span>
            <span className="dot">•</span>
            <span>PHARMACEUTICAL SUPPLY</span>
            <span className="dot">•</span>
          </div>
        </div>
      </section>

      {/* Live Platform Metrics Ticker (Phase 28) */}
      <section className="landing-stats-ticker">
        <div className="stats-ticker-container">
          <div className="stats-header-tag">
            <span className="pulse-indicator"></span>
            <strong>LIVE PLATFORM METRICS</strong> — Anonymous Multi-Tenant Benchmarks
          </div>

          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-number">{statsLoading ? '...' : stats.totalOrganizations}</div>
              <div className="stat-label">Active Enterprises</div>
              <div className="stat-sub">Isolated business tenants</div>
            </div>

            <div className="stat-card">
              <div className="stat-number">{statsLoading ? '...' : stats.totalProductsTracked}</div>
              <div className="stat-label">SKUs Under Management</div>
              <div className="stat-sub">Real-time stock monitoring</div>
            </div>

            <div className="stat-card">
              <div className="stat-number">{statsLoading ? '...' : stats.totalTransactionsProcessed}</div>
              <div className="stat-label">Stock Transactions</div>
              <div className="stat-sub">Atomic RESTOCK & SALES</div>
            </div>

            <div className="stat-card">
              <div className="stat-number">{statsLoading ? '...' : stats.totalOrdersFilled}</div>
              <div className="stat-label">Orders Completed</div>
              <div className="stat-sub">Automated inventory sync</div>
            </div>

            <div className="stat-card stat-card-highlight">
              <div className="stat-number stat-number-accent">
                {statsLoading ? '...' : `${stats.averageStockoutReductionPct}%`}
              </div>
              <div className="stat-label">Stockout Reduction</div>
              <div className="stat-sub">Customer benchmark average</div>
            </div>
          </div>
        </div>
      </section>

      {/* Solutions / Capability Modules */}
      <section id="solutions" className="landing-section">
        <div className="section-header">
          <div className="section-pill">ENTERPRISE CAPABILITIES</div>
          <h2>Everything Needed to Automate Supply Chains</h2>
          <p>
            StockWise replaces spreadsheets and manual guesswork with an integrated operating system for multi-tenant B2B inventory management.
          </p>
        </div>

        <div className="capabilities-grid">
          <div className="capability-card glass-panel">
            <div className="module-num">01</div>
            <h3>Autonomous Reordering Engine</h3>
            <p>Calculates dynamic Safety Stock & Reorder Points (ROP) using lead times and daily demand velocity.</p>
            <ul className="card-features">
              <li>✓ Automated Stockout Projections</li>
              <li>✓ Dynamic Lead Time Compliance</li>
              <li>✓ Low-Stock Risk Alerts</li>
            </ul>
          </div>

          <div className="capability-card glass-panel">
            <div className="module-num">02</div>
            <h3>ABC Pareto & Anomaly Analytics</h3>
            <p>Categorizes product catalogs into Class A, B, and C revenue tiers and flags Z-score demand anomalies.</p>
            <ul className="card-features">
              <li>✓ 90-Day Pareto Revenue Class</li>
              <li>✓ Z-Score Spike & Crash Radar</li>
              <li>✓ Seasonal Velocity Sparklines</li>
            </ul>
          </div>

          <div className="capability-card glass-panel">
            <div className="module-num">03</div>
            <h3>Supplier Intelligence & Portals</h3>
            <p>Tracks on-time supplier delivery, manages RFQ quotations, and provides a dedicated supplier portal.</p>
            <ul className="card-features">
              <li>✓ 0-100% Supplier Reliability Score</li>
              <li>✓ Automated RFQ Quote Comparison</li>
              <li>✓ One-Click Purchase Order Issuance</li>
            </ul>
          </div>

          <div className="capability-card glass-panel">
            <div className="module-num">04</div>
            <h3>What-If Scenario Simulator</h3>
            <p>Simulates demand multipliers, supplier delays, and price adjustments before placing purchase orders.</p>
            <ul className="card-features">
              <li>✓ Financial Impact Estimator</li>
              <li>✓ Side-by-Side Baseline Comparison</li>
              <li>✓ Actionable Decision Explanations</li>
            </ul>
          </div>

          <div className="capability-card glass-panel">
            <div className="module-num">05</div>
            <h3>Forecast Calibration Telemetry</h3>
            <p>Measures MAPE, MAE, and actual vs predicted sales variance to ensure ML pipeline precision.</p>
            <ul className="card-features">
              <li>✓ SKU Level Forecast Error Logs</li>
              <li>✓ Machine Learning Health Monitoring</li>
              <li>✓ Recommendation Acceptance Metrics</li>
            </ul>
          </div>

          <div className="capability-card glass-panel">
            <div className="module-num">06</div>
            <h3>Multi-Tenancy & Audit Governance</h3>
            <p>Organically isolates organizational data with strict RBAC permissions and immutable audit trails.</p>
            <ul className="card-features">
              <li>✓ Multi-Tenant org_id Isolation</li>
              <li>✓ Salted SHA-256 Password Security</li>
              <li>✓ Comprehensive Audit Logs</li>
            </ul>
          </div>
        </div>
      </section>

      {/* Interactive Sandbox Simulator Demo */}
      <section id="simulator" className="landing-section simulator-section">
        <div className="section-header">
          <div className="section-pill">INTERACTIVE DECISION ENGINE</div>
          <h2>Experience the What-If Inventory Simulator</h2>
          <p>
            Simulate demand surges, supply chain delivery delays, and price inflation in real-time before issuing real purchase orders.
          </p>
        </div>

        <div className="sandbox-card glass-panel">
          <div className="sandbox-top-bar">
            <div className="sandbox-product-picker">
              <label>Select Demo SKU:</label>
              <select
                value={selectedProduct.id}
                onChange={e => {
                  const p = DEMO_PRODUCTS.find(x => x.id === parseInt(e.target.value));
                  if (p) setSelectedProduct(p);
                }}
                className="sandbox-select"
              >
                {DEMO_PRODUCTS.map(p => (
                  <option key={p.id} value={p.id}>
                    {p.name} (${p.price} | Stock: {p.currentStock})
                  </option>
                ))}
              </select>
            </div>

            <div className="sandbox-status-badges">
              <span className="sandbox-badge" style={{ borderColor: baseRisk.color, color: baseRisk.color }}>
                Baseline Risk: {baseRisk.label}
              </span>
              <span className="sandbox-arrow">➔</span>
              <span className="sandbox-badge" style={{ borderColor: simRisk.color, color: simRisk.color, background: `${simRisk.color}15` }}>
                Simulated Risk: {simRisk.label}
              </span>
            </div>
          </div>

          <div className="sandbox-grid">
            {/* Controls */}
            <div className="sandbox-controls">
              <div className="control-group">
                <div className="control-label">
                  <span>Demand Surge Multiplier</span>
                  <strong className="control-val">{demandMultiplier.toFixed(1)}x ({((demandMultiplier - 1) * 100).toFixed(0)}%)</strong>
                </div>
                <input
                  type="range"
                  min="0.5"
                  max="3.0"
                  step="0.1"
                  value={demandMultiplier}
                  onChange={e => setDemandMultiplier(parseFloat(e.target.value))}
                  className="sandbox-slider"
                />
                <div className="slider-hints"><span>0.5x Slow</span><span>1.0x Normal</span><span>3.0x Viral Surge</span></div>
              </div>

              <div className="control-group">
                <div className="control-label">
                  <span>Supplier Lead Time Delay</span>
                  <strong className="control-val">+{extraLeadTime} Days (Total: {simLeadTime}d)</strong>
                </div>
                <input
                  type="range"
                  min="0"
                  max="14"
                  step="1"
                  value={extraLeadTime}
                  onChange={e => setExtraLeadTime(parseInt(e.target.value))}
                  className="sandbox-slider"
                />
                <div className="slider-hints"><span>+0d On Time</span><span>+7d Delayed</span><span>+14d Severe</span></div>
              </div>

              <div className="control-group">
                <div className="control-label">
                  <span>Supplier Price Inflation</span>
                  <strong className="control-val">+{priceAdjustmentPct}% (${effectiveSimPrice}/unit)</strong>
                </div>
                <input
                  type="range"
                  min="-20"
                  max="50"
                  step="5"
                  value={priceAdjustmentPct}
                  onChange={e => setPriceAdjustmentPct(parseInt(e.target.value))}
                  className="sandbox-slider"
                />
                <div className="slider-hints"><span>-20% Discount</span><span>0% Baseline</span><span>+50% Hike</span></div>
              </div>
            </div>

            {/* Simulated Results Table & Autonomous Explanation */}
            <div className="sandbox-results">
              <div className="sandbox-table-wrapper">
                <table className="sim-comparison-table">
                  <thead>
                    <tr>
                      <th>DECISION METRIC</th>
                      <th>BASELINE</th>
                      <th>SIMULATED</th>
                      <th>NET IMPACT</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td>Daily Demand Rate</td>
                      <td>{baseDailyDemand} units/d</td>
                      <td>{simDailyDemand} units/d</td>
                      <td>
                        <span className="impact-pill warning">
                          +{((demandMultiplier - 1) * 100).toFixed(0)}%
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <td>Supplier Lead Time</td>
                      <td>{baseLeadTime} days</td>
                      <td>{simLeadTime} days</td>
                      <td>
                        <span className={`impact-pill ${extraLeadTime > 0 ? 'danger' : 'neutral'}`}>
                          {extraLeadTime > 0 ? `+${extraLeadTime} days` : '0 days'}
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <td>Safety Stock Buffer</td>
                      <td>{baseSafetyStock} units</td>
                      <td>{simSafetyStock} units</td>
                      <td>
                        <span className="impact-pill purple">
                          +{simSafetyStock - baseSafetyStock} units
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <td>Projected Stockout</td>
                      <td>{baseStockoutDays} days</td>
                      <td className={simStockoutDays < baseStockoutDays ? 'val-warning' : 'val-healthy'}>
                        {simStockoutDays} days
                      </td>
                      <td>
                        <span className={`impact-pill ${stockoutDeltaDays < 0 ? 'danger' : 'success'}`}>
                          {stockoutDeltaDays >= 0 ? `+${stockoutDeltaDays}` : stockoutDeltaDays} days
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <td>Recommended Order Qty</td>
                      <td>{baseRequiredReorder} units</td>
                      <td className="val-info">{simRequiredReorder} units</td>
                      <td>
                        <span className="impact-pill info">
                          +{reorderQtyDelta} units
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <td>Estimated Financial Budget</td>
                      <td>${(baseRequiredReorder * selectedProduct.price).toFixed(2)}</td>
                      <td>${(simRequiredReorder * effectiveSimPrice).toFixed(2)}</td>
                      <td>
                        <span className="impact-pill info">
                          {estimatedCostDelta >= 0 ? `+$${estimatedCostDelta.toFixed(2)}` : `-$${Math.abs(estimatedCostDelta).toFixed(2)}`}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>

                <div className="sandbox-explanation-box">
                  <div className="explanation-header">
                    <strong>Autonomous Decision Recommendation:</strong>
                  </div>
                  <div className="explanation-body">
                    {simStockoutDays <= simLeadTime ? (
                      <span style={{ color: '#f87171' }}>
                        <strong>Critical Outage Window:</strong> Projected stockout in {simStockoutDays} days will occur before lead-time delivery ({simLeadTime} days). Trigger urgent purchase order of {simRequiredReorder} units.
                      </span>
                    ) : (
                      <span>
                        <strong>Stable Window:</strong> Current stock covers projected lead-time demand. Monitor safety stock buffer of {simSafetyStock} units.
                      </span>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Architecture & Security Showcase */}
      <section id="architecture" className="landing-section">
        <div className="section-header">
          <div className="section-pill">ENTERPRISE HARDENING</div>
          <h2>Built for High Concurrency & Multi-Tenant Scale</h2>
          <p>
            Engineered with Java 17, Spring Boot 3.2, raw JDBC HikariCP connection pooling, and scikit-learn machine learning.
          </p>
        </div>

        <div className="architecture-grid">
          <div className="arch-card glass-panel">
            <span className="arch-num">01</span>
            <h4>Raw JDBC & HikariCP Pool</h4>
            <p>Zero ORM overhead. Direct parameterized SQL queries optimized with compound database indexes.</p>
          </div>

          <div className="arch-card glass-panel">
            <span className="arch-num">02</span>
            <h4>Salted Security & Rate Limits</h4>
            <p>Salted SHA-256 password hashing, 24-hour token revocation, IP rate limiting (10 req/min), and XSS headers.</p>
          </div>

          <div className="arch-card glass-panel">
            <span className="arch-num">03</span>
            <h4>Multi-Tenant Organization Isolation</h4>
            <p>Every SQL query strictly filters by <code>org_id</code>, ensuring absolute tenant privacy.</p>
          </div>

          <div className="arch-card glass-panel">
            <span className="arch-num">04</span>
            <h4>Production Docker Orchestration</h4>
            <p>Multi-stage Dockerfiles for Nginx, Spring Boot, Python ML, and MySQL with health check dependency wiring.</p>
          </div>
        </div>
      </section>

      {/* SaaS Pricing Matrix */}
      <section id="pricing" className="landing-section">
        <div className="section-header">
          <div className="section-pill">TRANSPARENT PRICING</div>
          <h2>Plans for Single Shops to Large Distributors</h2>
          <p>Start with a 14-day free pilot. Upgrade as your product catalog and warehouse locations scale.</p>

          <div className="pricing-toggle-bar">
            <span className={!isAnnual ? 'active' : ''}>Monthly Billing</span>
            <button 
              className={`toggle-switch ${isAnnual ? 'annual' : ''}`}
              onClick={() => setIsAnnual(!isAnnual)}
            >
              <div className="switch-knob"></div>
            </button>
            <span className={isAnnual ? 'active' : ''}>
              Annual Billing <span className="discount-badge">Save 20%</span>
            </span>
          </div>
        </div>

        <div className="pricing-grid">
          <div className="pricing-card glass-panel">
            <div className="plan-name">Starter</div>
            <div className="plan-price">
              <strong>{isAnnual ? '$49' : '$59'}</strong>
              <span>/month</span>
            </div>
            <p className="plan-desc">For single retail shops and small storefronts.</p>
            <ul className="plan-features">
              <li>✓ Up to 500 Product SKUs</li>
              <li>✓ Autonomous ROP Reorder Engine</li>
              <li>✓ Basic Stockout Alerts</li>
              <li>✓ 2 User Accounts (Owner & Staff)</li>
            </ul>
            <button className="btn-plan-outline" onClick={onOpenRegister}>Start Starter Trial</button>
          </div>

          <div className="pricing-card glass-panel popular">
            <div className="popular-badge">MOST POPULAR</div>
            <div className="plan-name">Professional</div>
            <div className="plan-price">
              <strong>{isAnnual ? '$149' : '$179'}</strong>
              <span>/month</span>
            </div>
            <p className="plan-desc">For growing retail chains and regional wholesalers.</p>
            <ul className="plan-features">
              <li>✓ Up to 5,000 Product SKUs</li>
              <li>✓ ABC Pareto Revenue Segmentation</li>
              <li>✓ Z-Score Demand Anomaly Alerts</li>
              <li>✓ Machine Learning Forecast Telemetry</li>
              <li>✓ Supplier Quotation & PO Management</li>
              <li>✓ Unlimited Staff Accounts</li>
            </ul>
            <button className="btn-plan-primary" onClick={onOpenRegister}>Start Professional Pilot</button>
          </div>

          <div className="pricing-card glass-panel">
            <div className="plan-name">Enterprise</div>
            <div className="plan-price">
              <strong>{isAnnual ? '$399' : '$479'}</strong>
              <span>/month</span>
            </div>
            <p className="plan-desc">For commercial distributors, warehouses & multi-tenant networks.</p>
            <ul className="plan-features">
              <li>✓ Unlimited Product SKUs</li>
              <li>✓ Multi-Warehouse Location Support</li>
              <li>✓ Custom ML Forecast Calibration</li>
              <li>✓ Dedicated Supplier Portal Accounts</li>
              <li>✓ Custom SLA & Dedicated Support</li>
            </ul>
            <button className="btn-plan-outline" onClick={onOpenRegister}>Contact Sales</button>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="landing-footer">
        <div className="footer-container">
          <div className="footer-brand">
            <div className="brand">StockWise</div>
            <span className="brand-subtag-footer">BACKED BY BUSZ</span>
            <p>Enterprise inventory intelligence, demand forecasting, and autonomous procurement operating system.</p>
          </div>

          <div className="footer-links-grid">
            <div>
              <strong>Product</strong>
              <a href="#solutions">Capabilities</a>
              <a href="#simulator">What-If Simulator</a>
              <a href="#preview">Platform Preview</a>
              <a href="#pricing">Pricing Plans</a>
            </div>
            <div>
              <strong>Architecture</strong>
              <a href="#architecture">Tenant Isolation</a>
              <a href="#architecture">Security Hardening</a>
              <a href="#architecture">Docker Orchestration</a>
            </div>
            <div>
              <strong>Access</strong>
              <a href="#" onClick={e => { e.preventDefault(); onOpenLogin(); }}>Sign In</a>
              <a href="#" onClick={e => { e.preventDefault(); onOpenRegister(); }}>Register Business</a>
            </div>
          </div>
        </div>
        <div className="footer-bottom">
          <span>© 2026 StockWise Backed by BUSZ. All rights reserved.</span>
          <span>Version 2.0 • Enterprise Edition</span>
        </div>
      </footer>
    </div>
  );
}
