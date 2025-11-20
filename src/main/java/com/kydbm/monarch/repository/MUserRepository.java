package com.kydbm.monarch.repository;

import com.kydbm.monarch.domain.MUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * `M_USER` 테이블에 접근하기 위한 Spring Data JPA 리포지토리 인터페이스.
 * `MUser` 엔티티를 사용하여 데이터베이스 작업을 수행합니다.
 */
@Repository
public interface MUserRepository extends JpaRepository<MUser, Long> {
    /**
     * `userCode`를 기반으로 사용자 정보를 조회합니다.
     * @param userCode 조회할 사용자 코드 (로그인 ID)
     * @return 조회된 사용자 정보 (Optional - 결과가 없을 수도 있음을 나타냄)
     */
    Optional<MUser> findByUserCode(String userCode);
}