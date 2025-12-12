import React from 'react';
import DynamicGridWidget from './DynamicGridWidget';
import styles from './PopupGrid.module.css';

export interface GridRow {
    [key: string]: string | number | boolean | null | undefined;
}

interface PopupGridProps {
    structureName: string;
    onSelect: (selectedRow: GridRow) => void;
    onClose: () => void;
    title?: string;
}

const PopupGrid: React.FC<PopupGridProps> = ({ structureName, onSelect, onClose, title }) => {
    
    const handleRowClick = (row: GridRow) => {
        onSelect(row);
        onClose(); // 항목 선택 시 자동으로 팝업을 닫습니다.
    };

    return (
        <div className={styles.popupOverlay} onClick={onClose}>
            <div className={styles.popupContainer} onClick={(e) => e.stopPropagation()}>
                <div className={styles.popupHeader}>
                    <h2>{title || '항목 선택'}</h2>
                    <button onClick={onClose} className={styles.closeBtn} title="닫기">&times;</button>
                </div>
                <div className={styles.popupContent}>
                    <DynamicGridWidget
                        structureName={structureName}
                        onRowClick={handleRowClick}
                    />
                </div>
            </div>
        </div>
    );
};

export default PopupGrid;
