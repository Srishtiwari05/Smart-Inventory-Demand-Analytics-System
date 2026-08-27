import { useState } from 'react';
import './index.css';
import Dashboard from './Dashboard';
import Login from './Login';

function App() {
  const [user, setUser] = useState(null);

  if (!user) {
    return <Login onLogin={setUser} />;
  }

  return <Dashboard user={user} onLogout={() => {
    localStorage.removeItem('token');
    localStorage.removeItem('rememberedUser');
    setUser(null);
  }} />;
}

export default App;
