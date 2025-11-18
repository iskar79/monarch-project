import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

const ProtectedRoute: React.FC = () => {
    // sessionStorage에서 사용자 정보를 직접 확인합니다.
    const storedUser = sessionStorage.getItem('user');

    // 사용자 정보가 있으면 자식 컴포넌트(Outlet)를 렌더링하고,
    // 없으면 로그인 페이지로 리디렉션합니다.
    return storedUser ? <Outlet /> : <Navigate to="/login" replace />;
};

export default ProtectedRoute;