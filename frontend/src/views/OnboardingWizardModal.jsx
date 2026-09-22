import React, { useState } from 'react';

const INDUSTRY_TEMPLATES = {
  ELECTRONICS: [
    { name: '4K Ultra-HD Monitor 27"', category: 'Electronics', price: 299.99, stockQuantity: 20, rating: 4.8 },
    { name: 'Ergonomic Wireless Trackball', category: 'Peripherals', price: 59.99, stockQuantity: 35, rating: 4.6 },
    { name: 'Thunderbolt 4 Pro Cable 2m', category: 'Cables', price: 29.99, stockQuantity: 50, rating: 4.7 },
    { name: 'Noise-Cancelling Studio Headset', category: 'Audio', price: 179.99, stockQuantity: 15, rating: 4.9 },
    { name: 'High-Speed External SSD 1TB', category: 'Storage', price: 109.99, stockQuantity: 25, rating: 4.8 }
  ],
  GROCERY: [
    { name: 'Organic Almond Milk 1L', category: 'Dairy & Alternatives', price: 3.99, stockQuantity: 80, rating: 4.7 },
    { name: 'Artisan Sourdough Loaf', category: 'Bakery', price: 5.49, stockQuantity: 40, rating: 4.9 },
    { name: 'Cold Pressed Olive Oil 500ml', category: 'Pantry', price: 12.99, stockQuantity: 60, rating: 4.8 },
    { name: 'Fair Trade Espresso Roast 250g', category: 'Beverages', price: 9.99, stockQuantity: 45, rating: 4.8 },
    { name: 'Gourmet Dark Chocolate 70%', category: 'Snacks', price: 4.49, stockQuantity: 90, rating: 4.6 }
  ],
  HARDWARE: [
    { name: 'Cordless Brushless Drill 20V', category: 'Power Tools', price: 129.99, stockQuantity: 18, rating: 4.8 },
    { name: 'Heavy Duty Socket Wrench Set', category: 'Hand Tools', price: 49.99, stockQuantity: 30, rating: 4.7 },
    { name: 'Stainless Steel Wood Screws 500pk', category: 'Fasteners', price: 19.99, stockQuantity: 75, rating: 4.5 },
    { name: 'Professional Laser Level 360°', category: 'Power Tools', price: 89.99, stockQuantity: 12, rating: 4.9 },
    { name: 'Industrial Safety Goggles', category: 'Safety Equipment', price: 14.99, stockQuantity: 100, rating: 4.6 }
  ],
  APPAREL: [
    { name: 'Merino Wool Crewneck Sweater', category: "Men's Wear", price: 89.99, stockQuantity: 25, rating: 4.8 },
    { name: 'Water-Resistant Trail Jacket', category: "Women's Wear", price: 139.99, stockQuantity: 20, rating: 4.7 },
    { name: 'Everyday Minimalist Sneakers', category: 'Footwear', price: 99.99, stockQuantity: 35, rating: 4.6 },
    { name: 'Canvas Utility Backpack 20L', category: 'Accessories', price: 69.99, stockQuantity: 30, rating: 4.9 }
  ],
  GENERAL: [
    { name: 'Premium Ballpoint Pens 12pk', category: 'Office Supplies', price: 12.99, stockQuantity: 60, rating: 4.5 },
    { name: 'Recycled Paper Notebook A5', category: 'Stationery', price: 6.99, stockQuantity: 120, rating: 4.7 },
    { name: 'Adjustable LED Desk Lamp', category: 'Office Equipment', price: 34.99, stockQuantity: 25, rating: 4.8 }
  ]
};

