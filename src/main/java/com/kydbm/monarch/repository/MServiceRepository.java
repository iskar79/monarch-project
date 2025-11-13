package com.kydbm.monarch.repository;

import com.kydbm.monarch.domain.MService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MServiceRepository extends JpaRepository<MService, Long> {
    @Query("SELECT s FROM MService s WHERE s.serviceName = :serviceName AND s.methodName = :methodName AND s.mUsiteNo = :mUsiteNo")
    Optional<MService> findByServiceDetails(@Param("serviceName") String serviceName, @Param("methodName") String methodName, @Param("mUsiteNo") Long mUsiteNo);
}