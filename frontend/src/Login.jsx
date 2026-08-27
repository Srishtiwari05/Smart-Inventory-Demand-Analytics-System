import { useState, useEffect } from 'react';

export default function Login({ onLogin }) {
  const [tab, setTab] = useState('login');            // 'login' | 'register'
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // On mount: if a remembered session exists, restore it automatically
  useEffect(() => {
    const saved = localStorage.getItem('rememberedUser');
    if (saved) {
      try {
        const { user, token } = JSON.parse(saved);
        localStorage.setItem('token', token);
        onLogin(user);
      } catch (_) {
        localStorage.removeItem('rememberedUser');
      }
    }
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const endpoint = tab === 'login' ? '/api/auth/login' : '/api/auth/register';

    try {
      const res = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });

      if (!res.ok) {
        const msg = await res.text();
        throw new Error(msg || (tab === 'login' ? 'Invalid credentials' : 'Registration failed'));
      }

      const data = await res.json();
      localStorage.setItem('token', data.token);

      if (rememberMe) {
        // Store token + user so next visit auto-logs in
        localStorage.setItem('rememberedUser', JSON.stringify({ token: data.token, user: data.user }));
      } else {
        localStorage.removeItem('rememberedUser');
      }

      onLogin(data.user);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card glass-panel">
        <h1>Smart Inventory</h1>
        <p style={{marginBottom: '24px'}}>Demand Intelligence System</p>

        {/* Tabs */}
        <div style={{display: 'flex', gap: '8px', marginBottom: '24px', background: 'rgba(255,255,255,0.05)', borderRadius: '10px', padding: '4px'}}>
          {['login', 'register'].map(t => (
            <button
              key={t}
              type="button"
              onClick={() => { setTab(t); setError(''); }}
              style={{
                flex: 1,
                padding: '8px',
                border: 'none',
                borderRadius: '8px',
                cursor: 'pointer',
                fontWeight: 600,
                fontSize: '0.9rem',
                transition: 'all 0.2s',
                background: tab === t ? 'linear-gradient(135deg, #6366f1, #8b5cf6)' : 'transparent',
                color: tab === t ? '#fff' : 'rgba(255,255,255,0.5)',
              }}
            >
              {t === 'login' ? 'Sign In' : 'Sign Up'}
            </button>
          ))}
        </div>

        {error && <div className="error-msg">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label>Username</label>
            <input type="text" value={username} onChange={e => setUsername(e.target.value)} required autoComplete="username" />
          </div>
          <div className="input-group">
            <label>Password</label>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)} required autoComplete={tab === 'login' ? 'current-password' : 'new-password'} />
          </div>

          {/* Remember Me */}
          <div style={{display: 'flex', alignItems: 'center', gap: '8px', margin: '12px 0 20px', cursor: 'pointer'}} onClick={() => setRememberMe(!rememberMe)}>
            <div style={{
              width: '18px', height: '18px', borderRadius: '4px', border: '2px solid rgba(255,255,255,0.3)',
              background: rememberMe ? 'linear-gradient(135deg, #6366f1, #8b5cf6)' : 'transparent',
              display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, transition: 'all 0.2s'
            }}>
              {rememberMe && <span style={{color: '#fff', fontSize: '11px', lineHeight: 1}}>✓</span>}
            </div>
            <span style={{fontSize: '0.85rem', color: 'rgba(255,255,255,0.6)', userSelect: 'none'}}>Remember me</span>
          </div>

          <button type="submit" className="btn btn-primary" style={{width: '100%'}} disabled={loading}>
            {loading ? (tab === 'login' ? 'Signing in...' : 'Creating account...') : (tab === 'login' ? 'Sign In' : 'Create Account')}
          </button>
        </form>
      </div>
    </div>
  );
}

