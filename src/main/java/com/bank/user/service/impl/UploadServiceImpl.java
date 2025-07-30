package com.bank.user.service.impl;

import com.bank.user.exception.BusinessErrors;
import com.bank.user.exception.SystemException;
import com.bank.user.model.dto.UploadRequest;
import com.bank.user.model.entity.RequestType;
import com.bank.user.model.entity.UserAttachment;
import com.bank.user.model.entity.UserInfo;
import com.bank.user.model.entity.UserUploadRequest;
import com.bank.user.repository.RequestTypeRepository;
import com.bank.user.repository.UserInfoRepository;
import com.bank.user.repository.UserUploadRequestRepository;
import com.bank.user.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class UploadServiceImpl implements UploadService {

    private final Path storagePath;
    private final UserInfoRepository userInfoRepository;
    private final RequestTypeRepository requestTypeRepository;
    private final UserUploadRequestRepository userUploadRequestRepository;

    public UploadServiceImpl(
            @Value("${com.bank.user-service.storage-location}") String storagePath,
            UserInfoRepository userInfoRepository,
            RequestTypeRepository requestTypeRepository,
            UserUploadRequestRepository userUploadRequestRepository) {
        this.storagePath = Path.of(storagePath);
        this.userInfoRepository = userInfoRepository;
        this.requestTypeRepository = requestTypeRepository;
        this.userUploadRequestRepository = userUploadRequestRepository;
    }

    @Override
    public void upload(UploadRequest uploadRequest) {
        RequestType requestType = requestTypeRepository.findByName(uploadRequest.getRequestType())
                .orElseThrow(BusinessErrors.INVALID_REQUEST_TYPE::exception);

        UserInfo user = userInfoRepository.findByCivilId(uploadRequest.getCivilId()).orElseThrow(BusinessErrors.NO_SUCH_USER::exception);
        if (user.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw BusinessErrors.USER_EXPIRED.exception();
        }
        UserUploadRequest userUploadRequest = new UserUploadRequest();
        List<UserAttachment> userAttachments = uploadRequest.getAttachments().stream()
                .map(attachment -> storeAndTransform(user.getId(), attachment, userUploadRequest)).toList();

        userUploadRequest.setRequestName(uploadRequest.getRequestName());
        userUploadRequest.setRequestType(requestType);
        userUploadRequest.setUser(user);
        userUploadRequest.setUserAttachments(userAttachments);
        userUploadRequestRepository.save(userUploadRequest);
    }

    private UserAttachment storeAndTransform(Long userId, MultipartFile attachment, UserUploadRequest userUploadRequest) {
        String fileExtension = getFileExtension(attachment.getOriginalFilename());
        String newFileName = userId + "-" + UUID.randomUUID() + fileExtension;
        Path destinationFile = this.storagePath.resolve(newFileName).toAbsolutePath();

        try {
            Files.createDirectories(this.storagePath);
            attachment.transferTo(destinationFile);
        } catch (IOException e) {
            throw new SystemException(e);
        }
        UserAttachment userAttachment = new UserAttachment();
        userAttachment.setUserFilename(attachment.getOriginalFilename());
        userAttachment.setAttachmentUrl(destinationFile.toString());
        userAttachment.setUserUploadRequest(userUploadRequest);
        return userAttachment;
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex == -1) {
            return "";
        }
        return filename.substring(dotIndex); // Returns the file extension
    }
}
