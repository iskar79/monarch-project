import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

// 상수 정의: 하드코딩된 문자열을 대체하여 유지보수성을 높입니다.
export const STORAGE_KEYS = {
    TOKEN: 'token',
    USER: 'user',
};

export const USER_FIELDS = {
    NAME: 'USER_NAME',
    UID: 'M_USER_NO',
    USITE: 'M_USITE_NO',
};

// 사용자 데이터의 타입을 정의합니다.
interface UserDataRow {
    [key: string]: string | number | boolean | null | undefined;
}

/**
 * 인증 상태, 사용자 정보, 로그아웃 기능을 제공하는 커스텀 훅입니다.
 */
export const useAuth = () => {
    const navigate = useNavigate();
    const [user, setUser] = useState<UserDataRow | null>(null);
    const [uid, setUid] = useState<string | number | null>(null);
    const [usite, setUsite] = useState<string | number>(1); // 기본값 1
    // 인증 상태를 null로 초기화하여 '확인 중' 상태를 표현합니다.
    const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);

    // 로그아웃 핸들러
    const handleLogout = useCallback(async () => {
        // 클라이언트 측 상태 및 저장소 정리
        localStorage.removeItem(STORAGE_KEYS.TOKEN);
        sessionStorage.removeItem(STORAGE_KEYS.USER);
        axios.defaults.headers.common['Authorization'] = null;
        setUser(null);
        setIsAuthenticated(false);
        navigate('/login');
        setUid(null);
        setUsite(1);
    }, [navigate]);

    // 컴포넌트 마운트 시 인증 상태를 확인합니다.
    useEffect(() => {
        const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
        const storedUser = sessionStorage.getItem(STORAGE_KEYS.USER);

        if (token && storedUser) {
            try {
                const parsedUser = JSON.parse(storedUser);
                setUser(parsedUser);
                axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
                setUid(parsedUser[USER_FIELDS.UID] || null);
                // M_USITE_NO 값이 있으면 사용하고, 없으면 기본값 1을 유지합니다.
                setUsite(parsedUser[USER_FIELDS.USITE] || 1);
                setIsAuthenticated(true);
            } catch (error) {
                console.error("Failed to parse user data from session storage.", error);
                handleLogout(); // 사용자 데이터 파싱 실패 시 로그아웃 처리
            }
        } else {
            setIsAuthenticated(false);
        }
    }, [handleLogout]);

    return { user, isAuthenticated, handleLogout, uid, usite };
};