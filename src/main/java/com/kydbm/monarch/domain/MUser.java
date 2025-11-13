package com.kydbm.monarch.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 사용자 엔티티
 */
@Entity
@Table(name = "M_USER")
@Getter
@Setter
public class MUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "M_USER_NO_SEQ_GENERATOR")
    @SequenceGenerator(name = "M_USER_NO_SEQ_GENERATOR", sequenceName = "M_USER_NO_SEQ", allocationSize = 1) // 시퀀스 이름은 실제 DB에 맞게 조정 필요
    @Column(name = "M_USER_NO", nullable = false)
    private Long mUserNo;

    @Column(name = "USER_CODE", nullable = false, length = 50)
    private String userCode;

    @Column(name = "USER_PASSWORD", length = 100)
    private String userPassword;

    @Column(name = "USER_NAME", length = 100)
    private String userName;

    @Column(name = "HIRE_DATE")
    private Date hireDate;

    @Column(name = "RETIREMENT_DATE")
    private Date retirementDate;

    @Column(name = "TEL_NO", length = 20)
    private String telNo;

    @Column(name = "MOBILE_NO", length = 20)
    private String mobileNo;

    @Column(name = "EMAIL", length = 50)
    private String email;

    @Column(name = "GROUPWARE_KEY", length = 50)
    private String groupwareKey;

    @Column(name = "M_DEPT_NO")
    private Long mDeptNo;

    @Column(name = "DEPT_CODE", length = 12)
    private String deptCode;

    @Column(name = "USE_FLAG", length = 1, columnDefinition = "NCHAR(1)")
    private String useFlag;

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

    @Column(name = "POSITION_CODE", length = 20)
    private String positionCode;

    @Column(name = "DUTY_CODE", length = 20)
    private String dutyCode;

    @Column(name = "CONN_DUR", nullable = false)
    private Long connDur;

    @Column(name = "MULTIPLE_LOGIN_FLAG")
    private Long multipleLoginFlag;

    @Column(name = "START_MENU")
    private Long startMenu;

    @Column(name = "PW_UPD_DATE")
    private Date pwUpdDate;

    @Column(name = "ACCESS_LIMIT_FLAG", length = 1)
    private String accessLimitFlag;

    @Column(name = "LOGIN_FAIL_CNT")
    private Long loginFailCnt;

    @Column(name = "USER_LANG", length = 2)
    private String userLang;

    @Column(name = "USERDUTY", length = 200)
    private String userDuty; // USERDUTY

    @Column(name = "USERID", length = 50)
    private String userId; // USERID

    @Column(name = "AUTH_NUM")
    private Integer authNum;
}