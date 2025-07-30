package com.bank.user.model.mapper;

import com.bank.user.model.dto.UserInfoDto;
import com.bank.user.model.entity.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class UserInfoMapperTest {

    private UserInfoMapper userInfoMapper;

    @BeforeEach
    void setUp() {
        // We test the generated implementation
        userInfoMapper = new UserInfoMapperImpl();
    }

    @Test
    void whenToDto_withValidEntity_shouldReturnCorrectDto() {
        // Arrange
        UserInfo entity = new UserInfo();
        entity.setId(1L);
        entity.setCivilId("12345");
        entity.setName("Test User");
        LocalDateTime expiry = LocalDateTime.now();
        entity.setExpiryDate(expiry);

        // Act
        UserInfoDto dto = userInfoMapper.toDto(entity);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCivilId()).isEqualTo("12345");
        assertThat(dto.getName()).isEqualTo("Test User");
        assertThat(dto.getExpiryDate()).isEqualTo(expiry);
    }

    @Test
    void whenToEntity_withValidDto_shouldReturnCorrectEntity() {
        // Arrange
        UserInfoDto dto = new UserInfoDto();
        dto.setId(2L);
        dto.setCivilId("67890");
        dto.setName("Another User");
        LocalDateTime expiry = LocalDateTime.now().plusYears(1);
        dto.setExpiryDate(expiry);

        // Act
        UserInfo entity = userInfoMapper.toEntity(dto);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getCivilId()).isEqualTo("67890");
        assertThat(entity.getName()).isEqualTo("Another User");
        assertThat(entity.getExpiryDate()).isEqualTo(expiry);
    }

    @Test
    void whenToDto_withNullEntity_shouldReturnNull() {
        assertThat(userInfoMapper.toDto(null)).isNull();
    }

    @Test
    void whenToEntity_withNullDto_shouldReturnNull() {
        assertThat(userInfoMapper.toEntity(null)).isNull();
    }
}