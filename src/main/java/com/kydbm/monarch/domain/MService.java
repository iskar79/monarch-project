package com.kydbm.monarch.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 서비스 엔티티
 */
@Entity
@Table(name = "M_SERVICE", uniqueConstraints = {
    @UniqueConstraint(name = "M_SERVICE_UK", columnNames = {"SERVICE_NAME", "METHOD_NAME", "M_USITE_NO"})
})
@Getter
@Setter
public class MService {

    /**
     * 서비스번호
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "M_SERVICE_NO_SEQ_GENERATOR")
    @SequenceGenerator(name = "M_SERVICE_NO_SEQ_GENERATOR", sequenceName = "M_SERVICE_NO_SEQ", allocationSize = 1)
    @Column(name = "M_SERVICE_NO", nullable = false)
    private Long mServiceNo;

    /**
     * 쿼리명
     */
    @Column(name = "QUERY_NAME", length = 200)
    private String queryName;

    /**
     * 서비스명
     */
    @Column(name = "SERVICE_NAME", nullable = false, length = 50)
    private String serviceName;

    /**
     * 메소드명
     */
    @Column(name = "METHOD_NAME", nullable = false, length = 50)
    private String methodName;

    /**
     * 실행방식
     */
    @Column(name = "EXEC_TYPE", length = 50)
    private String execType;

    /**
     * 쿼리문
     */
    @Lob
    @Column(name = "QUERY_STMT", columnDefinition = "NCLOB")
    private String queryStmt;

    /**
     * 쿼리설명
     */
    @Column(name = "QUERY_DESC", length = 200)
    private String queryDesc;

    /**
     * 테이블명
     */
    @Column(name = "TABLE_NAME", length = 200)
    private String tableName;

    /**
     * 데이터소스명
     */
    @Column(name = "DS_NAME", length = 100)
    private String dsName;

    /**
     * 사용여부
     */
    @Column(name = "USE_FLAG", length = 1, columnDefinition = "NCHAR(1)")
    private String useFlag;

    /**
     * 회원사번호
     */
    @Column(name = "M_USITE_NO", nullable = false)
    private Long mUsiteNo;

    @CreationTimestamp
    @Column(name = "REG_DATE", nullable = false, updatable = false)
    private LocalDateTime regDate;

    @UpdateTimestamp
    @Column(name = "UPD_DATE", nullable = false)
    private LocalDateTime updDate;

    @Column(name = "REG_USER", nullable = false)
    private Long regUser;

    @Column(name = "UPD_USER", nullable = false)
    private Long updUser;
}