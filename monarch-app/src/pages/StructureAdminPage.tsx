import React, { useState } from 'react';
import axios from 'axios';
import styles from './LoginPage.module.css'; // 간단한 스타일 재사용
import Widget from '../components/Widget';

const StructureAdminPage: React.FC = () => {
    const [structureName, setStructureName] = useState('영업관리_MTBL');
    const [structureCont, setStructureCont] = useState('');

    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleFetchStructure = async () => {
        if (!structureName.trim()) {
            alert('Structure Name을 입력하세요.');
            return;
        }
        setIsLoading(true);
        setError(null);
        setStructureCont('');

        try {
            const storedUser = sessionStorage.getItem('user');
            const user = storedUser ? JSON.parse(storedUser) : {};
            const usiteNo = user?.M_USITE_NO || 1;

            const response = await axios.get('/api/data/execute', {
                params: {
                    serviceName: 'M_STRUCTURE',
                    methodName: 'MVIEW',
                    structureName: structureName,
                    usiteNo: usiteNo,
                }
            });

            if (response.data && response.data.structureCont) {
                // JSON 문자열을 보기 좋게 포맷팅
                const formattedJson = JSON.stringify(JSON.parse(response.data.structureCont), null, 2);
                setStructureCont(formattedJson);
            } else {
                setError('해당 이름의 Structure를 찾을 수 없거나, structureCont 내용이 비어있습니다.');
            }
        } catch (err) {
            setError('데이터를 불러오는 중 오류가 발생했습니다.');
            console.error(err);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div style={{ padding: '2rem' }}>
            <Widget title="Admin - 개발 정보">
                <div className={styles.inputGroup}>
                    <label htmlFor="structureName">Structure Name</label>
                    <input
                        id="structureName"
                        type="text"
                        value={structureName}
                        onChange={(e) => setStructureName(e.target.value)}
                        placeholder="예: 영업관리_MTBL"
                    />
                </div>
                <button className={styles.submitButton} onClick={handleFetchStructure} disabled={isLoading}>
                    {isLoading ? '조회 중...' : 'STRUCTURE_CONT 내용 조회'}
                </button>

                {error && <p className={styles.error}>{error}</p>}

                {structureCont && (
                    <pre style={{ backgroundColor: '#f4f4f4', padding: '1rem', borderRadius: '8px', whiteSpace: 'pre-wrap', wordBreak: 'break-all', marginTop: '1.5rem' }}>
                        <code>{structureCont}</code>
                    </pre>
                )}
            </Widget>
        </div>
    );
};

export default StructureAdminPage;