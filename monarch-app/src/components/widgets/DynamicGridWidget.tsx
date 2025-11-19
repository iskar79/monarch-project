import React, { useState, useEffect, useCallback } from "react";
import axios from "axios";
import styles from "../../styles/ListPage.module.css";
import Pagination from "../Pagination";
import Widget from "../Widget";

// 화면 구성 정보 타입 정의
interface ColModel {
    label: string;
    field: string;
    type: 'text' | 'date' | 'number';
    align: 'left' | 'center' | 'right';    labelAlign?: 'left' | 'center' | 'right'; // 헤더(라벨) 정렬을 위한 옵션 추가
}

interface FilterView {
    label: string;
    field: string;
    type: 'text' | 'select' | 'date' | 'dateBetween';
}

interface StructureConfig {
    title: string;
    filterView: FilterView[];
    colModel: ColModel[];
    service: string;
    method: string;
    keyName: string;
    order: string;
}

// 동적 데이터 행을 위한 타입 정의
interface GridRow {
    [key: string]: string | number | boolean | null | undefined;
}

interface DynamicGridWidgetProps {
    structureName: string;
}

const DynamicGridWidget: React.FC<DynamicGridWidgetProps> = ({ structureName }) => {
    const [structureConfig, setStructureConfig] = useState<StructureConfig | null>(null);
    const [gridData, setGridData] = useState<GridRow[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [currentPage, setCurrentPage] = useState(1);
    const [totalCount, setTotalCount] = useState(0);
    const [pageSize, setPageSize] = useState(10);

    // 텍스트 검색과 기간 검색 상태를 분리하여 관리
    const [searchColumn, setSearchColumn] = useState('');
    const [searchKeyword, setSearchKeyword] = useState('');
    const [singleFilterValues, setSingleFilterValues] = useState<{ [key: string]: string }>({});
    const [dateFilterValues, setDateFilterValues] = useState<{ [key: string]: { from?: string, to?: string } }>({});

    // 화면 크기에 따라 페이지 크기 조절
    useEffect(() => {
        const mediaQuery = window.matchMedia("(max-width: 768px)");
        const handleResize = (e: MediaQueryListEvent | MediaQueryList) => {
            setPageSize(e.matches ? 5 : 10);
            setCurrentPage(1);
        };
        mediaQuery.addEventListener('change', handleResize);
        handleResize(mediaQuery);
        return () => mediaQuery.removeEventListener('change', handleResize);
    }, []);

    // 1. structureName을 기반으로 화면 구성 정보(JSON)를 가져오는 함수
    const fetchStructure = useCallback(async () => {
        setIsLoading(true);
        setError(null);
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
            
            if (response.data?.structureCont) {
                const parsedConfig = JSON.parse(response.data.structureCont);
                setStructureConfig(parsedConfig);
            } else {
                throw new Error("화면 구성 정보를 찾을 수 없습니다.");
            }
        } catch (err) {
            setError(`화면 구성(${structureName})을 불러오는 데 실패했습니다.`);
            console.error('Structure fetch error:', err);
            setIsLoading(false); // 구성 정보 로딩 실패 시 로딩 중단
        }
    }, [structureName]);

    // 2. 가져온 화면 구성 정보를 기반으로 실제 데이터를 가져오는 함수
    const fetchData = useCallback(async (page: number) => {
        if (!structureConfig) return;
        setIsLoading(true);
        setError(null);
        try {
            const storedUser = sessionStorage.getItem('user');
            const user = storedUser ? JSON.parse(storedUser) : {};
            const usite = user?.M_USITE_NO || 1;
            const uid = user?.M_USER_NO || null;

            // 각 필터 상태를 취합하여 백엔드 파라미터로 변환
            const appliedFilters: { [key: string]: string } = {};

            // 1. 기간 검색 필터 추가
            Object.entries(dateFilterValues).forEach(([field, dates]) => {
                if (dates.from) {
                    appliedFilters[`${field}_FROM`] = dates.from;
                }
                if (dates.to) {
                    appliedFilters[`${field}_TO`] = dates.to;
                }
            });

            // 3. 단일 값 필터 추가 (예: type: 'date')
            Object.entries(singleFilterValues).forEach(([field, value]) => {
                if (value) {
                    appliedFilters[field] = value;
                }
            });

            // 2. 텍스트 검색 필터 추가
            if (searchColumn && searchKeyword.trim() !== '') {
                appliedFilters[searchColumn] = searchKeyword;
            }

            const response = await axios.get('/api/data/execute', {
                params: {
                    serviceName: structureConfig.service,
                    methodName: structureConfig.method,
                    USITE: usite,
                    UID: uid,
                    _page: page,
                    _sort: structureConfig.order,
                    _size: pageSize,
                    ...appliedFilters,
                }
            });
            const responseData = Array.isArray(response.data) ? response.data[0] : response.data;
            setGridData(responseData?.data || []);
            setTotalCount(responseData?.totalCount || 0);
        } catch (err) {
            setError('데이터를 불러오는 데 실패했습니다.');
            console.error('Data fetch error:', err);
        } finally {
            setIsLoading(false);
        }
    }, [structureConfig, searchColumn, searchKeyword, dateFilterValues, singleFilterValues, pageSize]);

    // 기간 필터 값 변경 핸들러
    const handleDateFilterChange = (field: string, value: string, subField: 'from' | 'to') => {
        setDateFilterValues(prev => ({
            ...prev,
            [field]: { ...prev[field], [subField]: value }
        }));
    };

    // 단일 값 필터 변경 핸들러 (예: 단일 날짜)
    const handleSingleFilterChange = (field: string, value: string) => {
        setSingleFilterValues(prev => ({ ...prev, [field]: value }));
    };

    // 검색 버튼 클릭 핸들러
    const handleSearch = () => {
        setCurrentPage(1);
        fetchData(1);
    };

    useEffect(() => {
        fetchStructure();
    }, [fetchStructure]); // 의존성 배열은 그대로 유지

    useEffect(() => {
        if (structureConfig) {
            fetchData(currentPage);
        }
    }, [structureConfig, fetchData, currentPage]);

    if (isLoading) return <Widget title={structureConfig?.title || "데이터 목록"}><div className={styles.loading}>데이터를 불러오는 중입니다...</div></Widget>;
    if (error) return <Widget title={structureConfig?.title || "오류"}><div className={styles.error}>{error}</div></Widget>;
    if (!structureConfig) return <Widget title="오류"><div className={styles.error}>화면 구성 정보를 찾을 수 없습니다.</div></Widget>;

    // 필터 타입에 따라 분리
    const dateBetweenFilters = structureConfig.filterView.filter(f => f.type === 'dateBetween');
    const singleDateFilters = structureConfig.filterView.filter(f => f.type === 'date');
    const textFilters = structureConfig.filterView.filter(f => {
        const type = f.type || 'text'; // type이 없으면 'text'를 기본값으로 사용
        return type === 'text' || type === 'select';
    });

    return (
        <Widget title={structureConfig.title}>
            <div className={styles.filterContainer}>
                {/* 기간 검색 필터 행 */}
                {dateBetweenFilters.map(filter => (
                    <div key={filter.field} className={styles.filterRow} style={{ flexWrap: 'nowrap' }}>
                        <label className={styles.filterLabel}>{filter.label}</label>
                        <input type="date" className={styles.dateInput} value={dateFilterValues[filter.field]?.from || ''} onChange={e => handleDateFilterChange(filter.field, e.target.value, 'from')} />
                        <span className={styles.dateSeparator}>~</span>
                        <input type="date" className={styles.dateInput} value={dateFilterValues[filter.field]?.to || ''} onChange={e => handleDateFilterChange(filter.field, e.target.value, 'to')} />
                    </div>
                ))}

                {/* 단일 날짜 검색 필터 행 */}
                {singleDateFilters.map(filter => (
                    <div key={filter.field} className={styles.filterRow}>
                        <label className={styles.filterLabel}>{filter.label}</label>
                        <input type="date" className={styles.dateInput} value={singleFilterValues[filter.field] || ''} onChange={e => handleSingleFilterChange(filter.field, e.target.value)} />
                    </div>
                ))}

                {/* 텍스트 검색 필터 및 조회 버튼 행 */}
                <div className={styles.filterRow}>
                    <select className={styles.filterSelect} value={searchColumn} onChange={e => setSearchColumn(e.target.value)} required>
                        <option value="" disabled>검색 항목</option>
                        {textFilters.map(filter => (
                            <option key={filter.field} value={filter.field}>{filter.label}</option>
                        ))}
                    </select>
                    <input type="text" className={styles.filterInput} value={searchKeyword} onChange={e => setSearchKeyword(e.target.value)} onKeyPress={e => e.key === 'Enter' && handleSearch()} placeholder="검색어를 입력하세요..." />
                    <button className={styles.searchButton} onClick={handleSearch}>조회</button>
                </div>
            </div>

            {gridData.length > 0 ? (
                <>
                    <div className={styles.tableContainer}>
                        <table className={styles.userDataTable}>
                            <thead>
                                <tr>{structureConfig.colModel.map(col => {
                                    const labelAlignValue = col.labelAlign || 'center'; // labelAlign이 없으면 'center'를 기본값으로 사용
                                    const labelClassName = styles[`text${labelAlignValue.charAt(0).toUpperCase() + labelAlignValue.slice(1)}`];
                                    return <th key={col.field} className={labelClassName}>{col.label}</th>;
                                })}</tr>
                            </thead>
                            <tbody>
                                {gridData.map((row, index) => (
                                    <tr key={row[structureConfig.keyName]?.toString() ?? index}>
                                        {structureConfig.colModel.map(col => {
                                            const align = col.align || 'center'; // align이 없으면 'center'를 기본값으로 사용
                                            const type = col.type || 'text'; // type이 없으면 'text'를 기본값으로 사용
                                            const className = styles[`text${align.charAt(0).toUpperCase() + align.slice(1)}`];
                                            return (
                                                <td 
                                                key={col.field} 
                                                data-label={col.label} 
                                                className={className}>
                                                {type === 'date' && row[col.field] ? new Date(row[col.field] as string | number | Date).toISOString().slice(0, 10) : row[col.field]}
                                                </td>
                                            );
                                        })}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                    <div className={styles.paginationWrapper}>
                        <span className={styles.totalCount}>총 {totalCount}건</span>
                        <Pagination currentPage={currentPage} totalCount={totalCount} pageSize={pageSize} onPageChange={setCurrentPage} />
                    </div>
                </>
            ) : (
                <div className={styles.noData}>표시할 데이터가 없습니다.</div>
            )}
        </Widget>
    );
};

export default DynamicGridWidget;