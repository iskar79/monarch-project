package com.kydbm.monarch.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 서비스(동적 쿼리) 정보에 접근하기 위한 MyBatis 매퍼 인터페이스.
 * `M_SERVICE` 테이블 관련 SQL 쿼리를 정의합니다.
 */
@Mapper
public interface MServiceMapper {

    /**
     * `serviceName`, `methodName`, `usiteNo`를 기반으로 서비스 정보를 조회합니다.
     * @param usiteNo 회원사 번호
     * @param serviceName 조회할 서비스 이름
     * @param methodName 조회할 메소드 이름
     * @return 조회된 서비스 정보를 담은 Map 객체 (쿼리문, 실행 타입 등 포함)
     */
    @Select("""
            SELECT M_SERVICE_NO, QUERY_NAME, SERVICE_NAME, METHOD_NAME, EXEC_TYPE,
                   TO_CHAR(QUERY_STMT) AS QUERY_STMT, QUERY_DESC, TABLE_NAME, DS_NAME,
                   USE_FLAG, M_USITE_NO, REG_DATE, UPD_DATE, REG_USER, UPD_USER
            FROM M_SERVICE
            WHERE SERVICE_NAME = #{serviceName} AND METHOD_NAME = #{methodName} AND M_USITE_NO = #{usiteNo}
            """)
    Map<String, Object> findServiceQuery(
            @Param("usiteNo") Long usiteNo,
            @Param("serviceName") String serviceName,
            @Param("methodName") String methodName
    );
}
