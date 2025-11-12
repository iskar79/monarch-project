import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import styles from './LoginPage.module.css'; // CSS 모듈 가져오기

const LoginPage: React.FC = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        // Spring Security의 formLogin은 FormData 형식을 사용합니다.
        const formData = new FormData();
        formData.append('username', username);
        formData.append('password', password);

        try {
            await axios.post('/api/login', formData);
            navigate('/'); // 로그인 성공 시 메인 페이지('/')로 이동합니다.
        } catch (err) {
            setError('Login failed. Please check your username and password.');
            console.error('Login error:', err);
        }
    };

    return (
        <div className={styles.container}>
            <div className={styles.formWrapper}>
                <div className={styles.header}>
                    <div className={styles.logoText}>MONARCH</div>
                    <p className={styles.subtitle}>Welcome back! Please sign in.</p>
                </div>
                <form onSubmit={handleLogin}>
                    <div className={styles.inputGroup}>
                        <label htmlFor="username">Username</label>
                        <input
                            id="username"
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            autoComplete="username"
                        />
                    </div>
                    <div className={styles.inputGroup}>
                        <label htmlFor="password">Password</label>
                        <input
                            id="password"
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            autoComplete="current-password"
                        />
                    </div>
                    <button type="submit" className={styles.submitButton}>Login</button>
                </form>
                {error && <p className={styles.error}>{error}</p>}
            </div>
        </div>
    );
};

export default LoginPage;