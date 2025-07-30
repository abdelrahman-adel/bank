package com.bank.user.repository;

import com.bank.user.model.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

    Optional<UserInfo> findByCivilId(String civilId);
}
