import React, { useState, useEffect } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';

import styles from './Layout.module.css';
// useAuth 훅과 상수를 가져옵니다. (실제 프로젝트에서는 경로에 맞게 수정)
import { useAuth, USER_FIELDS } from '../hooks/useAuth';

// 메뉴 구조 정의
const menuItems = [
    { name: '대시보드', path: '/', icon: '📊' },
    { name: '영업', icon: '💼', subItems: [
        { name: '영업관리', path: '/sales' },
        { name: '접촉관리', path: '/sales/contact' } // "접촉관리" 메뉴로 수정
    ] },
    { name: '고객', icon: '👥', subItems: [{ name: '고객관리', path: '/customer' }] },
    { name: 'Admin', icon: '⚙️', subItems: [{ name: '사용자정보', path: '/admin/users' }, { name: '개발정보', path: '/admin/dev' }] },
];

const Layout: React.FC = () => {
    // useAuth 훅을 사용하여 인증 관련 상태와 함수를 가져옵니다.
    const { user, isAuthenticated, handleLogout } = useAuth();
    const [isSidebarPinned, setSidebarPinned] = useState(false);
    const [isMobileMenuOpen, setMobileMenuOpen] = useState(false);
    const [openMenu, setOpenMenu] = useState<string | null>(null); // 아코디언 메뉴 상태
    const location = useLocation(); // 페이지 이동 감지를 위해 사용

    const sidebarClasses = `${styles.sidebar} ${isSidebarPinned ? styles.pinned : ''}`;
    const mainContentClasses = `${styles.mainContent} ${isSidebarPinned ? styles.shifted : ''}`;
    const mobileSidebarClasses = `${sidebarClasses} ${isMobileMenuOpen ? styles.mobileOpen : ''}`;

    // 메뉴 클릭 핸들러 (아코디언 토글)
    const handleMenuClick = (e: React.MouseEvent, itemName: string, hasSubItems: boolean) => {
        if (hasSubItems) {
            e.preventDefault(); // 링크 이동 방지
            setOpenMenu(openMenu === itemName ? null : itemName);
        } else {
            // 서브메뉴가 없는 경우, 모바일 메뉴 닫기
            setMobileMenuOpen(false);
        }
    };

    // 페이지 경로가 변경되면 모바일 메뉴와 아코디언 메뉴를 닫습니다.
    useEffect(() => { setMobileMenuOpen(false); setOpenMenu(null); }, [location.pathname]);

    // 인증 상태가 확인되기 전에는 로딩 상태를 표시할 수 있습니다.
    if (isAuthenticated === null) {
        return <div>Loading...</div>; // 혹은 스피너 컴포넌트
    }

    return (
        <div className={styles.pageContainer}>
            <nav className={mobileSidebarClasses}>
                <div className={styles.sidebarHeader}>
                    <span className={styles.monarchIcon}>
                        <svg width="28" height="28" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
                            <text x="50" y="75" fontFamily="serif" fontSize="90" fontWeight="bold" textAnchor="middle" fill="#007bff">M</text>
                            <rect x="10" y="80" width="80" height="5" fill="#ffffff" />
                        </svg>
                    </span>
                    <span className={styles.logoText}>MONARCH</span>
                    <label className={styles.switch}>
                        <input type="checkbox" checked={isSidebarPinned} onChange={() => setSidebarPinned(!isSidebarPinned)} />
                        <span className={`${styles.slider} ${styles.round}`}></span>
                    </label>
                </div>
                <ul className={styles.menuList}>
                    {menuItems.map((item) => {
                        const hasSubItems = !!(item.subItems && item.subItems.length > 0);
                        const isMenuOpen = openMenu === item.name;
                        return (
                            <li key={item.name} className={`${styles.menuItem} ${isMenuOpen ? styles.open : ''}`}>
                                <Link
                                    to={item.path || (hasSubItems ? item.subItems[0].path : '#')}
                                    className={styles.menuLink}
                                    onClick={(e) => handleMenuClick(e, item.name, hasSubItems)}
                                >
                                    <span className={styles.menuIcon}>{item.icon}</span>
                                    <span className={styles.menuText}>{item.name}</span>
                                    {hasSubItems && <span className={styles.arrowIcon}></span>}
                                </Link>
                                {hasSubItems && (
                                    <ul className={styles.submenu}>
                                        {item.subItems.map((subItem) => (
                                            <li key={subItem.name}><Link to={subItem.path}>{subItem.name}</Link></li>
                                        ))}
                                    </ul>
                                )}
                            </li>
                        );
                    })}
                </ul>
            </nav>

            <div className={mainContentClasses}>
                <header className={styles.topBar}>
                    <div className={styles.headerContent}>
                        <button className={styles.hamburgerButton} onClick={() => setMobileMenuOpen(!isMobileMenuOpen)}>
                            <svg viewBox="0 0 100 80" width="24" height="24" fill="#343a40">
                                <rect width="100" height="15"></rect><rect y="30" width="100" height="15"></rect><rect y="60" width="100" height="15"></rect>
                            </svg>
                        </button>
                        <nav className={styles.topMenu}>
                            {menuItems.map((item) => (
                                <div key={item.name} className={styles.topMenuItem}>
                                    <Link
                                        to={item.path || (item.subItems && item.subItems.length > 0 ? item.subItems[0].path : '#')}
                                        className={styles.topMenuLink}
                                    >
                                        {item.name}
                                    </Link>
                                    {item.subItems && item.subItems.length > 0 && (
                                        <ul className={styles.topSubmenu}>
                                            {item.subItems.map((subItem) => (<li key={subItem.name}><Link to={subItem.path}>{subItem.name}</Link></li>))}
                                        </ul>
                                    )}
                                </div>
                            ))}
                        </nav>
                        <div className={styles.userInfo}>
                            <span className={styles.welcomeMessage}>반갑습니다, {user?.[USER_FIELDS.NAME] || '사용자'} <span className={styles.honorific}>님</span></span>
                            <button onClick={handleLogout} className={styles.logoutButton}>Logout</button>
                        </div>
                    </div>
                </header>

                <div className={styles.mainWrapper}>
                    {isMobileMenuOpen && <div className={styles.overlay} onClick={() => setMobileMenuOpen(false)}></div>}
                    <Outlet /> {/* 이 부분이 페이지의 실제 내용으로 교체됩니다. */}
                </div>
            </div>
        </div>
    );
};

export default Layout;
