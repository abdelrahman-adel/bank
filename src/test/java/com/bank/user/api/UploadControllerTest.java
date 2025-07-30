package com.bank.user.api;

import com.bank.user.model.dto.UploadRequest;
import com.bank.user.service.UploadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadController.class)
public class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UploadService uploadService;

    @Test
    void whenUpload_withValidData_shouldReturnAccepted() throws Exception {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile(
                "attachments",
                "id_card.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test-data".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "attachments",
                "passport.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test-data".getBytes()
        );

        doNothing().when(uploadService).upload(any(UploadRequest.class));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                        .file(file1)
                        .file(file2)
                        .param("civilId", "12345")
                        .param("requestName", "New Account")
                        .param("requestType", "ACCOUNT_OPENING")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isAccepted());

        verify(uploadService).upload(any(UploadRequest.class));
    }

    @Test
    void whenUpload_withInvalidAttachmentCount_shouldReturnBadRequest() throws Exception {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile(
                "attachments",
                "only_one_file.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test-data".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                        .file(file1)
                        .param("civilId", "12345")
                        .param("requestName", "New Account")
                        .param("requestType", "ACCOUNT_OPENING"))
                .andExpect(status().isBadRequest());
    }
}