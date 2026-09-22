import { useState, useEffect } from 'react';
import './index.css';
import Dashboard from './Dashboard';
import Login from './Login';
import LandingPage from './views/LandingPage';
import OnboardingWizardModal from './views/OnboardingWizardModal';

function App() {
  const [user, setUser] = useState(null);
  const [authModal, setAuthModal] = useState(null); // 'login' | 'register' | 'onboard' | null

  // Check for existing remembered session on app load and validate token
  useEffect(() => {
    const saved = localStorage.getItem('rememberedUser');
    if (saved) {
      try {
        const { user: savedUser, token } = JSON.parse(saved);
        if (token && savedUser) {
          // Verify token against backend
          fetch('/api/alerts/summary', {
            headers: { 'Authorization': `Bearer ${token}` }
          })
            .then((res) => {
              if (res.ok) {
                localStorage.setItem('token', token);
                setUser(savedUser);
              } else {
                // Token invalid or expired
                localStorage.removeItem('token');
                localStorage.removeItem('rememberedUser');
              }
            })
            .catch(() => {
              // Network issue fallback - still set user for offline resiliency
              localStorage.setItem('token', token);
              setUser(savedUser);
            });
        }
      } catch (_) {
        localStorage.removeItem('rememberedUser');
      }
    }
  }, []);

  if (user) {
    return (
      <Dashboard
        user={user}
        onLogout={() => {
          localStorage.removeItem('token');
          localStorage.removeItem('rememberedUser');
          setUser(null);
        }}
      />
    );
  }

  return (
    <>
      <LandingPage
        onOpenLogin={() => setAuthModal('login')}
        onOpenRegister={() => setAuthModal('onboard')}
      />

      {authModal === 'login' && (
        <div className="modal-backdrop" onClick={() => setAuthModal(null)}>
          <div onClick={(e) => e.stopPropagation()}>
            <Login
              defaultTab="login"
              onLogin={(loggedInUser) => {
                setUser(loggedInUser);
                setAuthModal(null);
              }}
              onClose={() => setAuthModal(null)}
            />
          </div>
        </div>
      )}

      {(authModal === 'onboard' || authModal === 'register') && (
        <OnboardingWizardModal
          onComplete={(provisionedUser) => {
            setUser(provisionedUser);
            setAuthModal(null);
          }}
          onClose={() => setAuthModal(null)}
        />
      )}
    </>
  );
}

export default App;

