import React, { useState, useEffect } from "react";
import axios from "axios";
import { Chart } from "react-google-charts";
import styles from "../../styles/ListPage.module.css"; // styles import 추가
import Widget from "../Widget";

interface CustomerStatusItem {
    CSTGRADE_NM: string;
    CSTGRADE_CNT: number;
}

type CustomerStatusData = (string | number)[];

const CustomerStatusWidget: React.FC = () => {
    const [chartData, setChartData] = useState<CustomerStatusData[]>([]);
    const [showTable, setShowTable] = useState(false); // 테이블 표시 여부 상태
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchCustomerStatusData = async () => {
            setIsLoading(true);
            setError(null);
            try {
                const storedUser = sessionStorage.getItem('user');
                const user = storedUser ? JSON.parse(storedUser) : {};
                const usite = user?.M_USITE_NO || 1;
                const uid = user?.M_USER_NO || null;
                const response = await axios.get('/api/data/execute', {
                    params: {
                        serviceName: 'M_CUST',
                        methodName: 'CHART_LIST',
                        USITE: usite,
                        UID: uid,
                    }
                });

                // API 응답 데이터를 Google Charts 형식에 맞게 변환합니다.
                const formattedData: CustomerStatusData[] = [["고객 등급", "고객 수"]];
                // EXEC_TYPE이 'LIST'이므로, 응답은 { data: [...], totalCount: N } 형태의 객체를 포함한 배열입니다.
                const result = response.data[0]; 
                const actualData = result?.data || [];

                actualData.forEach((item: CustomerStatusItem) => {
                    formattedData.push([item.CSTGRADE_NM, Number(item.CSTGRADE_CNT)]);
                });

                setChartData(formattedData);
            } catch (err) {
                setError('고객 상태 정보를 불러오는 데 실패했습니다.');
                console.error('Customer status data fetch error:', err);
            } finally {
                setIsLoading(false);
            }
        };

        fetchCustomerStatusData();
    }, []);

    const chartOptions = {
        title: '고객 현황',
        is3D: true,
    };

    if (isLoading) return <Widget title="고객 현황"><div className={styles.loading}>데이터를 불러오는 중입니다...</div></Widget>;
    if (error) return <Widget title="고객 현황"><div className={styles.error}>{error}</div></Widget>;

    return (
        <Widget title="고객 현황">
            {chartData.length > 1 ? (
                <>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                        <span></span> {/* 빈 span으로 공간 확보 */}
                        <button className={styles.widgetToggleButton} onClick={() => setShowTable(!showTable)}>{showTable ? '📊 차트만 보기' : '📋 데이터 보기'}</button>
                    </div>
                   <Chart
                        chartType="PieChart"
                        data={chartData}
                        options={chartOptions}
                        width={"100%"}
                        height={"250px"} // 테이블 공간을 위해 차트 높이 조정
                    />
                    <div className={styles.tableContainer} style={{
                        display: showTable ? 'block' : 'none',
                        marginTop: '20px'
                    }}>
                        <table className={`${styles.userDataTable} ${styles.tableFixedMobile}`}>
                            <thead>
                                <tr>
                                    <th className={styles.textCenter}>고객 등급</th>
                                    <th className={styles.textCenter}>고객 수</th>
                                </tr>
                            </thead>
                            <tbody>
                                {chartData.slice(1).map((row, index) => ( // 헤더를 제외하고 데이터만 렌더링
                                    <tr key={index}>
                                        <td className={styles.textCenter}>{String(row[0])}</td>
                                        <td className={styles.textRight}>{Number(row[1]).toLocaleString()}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </>
            ) : (<div className={styles.noData}>표시할 고객 현황 데이터가 없습니다.</div>)}
        </Widget>
    );
};

export default CustomerStatusWidget;
