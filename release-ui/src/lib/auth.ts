export type Session = { token: string; role: 'ADMIN'|'APPROVER'|'USER'|string; email: string };

export function saveToken(token: string) {
  localStorage.setItem('jwt', token);
}
export function getToken(): string | null {
  return localStorage.getItem('jwt');
}
export function clearToken() {
  localStorage.removeItem('jwt');
}
export function parseSession(token: string | null): Session | null {
  if (!token) return null;
  try {
    const [, payload] = token.split('.');
    const json = JSON.parse(atob(payload));
    return { token, role: json.role, email: json.sub ?? '' };
  } catch {
    return null;
  }
}
