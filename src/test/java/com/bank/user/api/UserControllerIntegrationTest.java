package com.bank.user.api;

import com.bank.user.config.TestContainersConfiguration;
import com.bank.user.model.dto.UserInfoDto;
import com.bank.user.model.entity.UserInfo;
import com.bank.user.repository.UserInfoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestContainersConfiguration.class)
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Clean up the repository before each test to ensure isolation
        userInfoRepository.deleteAll();
    }

    @Test
    void whenCreateUser_withValidData_shouldSucceed() throws Exception {
        // Arrange
        UserInfoDto requestDto = new UserInfoDto();
        requestDto.setName("Integration User");
        requestDto.setCivilId("integ-123");
        requestDto.setExpiryDate(LocalDateTime.now().plusYears(2));

        // Act & Assert
        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Integration User"))
                .andExpect(header().exists("Location"));

        // Verify data was actually saved
        assertThat(userInfoRepository.findByCivilId("integ-123")).isPresent();
    }

    @Test
    void whenCreateUser_withDuplicateCivilId_shouldReturnConflict() throws Exception {
        // Arrange: Save a user first
        UserInfo existingUser = new UserInfo();
        existingUser.setCivilId("duplicate-id");
        existingUser.setName("Existing User");
        existingUser.setExpiryDate(LocalDateTime.now());
        userInfoRepository.save(existingUser);

        UserInfoDto requestDto = new UserInfoDto();
        requestDto.setName("New User");
        requestDto.setCivilId("duplicate-id"); // Same civilId
        requestDto.setExpiryDate(LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void whenCreateUser_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Arrange: DTO with a blank name to trigger @NotBlank validation
        UserInfoDto requestDto = new UserInfoDto();
        requestDto.setName(""); // Invalid
        requestDto.setCivilId("invalid-data-user");
        requestDto.setExpiryDate(LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGetUserById_withExistingUser_shouldReturnUser() throws Exception {
        // Arrange
        UserInfo savedUser = userInfoRepository.save(createTestUser("get-by-id", "Get Test"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/user/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.civilId").value("get-by-id"));
    }

    @Test
    void whenGetUserById_withNonExistentUser_shouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/user/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetUserByCivilId_withExistingUser_shouldReturnUser() throws Exception {
        // Arrange
        UserInfo savedUser = userInfoRepository.save(createTestUser("get-by-civil-id", "Civil ID Test"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/user").param("civilId", "get-by-civil-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.civilId").value("get-by-civil-id"));
    }

    @Test
    void whenGetAllUsers_shouldReturnPagedUsers() throws Exception {
        // Arrange
        userInfoRepository.save(createTestUser("user1", "Alice"));
        userInfoRepository.save(createTestUser("user2", "Bob"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/user").param("page", "0").param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "2"))
                .andExpect(header().string("X-Total-Pages", "1"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alice")); // Default sort is by name
    }

    @Test
    void whenUpdateUser_withValidData_shouldSucceed() throws Exception {
        // Arrange
        UserInfo savedUser = userInfoRepository.save(createTestUser("update-me", "Original Name"));

        UserInfoDto updateDto = new UserInfoDto();
        updateDto.setName("Updated Name");
        updateDto.setCivilId("update-me-new");
        updateDto.setExpiryDate(LocalDateTime.now().plusDays(10));

        // Act & Assert
        mockMvc.perform(put("/api/v1/user/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.civilId").value("update-me-new"));

        // Verify the update in the database
        UserInfo updatedUser = userInfoRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    void whenUpdateUser_toDuplicateCivilId_shouldReturnConflict() throws Exception {
        // Arrange: Create two users
        UserInfo userToUpdate = userInfoRepository.save(createTestUser("user-to-update", "User One"));
        userInfoRepository.save(createTestUser("existing-civil-id", "User Two"));

        // DTO to update userToUpdate's civilId to be the same as User Two's
        UserInfoDto updateDto = new UserInfoDto();
        updateDto.setName("Updated Name");
        updateDto.setCivilId("existing-civil-id"); // This civilId is already taken
        updateDto.setExpiryDate(LocalDateTime.now());

        // Act & Assert
        // This test verifies that the service layer prevents updating a user's civilId
        // to one that is already in use by another user.
        mockMvc.perform(put("/api/v1/user/{id}", userToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void whenDeleteUser_withExistingUser_shouldSucceed() throws Exception {
        // Arrange
        UserInfo savedUser = userInfoRepository.save(createTestUser("delete-me", "Delete Test"));
        long userId = savedUser.getId();

        // Act & Assert
        mockMvc.perform(delete("/api/v1/user/{id}", userId))
                .andExpect(status().isNoContent());

        // Verify deletion from the database
        assertThat(userInfoRepository.findById(userId)).isNotPresent();
    }

    @Test
    void whenDeleteUser_withNonExistentUser_shouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/user/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    private UserInfo createTestUser(String civilId, String name) {
        UserInfo user = new UserInfo();
        user.setCivilId(civilId);
        user.setName(name);
        user.setExpiryDate(LocalDateTime.now().plusYears(1));
        return user;
    }
}