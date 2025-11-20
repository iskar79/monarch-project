package com.kydbm.monarch.repository;

import com.kydbm.monarch.domain.MService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * `M_SERVICE` 테이블에 접근하기 위한 Spring Data JPA 리포지토리 인터페이스.
 * `MService` 엔티티를 사용하여 데이터베이스 작업을 수행합니다.
 */
@Repository
public interface MServiceRepository extends JpaRepository<MService, Long> {

    /**
     * `serviceName`, `methodName`, `mUsiteNo`를 기반으로 서비스 정보를 조회합니다.
     * @param serviceName 조회할 서비스 이름
     * @param methodName 조회할 메소드 이름
     * @param mUsiteNo 회원사 번호
     * @return 조회된 서비스 정보 (Optional - 결과가 없을 수도 있음을 나타냄)
     */
    @Query("SELECT s FROM MService s WHERE s.serviceName = :serviceName AND s.methodName = :methodName AND s.mUsiteNo = :mUsiteNo")
    Optional<MService> findByServiceDetails(@Param("serviceName") String serviceName, @Param("methodName") String methodName, @Param("mUsiteNo") Long mUsiteNo);
}
