package com.bank.user.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class UserAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String attachmentUrl;

    @Column
    private String userFilename;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_request_id", nullable = false)
    private UserUploadRequest userUploadRequest;
}
