import React from 'react';
import { useLocation } from 'react-router-dom';
import Widget from '../components/Widget';
import CustomerStatusWidget from '../components/widgets/CustomerStatusWidget';
import SalesStatusWidget from '../components/widgets/SalesStatusWidget';

import styles from './DashboardPage.module.css';

const DashboardPage: React.FC = () => {
    const location = useLocation();

    // 위젯 목록을 배열로 정의합니다.
    const dashboardWidgets = [
        // location.pathname이 '/'일 때만 CustomerStatusWidget을 렌더링합니다.
        { id: 'customerStatus', Component: CustomerStatusWidget, condition: location.pathname === '/' },
        { id: 'salesStatus', Component: SalesStatusWidget, condition: true },
        // 아래는 예시 위젯입니다. 실제 위젯으로 교체할 수 있습니다.
        { id: 'newWidget2', Component: () => <Widget title="새로운 위젯 2"><div>콘텐츠 준비 중...</div></Widget>, condition: true },
    ];

    return (
        <main className={styles.pageContent}>
            <h2 className={styles.pageTitle}>대시보드</h2>
            <div className={styles.widgetGrid}>
                {dashboardWidgets.filter(({ condition }) => condition)
                    .map(({ id, Component }) => React.createElement(Component, { key: id }))}
            </div>
        </main>
    );
};

export default DashboardPage;