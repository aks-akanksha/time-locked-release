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
  createdBy?: string;
  approvedBy?: string;
};

type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
};

type Statistics = {
  totalReleases: number;
  releasesByStatus: Record<string, number>;
  scheduledReleases: number;
  approvedReleases: number;
  executedReleases: number;
  cancelledReleases: number;
};

export default function Releases() {
  const session = parseSession(getToken())!;
  const role = session.role ?? '';

  const canSchedule = role === 'ADMIN';
  const canApprove = role === 'APPROVER' || role === 'ADMIN';
  const canExecute = role === 'ADMIN';
  const canCancel = role === 'ADMIN';

  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDir, setSortDir] = useState('desc');
  
  const [data, setData] = useState<PageResponse<Release> | null>(null);
  const [statistics, setStatistics] = useState<Statistics | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [payloadJson, setPayloadJson] = useState('{"x":1}');
  const [when, setWhen] = useState<string>('');

  useEffect(() => {
    loadReleases();
    loadStatistics();
  }, [page, size, search, statusFilter, sortBy, sortDir]);

  async function loadReleases() {
    try {
      setLoading(true);
      setError('');
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        sortBy,
        sortDir,
      });
      if (search.trim()) params.append('search', search.trim());
      if (statusFilter) params.append('status', statusFilter);

      const data = await fetch(`${API}/api/v1/releases?${params}`, {
        headers: { Authorization: `Bearer ${session.token}` }
      }).then(handleErrors);
      setData(data as PageResponse<Release>);
    } catch (e: any) {
      setError(e.message || 'Failed to load releases');
    } finally {
      setLoading(false);
    }
  }

  async function loadStatistics() {
    try {
      const stats = await fetch(`${API}/api/v1/releases/statistics`, {
        headers: { Authorization: `Bearer ${session.token}` }
      }).then(handleErrors);
      setStatistics(stats as Statistics);
    } catch (e) {
      console.error('Failed to load statistics:', e);
    }
  }

  async function createRelease() {
    try {
      setError('');
      setSuccess('');
      await fetch(`${API}/api/v1/releases`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${session.token}`
        },
        body: JSON.stringify({ title, description, payloadJson })
      }).then(handleErrors);
      setTitle('');
      setDescription('');
      setPayloadJson('{"x":1}');
      setSuccess('Release created successfully!');
      await loadReleases();
      await loadStatistics();
    } catch (e: any) {
      setError(e.message || 'Failed to create release');
    }
  }

  function toIsoUtc(local: string) {
    const d = new Date(local);
    return d.toISOString();
  }

  async function onSchedule(id: number) {
    try {
      setError('');
      setSuccess('');
      const iso = toIsoUtc(when);
      await fetch(`${API}/api/v1/releases/${id}/actions/schedule`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${session.token}`
        },
        body: JSON.stringify({ scheduledAt: iso })
      }).then(handleErrors);
      setSuccess('Release scheduled successfully!');
      await loadReleases();
    } catch (e: any) {
      setError(e.message || 'Failed to schedule release');
    }
  }

  async function onApprove(id: number) {
    try {
      setError('');
      setSuccess('');
      await fetch(`${API}/api/v1/releases/${id}/actions/approve`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${session.token}` }
      }).then(handleErrors);
      setSuccess('Release approved successfully!');
      await loadReleases();
      await loadStatistics();
    } catch (e: any) {
      setError(e.message || 'Failed to approve release');
    }
  }

  async function onExecute(id: number) {
    try {
      setError('');
      setSuccess('');
      await fetch(`${API}/api/v1/releases/${id}/actions/execute`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${session.token}` }
      }).then(handleErrors);
      setSuccess('Release executed successfully!');
      await loadReleases();
      await loadStatistics();
    } catch (e: any) {
      setError(e.message || 'Failed to execute release');
    }
  }

  async function onCancel(id: number) {
    try {
      setError('');
      setSuccess('');
      await fetch(`${API}/api/v1/releases/${id}/actions/cancel`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${session.token}` }
      }).then(handleErrors);
      setSuccess('Release cancelled successfully!');
      await loadReleases();
      await loadStatistics();
    } catch (e: any) {
      setError(e.message || 'Failed to cancel release');
    }
  }

  return (
    <div>
      {error && <div className="error">{error}</div>}
      {success && <div className="success">{success}</div>}

      {/* Statistics */}
      {statistics && (
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-value">{statistics.totalReleases}</div>
            <div className="stat-label">Total Releases</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{statistics.scheduledReleases}</div>
            <div className="stat-label">Scheduled</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{statistics.approvedReleases}</div>
            <div className="stat-label">Approved</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{statistics.executedReleases}</div>
            <div className="stat-label">Executed</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{statistics.cancelledReleases}</div>
            <div className="stat-label">Cancelled</div>
          </div>
        </div>
      )}

      {/* Create form */}
      <div className="card">
        <div className="card-title">Create New Release</div>
        <div className="form-group">
          <label>Title *</label>
          <input
            placeholder="Release title"
            value={title}
            onChange={e => setTitle(e.target.value)}
          />
        </div>
        <div className="form-group">
          <label>Description</label>
          <textarea
            placeholder="Release description"
            value={description}
            onChange={e => setDescription(e.target.value)}
          />
        </div>
        <div className="form-group">
          <label>Payload JSON</label>
          <textarea
            placeholder='{"key": "value"}'
            value={payloadJson}
            onChange={e => setPayloadJson(e.target.value)}
          />
        </div>
        <button className="btn btn-primary" onClick={createRelease} disabled={!title.trim()}>
          Create Release
        </button>
      </div>

      {/* Filters */}
      <div className="card">
        <div className="filters">
          <input
            type="text"
            placeholder="Search releases..."
            value={search}
            onChange={e => { setSearch(e.target.value); setPage(0); }}
            style={{ flex: 1, minWidth: '200px' }}
          />
          <select
            value={statusFilter}
            onChange={e => { setStatusFilter(e.target.value); setPage(0); }}
          >
            <option value="">All Statuses</option>
            <option value="DRAFT">Draft</option>
            <option value="SCHEDULED">Scheduled</option>
            <option value="APPROVED">Approved</option>
            <option value="EXECUTED">Executed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
          <select
            value={sortBy}
            onChange={e => { setSortBy(e.target.value); setPage(0); }}
          >
            <option value="createdAt">Sort by Created</option>
            <option value="scheduledAt">Sort by Scheduled</option>
            <option value="title">Sort by Title</option>
          </select>
          <select
            value={sortDir}
            onChange={e => { setSortDir(e.target.value); setPage(0); }}
          >
            <option value="desc">Descending</option>
            <option value="asc">Ascending</option>
          </select>
          <button className="btn btn-secondary" onClick={loadReleases}>
            Refresh
          </button>
        </div>
      </div>

      {/* Schedule datetime input */}
      {canSchedule && (
        <div className="card">
          <div className="form-group">
            <label>Schedule Time (for scheduling releases)</label>
            <input
              type="datetime-local"
              value={when}
              onChange={e => setWhen(e.target.value)}
            />
          </div>
        </div>
      )}

      {/* Loading */}
      {loading && <div className="loading">Loading releases...</div>}

      {/* Releases list */}
      {data && (
        <>
          <div style={{ marginBottom: '1rem', color: '#666' }}>
            Showing {data.content.length} of {data.totalElements} releases
          </div>
          {data.content.map(r => {
            const isDue = r.scheduledAt && new Date() >= new Date(r.scheduledAt);
            const canExecNow = canExecute && r.status === 'APPROVED' && isDue;
            const canCancelNow = canCancel && r.status !== 'EXECUTED' && r.status !== 'CANCELLED';

            return (
              <div key={r.id} className="release-item">
                <div className="release-header">
                  <div>
                    <div className="release-title">
                      #{r.id} — {r.title}
                      <span className={`status-badge status-${r.status}`} style={{ marginLeft: '0.5rem' }}>
                        {r.status}
                      </span>
                    </div>
                    {r.description && (
                      <div style={{ marginTop: '0.5rem', color: '#666' }}>{r.description}</div>
                    )}
                  </div>
                </div>
                <div className="release-meta">
                  <div>Created: {r.createdAt ? new Date(r.createdAt).toLocaleString() : '—'}</div>
                  {r.scheduledAt && (
                    <div>Scheduled: {new Date(r.scheduledAt).toLocaleString()}</div>
                  )}
                  {r.approvedAt && (
                    <div>Approved: {new Date(r.approvedAt).toLocaleString()} by {r.approvedBy}</div>
                  )}
                  {r.executedAt && (
                    <div>Executed: {new Date(r.executedAt).toLocaleString()}</div>
                  )}
                </div>
                <div className="release-actions">
                  {canSchedule && r.status !== 'EXECUTED' && r.status !== 'CANCELLED' && (
                    <button
                      className="btn btn-secondary"
                      onClick={() => onSchedule(r.id)}
                      disabled={!when}
                    >
                      Schedule
                    </button>
                  )}
                  {canApprove && (r.status === 'SCHEDULED' || r.status === 'DRAFT') && (
                    <button
                      className="btn btn-success"
                      onClick={() => onApprove(r.id)}
                    >
                      Approve
                    </button>
                  )}
                  {canExecNow && (
                    <button
                      className="btn btn-primary"
                      onClick={() => onExecute(r.id)}
                    >
                      Execute
                    </button>
                  )}
                  {canCancelNow && (
                    <button
                      className="btn btn-danger"
                      onClick={() => onCancel(r.id)}
                    >
                      Cancel
                    </button>
                  )}
                </div>
              </div>
            );
          })}

          {/* Pagination */}
          {data.totalPages > 1 && (
            <div className="pagination">
              <button
                className="btn btn-secondary"
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={!data.hasPrevious || loading}
              >
                Previous
              </button>
              <span style={{ padding: '0 1rem' }}>
                Page {data.page + 1} of {data.totalPages}
              </span>
              <button
                className="btn btn-secondary"
                onClick={() => setPage(p => p + 1)}
                disabled={!data.hasNext || loading}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
