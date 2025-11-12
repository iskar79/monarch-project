import React, { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import axios from 'axios';

interface ProtectedRouteProps {
    children: React.ReactElement;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);

    useEffect(() => {
        const checkAuth = async () => {
            try {
                // 백엔드에 인증 상태 확인 요청
                await axios.get('/api/auth/status');
                setIsAuthenticated(true);
            } catch (error) {
                setIsAuthenticated(false);
            }
        };
        checkAuth();
    }, []);

    if (isAuthenticated === null) {
        // 인증 상태 확인 중... 로딩 스피너 등을 보여줄 수 있습니다.
        return <div>Loading...</div>;
    }

    return isAuthenticated ? children : <Navigate to="/login" />;
};

export default ProtectedRoute;