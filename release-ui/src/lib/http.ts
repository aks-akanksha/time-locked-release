export async function handleErrors(res: Response) {
  if (res.ok) {
    try { return await res.json(); } catch { return; }
  }
  let msg = `${res.status} ${res.statusText}`;
  try {
    const j = await res.json();
    msg = (j && (j.message || j.error)) || msg;
  } catch {}
  if (res.status === 401) msg = 'Please sign in again.';
  if (res.status === 403) msg = 'You do not have permission for this action.';
  alert(msg);
  throw new Error(msg);
}
