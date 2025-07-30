package com.bank.user.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class UserUploadRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserInfo user;

    @Column(nullable = false)
    private String requestName;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_type_id", nullable = false)
    private RequestType requestType;

    @OneToMany(mappedBy = "userUploadRequest", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.JOIN)
    private List<UserAttachment> userAttachments = new ArrayList<>();
}
