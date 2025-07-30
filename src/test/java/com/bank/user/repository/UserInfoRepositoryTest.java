package com.bank.user.repository;

import com.bank.user.config.TestContainersConfiguration;
import com.bank.user.model.entity.UserInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(TestContainersConfiguration.class)
public class UserInfoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Test
    void whenFindByCivilId_withExistingCivilId_shouldReturnUser() {
        // Arrange
        UserInfo user = new UserInfo();
        user.setCivilId("12345");
        user.setName("Test User");
        user.setExpiryDate(LocalDateTime.now().plusYears(1));
        entityManager.persistAndFlush(user);

        // Act
        Optional<UserInfo> found = userInfoRepository.findByCivilId("12345");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getCivilId()).isEqualTo("12345");
    }

    @Test
    void whenFindByCivilId_withNonExistingCivilId_shouldReturnEmpty() {
        // Act
        Optional<UserInfo> found = userInfoRepository.findByCivilId("non-existent-id");

        // Assert
        assertThat(found).isNotPresent();
    }

    @Test
    void whenSavingUserWithDuplicateCivilId_shouldThrowException() {
        // Arrange
        UserInfo user1 = new UserInfo();
        user1.setCivilId("duplicate-id");
        user1.setName("First User");
        user1.setExpiryDate(LocalDateTime.now());
        entityManager.persistAndFlush(user1);

        UserInfo user2 = new UserInfo();
        user2.setCivilId("duplicate-id");
        user2.setName("Second User");
        user2.setExpiryDate(LocalDateTime.now());

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            userInfoRepository.saveAndFlush(user2);
        });
    }
}