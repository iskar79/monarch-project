// monarch-app/src/types/MService.d.ts

/**
 * @interface MService
 * @description 백엔드의 M_SERVICE 엔티티에 대응하는 타입 정의.
 * 이 인터페이스는 서비스(동적 쿼리) 정보를 나타냅니다.
 */
export interface MService {
    mServiceNo: number; // 서비스번호 (PK)
    queryName?: string; // 쿼리명
    serviceName: string; // 서비스명
    methodName: string; // 메소드명
    execType?: string; // 실행방식
    queryStmt?: string; // 쿼리문
    queryDesc?: string; // 쿼리설명
    tableName?: string; // 테이블명
    dsName?: string; // 데이터소스명
    useFlag?: string; // 사용여부 (1: 사용, 0: 미사용)
    mUsiteNo: number; // 회원사번호
    regDate?: string; // 등록일 (ISO 8601 string)
    updDate?: string; // 수정일 (ISO 8601 string)
    regUser: number; // 등록자
    updUser: number; // 수정자
}

/**
 * @interface PopupFilter
 * @description 팝업 필터의 속성을 정의합니다.
 */
export interface PopupFilter {
    label: string;
    field: string;
    displayField: string;
    type: 'popup';
    structureName: string;
    popupKey: string;
    colspan?: number | string;
}

