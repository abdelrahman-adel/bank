package com.bank.user.api;

import com.bank.user.model.dto.UploadRequest;
import com.bank.user.service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private final UploadService uploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void upload(@ModelAttribute @Valid UploadRequest uploadRequest) {
        uploadService.upload(uploadRequest);
    }
}
