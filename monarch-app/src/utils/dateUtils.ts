/**
 * YYYY-MM-DD 형식의 문자열로 날짜를 변환합니다.
 * @param value 변환할 날짜 값 (string, number, Date)
 * @returns 변환된 날짜 문자열 또는 빈 문자열
 */
export const formatDate = (value: unknown): string => {
    if (!value) return '';
    try {
        const date = new Date(value as string | number | Date);
        if (isNaN(date.getTime())) return String(value);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    } catch {
        return String(value);
    }
};

/**
 * 오늘을 기준으로 특정 년수 이전의 날짜부터 오늘까지의 기간을 반환합니다.
 * @param yearsAgo 오늘로부터 뺄 년수 (기본값: 10)
 * @returns { start: string, end: string } 형식의 객체
 */
export const getInitialDateRange = (yearsAgo: number = 10): { start: string, end: string } => {
    const today = new Date();
    const pastDate = new Date();
    pastDate.setFullYear(today.getFullYear() - yearsAgo);
    const toYYYYMMDD = (date: Date) => date.toISOString().slice(0, 10);
    return { start: toYYYYMMDD(pastDate), end: toYYYYMMDD(today) };
};