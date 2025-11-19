import React, { useState, useEffect, useMemo } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import axios from 'axios';

import styles from './Layout.module.css';
import { useNavigate } from 'react-router-dom';

interface UserData {
    USER_NAME: string;
    [key: string]: string | number | boolean | null | undefined;
}

// 메뉴 구조 정의
const menuItems = [
    { name: '대시보드', path: '/', icon: '📊' },
    { name: '영업', icon: '💼', subItems: [
        { name: '영업관리', path: '/sales' },
        { name: '접촉관리', path: '/sales/contact' } // "접촉관리" 메뉴로 수정
    ]},
    { name: '고객', icon: '👥', subItems: [{ name: '고객관리', path: '/customer' }] },
    { name: 'Admin', icon: '⚙️', subItems: [
        { name: '사용자관리', path: '/admin/users' },
        { name: '개발정보', path: '/admin/dev' }
    ] },
];

const Layout: React.FC = () => {
    const [isSidebarPinned, setSidebarPinned] = useState(false);
    const [isMobileMenuOpen, setMobileMenuOpen] = useState(false);
    const [openMenu, setOpenMenu] = useState<string | null>(null); // 아코디언 메뉴 상태
    const location = useLocation(); // 페이지 이동 감지를 위해 사용
    const navigate = useNavigate();

    // sessionStorage에서 사용자 정보를 읽어옵니다.
    const user: UserData | null = useMemo(() => {
        const storedUser = sessionStorage.getItem('user');
        return storedUser ? JSON.parse(storedUser) : null;
    }, []);

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

    const handleLogout = async () => {
        await axios.post('/api/logout').catch((err) => console.error("Logout failed", err));
        sessionStorage.removeItem('user');
        navigate('/login');
    };

    // // 재귀적으로 메뉴를 렌더링하는 함수
    // const renderMenuItems = (items: MenuItem[], isSubmenu = false) => {
    //     return items.map((item) => {
    //         const hasSubItems = !!(item.subItems && item.subItems.length > 0);
    //         const isMenuOpen = openMenu === item.name;

    //         // 서브메뉴가 있는 경우, 첫 번째 서브메뉴의 경로를 기본 경로로 사용
    //         const linkPath = item.path || (hasSubItems ? '#' : '#');

    //         return (
    //             <li key={item.name} className={`${styles.menuItem} ${isMenuOpen ? styles.open : ''} ${isSubmenu ? styles.subMenuItem : ''}`}>
    //                 <Link
    //                     to={linkPath}
    //                     className={styles.menuLink}
    //                     onClick={(e) => handleMenuClick(e, item.name, hasSubItems)}
    //                 >
    //                     {item.icon && <span className={styles.menuIcon}>{item.icon}</span>}
    //                     <span className={styles.menuText}>{item.name}</span>
    //                     {hasSubItems && <span className={styles.arrowIcon}></span>}
    //                 </Link>
    //                 {hasSubItems && (
    //                     <ul className={styles.submenu}>
    //                         {renderMenuItems(item.subItems!, true)}
    //                     </ul>
    //                 )}
    //             </li>
    //         );
    //     });
    // };

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
                                    to={item.path || (item.subItems && item.subItems.length > 0 ? item.subItems[0].path || '#' : '#')}
                                    className={styles.menuLink}
                                    onClick={(e) => handleMenuClick(e, item.name, hasSubItems)}
                                >
                                    {item.icon && <span className={styles.menuIcon}>{item.icon}</span>}
                                    <span className={styles.menuText}>{item.name}</span>
                                    {hasSubItems && <span className={styles.arrowIcon}></span>}
                                </Link>
                                {hasSubItems && (
                                    <ul className={styles.submenu}>
                                        {item.subItems.map((subItem) => (
                                            <li key={subItem.name}>
                                                <Link to={subItem.path || '#'}>{subItem.name}</Link>
                                            </li>
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
                                    <Link to={item.path || (item.subItems && item.subItems.length > 0 ? item.subItems[0].path || '#' : '#')} className={styles.topMenuLink}>
                                        {item.name}
                                    </Link>
                                    {item.subItems && item.subItems.length > 0 && (
                                        <ul className={styles.topSubmenu}>
                                            {item.subItems.map((subItem) => (
                                                <li key={subItem.name}><Link to={subItem.path || '#'}>{subItem.name}</Link></li>
                                            ))}
                                        </ul>
                                    )}
                                </div>
                            ))}
                        </nav>
                        <div className={styles.userInfo}>
                            <span className={styles.welcomeMessage}>반갑습니다, {user?.USER_NAME || '사용자'} <span className={styles.honorific}>님</span></span>
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
