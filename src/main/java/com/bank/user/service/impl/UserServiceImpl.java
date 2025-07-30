package com.bank.user.service.impl;

import com.bank.user.exception.BusinessErrors;
import com.bank.user.model.dto.UserInfoDto;
import com.bank.user.model.entity.UserInfo;
import com.bank.user.model.mapper.UserInfoMapper;
import com.bank.user.repository.UserInfoRepository;
import com.bank.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserInfoRepository userInfoRepository;

    private final UserInfoMapper userInfoMapper;

    @Override
    public Page<UserInfoDto> getAllUsers(Integer page, Integer pageSize) {
        Pageable pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name"));
        return userInfoRepository.findAll(pageRequest).map(userInfoMapper::toDto);
    }

    @Override
    public UserInfoDto getUser(String civilId) {
        return userInfoRepository.findByCivilId(civilId)
                .map(userInfoMapper::toDto)
                .orElseThrow(BusinessErrors.NO_SUCH_USER::exception);
    }

    @Override
    public UserInfoDto getUser(Long id) {
        return userInfoRepository.findById(id)
                .map(userInfoMapper::toDto)
                .orElseThrow(BusinessErrors.NO_SUCH_USER::exception);
    }

    @Override
    public UserInfoDto createUser(UserInfoDto userInfoDto) {
        userInfoRepository.findByCivilId(userInfoDto.getCivilId()).ifPresent(user -> {
            throw BusinessErrors.USER_CIVIL_ID_USED.exception();
        });
        UserInfo userInfo = userInfoMapper.toEntity(userInfoDto);
        userInfo = userInfoRepository.save(userInfo);
        return userInfoMapper.toDto(userInfo);
    }

    @Override
    public UserInfoDto updateUser(Long id, UserInfoDto userInfoDto) {
        userInfoRepository.findByCivilId(userInfoDto.getCivilId())
                .ifPresent(userWithSameCivilId -> {
                    if (!userWithSameCivilId.getId().equals(id)) {
                        throw BusinessErrors.USER_CIVIL_ID_USED.exception();
                    }
                });
        UserInfo existingUser = userInfoRepository.findById(id)
                .orElseThrow(BusinessErrors.NO_SUCH_USER::exception);
        existingUser.setName(userInfoDto.getName());
        existingUser.setCivilId(userInfoDto.getCivilId());
        existingUser.setExpiryDate(userInfoDto.getExpiryDate());
        UserInfo updatedUser = userInfoRepository.save(existingUser);
        return userInfoMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userInfoRepository.existsById(id)) {
            throw BusinessErrors.NO_SUCH_USER.exception();
        }
        userInfoRepository.deleteById(id);
    }
}
