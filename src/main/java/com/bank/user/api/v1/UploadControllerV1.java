package com.bank.user.api.v1;

import com.bank.user.model.dto.UploadRequest;
import com.bank.user.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Upload Controller")
@RestController
@RequestMapping("/api/v1/upload")
public class UploadControllerV1 {

    private final UploadService uploadService;

    @Operation(description = "Uploads user attachment related to a request.")
    @ApiResponse(responseCode = "202", description = "Upload successful.")
    @ApiResponse(responseCode = "400", description = "Request missing required fields, or RequestType given is invalid.")
    @ApiResponse(responseCode = "404", description = "User not found.")
    @ApiResponse(responseCode = "409", description = "User Civil ID is expired.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void upload(@ModelAttribute @Valid UploadRequest uploadRequest) {
        uploadService.upload(uploadRequest);
    }
}
