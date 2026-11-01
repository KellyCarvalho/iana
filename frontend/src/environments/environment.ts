const isLocalhost =
  typeof window !== 'undefined' &&
  (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1');

export const environment = {
  production: !isLocalhost,
  // Em produção na Vercel, altere a URL abaixo para a URL do seu serviço no Render (ex: 'https://iana-backend.onrender.com/api/v1')
  apiUrl: isLocalhost
    ? 'http://localhost:8080/api/v1'
    : ((typeof window !== 'undefined' && (window as any).__IANA_API_URL__) || 'https://iana-api.onrender.com/api/v1')
};
