package com.bank.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class UploadRequest {

    @NotBlank
    private String civilId;

    @NotBlank
    private String requestName;

    @NotBlank
    private String requestType;

    @NotEmpty
    @Size(min = 2, max = 4)
    private List<MultipartFile> attachments;

}
