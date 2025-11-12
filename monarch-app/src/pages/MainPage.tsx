import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

import styles from './MainPage.module.css'; // CSS 모듈 가져오기
const MainPage: React.FC = () => {
    const navigate = useNavigate();
    const [message, setMessage] = useState('Loading...');

    useEffect(() => {
        // 백엔드의 보호된 API를 호출하여 데이터를 가져옵니다.
        axios.get('/api/hello')
            .then(response => {
                setMessage(response.data.message);
            })
            .catch(error => {
                console.error('Failed to fetch message:', error);
                // 인증 에러(401) 등이 발생하면 로그인 페이지로 보낼 수도 있습니다.
                if (error.response && error.response.status === 401) {
                    navigate('/login');
                }
            });
    }, [navigate]);

    const handleLogout = async () => {
        try {
            await axios.post('/api/logout');
            navigate('/login'); // 로그아웃 성공 시 로그인 페이지로 이동합니다.
        } catch (error) {
            console.error('Logout error:', error);
        }
    };

    return (
        <div className={styles.container}>
            <div className={styles.contentWrapper}>
                <div className={styles.header}>
                    <h1 className={styles.title}>Main Page</h1>
                    <p className={styles.subtitle}>Welcome! You are logged in.</p>
                </div>
                <p className={styles.message}>
                    Message from backend: <strong>{message}</strong>
                </p>
                <button onClick={handleLogout} className={styles.logoutButton}>Logout</button>
            </div>
        </div>
    );
};

export default MainPage;