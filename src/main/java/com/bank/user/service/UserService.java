package com.bank.user.service;


import com.bank.user.model.dto.UserInfoDto;
import org.springframework.data.domain.Page;

public interface UserService {


    UserInfoDto getUser(Long id);

    UserInfoDto getUser(String civilId);

    Page<UserInfoDto> getAllUsers(Integer page, Integer pageSize);

    UserInfoDto createUser(UserInfoDto user);

    UserInfoDto updateUser(Long id, UserInfoDto user);

    void deleteUser(Long id);

}