export default function OnboardingWizardModal({ onComplete, onClose }) {
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Step 1 Form
  const [orgName, setOrgName] = useState('');
  const [ownerUsername, setOwnerUsername] = useState('');
  const [ownerPassword, setOwnerPassword] = useState('');
  const [contactEmail, setContactEmail] = useState('');
  const [industryType, setIndustryType] = useState('ELECTRONICS');

  // Step 2 Catalog Option
  const [catalogOption, setCatalogOption] = useState('TEMPLATE'); // 'TEMPLATE' | 'CSV'
  const [csvText, setCsvText] = useState(
    "Name, Category, Price, Stock, Rating\nWireless Earbuds, Audio, 49.99, 30, 4.7\nUSB Cable 1m, Cables, 9.99, 100, 4.5\nDesk Mat XL, Accessories, 19.99, 40, 4.8"
  );

  const handleStep1Submit = (e) => {
    e.preventDefault();
    setError('');

    if (!orgName.trim() || !ownerUsername.trim() || !ownerPassword) {
      setError('Please fill out all required fields.');
      return;
    }
    if (ownerPassword.length < 8 || !/\d/.test(ownerPassword) || !/[a-zA-Z]/.test(ownerPassword)) {
      setError('Password must be at least 8 characters long and contain both letters and digits.');
      return;
    }

    setStep(2);
  };

  const handleFinalSubmit = async () => {
    setError('');
    setLoading(true);

    try {
      // 1. Register Tenant & Create Organization
      const res = await fetch('/api/onboarding/register-tenant', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          organizationName: orgName.trim(),
          ownerUsername: ownerUsername.trim(),
          ownerPassword: ownerPassword,
          contactEmail: contactEmail.trim(),
          industryType: industryType
        })
      });

      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        throw new Error(data.error || 'Failed to provision tenant workspace.');
      }

      const session = await res.json();
      localStorage.setItem('token', session.token);

      // 2. Import initial catalog if selected
      let productsToImport = [];
      if (catalogOption === 'TEMPLATE') {
        productsToImport = INDUSTRY_TEMPLATES[industryType] || INDUSTRY_TEMPLATES.GENERAL;
      } else if (catalogOption === 'CSV') {
        productsToImport = parseCsv(csvText);
      }

      if (productsToImport.length > 0) {
        await fetch('/api/onboarding/import-catalog', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${session.token}`
          },
          body: JSON.stringify(productsToImport)
        }).catch(err => console.warn('Catalog import warning:', err));
      }

      // Success
      onComplete(session.user);
    } catch (err) {
      setError(err.message);
      setLoading(false);
    }
  };

  const parseCsv = (text) => {
    const lines = text.trim().split('\n');
    const result = [];
    // skip header if present
    const startIndex = lines[0].toLowerCase().includes('name') ? 1 : 0;
    for (let i = startIndex; i < lines.length; i++) {
      const parts = lines[i].split(',').map(s => s.trim());
      if (parts.length >= 3 && parts[0]) {
        result.push({
          name: parts[0],
          category: parts[1] || 'General',
          price: parseFloat(parts[2]) || 19.99,
          stockQuantity: parseInt(parts[3]) || 20,
          rating: parseFloat(parts[4]) || 4.5
        });
      }
    }
    return result;
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="onboarding-modal glass-panel" onClick={e => e.stopPropagation()}>
        <button className="modal-close-btn" onClick={onClose}>✕</button>

        <div className="wizard-progress">
          <div className={`step-dot ${step >= 1 ? 'step-active' : ''}`}>1. Business Profile</div>
          <div className="step-connector"></div>
          <div className={`step-dot ${step >= 2 ? 'step-active' : ''}`}>2. Catalog & SKUs</div>
        </div>

        {error && <div className="error-msg" style={{ marginTop: '16px' }}>{error}</div>}

        {step === 1 && (
          <form onSubmit={handleStep1Submit} className="wizard-form">
            <h2>🏢 Onboard Your Business</h2>
            <p className="wizard-subtitle">Set up your isolated tenant workspace with tailored AI demand models.</p>

            <div className="input-group">
              <label>Organization / Business Name *</label>
              <input
                type="text"
                placeholder="e.g. Apex Retail Logistics"
                value={orgName}
                onChange={e => setOrgName(e.target.value)}
                required
              />
            </div>

            <div className="input-group">
              <label>Industry Segment *</label>
              <select
                value={industryType}
                onChange={e => setIndustryType(e.target.value)}
                className="wizard-select"
              >
                <option value="ELECTRONICS">Electronics & Peripherals</option>
                <option value="GROCERY">Grocery, Food & Supermarket</option>
                <option value="HARDWARE">Industrial Tools & Hardware</option>
                <option value="APPAREL">Apparel, Fashion & Footwear</option>
                <option value="GENERAL">General Retail & Wholesale</option>
              </select>
            </div>

            <div className="wizard-grid-2">
              <div className="input-group">
                <label>Admin Username *</label>
                <input
                  type="text"
                  placeholder="e.g. apex_admin"
                  value={ownerUsername}
                  onChange={e => setOwnerUsername(e.target.value)}
                  required
                />
              </div>
              <div className="input-group">
                <label>Work Email</label>
                <input
                  type="email"
                  placeholder="owner@apexretail.com"
                  value={contactEmail}
                  onChange={e => setContactEmail(e.target.value)}
                />
              </div>
            </div>

            <div className="input-group">
              <label>Master Password * (min 8 chars, letters & numbers)</label>
              <input
                type="password"
                placeholder="••••••••"
                value={ownerPassword}
                onChange={e => setOwnerPassword(e.target.value)}
                required
              />
            </div>

            <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '12px' }}>
              Continue to Catalog Setup ➔
            </button>
          </form>
        )}

        {step === 2 && (
          <div className="wizard-form">
            <h2>📦 Seed Initial Inventory Catalog</h2>
            <p className="wizard-subtitle">Choose how you want to initialize your SKUs for demand intelligence.</p>

            <div className="catalog-option-cards">
              <div
                className={`catalog-option-card ${catalogOption === 'TEMPLATE' ? 'option-selected' : ''}`}
                onClick={() => setCatalogOption('TEMPLATE')}
              >
                <div className="option-title">✨ Industry Starter Pack (Recommended)</div>
                <p>Instantly loads 5 pre-configured products tailored for {industryType.toLowerCase()} with lead-time parameters.</p>
              </div>

              <div
                className={`catalog-option-card ${catalogOption === 'CSV' ? 'option-selected' : ''}`}
                onClick={() => setCatalogOption('CSV')}
              >
                <div className="option-title">📄 Paste Custom CSV Spreadsheet</div>
                <p>Paste or edit your own CSV list of products (Name, Category, Price, Stock, Rating).</p>
              </div>
            </div>

            {catalogOption === 'TEMPLATE' && (
              <div className="template-preview-box">
                <strong>Preview SKUs to be provisioned:</strong>
                <ul>
                  {(INDUSTRY_TEMPLATES[industryType] || INDUSTRY_TEMPLATES.GENERAL).map((p, idx) => (
                    <li key={idx}>▸ <strong>{p.name}</strong> — ${p.price} (Qty: {p.stockQuantity})</li>
                  ))}
                </ul>
              </div>
            )}

            {catalogOption === 'CSV' && (
              <div className="input-group" style={{ marginTop: '12px' }}>
                <label>Paste CSV Data (Comma-separated)</label>
                <textarea
                  rows={4}
                  value={csvText}
                  onChange={e => setCsvText(e.target.value)}
                  className="wizard-textarea"
                />
              </div>
            )}

            <div className="wizard-btn-row">
              <button className="btn btn-secondary" onClick={() => setStep(1)} disabled={loading}>
                ← Back
              </button>
              <button className="btn btn-primary" onClick={handleFinalSubmit} disabled={loading}>
                {loading ? '🚀 Provisioning Tenant...' : '🎉 Launch Workspace & Intelligence'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
