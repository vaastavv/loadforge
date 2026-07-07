/**
 * Base URL of the LoadForge control-plane API.
 *
 * An empty value means "same origin", which is the recommended local-dev setup:
 * the Vite dev server proxies `/api` to the backend (see vite.config.ts), so no
 * backend CORS configuration is required.
 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';
