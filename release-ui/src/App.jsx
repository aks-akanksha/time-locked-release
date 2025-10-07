import Releases from './pages/Releases';
import { useState } from 'react';
import { getToken, parseSession, saveToken, clearToken } from './lib/auth';
import { API } from './lib/api';
import { handleErrors } from './lib/http';

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
    <div style={{maxWidth: 420, margin: '4rem auto', display:'grid', gap:12}}>
      <h2>Login</h2>
      <input value={email} onChange={e=>setEmail(e.target.value)} placeholder="email" />
      <input value={password} onChange={e=>setPassword(e.target.value)} placeholder="password" type="password" />
      <button onClick={doLogin} disabled={loading}>{loading?'...':'Sign in'}</button>
      {err && <div style={{color:'crimson'}}>{err}</div>}
      <small>Try admin@example.com/admin123 or approver@example.com/approver123</small>
    </div>
  );
}

export default function App() {
  const session = parseSession(getToken());

  if (!session) return <Login onDone={()=>location.reload()} />;

  return (
    <>
      <div style={{ display:'flex', gap:12, alignItems:'center', margin:'1rem 0' }}>
        <div>Role: {session.role || '—'}</div>
        <button onClick={() => { clearToken(); location.reload(); }}>Logout</button>
      </div>
      <Releases />
    </>
  );
}
