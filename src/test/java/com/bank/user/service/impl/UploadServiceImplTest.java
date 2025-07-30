package com.bank.user.service.impl;

import com.bank.user.exception.BusinessErrors;
import com.bank.user.exception.BusinessException;
import com.bank.user.exception.SystemException;
import com.bank.user.model.dto.UploadRequest;
import com.bank.user.model.entity.RequestType;
import com.bank.user.model.entity.UserInfo;
import com.bank.user.model.entity.UserUploadRequest;
import com.bank.user.repository.RequestTypeRepository;
import com.bank.user.repository.UserInfoRepository;
import com.bank.user.repository.UserUploadRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UploadServiceImplTest {

    @Mock
    private UserInfoRepository userInfoRepository;
    @Mock
    private RequestTypeRepository requestTypeRepository;
    @Mock
    private UserUploadRequestRepository userUploadRequestRepository;

    private UploadServiceImpl uploadService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Manually instantiate the service to inject the temporary directory path
        uploadService = new UploadServiceImpl(
                tempDir.toString(),
                userInfoRepository,
                requestTypeRepository,
                userUploadRequestRepository
        );
    }

    @Test
    void whenUpload_withValidData_shouldSucceed() {
        // Arrange
        UploadRequest request = new UploadRequest();
        request.setCivilId("12345");
        request.setRequestType("ACCOUNT_OPENING");
        request.setAttachments(List.of(
                new MockMultipartFile("file1", "doc1.pdf", "application/pdf", "content".getBytes()),
                new MockMultipartFile("file2", "doc2.png", "image/png", "content".getBytes())
        ));

        UserInfo user = new UserInfo();
        user.setId(1L);
        user.setExpiryDate(LocalDateTime.now().plusYears(1));

        RequestType requestType = new RequestType();
        requestType.setName("ACCOUNT_OPENING");

        when(requestTypeRepository.findByName("ACCOUNT_OPENING")).thenReturn(Optional.of(requestType));
        when(userInfoRepository.findByCivilId("12345")).thenReturn(Optional.of(user));

        // Act
        uploadService.upload(request);

        // Assert
        ArgumentCaptor<UserUploadRequest> captor = ArgumentCaptor.forClass(UserUploadRequest.class);
        verify(userUploadRequestRepository).save(captor.capture());

        UserUploadRequest savedRequest = captor.getValue();
        assertThat(savedRequest.getUser()).isEqualTo(user);
        assertThat(savedRequest.getRequestType()).isEqualTo(requestType);
        assertThat(savedRequest.getUserAttachments()).hasSize(2);
        assertThat(savedRequest.getUserAttachments().get(0).getUserFilename()).isEqualTo("doc1.pdf");
        assertThat(savedRequest.getUserAttachments().get(1).getUserFilename()).isEqualTo("doc2.png");
    }

    @Test
    void whenUpload_withInvalidRequestType_shouldThrowException() {
        // Arrange
        UploadRequest request = new UploadRequest();
        request.setRequestType("INVALID_TYPE");

        when(requestTypeRepository.findByName("INVALID_TYPE")).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> uploadService.upload(request));
        assertThat(exception.getMessage()).isEqualTo(BusinessErrors.INVALID_REQUEST_TYPE.getMessage());
    }

    @Test
    void whenUpload_withNonExistentUser_shouldThrowException() {
        // Arrange
        UploadRequest request = new UploadRequest();
        request.setCivilId("unknown-user");
        request.setRequestType("ACCOUNT_OPENING");

        RequestType requestType = new RequestType();
        when(requestTypeRepository.findByName(anyString())).thenReturn(Optional.of(requestType));
        when(userInfoRepository.findByCivilId("unknown-user")).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> uploadService.upload(request));
        assertThat(exception.getMessage()).isEqualTo(BusinessErrors.NO_SUCH_USER.getMessage());
    }

    @Test
    void whenUpload_withExpiredUser_shouldThrowException() {
        // Arrange
        UploadRequest request = new UploadRequest();
        request.setCivilId("expired-user");
        request.setRequestType("ACCOUNT_OPENING");

        UserInfo expiredUser = new UserInfo();
        expiredUser.setExpiryDate(LocalDateTime.now().minusDays(1));

        RequestType requestType = new RequestType();
        when(requestTypeRepository.findByName(anyString())).thenReturn(Optional.of(requestType));
        when(userInfoRepository.findByCivilId("expired-user")).thenReturn(Optional.of(expiredUser));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> uploadService.upload(request));
        assertThat(exception.getMessage()).isEqualTo(BusinessErrors.USER_EXPIRED.getMessage());
    }

    @Test
    void whenStoreFile_throwsIOException_shouldThrowSystemException() throws IOException {
        // Arrange
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("error.txt");

        UploadRequest request = new UploadRequest();
        request.setCivilId("12345");
        request.setRequestType("ACCOUNT_OPENING");
        request.setAttachments(List.of(file));

        UserInfo user = new UserInfo();
        user.setId(1L);
        user.setExpiryDate(LocalDateTime.now().plusYears(1));

        RequestType requestType = new RequestType();

        when(requestTypeRepository.findByName(anyString())).thenReturn(Optional.of(requestType));
        when(userInfoRepository.findByCivilId(anyString())).thenReturn(Optional.of(user));

        // This will cause attachment.transferTo(path) to fail
        doThrow(new IOException("Disk is full")).when(file).transferTo(any(Path.class));

        // Act & Assert
        assertThrows(SystemException.class, () -> uploadService.upload(request));
    }
}