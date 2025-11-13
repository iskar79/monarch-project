package com.kydbm.monarch.repository;

import com.kydbm.monarch.domain.MUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MUserRepository extends JpaRepository<MUser, Long> {
    // 필요한 경우 여기에 추가적인 쿼리 메소드를 정의할 수 있습니다.
    Optional<MUser> findByUserCode(String userCode);
}