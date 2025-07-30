package com.bank.user.api;

import com.bank.user.config.CachingTestConfig;
import com.bank.user.config.TestContainersConfiguration;
import com.bank.user.model.entity.RequestType;
import com.bank.user.model.entity.UserInfo;
import com.bank.user.model.entity.UserUploadRequest;
import com.bank.user.repository.RequestTypeRepository;
import com.bank.user.repository.UserInfoRepository;
import com.bank.user.repository.UserUploadRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import({TestContainersConfiguration.class, CachingTestConfig.class})
public class UploadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private RequestTypeRepository requestTypeRepository;

    @Autowired
    private UserUploadRequestRepository userUploadRequestRepository;

    private UserInfo testUser;
    private RequestType testRequestType;

    @BeforeEach
    void setUp() {
        // Clean up to ensure test isolation
        userUploadRequestRepository.deleteAll();
        userInfoRepository.deleteAll();
        requestTypeRepository.deleteAll();

        // Arrange: Create prerequisite data
        testUser = new UserInfo();
        testUser.setCivilId("integ-test-user");
        testUser.setName("Integration User");
        testUser.setExpiryDate(LocalDateTime.now().plusYears(1));
        userInfoRepository.save(testUser);

        testRequestType = new RequestType();
        testRequestType.setName("ACCOUNT_OPENING");
        requestTypeRepository.save(testRequestType);
    }

    @Test
    void whenUpload_withValidData_shouldSucceed() throws Exception {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile("attachments", "id.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf-content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("attachments", "passport.jpg", MediaType.IMAGE_JPEG_VALUE, "jpg-content".getBytes());

        // Act
        mockMvc.perform(multipart("/api/v1/upload")
                        .file(file1)
                        .file(file2)
                        .param("civilId", testUser.getCivilId())
                        .param("requestName", "New Savings Account")
                        .param("requestType", testRequestType.getName())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isAccepted());

        // Assert: Verify that the data was saved correctly in the database
        List<UserUploadRequest> requests = userUploadRequestRepository.findAll();
        assertThat(requests).hasSize(1);

        UserUploadRequest savedRequest = requests.getFirst();
        assertThat(savedRequest.getRequestName()).isEqualTo("New Savings Account");
        assertThat(savedRequest.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(savedRequest.getRequestType().getId()).isEqualTo(testRequestType.getId());
        assertThat(savedRequest.getUserAttachments()).hasSize(2);
        assertThat(savedRequest.getUserAttachments().getFirst().getUserFilename()).isEqualTo("id.pdf");
    }

    @Test
    void whenUpload_withInvalidAttachmentCount_shouldReturnBadRequest() throws Exception {
        // Arrange: Only one file, but the requirement is a minimum of two
        MockMultipartFile file1 = new MockMultipartFile("attachments", "onefile.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                        .file(file1)
                        .param("civilId", testUser.getCivilId())
                        .param("requestName", "Request with one file")
                        .param("requestType", testRequestType.getName()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenUpload_withExpiredUser_shouldReturnConflict() throws Exception {
        // Arrange: Update the user to be expired
        testUser.setExpiryDate(LocalDateTime.now().minusDays(1));
        userInfoRepository.save(testUser);

        MockMultipartFile file1 = new MockMultipartFile("attachments", "file1.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("attachments", "file2.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                        .file(file1)
                        .file(file2)
                        .param("civilId", testUser.getCivilId())
                        .param("requestName", "Request for expired user")
                        .param("requestType", testRequestType.getName()))
                .andExpect(status().isConflict());
    }

    @Test
    void whenUpload_withNonExistentUser_shouldReturnNotFound() throws Exception {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile("attachments", "file1.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("attachments", "file2.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                        .file(file1)
                        .file(file2)
                        .param("civilId", "non-existent-user-id") // This user does not exist
                        .param("requestName", "Request for non-existent user")
                        .param("requestType", testRequestType.getName()))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenUpload_withInvalidRequestType_shouldReturnBadRequest() throws Exception {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile("attachments", "file1.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("attachments", "file2.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                        .file(file1)
                        .file(file2)
                        .param("civilId", testUser.getCivilId())
                        .param("requestName", "Request with invalid type")
                        .param("requestType", "INVALID_TYPE")) // This type does not exist
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenUpload_withTooManyAttachments_shouldReturnBadRequest() throws Exception {
        // Arrange: 5 files, but the requirement is a maximum of four
        MockMultipartFile file1 = new MockMultipartFile("attachments", "file1.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("attachments", "file2.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes());
        MockMultipartFile file3 = new MockMultipartFile("attachments", "file3.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes());
        MockMultipartFile file4 = new MockMultipartFile("attachments", "file4.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes());
        MockMultipartFile file5 = new MockMultipartFile("attachments", "file5.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                        .file(file1).file(file2).file(file3).file(file4).file(file5)
                        .param("civilId", testUser.getCivilId())
                        .param("requestName", "Request with too many files")
                        .param("requestType", testRequestType.getName()))
                .andExpect(status().isBadRequest());
    }
}