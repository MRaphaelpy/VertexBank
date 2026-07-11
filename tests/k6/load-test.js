import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 20 },
    { duration: '1m', target: 20 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.API_URL || 'http://localhost:8081';

export default function () {
  let res = http.get(`${BASE_URL}/actuator/health`);
  check(res, {
    'status is 200': (r) => r.status === 200,
    'health is UP': (r) => r.json().status === 'UP',
  });

  sleep(1);

  const payload = JSON.stringify({
    email: 'k6test@vertexbank.com',
    password: 'SecurePassword123',
    name: 'K6 Performance User',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  let signupRes = http.post(`${BASE_URL}/auth/signup`, payload, params);
  check(signupRes, {
    'signup status is 201 or 400 (if exists)': (r) => r.status === 201 || r.status === 400,
  });

  sleep(1);
}
