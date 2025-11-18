import React from 'react';
import styles from '../styles/ListPage.module.css';
import ContactWidget from '../components/widgets/ContactWidget';

const ContactPage: React.FC = () => {
    return (
        <main className={styles.pageContent}>
            <ContactWidget />
        </main>
    );
};

export default ContactPage;