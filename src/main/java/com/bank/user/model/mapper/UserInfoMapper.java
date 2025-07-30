package com.bank.user.model.mapper;

import com.bank.user.model.dto.UserInfoDto;
import com.bank.user.model.entity.UserInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserInfoMapper {

    UserInfo toEntity(UserInfoDto userInfoDto);

    UserInfoDto toDto(UserInfo userInfo);
}
