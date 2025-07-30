package com.bank.user.repository;

import com.bank.user.config.CachingTestConfig;
import com.bank.user.config.TestContainersConfiguration;
import com.bank.user.model.entity.RequestType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DataJpaTest
@Import({TestContainersConfiguration.class, CachingTestConfig.class})
public class RequestTypeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @MockitoSpyBean
    private RequestTypeRepository requestTypeRepository;

    @Test
    void whenFindByName_withExistingName_shouldReturnRequestType() {
        // Arrange
        RequestType newRequestType = new RequestType();
        newRequestType.setName("ACCOUNT_OPENING");
        entityManager.persistAndFlush(newRequestType);

        // Act
        Optional<RequestType> found = requestTypeRepository.findByName("ACCOUNT_OPENING");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ACCOUNT_OPENING");
    }

    @Test
    void whenFindByName_withNonExistingName_shouldReturnEmpty() {
        // Act
        Optional<RequestType> found = requestTypeRepository.findByName("NON_EXISTENT_TYPE");

        // Assert
        assertThat(found).isNotPresent();
    }

    @Test
    void whenFindByName_isCalledMultipleTimes_shouldHitCache() {
        // Arrange
        RequestType newRequestType = new RequestType();
        newRequestType.setName("CACHE_TEST");
        entityManager.persistAndFlush(newRequestType);

        // Act: Call the method twice with the same argument
        Optional<RequestType> foundFirst = requestTypeRepository.findByName("CACHE_TEST");
        Optional<RequestType> foundSecond = requestTypeRepository.findByName("CACHE_TEST");

        // Assert
        assertThat(foundFirst).isPresent();
        assertThat(foundSecond).isPresent();
        assertThat(foundFirst.get().getName()).isEqualTo("CACHE_TEST");
        assertThat(foundSecond.get().getName()).isEqualTo("CACHE_TEST");

        // Verify that the repository method was only called ONCE.
        // The second call should have been served from the cache.
        verify(requestTypeRepository, times(1)).findByName("CACHE_TEST");
    }
}