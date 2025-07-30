package com.bank.user.repository;

import com.bank.user.model.entity.RequestType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequestTypeRepository extends JpaRepository<RequestType, Integer> {

    @Cacheable(value = "requestTypes", key = "#name")
    Optional<RequestType> findByName(String name);
}
