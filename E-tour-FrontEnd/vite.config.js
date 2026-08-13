import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const BACKEND = 'http://localhost:5000'
const proxy = {
  '/api':          { target: BACKEND, changeOrigin: true },
  '/images':       { target: BACKEND, changeOrigin: true },
  '/oauth2':       { target: BACKEND, changeOrigin: true },
  '/login/oauth2': { target: BACKEND, changeOrigin: true }
}
export default defineConfig({
  plugins: [react()],

  server: {
    port: 5173,
    proxy
  },
  
  preview: {
    port: 5173,
    proxy
  }
})
