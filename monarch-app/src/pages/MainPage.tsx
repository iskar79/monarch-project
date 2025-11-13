import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

import styles from './MainPage.module.css'; // CSS 모듈 가져오기

// 백엔드에서 오는 데이터 구조를 위한 인터페이스 정의
interface UserDataRow {
    [key: string]: string | number | boolean | null | undefined; // 키는 문자열, 값은 다양한 기본 타입
}

const MainPage: React.FC = () => {
    const navigate = useNavigate();
    const [user, setUser] = useState<UserDataRow | null>(null);
    const [loginError, setLoginError] = useState<string | null>(null);
    const [message, setMessage] = useState('Loading...');
    const [userData, setUserData] = useState<UserDataRow[]>([]); // 동적 쿼리 결과 저장
    const [isLoading, setIsLoading] = useState(true); // 데이터 로딩 상태 추가
    const [error, setError] = useState<string | null>(null); // 에러 메시지 상태 추가

    useEffect(() => {
        // sessionStorage에서 사용자 정보를 가져옵니다.
        const storedUser = sessionStorage.getItem('user');
        if (storedUser) {
            const parsedUser = JSON.parse(storedUser);
            setUser(parsedUser);
            setUserData([parsedUser]); // 상세 정보 테이블을 위해 데이터 설정

            // sessionStorage에서 로그인 에러 메시지를 가져옵니다.
            const storedLoginError = sessionStorage.getItem('loginError');
            if (storedLoginError) {
                setLoginError(storedLoginError);
            }
            // 백엔드 테스트 메시지만 가져옵니다.
            fetchHelloMessage();
        } else {
            // 사용자 정보가 없으면 로그인 페이지로 이동
            navigate('/login');
        }

        async function fetchHelloMessage() {
            setIsLoading(true);
            setError(null);
            try {
                const messageResponse = await axios.get('/api/hello');
                setMessage(messageResponse.data.message);
            } catch (error) {
                const errorMessage = axios.isAxiosError(error) ? error.message : 'An unexpected error occurred';
                console.error('Failed to fetch data:', errorMessage);
                setError(`Failed to load data from server. (${errorMessage})`);
                if (axios.isAxiosError(error) && error.response?.status === 401) {
                    navigate('/login');
                }
            } finally {
                setIsLoading(false); // 로딩 완료
            }
        }
    }, [navigate]);
    useEffect(() => {
        // 컴포넌트가 언마운트될 때 sessionStorage에서 loginError를 제거합니다.
        return () => {
            sessionStorage.removeItem('loginError');
        };
    }, []);

    const handleLogout = async () => {
        try {
            await axios.post('/api/logout');
            // sessionStorage에서 사용자 정보를 삭제합니다.
            sessionStorage.removeItem('user');
            navigate('/login'); // 로그아웃 성공 시 로그인 페이지로 이동합니다.
        } catch (error) {
            console.error('Logout error:', error);
        }
    };

    return (
        <div className={styles.container}>
            <div className={styles.contentWrapper}>
                <div className={styles.header}>
                    <h1 className={styles.title}>
                        반갑습니다, {user?.USER_NAME} <span className={styles.honorific}>님</span>
                    </h1>
                    <p className={styles.subtitle}>You are successfully logged in.</p>
                </div>
                <p className={styles.message}>
                    Message from backend: <strong>{message}</strong>
                </p>
                {loginError && <div className={styles.error}>{loginError}</div>}
                {isLoading && <div className={styles.loading}>Loading user data...</div>}
                {error && <div className={styles.error}>{error}</div>}
                {!isLoading && !error && (!userData || userData.length === 0) && (
                    <div className={styles.noData}>
                        No user data found for '{user?.username}'. Please check the database.
                    </div>
                )}
                {userData.length > 0 && (
                    <div className={styles.userDataSection}>
                        <h2 className={styles.userDataTitle}>User Details from DB</h2>
                        <table className={styles.userDataTable}>
                            <thead>
                                <tr>
                                    {Object.keys(userData[0]).map(key => <th key={key}>{key}</th>)}
                                </tr>
                            </thead>
                            <tbody>
                                {userData.map((row, index) => <tr key={index}>{Object.values(row).map((val, i) => <td key={i}>{String(val)}</td>)}</tr>)}
                            </tbody>
                        </table>
                    </div>
                )}
                <button onClick={handleLogout} className={styles.logoutButton}>Logout</button>
            </div>
        </div>
    );
};

export default MainPage;