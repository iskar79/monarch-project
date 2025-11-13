package com.kydbm.monarch.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface MServiceMapper {
    @Select("""
            SELECT EXEC_TYPE, QUERY_STMT
            FROM M_SERVICE
            WHERE M_USITE_NO = #{uSite} AND SERVICE_NAME = #{serviceNm} AND METHOD_NAME = #{methodNm}
            """)
    Map<String, Object> findServiceQuery(@Param("uSite") Long uSite,
                                                   @Param("serviceNm") String serviceNm,
                                                   @Param("methodNm") String methodNm);
}
