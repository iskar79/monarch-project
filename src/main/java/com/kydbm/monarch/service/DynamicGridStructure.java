package com.kydbm.monarch.service;

import com.kydbm.monarch.mapper.MStructureMapper;
import org.springframework.stereotype.Service;

/**
 * 동적 그리드의 구조(Structure) 정보를 관리하는 서비스.
 * M_STRUCTURE 테이블에서 화면 구성 정보를 조회합니다.
 */
@Service
public class DynamicGridStructure {

    private final MStructureMapper mStructureMapper;

    public DynamicGridStructure(MStructureMapper mStructureMapper) {
        this.mStructureMapper = mStructureMapper;
    }

    /**
     * structureName과 usiteNo를 기반으로 화면 구성 정보(JSON 문자열)를 가져옵니다.
     * @param structureName 조회할 화면의 고유 이름
     * @param usiteNo 회원사 번호
     * @return 조회된 화면 구성 정보 (JSON 형식의 문자열)
     */
    public String getStructureByName(String structureName, Long usiteNo) {
        return mStructureMapper.findByName(structureName, usiteNo);
    }
}
