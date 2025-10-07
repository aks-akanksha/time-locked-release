import { useEffect, useState } from 'react';
import { parseSession, getToken } from '../lib/auth';
import { handleErrors } from '../lib/http';
import { API } from '../lib/api';

type Release = {
  id: number;
  title: string;
  description?: string;
  payloadJson?: string;
  status: string;
  scheduledAt?: string;
  createdAt?: string;
  approvedAt?: string;
  executedAt?: string;
};

export default function Releases() {
  // --- session / role ---
  const session = parseSession(getToken())!;
  const role = session.role ?? '';

  // RBAC gating (Snippet 1)
  const canSchedule = role === 'ADMIN';
  const canApprove  = role === 'APPROVER' || role === 'ADMIN';
  const canExecute  = role === 'ADMIN';

  // --- state ---
  const [list, setList] = useState<Release[]>([]);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [payloadJson, setPayloadJson] = useState('{"x":1}');
  const [when, setWhen] = useState<string>(''); // Snippet 2: datetime-local

  // --- load ---
  async function loadReleases() {
    const data = await fetch(`${API}/api/v1/releases`, {
      headers: { Authorization: `Bearer ${session.token}` }
    }).then(handleErrors);
    setList(data as Release[]);
  }
  useEffect(() => { loadReleases(); }, []);

  // --- create (Snippet 3 usage .then(handleErrors)) ---
  async function createRelease() {
    await fetch(`${API}/api/v1/releases`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${session.token}`
      },
      body: JSON.stringify({ title, description, payloadJson })
    }).then(handleErrors);
    setTitle(''); setDescription(''); setPayloadJson('{"x":1}');
    await loadReleases();
  }

  // --- helpers for schedule (Snippet 2) ---
  function toIsoUtc(local: string) {
    const d = new Date(local); // local like "2025-10-04T11:00"
    return d.toISOString();     // backend Instant.parse(...)
  }

  async function onSchedule(id: number) {
    const iso = toIsoUtc(when);
    await fetch(`${API}/api/v1/releases/${id}/actions/schedule`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${session.token}`
      },
      body: JSON.stringify({ scheduledAt: iso })
    }).then(handleErrors);
    await loadReleases();
  }

  async function onApprove(id: number) {
    await fetch(`${API}/api/v1/releases/${id}/actions/approve`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${session.token}` }
    }).then(handleErrors);
    await loadReleases();
  }

  async function onExecute(id: number) {
    await fetch(`${API}/api/v1/releases/${id}/actions/execute`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${session.token}` }
    }).then(handleErrors);
    await loadReleases();
  }

  return (
    <div style={{ padding: '1rem' }}>
      <h2>Releases</h2>

      {/* Create form */}
      <div style={{ border:'1px solid #444', borderRadius:8, padding:12, marginBottom:12, maxWidth:700 }}>
        <div style={{ marginBottom:8 }}>Create Release</div>
        <input
          placeholder="Title"
          value={title}
          onChange={e => setTitle(e.target.value)}
          style={{ width:'100%', marginBottom:6 }}
        />
        <textarea
          placeholder="Description"
          value={description}
          onChange={e => setDescription(e.target.value)}
          style={{ width:'100%', marginBottom:6, height:56 }}
        />
        <textarea
          placeholder='Payload JSON'
          value={payloadJson}
          onChange={e => setPayloadJson(e.target.value)}
          style={{ width:'100%', marginBottom:6, height:56 }}
        />
        <button onClick={createRelease}>Create (USER)</button>
      </div>

      {/* One datetime-local input for scheduling */}
      <div style={{ margin:'12px 0', maxWidth:700 }}>
        <input
          type="datetime-local"
          value={when}
          onChange={e => setWhen(e.target.value)}
          style={{ marginRight: 8 }}
        />
      </div>

      <button onClick={loadReleases} style={{ marginBottom:12 }}>Refresh</button>

      {/* List */}
      <div style={{ display:'grid', gap:12, maxWidth:700 }}>
        {list.map(r => {
          // 👇 add these two lines
          const isDue = r.scheduledAt && new Date() >= new Date(r.scheduledAt);
          const canExecNow = canExecute && r.status === 'APPROVED' && isDue;

          return (
            <div key={r.id} style={{ border:'1px solid #444', borderRadius:8, padding:12 }}>
              <div style={{ fontWeight:600 }}>
                #{r.id} — {r.title} <span style={{ fontWeight:400 }}>· status: {r.status}</span>
              </div>
              <div style={{ fontSize:12, opacity:.8 }}>
                {r.scheduledAt ? new Date(r.scheduledAt).toLocaleString() : '—'}
              </div>

              {/* RBAC-gated buttons */}
              <div style={{ display:'flex', gap:8, marginTop:8 }}>
                <button disabled={!canSchedule} hidden={!canSchedule} onClick={() => onSchedule(r.id)}>
                  Schedule (ADMIN)
                </button>
                <button disabled={!canApprove} hidden={!canApprove} onClick={() => onApprove(r.id)}>
                  Approve (APPROVER)
                </button>
                {/* 👇 use canExecNow here */}
                <button disabled={!canExecNow} hidden={!canExecute} onClick={() => onExecute(r.id)}>
                  Execute (ADMIN)
                </button>
              </div>
            </div>
          );
        })}
      </div>

    </div>
  );
}
