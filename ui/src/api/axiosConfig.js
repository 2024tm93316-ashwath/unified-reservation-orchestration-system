import axios from 'axios';

const BASE_URL = '/api/v1';

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

// Request interceptor – attach userId header if available
api.interceptors.request.use((config) => {
  const user = JSON.parse(sessionStorage.getItem('uros_user') || '{}');
  if (user?.userId) config.headers['X-User-Id'] = user.userId;
  return config;
});

// Response interceptor – unwrap .data field
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const msg =
      error?.response?.data?.message ||
      error?.message ||
      'Something went wrong';
    return Promise.reject(new Error(msg));
  }
);

export default api;
