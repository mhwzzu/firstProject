import { createApp } from 'vue'
import App from './App.vue'
import './styles.css'

createApp(App).mount('#app')

if (import.meta.env.PROD && 'serviceWorker' in navigator) {
  window.addEventListener('load', () => navigator.serviceWorker.register('/sw.js').catch(() => null))
} else if ('serviceWorker' in navigator) {
  navigator.serviceWorker.getRegistrations().then(registrations => registrations.forEach(registration => registration.unregister()))
  caches.keys().then(keys => keys.filter(key => key.startsWith('journey-shell-')).forEach(key => caches.delete(key)))
}

