interface ImportMetaEnv {
  VITE_API_URL?: string;
}

declare global {
  interface ImportMeta {
    readonly env: ImportMetaEnv;
  }
}

export const API = import.meta.env.VITE_API_URL ?? 'http://localhost:8081';
