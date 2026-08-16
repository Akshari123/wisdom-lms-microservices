const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const gatewayApiKey = import.meta.env.VITE_API_KEY || '';

async function request(path, options = {}) {
  const headers = new Headers(options.headers || {});
  const hasBody = options.body !== undefined && options.body !== null;

  if (gatewayApiKey) {
    headers.set('X-API-KEY', gatewayApiKey);
  }

  if (hasBody && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...options,
    headers
  });

  const contentType = response.headers.get('content-type') || '';
  const payload = contentType.includes('application/json')
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message =
      typeof payload === 'string'
        ? payload || `Request failed with status ${response.status}`
        : payload?.message || payload?.error || `Request failed with status ${response.status}`;

    throw new Error(message);
  }

  return payload;
}

function jsonBody(value) {
  return JSON.stringify(value);
}

export const api = {
  auth: {
    test: () => request('/api/auth/test'),
    login: (payload) =>
      request('/api/auth/login', {
        method: 'POST',
        body: jsonBody(payload)
      }),
    register: (payload) =>
      request('/api/auth/register', {
        method: 'POST',
        body: jsonBody(payload)
      })
  },
  students: {
    list: () => request('/api/students'),
    create: (payload) =>
      request('/api/students', {
        method: 'POST',
        body: jsonBody(payload)
      }),
    update: (id, payload) =>
      request(`/api/students/${id}`, {
        method: 'PUT',
        body: jsonBody(payload)
      }),
    remove: (id) =>
      request(`/api/students/${id}`, {
        method: 'DELETE'
      })
  },
  teachers: {
    list: () => request('/api/teachers'),
    create: (payload) =>
      request('/api/teachers', {
        method: 'POST',
        body: jsonBody(payload)
      }),
    update: (id, payload) =>
      request(`/api/teachers/${id}`, {
        method: 'PUT',
        body: jsonBody(payload)
      }),
    remove: (id) =>
      request(`/api/teachers/${id}`, {
        method: 'DELETE'
      })
  },
  classes: {
    list: () => request('/api/classes'),
    create: (payload) =>
      request('/api/classes', {
        method: 'POST',
        body: jsonBody(payload)
      }),
    update: (id, payload) =>
      request(`/api/classes/${id}`, {
        method: 'PUT',
        body: jsonBody(payload)
      }),
    remove: (id) =>
      request(`/api/classes/${id}`, {
        method: 'DELETE'
      })
  },
  payments: {
    history: () => request('/api/payments/history'),
    process: (payload) =>
      request('/api/payments/process', {
        method: 'POST',
        body: jsonBody(payload)
      }),
    updateStatus: (id, payload) =>
      request(`/api/payments/${id}/status`, {
        method: 'PUT',
        body: jsonBody(payload)
      })
  }
};

export function getApiBaseUrl() {
  return apiBaseUrl;
}
