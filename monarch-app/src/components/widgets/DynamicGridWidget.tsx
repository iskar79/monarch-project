import React, { useState, useEffect, useCallback } from "react";
import axios from "axios";
import styles from "../../styles/ListPage.module.css";
import Pagination from "../Pagination";
import Widget from "../Widget";

/**
 * @interface ColModel
 * @description 테이블의 각 컬럼(열)에 대한 속성을 정의합니다.
 * 이 정보를 기반으로 테이블의 헤더와 각 행의 셀이 동적으로 생성됩니다.
 */
interface ColModel {
    label: string; // 화면에 표시될 컬럼의 제목 (예: "영업건명")
    field: string; // API 응답 데이터에서 이 컬럼에 해당하는 데이터의 키(key) (예: "SALENAME")
    type: 'text' | 'date' | 'number'; // 데이터의 종류. 'date' 타입은 'YYYY-MM-DD' 형식으로 자동 포맷팅됩니다.
    align: 'left' | 'center' | 'right'; // 셀 내용의 가로 정렬 방식
    labelAlign?: 'left' | 'center' | 'right'; // 헤더(라벨)의 가로 정렬 방식. 지정하지 않으면 'center'가 기본값입니다.
}

/**
 * @interface FilterView
 * @description 검색 영역에 표시될 각 필터 UI의 속성을 정의합니다.
 */
interface FilterView {
    label: string; // 화면에 표시될 필터의 이름 (예: "예상수주일")
    field: string; // 이 필터가 제어할 데이터의 키(key) (예: "FORDDATE")
    type: 'text' | 'select' | 'date' | 'dateBetween'; // 필터의 UI 종류. 'dateBetween'은 시작일/종료일 쌍으로, 'date'는 단일 날짜 선택으로, 'text'/'select'는 텍스트 검색으로 렌더링됩니다.
}

/**
 * @interface StructureConfig
 * @description 하나의 동적 그리드 위젯 전체의 구성 정보를 정의하는 핵심 인터페이스입니다.
 * 백엔드의 M_STRUCTURE 테이블에서 이 구조에 맞는 JSON 문자열을 받아와 파싱하여 사용합니다.
 */
interface StructureConfig {
    title: string; // 위젯의 제목
    filterView: FilterView[]; // 검색 필터 UI 구성 정보 배열
    colModel: ColModel[]; // 테이블 컬럼 구성 정보 배열
    service: string; // 데이터를 조회할 때 사용할 백엔드 서비스 이름 (예: "M_SALES")
    method: string;  // 해당 서비스 내에서 호출할 메소드 이름 (예: "MLIST")
    keyName:string; // 각 데이터 행을 고유하게 식별하는 키 필드의 이름 (예: "SALESID"). React의 key prop 생성에 사용됩니다.
    order: string;   // 데이터를 정렬할 기본 조건 (예: "SALESID DESC")
}

/**
 * @interface GridRow
 * @description 그리드에 표시될 각 데이터 행(row)의 타입을 유연하게 정의합니다.
 * API로부터 받은 데이터는 다양한 키와 값을 가질 수 있으므로, 인덱스 시그니처를 사용합니다.
 */
interface GridRow {
    [key: string]: string | number | boolean | null | undefined;
}

/**
 * @interface DynamicGridWidgetProps
 * @description DynamicGridWidget 컴포넌트가 부모로부터 받는 props의 타입을 정의합니다.
 */
interface DynamicGridWidgetProps {
    structureName: string; // 어떤 그리드를 그릴지 식별하는 고유한 이름. 이 값을 기반으로 백엔드에 그리드 구성 정보를 요청합니다.
}

/**
 * @component DynamicGridWidget
 * @description
 * 백엔드에서 정의된 구조(Structure)에 따라 검색 필터와 데이터 그리드를 동적으로 생성하는 매우 유연하고 재사용 가능한 위젯입니다.
 * 이 컴포넌트는 `structureName` prop 하나만 받아서, 스스로 화면 구성 정보를 요청하고, 그에 맞춰 데이터를 조회하며, 페이징까지 처리하는 모든 로직을 내장하고 있습니다.
 *
 * @param {string} structureName - `M_STRUCTURE` 테이블의 `STRUCTURE_NAME`에 해당하는 값. 이 값을 기반으로 위젯의 모든 동작이 결정됩니다.
 */
