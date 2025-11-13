package com.kydbm.monarch.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("""
            SELECT * FROM M_USER WHERE USER_CODE = #{userCode} AND USE_FLAG = '1'
            """)
    List<Map<String, Object>> findUserDetailsByUserCode(String userCode);

    @Update("UPDATE M_USER SET LOGIN_FAIL_CNT = NVL(LOGIN_FAIL_CNT, 0) + 1, UPD_DATE = SYSDATE WHERE USER_CODE = #{userCode}")
    void incrementLoginFailCount(String userCode);

    @Update("UPDATE M_USER SET LOGIN_FAIL_CNT = 0, UPD_DATE = SYSDATE WHERE USER_CODE = #{userCode}")
    void resetLoginFailCount(String userCode);
}