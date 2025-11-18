import React from 'react';
import styles from './Widget.module.css';

interface WidgetProps {
    title: React.ReactNode;
    children: React.ReactNode;
}

const Widget: React.FC<WidgetProps> = ({ title, children }) => {
    return (
        <div className={styles.widget}>
            <h3 className={styles.widgetTitle}>{title}</h3>
            <div className={styles.widgetContent}>{children}</div>
        </div>
    );
};

export default Widget;