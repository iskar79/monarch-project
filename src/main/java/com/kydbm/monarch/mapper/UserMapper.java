package com.kydbm.monarch.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 사용자 정보(`M_USER` 테이블)에 접근하기 위한 MyBatis 매퍼 인터페이스.
 * 사용자 인증 및 관리와 관련된 SQL 쿼리를 정의합니다.
 */
@Mapper
@Repository // Spring이 이 인터페이스를 데이터 접근 계층의 컴포넌트로 인식하도록 합니다.
public interface UserMapper {

    /**
     * `userCode`를 기반으로 사용자의 상세 정보를 조회합니다.
     * `USE_FLAG`가 '1'인 활성 사용자만 조회합니다.
     * @param userCode 조회할 사용자 코드 (로그인 ID)
     * @return 사용자의 상세 정보를 담은 Map 리스트 (일반적으로 1개의 결과)
     */
    @Select("""
            SELECT M_USER_NO, USER_CODE, USER_PASSWORD, USER_NAME, HIRE_DATE, RETIREMENT_DATE, TEL_NO, MOBILE_NO, EMAIL, GROUPWARE_KEY, M_DEPT_NO, DEPT_CODE, USE_FLAG, M_USITE_NO, REG_DATE, UPD_DATE, REG_USER, UPD_USER, POSITION_CODE, DUTY_CODE, CONN_DUR, MULTIPLE_LOGIN_FLAG, START_MENU, PW_UPD_DATE, ACCESS_LIMIT_FLAG, LOGIN_FAIL_CNT, USER_LANG, USERDUTY, USERID, AUTH_NUM
            FROM M_USER
            WHERE USER_CODE = #{userCode} AND USE_FLAG = '1'
            """)
    List<Map<String, Object>> findUserDetailsByUserCode(@Param("userCode") String userCode);

    /**
     * 특정 사용자의 로그인 실패 횟수(`LOGIN_FAIL_CNT`)를 1 증가시킵니다.
     * @param userCode 로그인 실패한 사용자 코드
     */
    @Update("UPDATE M_USER SET LOGIN_FAIL_CNT = NVL(LOGIN_FAIL_CNT, 0) + 1, UPD_DATE = SYSDATE WHERE USER_CODE = #{userCode}")
    void incrementLoginFailCount(@Param("userCode") String userCode);

    /**
     * 특정 사용자의 로그인 실패 횟수(`LOGIN_FAIL_CNT`)를 0으로 초기화합니다.
     * @param userCode 로그인 성공한 사용자 코드
     */
    @Update("UPDATE M_USER SET LOGIN_FAIL_CNT = 0, UPD_DATE = SYSDATE WHERE USER_CODE = #{userCode}")
    void resetLoginFailCount(@Param("userCode") String userCode);
}
