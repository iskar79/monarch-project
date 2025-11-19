package com.kydbm.monarch.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MStructureMapper {

    @Select("""
            SELECT TO_CHAR(STRUCTURE_CONT)
            FROM M_STRUCTURE
            WHERE STRUCTURE_NAME = #{structureName} AND M_USITE_NO = #{usiteNo}
            """)
    String findByName(@Param("structureName") String structureName, @Param("usiteNo") Long usiteNo);
}