const DynamicGridWidget: React.FC<DynamicGridWidgetProps> = ({ structureName }) => {
    // --- 상태(State) 관리 ---
    // React의 상태는 컴포넌트가 기억해야 할 값이며, 상태가 변경되면 컴포넌트가 다시 렌더링됩니다.

    // 백엔드에서 받아온 그리드 구성 정보(JSON 객체)를 저장하는 상태입니다. 초기값은 null입니다.
    const [structureConfig, setStructureConfig] = useState<StructureConfig | null>(null);
    // 실제 그리드에 표시될 데이터 배열(행의 목록)을 저장하는 상태입니다.
    const [gridData, setGridData] = useState<GridRow[]>([]);
    // 데이터 로딩 상태를 관리합니다. API 요청이 시작되면 true, 완료되면 false가 됩니다.
    const [isLoading, setIsLoading] = useState(true);
    // API 요청 중 오류가 발생했을 때, 오류 메시지를 저장하는 상태입니다.
    const [error, setError] = useState<string | null>(null);

    // --- 페이징 관련 상태 ---
    // 사용자가 보고 있는 현재 페이지 번호를 저장합니다.
    const [currentPage, setCurrentPage] = useState(1);
    // 조회된 데이터의 전체 개수를 저장합니다. 페이징 계산에 사용됩니다.
    const [totalCount, setTotalCount] = useState(0);
    // 한 페이지에 보여줄 데이터의 개수를 저장합니다. 화면 크기에 따라 동적으로 변경됩니다.
    const [pageSize, setPageSize] = useState(10);

    // --- 검색 필터 관련 상태 ---
    // 텍스트 검색 필터에서 사용자가 선택한 검색 항목(컬럼)을 저장합니다. (예: "SALENAME")
    const [searchColumn, setSearchColumn] = useState('');
    // 텍스트 검색 필터에 사용자가 입력한 검색어를 저장합니다.
    const [searchKeyword, setSearchKeyword] = useState('');
    // 단일 값 필터(예: type: 'date')의 값들을 { 필드명: 값 } 형태로 저장합니다.
    const [singleFilterValues, setSingleFilterValues] = useState<{ [key: string]: string }>({});
    // 기간 검색 필터(type: 'dateBetween')의 값들을 { 필드명: { from: 시작일, to: 종료일 } } 형태로 저장합니다.
    const [dateFilterValues, setDateFilterValues] = useState<{ [key: string]: { from?: string, to?: string } }>({});

    // --- 사이드 이펙트(Side Effect) 관리 ---
    // useEffect 훅은 컴포넌트의 렌더링과 별개로 비동기 작업이나 구독(subscription) 등을 처리할 때 사용됩니다.

    // 화면 크기(모바일/데스크탑)에 따라 한 페이지에 보여줄 아이템 개수를 동적으로 조절합니다.
    useEffect(() => {
        const mediaQuery = window.matchMedia("(max-width: 768px)");
        const handleResize = (e: MediaQueryListEvent | MediaQueryList) => {
            // 화면 너비가 768px 이하이면 페이지 크기를 5로, 그보다 크면 10으로 설정하고 첫 페이지로 이동시킵니다.
            setPageSize(e.matches ? 5 : 10);
            setCurrentPage(1);
        };
        mediaQuery.addEventListener('change', handleResize);
        handleResize(mediaQuery); // 컴포넌트가 처음 로드될 때 현재 화면 크기에 맞춰 한 번 실행합니다.
        // 컴포넌트가 화면에서 사라질 때(unmount) 등록했던 이벤트 리스너를 정리합니다.
        // 이는 메모리 누수를 방지하는 중요한 작업입니다.
        return () => mediaQuery.removeEventListener('change', handleResize);
    }, []);

    /**
     * @function fetchStructure
     * @description 1. structureName을 기반으로 화면 구성 정보(JSON)를 백엔드에서 가져오는 함수입니다.
     * 이 함수는 위젯이 어떻게 보이고 동작해야 하는지에 대한 '설계도'를 가져오는 역할을 합니다.
     * useCallback을 사용하여 structureName prop이 변경되지 않는 한, 이 함수가 불필요하게 재생성되는 것을 방지하여 성능을 최적화합니다.
     */
    const fetchStructure = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            // 세션 스토리지에서 사용자 정보를 가져와 usiteNo를 설정합니다. 로그인 정보가 없으면 기본값 1을 사용합니다.
            const storedUser = sessionStorage.getItem('user');
            const user = storedUser ? JSON.parse(storedUser) : {};
            const usiteNo = user?.M_USITE_NO || 1;

            // 백엔드에 'M_STRUCTURE' 서비스로 그리드 구성 정보를 요청합니다.
            const response = await axios.get('/api/data/execute', {
                params: {
                    serviceName: 'M_STRUCTURE',
                    methodName: 'MVIEW',
                    structureName: structureName,
                    usiteNo: usiteNo,
                }
            });
            
            if (response.data?.structureCont) {
                // 백엔드에서 받은 JSON 문자열을 실제 자바스크립트 객체로 파싱하여 상태에 저장합니다.
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

    /**
     * 2. 가져온 화면 구성 정보(structureConfig)와 현재 필터/페이지 정보를 기반으로 실제 데이터를 가져오는 함수입니다.
     * useCallback을 사용하여 의존성이 변경되지 않는 한 함수를 재생성하지 않도록 최적화합니다.
     */
    const fetchData = useCallback(async (page: number) => {
        // 구성 정보가 아직 로드되지 않았으면 데이터 조회를 시도하지 않습니다.
        if (!structureConfig) return;
        setIsLoading(true);
        setError(null);
        try {
            // 세션 스토리지에서 사용자 정보를 가져와 usite, uid를 설정합니다.
            const storedUser = sessionStorage.getItem('user');
            const user = storedUser ? JSON.parse(storedUser) : {};
            const usite = user?.M_USITE_NO || 1;
            const uid = user?.M_USER_NO || null;

            // 각 필터 상태를 취합하여 백엔드 API 요청에 사용할 파라미터 객체를 생성합니다.
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

            // 백엔드에 실제 데이터 조회를 요청합니다.
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
            // 백엔드 응답은 항상 배열 형태이므로, 첫 번째 요소를 사용합니다.
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

    // --- 이벤트 핸들러 ---

    /**
     * @handler handleDateFilterChange
     * @description 기간 필터(dateBetween)의 값이 변경될 때 호출되어 `dateFilterValues` 상태를 업데이트합니다.
     */
    const handleDateFilterChange = (field: string, value: string, subField: 'from' | 'to') => {
        setDateFilterValues(prev => ({
            ...prev,
            [field]: { ...prev[field], [subField]: value }
        }));
    };

    /**
     * @handler handleSingleFilterChange
     * @description 단일 값 필터(예: type: 'date')의 값이 변경될 때 호출되어 `singleFilterValues` 상태를 업데이트합니다.
     */
    const handleSingleFilterChange = (field: string, value: string) => {
        setSingleFilterValues(prev => ({ ...prev, [field]: value }));
    };

    /**
     * @handler handleSearch
     * @description '조회' 버튼 클릭 시 호출됩니다. 현재 필터 조건으로 데이터를 다시 조회합니다.
     */
    const handleSearch = () => {
        // 새로운 검색 조건으로 조회할 때는 항상 첫 페이지부터 결과를 보여주는 것이 사용자 경험에 좋습니다.
        setCurrentPage(1);
        fetchData(1);
    };

    // --- 컴포넌트 생명주기 관리 ---

    // 컴포넌트가 처음 마운트되거나, 부모로부터 받은 `structureName` prop이 변경될 때, 그리드 구성 정보를 가져오는 `fetchStructure` 함수를 호출합니다.
    useEffect(() => {
        fetchStructure();
    }, [fetchStructure]); // 의존성 배열에 `fetchStructure` 함수 자체를 넣어, 이 함수가 변경될 때만 effect가 실행되도록 합니다. (useCallback으로 최적화됨)

    // 그리드 구성 정보(structureConfig)가 성공적으로 로드되거나, 사용자가 페이지네이션을 통해 `currentPage`를 변경할 때,
    // 해당 페이지의 데이터를 가져오는 `fetchData` 함수를 호출합니다.
    useEffect(() => {
        if (structureConfig) {
            fetchData(currentPage);
        }
    }, [structureConfig, fetchData, currentPage]); // `structureConfig`가 있어야만 데이터를 가져올 수 있으므로 의존성 배열에 포함합니다.

    // --- 조건부 렌더링 ---
    // 컴포넌트의 현재 상태(로딩, 에러 등)에 따라 다른 UI를 보여줍니다.

    // 로딩 중일 때 표시할 UI입니다. 사용자에게 시스템이 동작 중임을 알려줍니다.
    if (isLoading) return <Widget title={structureConfig?.title || "데이터 목록"}><div className={styles.loading}>데이터를 불러오는 중입니다...</div></Widget>;
    // 오류가 발생했을 때 표시할 UI입니다. 사용자에게 문제가 발생했음을 명확히 알려줍니다.
    if (error) return <Widget title={structureConfig?.title || "오류"}><div className={styles.error}>{error}</div></Widget>;
    // 구성 정보 로딩에 실패하여 `structureConfig`가 null일 경우 표시할 UI입니다.
    if (!structureConfig) return <Widget title="오류"><div className={styles.error}>화면 구성 정보를 찾을 수 없습니다.</div></Widget>;

    // 렌더링을 위해 `structureConfig`에 정의된 필터들을 UI 타입에 따라 분리합니다.
    const dateBetweenFilters = structureConfig.filterView.filter(f => f.type === 'dateBetween');
    const singleDateFilters = structureConfig.filterView.filter(f => f.type === 'date');
    const textFilters = structureConfig.filterView.filter(f => {
        const type = f.type || 'text'; // type이 없으면 'text'를 기본값으로 사용
        return type === 'text' || type === 'select';
    });

    return (
        <Widget title={structureConfig.title}>
            {/* --- 검색 필터 UI --- */}
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

            {/* --- 데이터 그리드 및 페이징 UI --- */}
            {gridData.length > 0 ? (
                <>
                    {/* 테이블 컨테이너 (가로 스크롤 지원) */}
                    <div className={styles.tableContainer}>
                        <table className={styles.userDataTable}>
                            <thead>
                                {/* colModel을 기반으로 테이블 헤더를 동적으로 생성합니다. */}
                                <tr>{structureConfig.colModel.map(col => {
                                    const labelAlignValue = col.labelAlign || 'center'; // labelAlign이 없으면 'center'를 기본값으로 사용
                                    const labelClassName = styles[`text${labelAlignValue.charAt(0).toUpperCase() + labelAlignValue.slice(1)}`];
                                    return <th key={col.field} className={labelClassName}>{col.label}</th>;
                                })}</tr>
                            </thead>
                            <tbody>
                                {/* gridData를 기반으로 테이블 본문을 동적으로 생성합니다. */}
                                {gridData.map((row, index) => (
                                    <tr key={row[structureConfig.keyName]?.toString() ?? index}>
                                        {structureConfig.colModel.map(col => {
                                            const align = col.align || 'center'; // align이 없으면 'center'를 기본값으로 사용
                                            const type = col.type || 'text'; // type이 없으면 'text'를 기본값으로 사용
                                            const className = styles[`text${align.charAt(0).toUpperCase() + align.slice(1)}`];
                                            return (
                                                // 모바일 뷰를 위해 data-label 속성을 추가합니다.
                                                <td 
                                                key={col.field} 
                                                data-label={col.label} 
                                                className={className}>
                                                {/* 컬럼 타입이 'date'이면 'YYYY-MM-DD' 형식으로 포맷팅합니다. */}
                                                {type === 'date' && row[col.field] ? new Date(row[col.field] as string | number | Date).toISOString().slice(0, 10) : row[col.field]}
                                                </td>
                                            );
                                        })}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                    {/* 페이징 컨트롤 UI */}
                    <div className={styles.paginationWrapper}>
                        <span className={styles.totalCount}>총 {totalCount}건</span>
                        <Pagination currentPage={currentPage} totalCount={totalCount} pageSize={pageSize} onPageChange={setCurrentPage} />
                    </div>
                </>
            ) : (
                // 데이터가 없을 때 표시할 UI
                <div className={styles.noData}>표시할 데이터가 없습니다.</div>
            )}
        </Widget>
    );
};

export default DynamicGridWidget;