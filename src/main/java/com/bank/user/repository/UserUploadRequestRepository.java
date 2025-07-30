package com.bank.user.repository;

import com.bank.user.model.entity.UserUploadRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserUploadRequestRepository extends JpaRepository<UserUploadRequest, Long> {

}
