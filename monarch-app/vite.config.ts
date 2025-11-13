import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: { // server 옵션 추가
    proxy: { // proxy 옵션 추가
      // '/api'로 시작하는 요청을 백엔드 서버로 전달
      '/api': { 
        target: 'http://localhost:8080', // 백엔드 서버 주소
        changeOrigin: true, // CORS 문제를 피하기 위해 origin을 변경
      },
    }
  }
})