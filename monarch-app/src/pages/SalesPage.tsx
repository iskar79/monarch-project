import React from 'react';
import styles from '../styles/ListPage.module.css';
import SalesWidget from '../components/widgets/SalesWidget';

const SalesPage: React.FC = () => {
    return (
        <main className={styles.pageContent}>
            <SalesWidget />
        </main>
    );
};

export default SalesPage;