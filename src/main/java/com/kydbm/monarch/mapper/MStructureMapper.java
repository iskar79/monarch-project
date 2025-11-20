package com.kydbm.monarch.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 화면 구성(Structure) 정보에 접근하기 위한 MyBatis 매퍼 인터페이스.
 * `M_STRUCTURE` 테이블 관련 SQL 쿼리를 정의합니다.
 */
@Mapper
public interface MStructureMapper {

    /**
     * `structureName`과 `usiteNo`를 기반으로 화면 구성 내용(JSON 문자열)을 조회합니다.
     * @param structureName 조회할 화면의 고유 이름
     * @param usiteNo 회원사 번호
     * @return 조회된 화면 구성 정보 (JSON 형식의 문자열)
     */
    @Select("""
            SELECT TO_CHAR(STRUCTURE_CONT)
            FROM M_STRUCTURE
            WHERE STRUCTURE_NAME = #{structureName} AND M_USITE_NO = #{usiteNo}
            """)
    String findByName(@Param("structureName") String structureName, @Param("usiteNo") Long usiteNo);
}
