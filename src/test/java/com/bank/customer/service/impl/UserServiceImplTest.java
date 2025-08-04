package com.bank.user.service.impl;

import com.bank.user.exception.BusinessErrors;
import com.bank.user.exception.BusinessException;
import com.bank.user.model.dto.UserInfoDto;
import com.bank.user.model.entity.UserInfo;
import com.bank.user.model.mapper.UserInfoMapper;
import com.bank.user.repository.UserInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserInfoRepository userInfoRepository;

    @Mock
    private UserInfoMapper userInfoMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void whenCreateUser_withUniqueCivilId_shouldSucceed() {
        // Arrange
        UserInfoDto newUserDto = new UserInfoDto();
        newUserDto.setCivilId("12345");
        newUserDto.setName("Test User");
        newUserDto.setExpiryDate(LocalDateTime.now().plusYears(1));

        UserInfo newUserEntity = new UserInfo();
        newUserEntity.setCivilId("12345");

        UserInfo savedUserEntity = new UserInfo();
        savedUserEntity.setId(1L);
        savedUserEntity.setCivilId("12345");

        when(userInfoRepository.findByCivilId("12345")).thenReturn(Optional.empty());
        when(userInfoMapper.toEntity(any(UserInfoDto.class))).thenReturn(newUserEntity);
        when(userInfoRepository.save(any(UserInfo.class))).thenReturn(savedUserEntity);
        when(userInfoMapper.toDto(any(UserInfo.class))).thenAnswer(invocation -> {
            UserInfoDto dto = new UserInfoDto();
            dto.setId(1L);
            dto.setCivilId("12345");
            return dto;
        });

        // Act
        UserInfoDto createdUser = userService.createUser(newUserDto);

        // Assert
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isEqualTo(1L);
        verify(userInfoRepository).save(newUserEntity);
    }

    @Test
    void whenCreateUser_withDuplicateCivilId_shouldThrowException() {
        // Arrange
        UserInfoDto newUserDto = new UserInfoDto();
        newUserDto.setCivilId("12345");

        UserInfo existingUser = new UserInfo();
        when(userInfoRepository.findByCivilId("12345")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.createUser(newUserDto));

        assertThat(exception.getMessage()).isEqualTo(BusinessErrors.USER_CIVIL_ID_USED.getMessage());
    }


    @Test
    void whenGetUserById_withExistingId_shouldReturnUser() {
        // Arrange
        long userId = 1L;
        UserInfo userEntity = new UserInfo();
        userEntity.setId(userId);
        UserInfoDto userDto = new UserInfoDto();
        userDto.setId(userId);

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userInfoMapper.toDto(userEntity)).thenReturn(userDto);

        // Act
        UserInfoDto foundUser = userService.getUser(userId);

        // Assert
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(userId);
    }

    @Test
    void whenGetUserById_withNonExistingId_shouldThrowException() {
        // Arrange
        long userId = 99L;
        when(userInfoRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.getUser(userId));

        assertThat(exception.getMessage()).isEqualTo(BusinessErrors.NO_SUCH_USER.getMessage());
    }

    @Test
    void whenGetAllUsers_shouldReturnPagedResult() {
        // Arrange
        UserInfo userEntity = new UserInfo();
        userEntity.setId(1L);
        Page<UserInfo> userPage = new PageImpl<>(List.of(userEntity));

        UserInfoDto userDto = new UserInfoDto();
        userDto.setId(1L);

        when(userInfoRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userInfoMapper.toDto(userEntity)).thenReturn(userDto);

        // Act
        Page<UserInfoDto> resultPage = userService.getAllUsers(0, 10);

        // Assert
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent().getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void whenUpdateUser_withExistingId_shouldSucceed() {
        // Arrange
        long userId = 1L;
        UserInfoDto updateDto = new UserInfoDto();
        updateDto.setName("Updated Name");
        updateDto.setCivilId("54321");

        UserInfo existingUser = new UserInfo();
        existingUser.setId(userId);
        existingUser.setName("Old Name");

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userInfoRepository.save(any(UserInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userInfoMapper.toDto(any(UserInfo.class))).thenAnswer(invocation -> {
            UserInfo savedUser = invocation.getArgument(0);
            UserInfoDto dto = new UserInfoDto();
            dto.setId(savedUser.getId());
            dto.setName(savedUser.getName());
            dto.setCivilId(savedUser.getCivilId());
            return dto;
        });

        // Act
        UserInfoDto updatedUser = userService.updateUser(userId, updateDto);

        // Assert
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getCivilId()).isEqualTo("54321");

        ArgumentCaptor<UserInfo> userCaptor = ArgumentCaptor.forClass(UserInfo.class);
        verify(userInfoRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getId()).isEqualTo(userId);
        assertThat(userCaptor.getValue().getName()).isEqualTo("Updated Name");
    }

    @Test
    void whenUpdateUser_withNonExistingId_shouldThrowException() {
        // Arrange
        long userId = 99L;
        UserInfoDto updateDto = new UserInfoDto();
        when(userInfoRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.updateUser(userId, updateDto));

        assertThat(exception.getMessage()).isEqualTo(BusinessErrors.NO_SUCH_USER.getMessage());
    }

    @Test
    void whenDeleteUser_withExistingId_shouldSucceed() {
        // Arrange
        long userId = 1L;
        when(userInfoRepository.existsById(userId)).thenReturn(true);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userInfoRepository).deleteById(userId);
    }

    @Test
    void whenDeleteUser_withNonExistingId_shouldThrowException() {
        // Arrange
        long userId = 99L;
        when(userInfoRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.deleteUser(userId));

        assertThat(exception.getMessage()).isEqualTo(BusinessErrors.NO_SUCH_USER.getMessage());
    }
}