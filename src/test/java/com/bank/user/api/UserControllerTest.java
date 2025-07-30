package com.bank.user.api;

import com.bank.user.exception.BusinessErrors;
import com.bank.user.model.dto.UserInfoDto;
import com.bank.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenGetUserById_withValidId_shouldReturnUser() throws Exception {
        // Arrange
        long userId = 1L;
        UserInfoDto userDto = new UserInfoDto();
        userDto.setId(userId);
        userDto.setName("Test User");
        when(userService.getUser(userId)).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/user/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void whenGetUserById_withNonExistentId_shouldReturnNotFound() throws Exception {
        // Arrange
        long userId = 99L;
        when(userService.getUser(userId)).thenThrow(BusinessErrors.NO_SUCH_USER.exception());

        // Act & Assert
        mockMvc.perform(get("/api/v1/user/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetUserByCivilId_withValidCivilId_shouldReturnUser() throws Exception {
        // Arrange
        String civilId = "12345";
        UserInfoDto userDto = new UserInfoDto();
        userDto.setId(1L);
        userDto.setCivilId(civilId);
        when(userService.getUser(civilId)).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/user").param("civilId", civilId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.civilId").value(civilId));
    }

    @Test
    void whenGetAllUsers_shouldReturnPagedUsers() throws Exception {
        // Arrange
        UserInfoDto userDto = new UserInfoDto();
        userDto.setId(1L);
        List<UserInfoDto> userList = List.of(userDto);
        Page<UserInfoDto> userPage = new PageImpl<>(userList, PageRequest.of(0, 10), 1);
        when(userService.getAllUsers(0, 10)).thenReturn(userPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/user").param("page", "0").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(header().string("X-Total-Pages", "1"))
                .andExpect(header().string("X-Current-Page", "0"))
                .andExpect(header().string("X-Page-Size", "10"))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void whenCreateUser_withValidData_shouldReturnCreated() throws Exception {
        // Arrange
        UserInfoDto requestDto = new UserInfoDto();
        requestDto.setName("New User");
        requestDto.setCivilId("new123");
        requestDto.setExpiryDate(LocalDateTime.now().plusYears(1));

        UserInfoDto responseDto = new UserInfoDto();
        responseDto.setId(1L);
        responseDto.setName("New User");
        responseDto.setCivilId("new123");

        when(userService.createUser(any(UserInfoDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/user/1"))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void whenCreateUser_withDuplicateCivilId_shouldReturnConflict() throws Exception {
        // Arrange
        UserInfoDto requestDto = new UserInfoDto();
        requestDto.setName("New User");
        requestDto.setCivilId("existing123");
        requestDto.setExpiryDate(LocalDateTime.now().plusYears(1));

        when(userService.createUser(any(UserInfoDto.class)))
                .thenThrow(BusinessErrors.USER_CIVIL_ID_USED.exception());

        // Act & Assert
        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void whenCreateUser_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Arrange: Create a DTO with a blank name to trigger @NotBlank validation
        UserInfoDto requestDto = new UserInfoDto();
        requestDto.setName(""); // Invalid
        requestDto.setCivilId("new123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenUpdateUser_withValidData_shouldReturnUpdatedUser() throws Exception {
        // Arrange
        String expiryDateStr = "3026-07-30T12:57:02.9302779";
        LocalDateTime expiryDate = LocalDateTime.from(FORMATTER.parse(expiryDateStr));

        long userId = 1L;
        UserInfoDto requestDto = new UserInfoDto();
        requestDto.setName("Updated User");
        requestDto.setCivilId("12345");
        requestDto.setExpiryDate(expiryDate);

        UserInfoDto responseDto = new UserInfoDto();
        responseDto.setId(userId);
        responseDto.setName("Updated User");
        responseDto.setCivilId("12345");
        responseDto.setExpiryDate(expiryDate);

        when(userService.updateUser(eq(userId), any(UserInfoDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/v1/user/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.civilId").value("12345"))
                .andExpect(jsonPath("$.expiryDate").value(expiryDateStr));
    }

    @Test
    void whenUpdateUser_withNonExistentId_shouldReturnNotFound() throws Exception {
        // Arrange
        long userId = 99L;
        UserInfoDto requestDto = new UserInfoDto();
        requestDto.setName("Updated User");
        requestDto.setCivilId("12345");
        LocalDateTime expiryDate = LocalDateTime.now().plusYears(1);
        requestDto.setExpiryDate(expiryDate);

        when(userService.updateUser(eq(userId), any(UserInfoDto.class)))
                .thenThrow(BusinessErrors.NO_SUCH_USER.exception());

        // Act & Assert
        mockMvc.perform(put("/api/v1/user/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenDeleteUser_withValidId_shouldReturnNoContent() throws Exception {
        // Arrange
        long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/user/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(userId);
    }

    @Test
    void whenDeleteUser_withNonExistentId_shouldReturnNotFound() throws Exception {
        // Arrange
        long userId = 99L;
        doThrow(BusinessErrors.NO_SUCH_USER.exception()).when(userService).deleteUser(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/user/{id}", userId))
                .andExpect(status().isNotFound());
    }
}