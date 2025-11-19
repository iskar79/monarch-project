package com.kydbm.monarch.service;

import com.kydbm.monarch.mapper.MStructureMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DynamicGridStructure {

    private static final Logger log = LoggerFactory.getLogger(DynamicGridStructure.class);
    private final MStructureMapper mStructureMapper;

    public DynamicGridStructure(MStructureMapper mStructureMapper) {
        this.mStructureMapper = mStructureMapper;
    }

    public String getStructureByName(String structureName, Long usiteNo) {
        log.info("Fetching structure for name: {} and site: {}", structureName, usiteNo);
        return mStructureMapper.findByName(structureName, usiteNo);
    }
}