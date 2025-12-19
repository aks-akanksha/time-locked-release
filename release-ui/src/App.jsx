import Releases from './pages/Releases';
import { useState } from 'react';
import { getToken, parseSession, saveToken, clearToken } from './lib/auth';
import { API } from './lib/api';
import { handleErrors } from './lib/http';
import './App.css';

function Login({ onDone }) {
  const [email, setEmail] = useState('user@example.com');
  const [password, setPassword] = useState('user123');
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');

  async function doLogin() {
    try {
      setLoading(true); setErr('');
      const res = await fetch(`${API}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });
      const { token } = await handleErrors(res);
      saveToken(token);
      onDone?.();
    } catch (e) {
      setErr(String(e.message || e));
    } finally { setLoading(false); }
  }

  return (
    <div className="login-container">
      <h2>Time-Locked Release</h2>
      {err && <div className="error">{err}</div>}
      <input 
        value={email} 
        onChange={e=>setEmail(e.target.value)} 
        placeholder="Email" 
        type="email"
      />
      <input 
        value={password} 
        onChange={e=>setPassword(e.target.value)} 
        placeholder="Password" 
        type="password" 
      />
      <button className="btn btn-primary" onClick={doLogin} disabled={loading}>
        {loading ? 'Signing in...' : 'Sign in'}
      </button>
      <div className="login-hint">
        Try: admin@example.com/admin123 or approver@example.com/approver123
      </div>
    </div>
  );
}

export default function App() {
  const session = parseSession(getToken());

  if (!session) return <Login onDone={()=>location.reload()} />;

  return (
    <div className="app-container">
      <div className="header">
        <h1>Time-Locked Release System</h1>
        <div className="user-info">
          <span className="role-badge">{session.role || 'USER'}</span>
          <button className="btn btn-secondary" onClick={() => { clearToken(); location.reload(); }}>
            Logout
          </button>
        </div>
      </div>
      <Releases />
    </div>
  );
}
