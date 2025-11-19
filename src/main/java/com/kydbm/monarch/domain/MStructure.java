package com.kydbm.monarch.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 동적 화면 구성 정보를 관리하는 엔티티
 */
@Entity
@Table(name = "M_STRUCTURE", uniqueConstraints = {
    @UniqueConstraint(name = "M_STRUCTURE_UK", columnNames = {"STRUCTURE_NAME", "M_USITE_NO"})
})
@Getter
@Setter
public class MStructure {
    /**
     * 화면구성번호 (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "M_STRUCTURE_NO_SEQ_GENERATOR")
    @SequenceGenerator(name = "M_STRUCTURE_NO_SEQ_GENERATOR", sequenceName = "M_STRUCTURE_NO_SEQ", allocationSize = 1)
    @Column(name = "M_STRUCTURE_NO")
    private Long mStructureNo;

    /**
     * 화면구성명 (위젯을 식별하는 고유한 이름)
     */
    @Column(name = "STRUCTURE_NAME")
    private String structureName;

    /**
     * 화면구성 유형 (예: 고객 > 고객관리)
     */
    @Column(name = "STRUCTURE_TYPE")
    private String structureType;

    /**
     * 화면 구성 내용 (JSON 형식)
     */
    @Lob
    @Column(name = "STRUCTURE_CONT", columnDefinition = "NCLOB")
    private String structureCont;

    /**
     * 화면구성 설명
     */
    @Column(name = "STRUCTURE_DESC")
    private String structureDesc;

    /**
     * 사용 여부 (1: 사용, 0: 미사용)
     */
    @Column(name = "USE_FLAG")
    private String useFlag;

    /**
     * 회원사번호
     */
    @Column(name = "M_USITE_NO")
    private Long mUsiteNo;

    /**
     * 등록일
     */
    @CreationTimestamp
    @Column(name = "REG_DATE")
    private LocalDateTime regDate;

    /**
     * 수정일
     */
    @UpdateTimestamp
    @Column(name = "UPD_DATE")
    private LocalDateTime updDate;

    /**
     * 등록자
     */
    @Column(name = "REG_USER")
    private Long regUser;

    /**
     * 수정자
     */
    @Column(name = "UPD_USER")
    private Long updUser;

    /**
     * HTML 내용 (사용 시)
     */
    @Lob
    @Column(name = "HTML_CONT", columnDefinition = "NCLOB")
    private String htmlCont;
}