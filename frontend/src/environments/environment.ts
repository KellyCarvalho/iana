const isLocalhost =
  typeof window !== 'undefined' &&
  (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1');

export const environment = {
  production: !isLocalhost,
  apiUrl: isLocalhost
    ? 'http://localhost:8080/api/v1'
    : 'https://iana-aag2.onrender.com/api/v1'
};
