import React from 'react';
import styles from '../styles/ListPage.module.css';
import CustomerWidget from '../components/widgets/CustomerWidget';

const CustomerPage: React.FC = () => {
    return (
        <main className={styles.pageContent}>
            <CustomerWidget />
        </main>
    );
};

export default CustomerPage;