import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LoginPage from './pages/LoginPage.tsx';
import './App.css';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 다른 모든 복잡한 라우팅을 제거하고, 오직 로그인 페이지만 표시합니다. */}
        <Route path="/" element={<LoginPage />} />
        <Route path="/login" element={<LoginPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
